import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.wildfly.build.gradle.provisioning.ProvisionTask;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BuildFunctionalTest {

	private static String SEPARATOR = "\n--------------\n";

	@Rule
	public final TemporaryFolder testProjectDir = new TemporaryFolder();
	private File buildFile;

	@Before
	public void setup() throws IOException {
		buildFile = getWorkDirectory().resolve( "build.gradle" ).toFile();
		buildFile.createNewFile();
	}

	@Test
	public void testProvisioningTask() throws IOException, InterruptedException {
		System.out.print( "Work directory: " + getWorkDirectory() );
		writeBuildFile( buildFile );

		BuildResult result = GradleRunner.create()
				.withProjectDir( getWorkDirectory().toFile() )
				.withArguments( "provision", "--stacktrace", "--refresh-dependencies" )
				.withDebug( true ) //Useful to debug the plugin code directly in the IDE
				.withPluginClasspath()
				.build();

		assertEquals( result.task( ":provision" ).getOutcome(), TaskOutcome.SUCCESS );

		//now check some key file from WildFly was really materialized in the right place:
		final Path provisionedWildFlyPath = getWorkDirectory().resolve( "build" ).resolve( ProvisionTask.DEFAULT_OUTPUT_DIR );
		System.out.println( "Server expected in: " + provisionedWildFlyPath );
		assertTrue( provisionedWildFlyPath.resolve( "jboss-modules.jar" ).toFile().isFile() );
	}

	//Following have been manually tested, need automated tests:

	//TODO add test for custom 'configuration' property

	//TODO add test for custom output directory

	//TODO add test for nice error message on 'configuration' being set but file missing

	//TODO add test for nice error message on 'configuration' being set but illegal XML format

	private void writeBuildFile(File destination) throws IOException {
		String content = loadContent();
		System.out.print( "Simulating build script 'build.gradle' :" + SEPARATOR + content + SEPARATOR );
		BufferedWriter output = null;
		try {
			output = new BufferedWriter( new FileWriter( destination ) );
			output.write( content );
		}
		finally {
			if ( output != null ) {
				output.close();
			}
		}
	}

	private String loadContent() throws IOException {
		final ClassLoader loader = BuildFunctionalTest.class.getClassLoader();
		try ( InputStream inputStream = loader.getResourceAsStream( "template-build.gradle" ) ) {
			return new BufferedReader( new InputStreamReader( inputStream ) )
					.lines().collect( Collectors.joining( "\n" ) ) + "\n";
		}
	}

	public Path getWorkDirectory() {
		//Swap these lines to simplify debugging:
//		return Paths.get("/tmp/EXPR");
		return testProjectDir.getRoot().toPath();
	}

}
