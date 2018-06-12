package org.wildfly.build.gradle.provisioning;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.wildfly.build.ArtifactResolver;
import org.wildfly.build.pack.model.Artifact;

public class MapBasedOverridesResolver implements ArtifactResolver {

	private static final Pattern pattern = Pattern.compile( ":" );

	private final Map<String, ProvisionOverride> overrides;

	//Useful to verify that all declared overrides have actually been used;
	//this might help to spot typos and usage of wrong format, as there's no better validation.
	private final Set<String> processedOnes = new HashSet<>();

	private RuntimeException firstError = null;

	public MapBasedOverridesResolver(Map<String, ProvisionOverride> properties) {
		this.overrides = properties;
	}

	public RuntimeException getFirstErrorIfAny() {
		return firstError;
	}

	public Set<String> getUnusedOverrideDirectives() {
		Set<String> s = new HashSet<>( overrides.keySet() );
		s.removeAll( processedOnes );
		return s;
	}

	private Artifact getArtifact(String coords) {
		ProvisionOverride o = this.overrides.get( coords );
		if (o == null) {
			return null;
		} else {
			processedOnes.add( coords );

			String[] parts = coords.split(":");

			//(String groupId, String artifactId, String classifier, String extension, String version)
			final String groupId;
			final String artifactId;
			final String classifier;
			final String extension;
			final String version;
			groupId = isEmpty( o.getGroupId() ) ? parts[0] : o.getGroupId();
			artifactId = isEmpty( o.getArtifactId() ) ? parts[1] : o.getArtifactId();
			if ( parts.length == 2 ) {
				classifier = null;
			}
			else {
				classifier = isEmpty( o.getClassifier() ) ? parts[3] : o.getClassifier();
			}
			//In the maven tooling the extension is hardcoded to "jar". Go figure?
			extension = isEmpty( o.getExtension() ) ? "jar" : o.getExtension();

			//Now deal with 'version': complex as we can't infer a valid fallback in case it's missing.
			version = o.getVersion();
			if ( isEmpty( version ) ) {
				//This is critical, but the below error message is going to get buried deep into nested stacktraces: store the exception
				//so we can declare it as cause in the outer task.
				final MissingVersionException exception = new MissingVersionException(
						"Missing 'version' property for override configuration of '" + coords +"'. The version property is required.");
				if ( firstError == null ) {
					firstError = exception;
				}
				throw exception;
			}
			return new Artifact( groupId, artifactId, extension, classifier, version );
		}
	}

	@Override
	public Artifact getArtifact(final Artifact unversioned) {
		StringBuilder sb = new StringBuilder();
		sb.append(unversioned.getGroupId());
		sb.append(':');
		sb.append(unversioned.getArtifactId());
		if (unversioned.getClassifier() != null) {
			sb.append("::").append(unversioned.getClassifier());
		}
		return this.getArtifact(sb.toString());
	}

	private boolean isEmpty(final String s) {
		return s == null || s.trim().equals( "" );
	}

}
