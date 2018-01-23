import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;

import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;

public final class GradleBuildTestingRule extends ExternalResource {

	private static String SEPARATOR = "\n--------------\n";

	private final String buildScript;
	private final TemporaryFolder directory = new TemporaryFolder();
	private final HashSet<String> includeResources = new HashSet<>();
	private final Map<String,Set<String>> subdirs = new HashMap<>();

	public GradleBuildTestingRule(String buildScript) {
		this.buildScript = buildScript;
	}

	@Override
	protected void before() throws Throwable {
		directory.create();
		File buildFile = getWorkDirectory( true ).resolve( "build.gradle" ).toFile();
		buildFile.createNewFile();
		writeBuildFile( buildFile );
		for ( String s : includeResources ) {
			createFileFromresource( directory.getRoot().toPath(), s );
		}
		for ( Map.Entry<String, Set<String>> pack : subdirs.entrySet() ) {
			final String subdir = pack.getKey();
			final Path path = directory.getRoot().toPath().resolve( subdir );
			Files.createDirectories( path );
			for ( String res : pack.getValue() ) {
				createFileFromresource( path, res );
			}
		}
	}

	@Override
	protected void after() {
		directory.delete();
	}

	private void createFileFromresource(Path root, String filename) throws IOException {
		createFileFromresource( root, filename, filename );
	}

	private void createFileFromresource(Path root, String filename, String sourceResource) throws IOException {
		final String fileContent = loadFileContent( sourceResource );
		Path file = root.resolve( filename );
		Files.write( file, Collections.singletonList( fileContent ) );
	}

	public BuildResult runTask(String taskName) {
		return GradleRunner.create()
				.withProjectDir( getWorkDirectory().toFile() )
				.withArguments( taskName , "--stacktrace", "--refresh-dependencies", "--no-build-cache" )
				.withDebug( true ) //Useful to debug the plugin code directly in the IDE
				.withPluginClasspath()
				.build();
	}

	private void writeBuildFile(File destination) throws IOException {
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
		return loadFileContent( buildScript );
	}

	private String loadFileContent(String fileName) throws IOException {
		final ClassLoader loader = BuildFunctionalTest.class.getClassLoader();
		try (InputStream inputStream = loader.getResourceAsStream( fileName )) {
			return new BufferedReader( new InputStreamReader( inputStream ) )
					.lines().collect( Collectors.joining( "\n" ) ) + "\n";
		}
	}

	private Path getWorkDirectory(boolean createIfNeeded) throws IOException {
		final Path workDirectory = getWorkDirectory();
		if ( Files.isDirectory( workDirectory ) ) {
			return workDirectory;
		}
		else {
			if ( createIfNeeded ) {
				Files.createDirectories( workDirectory );
				return workDirectory;
			}
			else {
				throw new IllegalStateException( "WorkDirectory not existing!" );
			}
		}
	}

	public Path getWorkDirectory() {
		return directory.getRoot().toPath();
	}

	public GradleBuildTestingRule includeResource(String resourceName) {
		includeResources.add( resourceName );
		return this;
	}

	/**
	 * Allows copying some more resource in a sub directory.
	 * We only need a single level, so keeping it simple with a dumb multimap.
	 */
	public GradleBuildTestingRule includeResourceInSubDirectory(String resourceName, String directoryName) {
		if ( subdirs.containsKey( directoryName ) == false ) {
			subdirs.put( directoryName, new HashSet<>() );
		}
		subdirs.get( directoryName ).add( resourceName );
		return this;
	}

}
