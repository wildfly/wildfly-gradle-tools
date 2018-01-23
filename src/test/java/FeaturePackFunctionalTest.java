import java.io.File;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.TaskOutcome;

import org.junit.Rule;
import org.junit.Test;

import org.wildfly.build.util.ModuleParseResult;
import org.wildfly.build.util.ModuleParser;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class FeaturePackFunctionalTest {

	@Rule
	public final GradleBuildTestingRule testProject = new GradleBuildTestingRule( "feature-pack.gradle" )
			.includeResource( "combined-server-provisioning.xml" )
			.includeResourceInSubDirectory( "hibernateorm-module.xml", "examplemodules" )
			.includeResourceInSubDirectory( "hibernatespatial-module.xml", "examplemodules" );

	@Test
	public void testFeaturePackTask() throws Exception {
		BuildResult result = testProject.runTask( "featurepack" );

		assertEquals( result.task( ":featurepack" ).getOutcome(), TaskOutcome.SUCCESS );

		File expectedModuleFile = new File( testProject.getWorkDirectory()
													+ File.separator + "featurepack"
													+ File.separator + "modules"
													+ File.separator + "system"
													+ File.separator + "layers"
													+ File.separator + "base"
													+ File.separator + "org"
													+ File.separator + "hibernate"
													+ File.separator + "orm53"
													+ File.separator + "module.xml" );
		assertTrue(
				"The module.xml has not been created " + expectedModuleFile.getPath(),
				expectedModuleFile.exists()
		);

		ModuleParseResult parseResult = ModuleParser.parse( expectedModuleFile.toPath() );
		assertThat( parseResult.getIdentifier().getSlot(), is("orm53"));
	}

}
