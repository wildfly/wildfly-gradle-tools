package org.wildfly.build.gradle.featurepackbuild;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.concurrent.ThreadSafe;

import org.wildfly.build.gradle.provisioning.GradleArtifactFileResolver;
import org.wildfly.build.pack.model.Artifact;
import org.wildfly.build.util.ModuleParseResult;

@ThreadSafe
final class ArtifactsRegistry {

	private final Pattern SHORT_FEATURE_PACK_PATTERN = Pattern.compile( "([^\\s^:]+):([^\\s^:]+):([^\\s^:]+)" );
	private final Pattern LONG_FEATURE_PACK_PATTERN = Pattern.compile( "([^\\s^:]+):([^\\s^:]+):([^\\s^:]+):([^\\s^:]+)@([^\\s^:]+)" );

	private final ConcurrentHashMap<String,Artifact> map = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<String,Artifact> featurePacks = new ConcurrentHashMap<>();
	private final GradleArtifactFileResolver resolver;
	private final String gradleConfigurationName;

	public ArtifactsRegistry(GradleArtifactFileResolver resolver, String gradleConfigurationName) {
		this.resolver = resolver;
		this.gradleConfigurationName = gradleConfigurationName;
	}

	public void register(ModuleParseResult.ArtifactName a) {
		//If the version is hard coded, we won't need to encode the version in the feature pack definition.
		if ( a.hasVersion() == false ) {
			final String coords = a.getArtifactCoords();
			String[] parts = coords.split(":");
			if ( parts.length != 2 ) {
				throw new IllegalArgumentException( "Format of artifact coordinates is expected to be as 'groupId:artifactId'. Please fix the format of artifacts listed in the module templates." );
			}
			map.computeIfAbsent( coords, c -> resolveArtifact( parts[0], parts[1] ) );
		}
	}

	private Artifact resolveArtifact(String groupId, String artifactId) {
		final Artifact.GACE gace = new Artifact.GACE( groupId, artifactId, null, null );
		return new Artifact( gace, resolveVersion( groupId, artifactId ) );
	}

	private synchronized String resolveVersion(String groupId, String artifactId) {
		return resolver.getArtifactWithVersion( groupId, artifactId, gradleConfigurationName );
	}

	public Collection<Artifact> getAllArtifacts() {
		return Stream.concat( map.values().stream(), featurePacks.values().stream() ).collect( Collectors.toSet() );
	}

	public void addDependencyFeaturePacks(Set<String> dependencies) {
		dependencies.forEach( this::addDependencyFeaturePack );
	}

	private void addDependencyFeaturePack(String encodedFPackName) {
		featurePacks.computeIfAbsent( encodedFPackName, this::decodeFPackName );
	}

	private Artifact decodeFPackName(String encodedFPackName) {
		//CUSTOM FORMAT! we want extension=zip as the default.
		final Matcher shortMatcher = SHORT_FEATURE_PACK_PATTERN.matcher( encodedFPackName );
		if ( shortMatcher.find() ) {
			final String groupId = shortMatcher.group( 1 );
			final String artifactId = shortMatcher.group( 2 );
			final String extension = "zip"; //hardcoded as better default for this use case!
			//TODO should we support "classifier" as well?
			final Artifact.GACE gace = new Artifact.GACE( groupId, artifactId, null, extension );
			final String version = shortMatcher.group( 3 );
			return new Artifact( gace, version );
		}
		throw new IllegalArgumentException( "Illegal format '" + encodedFPackName + "'. Expecting format: 'group:name:version' for feature pack dependencies." );
	}

	public Collection<String> getDependencyFeaturePacks() {
		return featurePacks.values().stream()
				.map( s -> s.getGACE().getGroupId() + ":" + s.getGACE().getArtifactId() )
				.collect( Collectors.toSet() );
	}

}
