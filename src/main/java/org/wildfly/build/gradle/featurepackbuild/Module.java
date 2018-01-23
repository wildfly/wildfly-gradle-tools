package org.wildfly.build.gradle.featurepackbuild;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

class Module {

	private static final String FILENAME = "module.xml";

	private final Path moduleDescriptorTarget;

	public Module(ModuleFolderPath moduleFolderPath) {
		moduleDescriptorTarget = moduleFolderPath
				.getModuleXmlFolder()
				.resolve( FILENAME );
	}

	public void createWithContent(String fileContent) throws IOException {
		Files.write( moduleDescriptorTarget,
					 Collections.singletonList( fileContent ),
					 CustomCharset.utf8 );
	}

}
