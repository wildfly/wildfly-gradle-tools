package org.wildfly.build.gradle.featurepackbuild;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

import nu.xom.ParsingException;
import org.wildfly.build.util.ModuleParseResult;
import org.wildfly.build.util.ModuleParser;

class ModuleTemplate {

	private final String fileContent;
	private final ModuleParseResult parsedModule;

	public ModuleTemplate(String fileContent, ModuleParseResult parsedModule) {
		this.fileContent = fileContent;
		this.parsedModule = parsedModule;
	}

	public String getXmlModuleContent() {
		return fileContent;
	}

	public String getModuleIdentifier() {
		return parsedModule.getIdentifier().getName();
	}

	public String getSlot() {
		return parsedModule.getIdentifier().getSlot();
	}

	public static ModuleTemplate parse(Path template, TemplatePatterns templateValues) throws IOException, ParsingException {
		final String fileContent = Files.lines( template, CustomCharset.utf8 ).collect( Collectors.joining("\n" ) );
		final String replacedContent = templateValues.replaceAllVariables( fileContent );
		final ModuleParseResult parseResult;
		try ( InputStream stream = new ByteArrayInputStream( replacedContent.getBytes( CustomCharset.utf8 ) ) ) {
			parseResult = ModuleParser.parse( stream );
		}
		return new ModuleTemplate( replacedContent, parseResult );
	}

	public void collectArtifacts(ArtifactsRegistry requiredArtifacts) {
		parsedModule.getArtifacts().forEach( a -> requiredArtifacts.register( a ) );
	}

}
