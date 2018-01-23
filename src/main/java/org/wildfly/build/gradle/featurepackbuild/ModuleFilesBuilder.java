package org.wildfly.build.gradle.featurepackbuild;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import nu.xom.ParsingException;
import org.wildfly.build.gradle.provisioning.GradleArtifactFileResolver;

class ModuleFilesBuilder {

	private final Path templateFolder;
	private final Path outputFolder;
	private final AtomicReference<Exception> firstError = new AtomicReference<>(  );
	private final TemplatePatterns patterns;
	private final Set<String> dependencies; //Specifically: other feature packs
	private final ArtifactsRegistry requiredArtifacts;

	public ModuleFilesBuilder(
			Path templateFolder,
			Map<String, String> variables,
			Path rootModulesFolder,
			GradleArtifactFileResolver resolver,
			String gradleConfigurationName,
			Set<String> dependencies) {
		this.templateFolder = templateFolder;
		this.outputFolder = rootModulesFolder;
		this.patterns = new TemplatePatterns( variables );
		this.dependencies = dependencies;
		this.requiredArtifacts = new ArtifactsRegistry( resolver, gradleConfigurationName );
	}

	void build() throws Exception {
		firstError.set( null );
		Files.walk( templateFolder, FileVisitOption.FOLLOW_LINKS )
				.filter( p -> isRegularXML( p ) )
				.parallel()
				.forEach( t -> generateModuleDefinitionSafely( t ) );
		final Exception anyError = firstError.get();
		if ( anyError != null ) {
			throw anyError;
		}
		requiredArtifacts.addDependencyFeaturePacks( dependencies );
		DefinitionWriter dw = new DefinitionWriter();
		dw.registerRequiredArtifacts( requiredArtifacts );
//		dw.registerDependencies( dependencies );
		dw.writeFile( outputFolder );
	}

	private void generateModuleDefinitionSafely(Path file) {
		try {
			if ( isRegularXML( file ) ) {
				generateModuleDefinition( file );
			}
		}
		catch (Exception e) {
			firstError.compareAndSet( null, e );
		}
	}

	//We expect all templates files to be xml files
	private static boolean isRegularXML(Path file) {
		return Files.isRegularFile( file ) && file.getFileName().toString().toLowerCase().trim().endsWith( ".xml" );
	}

	private void generateModuleDefinition(Path templateFile) throws IOException, ParsingException {
		final ModuleTemplate moduleDef = ModuleTemplate.parse( templateFile, patterns );
		moduleDef.collectArtifacts( requiredArtifacts );

		final ModuleFolderPath moduleFolderPath = new ModuleFolderPath( outputFolder, moduleDef );
		moduleFolderPath.createFolders();

		final Module xmlFile = new Module( moduleFolderPath );
		xmlFile.createWithContent( moduleDef.getXmlModuleContent() );
	}

}
