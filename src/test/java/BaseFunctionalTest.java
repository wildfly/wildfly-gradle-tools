/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.stream.Collectors;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

/**
 * @author Andrea Boriero
 */
public abstract class BaseFunctionalTest {
	private static String SEPARATOR = "\n--------------\n";

	@Rule
	public final TemporaryFolder testProjectDir = new TemporaryFolder();
	protected File buildFile;

	@Before
	public void setup() throws IOException {
		buildFile = getWorkDirectory().resolve( "build.gradle" ).toFile();
		buildFile.createNewFile();
		writeBuildFile( buildFile );
	}

	protected BuildResult runTask(String taskName) {
		return GradleRunner.create()
				.withProjectDir( getWorkDirectory().toFile() )
				.withArguments( taskName , "--stacktrace", "--refresh-dependencies" )
				.withDebug( true ) //Useful to debug the plugin code directly in the IDE
				.withPluginClasspath()
				.build();
	}

	protected void writeBuildFile(File destination) throws IOException {
		String content = loadFileContent();
		System.out.print( "Simulating build script 'build.gradle' :" + SEPARATOR + content + SEPARATOR );
		writeFile( destination, content );
	}

	protected void writeFile(File destination, String content) throws IOException {
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

	private String loadFileContent() throws IOException {
		return loadFileContent( getBuildScriptName() );
	}

	protected String loadFileContent(String fileName) throws IOException {
		final ClassLoader loader = BuildFunctionalTest.class.getClassLoader();
		try (InputStream inputStream = loader.getResourceAsStream( fileName )) {
			return new BufferedReader( new InputStreamReader( inputStream ) )
					.lines().collect( Collectors.joining( "\n" ) ) + "\n";
		}
	}

	public Path getWorkDirectory() {
		//Swap these lines to simplify debugging:
//		return Paths.get( "/tmp/EXPR");
		return testProjectDir.getRoot().toPath();
	}

	public abstract String getBuildScriptName();
}
