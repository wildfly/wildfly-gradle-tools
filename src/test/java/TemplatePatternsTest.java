import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import org.wildfly.build.gradle.featurepackbuild.TemplatePatterns;

public class TemplatePatternsTest {

	@Test
	public void testFeaturePackTask() throws Exception {
		Map<String,String> variables = new HashMap<>();
		variables.put( "slot", "alpha" );
		variables.put( "another", "dunno" );
		TemplatePatterns tps = new TemplatePatterns( variables );
		final String s = tps.replaceAllVariables( "Hello ${slot} this is ${another} test" );
		Assert.assertEquals( "Hello alpha this is dunno test", s );
	}

}
