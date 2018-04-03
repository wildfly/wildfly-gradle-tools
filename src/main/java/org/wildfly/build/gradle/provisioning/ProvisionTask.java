package org.wildfly.build.gradle.provisioning;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;
import javax.xml.stream.XMLStreamException;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.incremental.IncrementalTaskInputs;

import groovy.lang.Closure;
import org.wildfly.build.ArtifactResolver;
import org.wildfly.build.provisioning.ServerProvisioner;
import org.wildfly.build.provisioning.model.ServerProvisioningDescription;
import org.wildfly.build.provisioning.model.ServerProvisioningDescriptionModelParser;
import org.wildfly.build.util.MapPropertyResolver;
import org.wildfly.build.util.PropertiesBasedArtifactResolver;

/**
 * Invokes the actual provisioning libraries from WildFly.
 *
 * We allow either to inject a configuration file, or when this is missing we'll apply a default assembly
 * which materializes a full server. In this case the wildfly.version property should be set.
 *
 * @author Sanne Grinovero sanne@hibernate.org (C) 2018 Red Hat Inc.
 */
public class ProvisionTask extends DefaultTask {

	public static final String DEFAULT_OUTPUT_DIR = "provisioned-wildfly";

	public ProvisionTask() {
		//A reasonable output path as default:
		destinationDir = getProject().getBuildDir().toPath().resolve( DEFAULT_OUTPUT_DIR ).toFile();
	}

	private Map<String,String> variables = new HashMap<>();

	@InputFile
	@Optional
	@PathSensitive(PathSensitivity.NONE)
	String configuration;

	@OutputDirectory
	File destinationDir;

	@Input
	boolean autoAddRepositories = true;

	private Map<String,ProvisionOverride> overrides = new HashMap<>(  );

	@Input
	public Map<String, String> getVariables() {
		return variables;
	}

	public void setVariables(Map<String, String> variables) {
		this.variables = variables;
	}

	@TaskAction
	void doProvisioning() throws IOException {
		getLogger().info( "Server Provisioning Configuration resource: '{}'",  configuration );
		final ServerProvisioningDescription serverProvisioningDescription = parseServerProvisioningDescriptor();
		final GradleArtifactFileResolver resolver = new GradleArtifactFileResolver( this.getProject(), autoAddRepositories );
		final MapBasedOverridesResolver overridesResolver = new MapBasedOverridesResolver( overrides );
		try {
			ServerProvisioner.build( serverProvisioningDescription, destinationDir, false, resolver, overridesResolver );
			getLogger().info( "Server provisioned into path: '{}'", destinationDir );
			getState().setDidWork( true );
		}
		catch (RuntimeException re) {
			final RuntimeException firstErrorIfAny = overridesResolver.getFirstErrorIfAny();
			if ( firstErrorIfAny != null ) {
				throw firstErrorIfAny;
			}
			else {
				throw re;
			}
		}
		final Set<String> unusedOverrideDirectives = overridesResolver.getUnusedOverrideDirectives();
		if ( unusedOverrideDirectives.isEmpty() == false ) {
			getLogger().warn( "The following override directives have not been used by the WildFly Privisioning system; typo? {}", unusedOverrideDirectives );
		}
	}

	public void override(String artifactIdentifier, Closure<ProvisionOverride> closure) {
		ProvisionOverride override = new ProvisionOverride( artifactIdentifier );
		getProject().configure( override, closure );
		overrides.put( artifactIdentifier, override );
	}

	private ServerProvisioningDescription parseServerProvisioningDescriptor() throws IOException {
		final ServerProvisioningDescriptionModelParser parser = new ServerProvisioningDescriptionModelParser( new MapPropertyResolver( variables ) );
		try ( InputStream configStream = openConfigurationInputStream() ) {
			return parser.parse( configStream );
		}
		catch (XMLStreamException e) {
			throw new RuntimeException( "Invalid XML in configuration file " + configuration + " : " + e.getMessage(), e );
		}
	}

	/**
	 * Open the server provisioning configuration file if any is provided, or fall back to our
	 * default one if none was set. Setting a configuration file which is not found will result in a
	 * FileNotFoundException.
	 * @return
	 * @throws FileNotFoundException
	 */
	private InputStream openConfigurationInputStream() throws FileNotFoundException {
		if ( configuration == null ) {
			return openDefaultConfigurationInputStream();
		}
		else {
			return new FileInputStream( configuration );
		}
	}

	/**
	 * Opens a default server-provisioning.xml which we'll include, for convenience of
	 * what we believe will be the most common use case: a full server.
	 * @return
	 */
	private InputStream openDefaultConfigurationInputStream() {
		//Load as a resource which we'll include in the plugin jar, with path scoped to this same package
		return ProvisionPlugin.class.getResourceAsStream( "default-server-provisioning.xml" );
	}

}
