/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.wildfly.build.gradle.featurepackbuild;

import java.io.File;
import java.io.IOException;

import nu.xom.ParsingException;

/**
 * @author Andrea Boriero
 */
public class ModuleFilesBuilder {

	private File templateDir;
	private String slotName;
	private File baseModulFoder;

	public ModuleFilesBuilder(File templateDir, String slot, File destinationRootFolder) {
		this.templateDir = templateDir;
		this.slotName = slot;
		this.baseModulFoder = destinationRootFolder;
	}

	void build() throws IOException, ParsingException {
		File[] templates = templateDir.listFiles();
		for ( File templateFile : templates ) {
			if ( templateFile.isFile() ) {
				Template template = new Template( templateFile, slotName );

				ModuleDestinationFolder moduleDestinationFolder = new ModuleDestinationFolder(
						baseModulFoder,
						template.getArtifactName(),
						slotName

				);

				moduleDestinationFolder.createFolder();

				Module xmlFile = new Module( moduleDestinationFolder );

				xmlFile.create( template.getXmlModuleContent() );
			}
		}
	}

}
