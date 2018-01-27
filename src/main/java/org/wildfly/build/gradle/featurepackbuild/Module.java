package org.wildfly.build.gradle.featurepackbuild;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * @author Andrea Boriero
 */
class Module {
	private static final String FILENAME = "module.xml";
	private final File module;

	public Module(ModuleFolderPath moduleFolderPath) {
		this.module = new File( moduleFolderPath.toString() + File.separator + FILENAME );
	}

	public void create(List<String> fileContentLines) throws IOException {
		createModuleFile();
		writeContent( fileContentLines, module );
	}

	private void writeContent(List<String> fileContentLines, File moduleXml) throws IOException {
		try (FileWriter fw = new FileWriter( moduleXml, true )) {
			try (BufferedWriter bw = new BufferedWriter( fw )) {
				for ( String line : fileContentLines ) {
					bw.append( line ).append( System.lineSeparator() );
				}
			}
		}
	}

	private void createModuleFile() throws IOException {
		module.createNewFile();
	}
}
