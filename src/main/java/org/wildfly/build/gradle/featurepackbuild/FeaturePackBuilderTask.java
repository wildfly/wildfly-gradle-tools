package org.wildfly.build.gradle.featurepackbuild;

import java.io.File;
import java.io.IOException;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import org.wildfly.build.gradle.ModuleFilesGenerator;

public class FeaturePackBuilderTask extends DefaultTask {

	@InputDirectory
	File moduleTemplates;

	@OutputDirectory
	File destinationDir;


	@Input
	@Optional
	String slot = "main";

	@TaskAction
	void doBuildFeaturePack() throws IOException {
		getLogger().info( "Starting creationg of feature pack from templates in: '{}'",  moduleTemplates );
		ModuleFilesGenerator moduleFileCreator = new ModuleFilesGenerator( moduleTemplates, slot, destinationDir );
		moduleFileCreator.create();
	}


	public File getDestinationDir() {
		return destinationDir;
	}
}
