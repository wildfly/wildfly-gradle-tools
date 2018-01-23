package org.wildfly.build.gradle.featurepackbuild;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

class ModuleFolderPath {

	private static final Pattern moduleId2PathSeparator = Pattern.compile( "\\." );

	private final Path configuredRootPath;
	private final String moduleIdentifier;
	private final String slotName;

	private Path moduleXmlFolder;

	public ModuleFolderPath(Path rootFolder, ModuleTemplate moduleDef) {
		if ( rootFolder == null ) throw new NullPointerException( "rootFolder is required" );
		if ( moduleDef == null ) throw new NullPointerException( "moduleIdentifier is required" );
		if ( false == Files.isDirectory( rootFolder ) ) throw new IllegalArgumentException( "Is not an existing directory: " + rootFolder );
		this.configuredRootPath = rootFolder;
		this.moduleIdentifier = moduleDef.getModuleIdentifier();
		this.slotName = moduleDef.getSlot();
	}

	public void createFolders() throws IOException {
		moduleXmlFolder = getModuleXmlFileFolderPath( configuredRootPath, moduleIdentifier, slotName );
		Files.createDirectories( moduleXmlFolder );
	}

	public Path getModuleXmlFolder() {
		if ( moduleXmlFolder == null ) {
			throw new IllegalArgumentException( "Directories not created yet: successfully invoke #createFolders() first." );
		}
		return moduleXmlFolder;
	}

	private Path getModuleXmlFileFolderPath(Path baseRootFolder, String artifactName, String slotName) throws IOException {
		final Path layerPath = getBaseModuleFolderName( baseRootFolder );
		return getModuleFolderName( layerPath, artifactName, slotName );
	}

	private Path getBaseModuleFolderName(Path baseRootFolder) throws IOException {
		return baseRootFolder
				.resolve( "modules" )
				.resolve( "system" )
				.resolve( "layers" )
				.resolve( "base" );
	}

	private Path getModuleFolderName(Path layerPath, String artifactName, String slotName) throws IOException {
		final String[] split = moduleId2PathSeparator.split( artifactName );
		Path base = layerPath;
		for ( String part : split ) {
			part = part.trim();
			if ( part.length() > 0 ) {
				base = base.resolve( part );
			}
		}
		final String slot = slotName.trim();
		if ( slot.length() > 0 && ! "main".equals( slot ) ) {
			base = base.resolve( slot );
		}
		return base;
	}

}
