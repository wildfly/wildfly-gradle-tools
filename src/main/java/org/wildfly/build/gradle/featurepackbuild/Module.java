/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.wildfly.build.gradle.featurepackbuild;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * @author Andrea Boriero
 */
public class Module {
	private static final String FILENAME = "module.xml";
	private final File module;

	public Module(ModuleDestinationFolder moduleDestinationFolder) {
		this.module = new File( moduleDestinationFolder.getModuleFilePath( FILENAME ) );
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
