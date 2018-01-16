package org.wildfly.build.gradle.provisioning;

import java.io.File;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;

import org.wildfly.build.ArtifactFileResolver;
import org.wildfly.build.pack.model.Artifact;

/**
 * Implementing the org.wildfly.build.ArtifactFileResolver by delegating
 * artifact resolution to the Gradle engine.
 * @author Sanne Grinovero sanne@hibernate.org (C) 2018 Red Hat Inc.
 * @author Andrea Boriero andrea@hibernate.org (C) 2018 Red Hat Inc.
 */
public class GradleArtifactFileResolver implements ArtifactFileResolver {

	private final Project project;
	private final DependencyHandler dependencies;
	private final ConfigurationContainer configurations;

	public GradleArtifactFileResolver(Project project) {
		this.project = project;
		this.dependencies = project.getDependencies();
		this.configurations = project.getConfigurations();
	}

	@Override
	public File getArtifactFile(String gav) {
		return getArtifactFile( Artifact.parse( gav ) );
	}

	@Override
	public File getArtifactFile(Artifact artifact) {
		final Dependency dependency = asGradleDependency( artifact );
		return createTemporaryConfiguration( dependency ).getSingleFile();
	}

	private Dependency asGradleDependency(Artifact artifact) {
		DependencyAdaptor dep = new DependencyAdaptor( artifact );
		String gradleNotation = dep.toGradleNotation();
		return dependencies.create( gradleNotation );
	}

	private Configuration createTemporaryConfiguration(Dependency dependency) {
		Configuration config = configurations.detachedConfiguration( dependency );
		//TODO Should we extend a well known configuration to allow ad-hoc setting of repositories?
		//config.setExtendsFrom(  )
		config.setTransitive( false );
		config.setVisible( false );
		return config;
	}

}
