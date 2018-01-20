package org.wildfly.build.gradle.provisioning;

import java.io.File;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ResolvedConfiguration;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.artifacts.dsl.RepositoryHandler;

import org.wildfly.build.ArtifactFileResolver;
import org.wildfly.build.pack.model.Artifact;

/**
 * Implementing the org.wildfly.build.ArtifactFileResolver by delegating
 * artifact resolution to the Gradle engine.
 * @author Sanne Grinovero sanne@hibernate.org (C) 2018 Red Hat Inc.
 * @author Andrea Boriero andrea@hibernate.org (C) 2018 Red Hat Inc.
 */
public class GradleArtifactFileResolver implements ArtifactFileResolver {

	private static final String JBOSS_REPOSITORY = "http://repository.jboss.org/nexus/content/groups/public/";

	private final Project project;
	private final DependencyHandler dependencies;
	private final ConfigurationContainer configurations;

	//We set this flag when we enable additional repositories automatically.
	private boolean repositoriesAutomaticallyAdded = false;

	public GradleArtifactFileResolver(Project project) {
		this.project = project;
		this.dependencies = project.getDependencies();
		this.configurations = project.getConfigurations();
	}

	@Override
	public File getArtifactFile(String gav) {
		return getArtifactFile( Artifact.parse( gav ) );
	}

	@Override
	public File getArtifactFile(Artifact artifact) {
		final Dependency dependency = asGradleDependency( artifact );
		return createTemporaryConfiguration( dependency, artifact ).getSingleFile();
	}

	private Dependency asGradleDependency(Artifact artifact) {
		DependencyAdaptor dep = new DependencyAdaptor( artifact );
		String gradleNotation = dep.toGradleNotation();
		return dependencies.create( gradleNotation );
	}

	private Configuration createTemporaryConfiguration(Dependency dependency, Artifact artifact) {
		Configuration config = configurations.detachedConfiguration( dependency );
		config.setTransitive( false );
		config.setVisible( false );
		//This next line actually resolves it (Attempts to download dependencies):
		final ResolvedConfiguration resolvedConfiguration = config.getResolvedConfiguration();
		if ( resolvedConfiguration.hasError() ) {
			//In case of error, user is likely not having the JBoss Nexus repository enabled.
			//Attempt configuration correction:
			if ( repositoriesAutomaticallyAdded == false ) {
				repositoriesAutomaticallyAdded = true;
				addDefaultRepositories( artifact );
				//And retry:
				return createTemporaryConfiguration( dependency, artifact );
			}
			else {
				resolvedConfiguration.rethrowFailure();
			}
		}
		return config;
	}

	private void addDefaultRepositories(Artifact artifact) {
		project.getLogger().warn( "Error fetching WildFly component '{}' from configured repositories; check your repository configurations to prevent this problem.\nAttempting to resolve this by enabling additional repositories automatically! Adding: [mavenLocal(), mavenCentral() and {}]", artifact, JBOSS_REPOSITORY );
		final RepositoryHandler repositories = project.getRepositories();
		repositories.mavenLocal();
		repositories.mavenCentral();
		repositories.maven( mavenArtifactRepository -> {
			mavenArtifactRepository.setUrl( JBOSS_REPOSITORY );
		} );
	}

}
