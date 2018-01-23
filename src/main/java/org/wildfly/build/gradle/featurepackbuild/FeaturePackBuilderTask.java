package org.wildfly.build.gradle.featurepackbuild;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import org.wildfly.build.gradle.provisioning.GradleArtifactFileResolver;
import org.wildfly.build.gradle.provisioning.MapBasedOverridesResolver;
import org.wildfly.build.provisioning.ServerProvisioner;
import org.wildfly.build.provisioning.model.ServerProvisioningDescription;

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
	}

}
