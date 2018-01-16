package org.wildfly.build.gradle.provisioning;

import org.gradle.api.artifacts.component.ModuleComponentIdentifier;

import org.wildfly.build.pack.model.Artifact;

/**
 * Wrapping an org.wildfly.build.pack.model.Artifact type for convenience.
 */
public class DependencyAdaptor implements ModuleComponentIdentifier {

	private final Artifact artifact;

	public DependencyAdaptor(Artifact artifact) {
		if ( artifact == null ) {
			throw new NullPointerException( "Constructor parameter is mandatory" );
		}
		this.artifact = artifact;
	}

	@Override
	public String getGroup() {
		return artifact.getGACE().getGroupId();
	}

	@Override
	public String getModule() {
		return artifact.getGACE().getArtifactId();
	}

	@Override
	public String getVersion() {
		return artifact.getVersion();
	}

	@Override
	public String getDisplayName() {
		return artifact.getGACE().toString();
	}

	/**
	 * Convert the artifact descriptor into a String in the format expected
	 * by Gradle, so that we can use this to feed into the Gradle API.
	 * @return a string in the gradle notation format
	 */
	public String toGradleNotation() {
		//Needs to match format: "<i>group</i>:<i>name</i>:<i>version</i>:<i>classifier</i>@<i>extension</i>"
		StringBuilder sb = new StringBuilder(  )
				.append( artifact.getGACE().getGroupId() )
				.append( ':' )
				.append( artifact.getGACE().getArtifactId() )
				.append( ':' )
				.append( artifact.getVersion() );
		final String classifier = artifact.getGACE().getClassifier();
		if ( classifier != null && ! classifier.isEmpty() ) {
			sb.append( ':' );
			sb.append( classifier );
		}
		final String extension = artifact.getGACE().getExtension();
		if ( extension != null && ! extension.isEmpty() ) {
			sb.append( '@' );
			sb.append( extension );
		}
		return sb.toString();
	}
}
