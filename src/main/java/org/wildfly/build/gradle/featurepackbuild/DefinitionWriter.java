package org.wildfly.build.gradle.featurepackbuild;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import javax.xml.stream.XMLStreamException;

import org.wildfly.build.Locations;
import org.wildfly.build.pack.model.FeaturePackDescription;
import org.wildfly.build.pack.model.FeaturePackDescriptionXMLWriter11;

public class DefinitionWriter {

	private static final String FILENAME = Locations.FEATURE_PACK_DESCRIPTION;
	private FeaturePackDescription fpd = new FeaturePackDescription();

	public void writeFile(Path outputDirectory) throws IOException, XMLStreamException {
		final File outputFile = outputDirectory.resolve( FILENAME ).toFile();
		FeaturePackDescriptionXMLWriter11.INSTANCE.write( fpd, outputFile );
	}

	public void registerRequiredArtifacts(ArtifactsRegistry requiredArtifacts) {
		fpd.getArtifactVersions().addAll( requiredArtifacts.getAllArtifacts() );
		fpd.getDependencies().addAll( requiredArtifacts.getDependencyFeaturePacks() );
	}

}
