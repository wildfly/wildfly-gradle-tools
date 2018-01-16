package org.wildfly.build.gradle.provisioning;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * A Gradle plugin to provision a WildFly server.
 * Can be used to simply materialize a full server into a target
 * directory, or by specifying a provisioning assembly descriptor
 * to create a custom tailored version of the server, for example
 * having only servlets support.
 *
 * @author Sanne Grinovero sanne@hibernate.org (C) 2018 Red Hat Inc.
 */
public class ProvisionPlugin implements Plugin<Project> {

	public void apply(Project project) {
		project.getTasks().create( "provision", ProvisionTask.class, (task) -> {
			//TODO pass some more properties to allow:
			//  - version overrides
			//  - resolving properties to go in the server-provisioning.xml
			//task.setServerName( "wildfly" );
		});
	}

}
