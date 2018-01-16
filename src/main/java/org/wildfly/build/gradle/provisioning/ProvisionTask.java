package org.wildfly.build.gradle.provisioning;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.xml.stream.XMLStreamException;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.incremental.IncrementalTaskInputs;

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

	@InputFile
	@Optional
	@PathSensitive(PathSensitivity.NONE)
	String configuration;

	@OutputDirectory
	File destinationDir;

	@TaskAction
	void doProvisioning() throws IOException {
		getLogger().info( "Server Provisioning Configuration resource: '{}'",  configuration );
		final Properties properties = new Properties();
		final ServerProvisioningDescription serverProvisioningDescription = parseServerProvisioningDescriptor( properties );
		final GradleArtifactFileResolver fileResolver = new GradleArtifactFileResolver( this.getProject() );
		final ArtifactResolver overrideArtifactResolver = new PropertiesBasedArtifactResolver( properties );
		ServerProvisioner.build( serverProvisioningDescription, destinationDir, false, fileResolver, overrideArtifactResolver );
		getLogger().info( "Server provisioned into path: '{}'", destinationDir );
		getState().setDidWork( true );
	}

	private ServerProvisioningDescription parseServerProvisioningDescriptor(Properties properties) throws IOException {
		final ServerProvisioningDescriptionModelParser parser = new ServerProvisioningDescriptionModelParser( new MapPropertyResolver( properties ) );
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
