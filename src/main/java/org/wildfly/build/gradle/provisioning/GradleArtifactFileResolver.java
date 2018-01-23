package org.wildfly.build.gradle.provisioning;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.LenientConfiguration;
import org.gradle.api.artifacts.ResolvedConfiguration;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.logging.Logger;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.publish.Publication;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenArtifactSet;
import org.gradle.api.publish.maven.MavenPublication;

import org.wildfly.build.ArtifactFileResolver;
import org.wildfly.build.pack.model.Artifact;

/**
 * Implementing the org.wildfly.build.ArtifactFileResolver by delegating
 * artifact resolution to the Gradle engine.
 * This is not as simple as one might think, as it needs to adapt different
 * tools which have different notions of identity for each artifact.
 *
 * @author Sanne Grinovero sanne@hibernate.org (C) 2018 Red Hat Inc.
 * @author Andrea Boriero andrea@hibernate.org (C) 2018 Red Hat Inc.
 */
public class GradleArtifactFileResolver implements ArtifactFileResolver {

	private static final String JBOSS_REPOSITORY = "http://repository.jboss.org/nexus/content/groups/public/";

	private final Project project;
	private final DependencyHandler dependencies;
	private final ConfigurationContainer configurations;
	private final Logger logger;
	private final List<Publication> knownPublications;

	//We set this flag when we enable additional repositories automatically.
	private boolean repositoriesAutomaticallyAdded = false;

	public GradleArtifactFileResolver(Project project) {
		this.project = project;
		this.dependencies = project.getDependencies();
		this.configurations = project.getConfigurations();
		this.knownPublications = extractPublishingExtensions( project.getRootProject().getAllprojects() );
		this.logger = project.getLogger();
	}

	private static List<Publication> extractPublishingExtensions(Set<Project> subprojects) {
		return subprojects.stream()
				.map( project -> extractPublishingExtension( project ) )
				.filter( Objects::nonNull )
				.map( PublishingExtension::getPublications )
				.filter( Objects::nonNull )
				.flatMap( ps -> ps.stream() )
				.collect( Collectors.toList() );
	}

	/**
	 * @param project
	 * @return the list of PublishingExtension(s), if some are defined: this will depend on the project (could be empty).
	 */
	private static PublishingExtension extractPublishingExtension(final Project project) {
		final ExtensionContainer extensions = project.getExtensions();
		final Object publishing = extensions.findByName( "publishing" );
		if ( publishing == null ) {
			return null;
		}
		if ( ! PublishingExtension.class.isAssignableFrom( publishing.getClass() ) ) {
			throw new RuntimeException( "The registered 'publishing' extension is not of the expected type PublishingExtension" );
		}
		return (PublishingExtension) publishing;
	}

	@Override
	public File getArtifactFile(String gav) {
		return getArtifactFile( Artifact.parse( gav ) );
	}

	@Override
	public File getArtifactFile(Artifact artifact) {
		final Dependency dependency = asGradleDependency( artifact );
		final DependencyAdaptor da = new DependencyAdaptor( artifact );
		final Optional<File> local = attemptLocalResolution( da );
		if ( local.isPresent() ) {
			return local.get();
		}
		else {
			return createTemporaryConfiguration( dependency, artifact.toString() ).getSingleFile();
		}
	}

	/**
	 * Return a reference to the publication as a File, or null if this is not a known publication at this point.
	 * A "publication" is an artifact which is produced (output) by the current project: this has to be handled
	 * differently than an artifact which is fetched from a repository.
	 * N.B. a matching publication might exist but not been created yet; this is most likely a build script
	 * mistake from the user but we can't be sure, so attempt to warn about it but don't enforce corrections.
	 * @param da The identity of the dependency being looked up
	 * @return a File reference to a local publication matching the parameter, if it exists and if it was already published.
	 */
	private Optional<File> attemptLocalResolution(DependencyAdaptor da) {
		return attemptLocalResolutionFromPublication( da, knownPublications.stream() );
	}

	private Optional<File> attemptLocalResolutionFromPublication(DependencyAdaptor da, Stream<Publication> publications) {
		//If any of these is null, we'll fall back to external repositories as well.
		final String group = da.getGroup();
		if ( group == null ) return null;
		final String version = da.getVersion();
		if ( version == null ) return null;
		final String module = da.getModule();
		if ( module == null ) return null;
		final Optional<File> anyMatch =
				publications
				.filter( d -> d instanceof MavenPublication )
				.map( d -> (MavenPublication) d )
				.filter( m -> module.equals( m.getArtifactId() ) && group.equals( m.getGroupId() ) && version.equals( m.getVersion() ) )
				.map( MavenPublication::getArtifacts )
				.flatMap( MavenArtifactSet::stream )
				.filter( m -> m.getFile() != null )
				.map( m -> m.getFile() )
				.findAny();
		return anyMatch;
	}

	private Configuration createTemporaryConfiguration(Dependency dependency, String artifactLabel) {
		Configuration config = configurations.detachedConfiguration( dependency );
		//parentConfiguration.ifPresent( c -> config.setExtendsFrom( Collections.singletonList( c ) ) );
		config.setTransitive( false );
		config.setVisible( false );
		config.setDescription( "Generated configuration to resolve '" + artifactLabel + "' on behalf of the WildFly Provisioning plugin" );
		//This next line actually resolves it (Attempts to download dependencies):
		final ResolvedConfiguration resolvedConfiguration = config.getResolvedConfiguration();
		if ( resolvedConfiguration.hasError() ) {
			//In case of error, user is likely not having the JBoss Nexus repository enabled.
			//Attempt configuration correction:
			if ( repositoriesAutomaticallyAdded == false ) {
				repositoriesAutomaticallyAdded = true;
				addDefaultRepositories( artifactLabel );
				//And retry:
				return createTemporaryConfiguration( dependency, artifactLabel );
			}
			else {
				resolvedConfiguration.rethrowFailure();
			}
		}
		return config;
	}

	private void addDefaultRepositories(String artifactLabel) {
		logger.warn( "Error fetching WildFly component '{}' from configured repositories; check your repository configurations to prevent this problem.\nAttempting to resolve this by enabling additional repositories automatically! Adding: [mavenLocal(), mavenCentral() and {}]", artifactLabel, JBOSS_REPOSITORY );
		final RepositoryHandler repositories = project.getRepositories();
		repositories.mavenLocal();
		repositories.mavenCentral();
		repositories.maven( mavenArtifactRepository -> {
			mavenArtifactRepository.setUrl( JBOSS_REPOSITORY );
		} );
	}

	//Needs to resolve the groupId:artifactId coordinate pairs to enrich it with the
	//version from the current project configuration.
	public String getArtifactWithVersion(String groupId, String artifactId, String configurationName) {
		final ResolvedConfiguration resolvedConfiguration = configurations.getByName( configurationName ).getResolvedConfiguration();
		resolvedConfiguration.rethrowFailure();

		final LenientConfiguration lenientConfiguration = resolvedConfiguration.getLenientConfiguration();
		final Optional<ResolvedDependency> dependency = lenientConfiguration.
				getFirstLevelModuleDependencies( d -> ( d.getGroup().equals( groupId ) && d.getName().equals( artifactId ) ) )
				.stream()
				.findFirst();
		return dependency.get().getModuleVersion();
	}

	private Dependency asGradleDependency(final String someId) {
		return dependencies.create( someId );
	}

	private Dependency asGradleDependency(final Artifact artifact) {
		DependencyAdaptor dep = new DependencyAdaptor( artifact );
		String gradleNotation = dep.toGradleNotation();
		return asGradleDependency( gradleNotation );
	}

}
