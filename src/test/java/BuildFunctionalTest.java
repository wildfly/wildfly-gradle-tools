import java.nio.file.Path;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.TaskOutcome;

import org.junit.Test;

import org.wildfly.build.gradle.provisioning.ProvisionTask;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BuildFunctionalTest extends BaseFunctionalTest {

	@Test
	public void testProvisioningTask() {
		System.out.print( "Work directory: " + getWorkDirectory() );

		BuildResult result = runTask( "provision" );

		assertEquals( result.task( ":provision" ).getOutcome(), TaskOutcome.SUCCESS );

		//now check some key file from WildFly was really materialized in the right place:
		final Path provisionedWildFlyPath = getWorkDirectory().resolve( "build" )
				.resolve( ProvisionTask.DEFAULT_OUTPUT_DIR );
		System.out.println( "Server expected in: " + provisionedWildFlyPath );
		assertTrue( provisionedWildFlyPath.resolve( "jboss-modules.jar" ).toFile().isFile() );
	}

	//Following have been manually tested, need automated tests:

	//TODO add test for custom 'configuration' property

	//TODO add test for custom output directory

	//TODO add test for nice error message on 'configuration' being set but file missing

	//TODO add test for nice error message on 'configuration' being set but illegal XML format


	@Override
	public String getBuildScriptName() {
		return "template-build.gradle";
	}
}
