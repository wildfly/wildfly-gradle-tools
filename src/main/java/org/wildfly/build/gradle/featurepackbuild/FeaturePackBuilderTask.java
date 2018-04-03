package org.wildfly.build.gradle.featurepackbuild;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import org.wildfly.build.gradle.provisioning.GradleArtifactFileResolver;

public class FeaturePackBuilderTask extends DefaultTask {

	private File moduleTemplates;
	private File destinationDir;
	private Map<String,String> variables = new HashMap<>();
	private String gradleConfigurationName;
	private Set<String> dependencies = new HashSet<>();
	private boolean autoAddRepositories = true;

	@TaskAction
	void doBuildFeaturePack() throws Exception {
		getLogger().info( "Starting creationg of feature pack from templates in: '{}'",  moduleTemplates );
		final GradleArtifactFileResolver resolver = new GradleArtifactFileResolver( this.getProject(), autoAddRepositories );
		ModuleFilesBuilder moduleFileCreator = new ModuleFilesBuilder( moduleTemplates.toPath(), variables, destinationDir.toPath(), resolver, gradleConfigurationName, dependencies );
		moduleFileCreator.build();
	}

	@OutputDirectory
	public File getDestinationDir() {
		return destinationDir;
	}

	public void setDestinationDir(File destinationDir) {
		this.destinationDir = destinationDir;
	}

	@InputDirectory
	public File getModuleTemplates() {
		return moduleTemplates;
	}

	public void setModuleTemplates(File moduleTemplates) {
		this.moduleTemplates = moduleTemplates;
	}

	@Input
	public Map<String, String> getVariables() {
		return variables;
	}

	public void setVariables(Map<String, String> variables) {
		this.variables = variables;
	}

	@Input
	public String getConfigurationName() {
		return gradleConfigurationName;
	}

	public void setConfigurationName(String gradleConfigurationName) {
		this.gradleConfigurationName = gradleConfigurationName;
	}

	@Input
	public boolean isAutoAddRepositories() {
		return autoAddRepositories;
	}

	public void setAutoAddRepositories(boolean autoAddRepositories) {
		this.autoAddRepositories = autoAddRepositories;
	}

	//TODO how to get Gradle keep track of these inputs?
	public void setDependency(String featurepack) {
		dependencies.add( featurepack );
	}

}
