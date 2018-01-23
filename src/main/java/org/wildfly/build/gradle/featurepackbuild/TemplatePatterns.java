package org.wildfly.build.gradle.featurepackbuild;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class TemplatePatterns {

	private final Iterable<PatternRep> allPatterns;

	public TemplatePatterns(Map<String, String> variables) {
		this.allPatterns = variables.entrySet().stream().map( kv -> new PatternRep( kv.getKey(), kv.getValue() ) ).collect( Collectors.toList() );
	}

	public String replaceAllVariables(String line) {
		for ( PatternRep pr : allPatterns ) {
			line = pr.replaceAll( line );
		}
		return line;
	}

	private static class PatternRep {
		private final String value;
		private final Pattern pattern;

		public PatternRep(String key, String value) {
			this.value = value;
			this.pattern = Pattern.compile( "\\$\\{" + key + "\\}" );
		}

		public String replaceAll(String line) {
			return pattern.matcher( line ).replaceAll( value );
		}
	}
}
