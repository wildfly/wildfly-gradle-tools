package org.wildfly.build.gradle.provisioning;

public class ProvisionOverride {

	private final String match;
	private String groupId = "";
	private String artifactId = "";
	private String version = "";
	private String extension = "";
	private String classifier = "";

	public ProvisionOverride(String overrideKey) {
		//'match' contains the source of the override, while the following properties contain the target of the override.
		//Properties are set via the Gradle DSL
		this.match = overrideKey;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	public String getClassifier() {
		return classifier;
	}

	public void setClassifier(String classifier) {
		this.classifier = classifier;
	}
}
