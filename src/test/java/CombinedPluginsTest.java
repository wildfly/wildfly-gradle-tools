import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.TaskOutcome;

import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CombinedPluginsTest  {

	@Rule
	public final GradleBuildTestingRule testProject = new GradleBuildTestingRule( "combined-plugins.gradle" )
			.includeResource( "combined-server-provisioning.xml" )
			.includeResourceInSubDirectory( "hibernateorm-module.xml", "examplemodules" )
			.includeResourceInSubDirectory( "hibernatespatial-module.xml", "examplemodules" );

	@Test
	public void testFeaturePackTask() throws Exception {
		BuildResult result = testProject.runTask( "provision" );
		assertEquals( TaskOutcome.SUCCESS, result.task( ":provision" ).getOutcome() );
	}

}
