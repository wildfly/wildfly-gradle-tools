package org.wildfly.build.gradle.featurepackbuild;

import java.io.File;
import java.io.IOException;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import nu.xom.ParsingException;

public class FeaturePackBuilderTask extends DefaultTask {

	@InputDirectory
	File moduleTemplates;

	@OutputDirectory
	File destinationDir;


	@Input
	@Optional
	String slot = "main";

	@TaskAction
	void doBuildFeaturePack() throws IOException, ParsingException {
		getLogger().info( "Starting creationg of feature pack from templates in: '{}'",  moduleTemplates );
		ModuleFilesBuilder moduleFileCreator = new ModuleFilesBuilder( moduleTemplates, slot, destinationDir );
		moduleFileCreator.build();
	}


	public File getDestinationDir() {
		return destinationDir;
	}
}
