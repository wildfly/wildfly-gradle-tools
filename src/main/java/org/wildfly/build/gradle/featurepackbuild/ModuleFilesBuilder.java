package org.wildfly.build.gradle.featurepackbuild;

import java.io.File;
import java.io.IOException;

import nu.xom.ParsingException;

/**
 * @author Andrea Boriero
 */
class ModuleFilesBuilder {

	private String slotName;
	private File templateFolder;
	private File rootModulesFolder;

	public ModuleFilesBuilder(File templateFolder, String slot, File rootModulesFolder) {
		this.templateFolder = templateFolder;
		this.slotName = slot;
		this.rootModulesFolder = rootModulesFolder;
	}

	void build() throws IOException, ParsingException {
		File[] templates = templateFolder.listFiles();
		for ( File template : templates ) {
			if ( template.isFile() ) {
				buildModule( template );
			}
		}
	}

	private void buildModule(File templateFile) throws IOException, ParsingException {
		ModuleTemplate moduleTemplate = new ModuleTemplate( templateFile, slotName );

		ModuleFolderPath moduleFolderPath = new ModuleFolderPath(
				rootModulesFolder,
				moduleTemplate.getArtifactName(),
				slotName
		);

		moduleFolderPath.createFolder();

		Module xmlFile = new Module( moduleFolderPath );

		xmlFile.create( moduleTemplate.getXmlModuleContent() );
	}

}
