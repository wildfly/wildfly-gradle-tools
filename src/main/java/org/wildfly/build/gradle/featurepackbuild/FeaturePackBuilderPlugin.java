package org.wildfly.build.gradle.featurepackbuild;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import org.wildfly.build.gradle.provisioning.ProvisionTask;

public class FeaturePackBuilderPlugin implements Plugin<Project> {


	public void apply(Project project) {
		project.getTasks().create( "featurepack", FeaturePackBuilderTask.class, (task) -> {
//			task.slot
		});
	}
}
