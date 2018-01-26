/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.TaskOutcome;

import org.junit.Test;

import org.wildfly.build.util.ModuleParseResult;
import org.wildfly.build.util.ModuleParser;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author Andrea Boriero
 */
public class FeaturePackFunctionalTest extends BaseFunctionalTest {

	@Test
	public void testFeaturePackTask() throws Exception {
		copyTemplateModulesForTesting();

		BuildResult result = runTask( "featurepack" );

		assertEquals( result.task( ":featurepack" ).getOutcome(), TaskOutcome.SUCCESS );

		File expectedModuleFile = new File( getWorkDirectory()
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

	private void copyTemplateModulesForTesting() throws IOException {
		Path examplemodulesDirectory = getWorkDirectory().resolve( "examplemodules" );
		new File( examplemodulesDirectory.toUri() ).mkdir();
		File moduleTemplate = new File( examplemodulesDirectory.toString() + File.separator + "hibernateorm-module.xml" );
		moduleTemplate.createNewFile();
		writeFile( moduleTemplate, loadFileContent( "hibernateorm-module.xml" ) );


	}

	@Override
	public String getBuildScriptName() {
		return "feature-pack.gradle";
	}
}
