/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.wildfly.build.gradle.featurepackbuild;

import java.io.File;

/**
 * @author Andrea Boriero
 */
class ModuleDestinationFolder {
	private File moduleXmlBaseFoder;

	public ModuleDestinationFolder(File baseRootFoder, String artifactName, String slotName) {
		moduleXmlBaseFoder = new File( getModuleXmlFileFolerName( baseRootFoder, artifactName, slotName ) );
	}

	public void createFolder() {
		moduleXmlBaseFoder.mkdirs();
	}

	public String getModuleFilePath(String fileName) {
		return moduleXmlBaseFoder.getPath() + File.separator + fileName;
	}

	private String getModuleXmlFileFolerName(File baseRootFoder, String artifactName, String slotName) {
		return getdModuleFolderName( getBaseModuleFolderName( baseRootFoder ), artifactName, slotName );
	}

	private String getBaseModuleFolderName(File baseRootFoder) {
		return baseRootFoder.getAbsolutePath()
				+ File.separator + "modules"
				+ File.separator + "system"
				+ File.separator + "layers"
				+ File.separator + "base";
	}

	private String getdModuleFolderName(String baseModuleFolder, String artifactName, String slotName) {
		return baseModuleFolder
				+ File.separator + getArtifactNameRelatedPath( artifactName )
				+ File.separator + slotName;
	}

	private String getArtifactNameRelatedPath(String artifactName) {
		return artifactName.replace( ".", File.separator );
	}
}
