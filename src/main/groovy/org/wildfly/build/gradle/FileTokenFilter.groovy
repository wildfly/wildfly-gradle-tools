package org.wildfly.build.gradle

import org.wildfly.build.util.ModuleParseResult
import org.wildfly.build.util.ModuleParser

/**
 * @author Andrea Boriero
 */
class ModuleFilesGenerator {
	String placeHolder = '\\$\\{slot\\}'
	File templateDir
	String tokenReplacement
	String baseModulFoder

	ModuleFilesGenerator(File templateDir, String slot, File destinationDir) {
		this.templateDir = templateDir
		this.tokenReplacement = slot
		this.baseModulFoder = destinationDir.getAbsolutePath() + File.separator + "modules" + File.separator + "system" + File.separator + "layers" + File.separator + "base"
	}

	void create() {
		File[] templates = templateDir.listFiles()
		for ( File template : templates ) {
			if ( template.isFile() ) {
				ModuleParseResult parseResult = ModuleParser.parse( template.newInputStream() )
				ModuleParseResult.ArtifactName name = parseResult.getVersionArtifactName()
				def identifier = parseResult.getIdentifier()
				def artifactName = identifier.name
				File moduleFileFinalDestination = new File(
						baseModulFoder + File.separator + artifactName.replace(
								'.',
								File.separator
						) + File.separator + tokenReplacement + File.separator + "module.xml"
				)
				moduleFileFinalDestination.getParentFile(  ).mkdirs(  );
				moduleFileFinalDestination.write( template.text.replaceAll( placeHolder, tokenReplacement ) )
			}
		}
	}

}
