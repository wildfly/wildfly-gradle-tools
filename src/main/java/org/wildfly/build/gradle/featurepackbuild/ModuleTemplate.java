/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.wildfly.build.gradle.featurepackbuild;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import nu.xom.ParsingException;
import org.wildfly.build.util.ModuleParseResult;
import org.wildfly.build.util.ModuleParser;

/**
 * @author Andrea Boriero
 */
class ModuleTemplate {
	private final String placeHolder = "\\$\\{slot\\}";
	private final File template;
	private final String slot;

	public ModuleTemplate(File template, String slot) {
		this.template = template;
		this.slot = slot;
	}

	public List<String> getXmlModuleContent() throws IOException {
		List<String> templateContent = Files.readAllLines( template.toPath(), Charset.forName( "UTF-8" ) );
		List<String> replacedContent = new ArrayList<>();
		templateContent.forEach( line -> {
									 replacedContent.add( line.replaceAll( placeHolder, slot ) );
								 }
		);
		return replacedContent;
	}

	public String getArtifactName() throws IOException, ParsingException {
		ModuleParseResult parseResult = ModuleParser.parse( template.toPath() );
		return parseResult.getIdentifier().getName();
	}
}
