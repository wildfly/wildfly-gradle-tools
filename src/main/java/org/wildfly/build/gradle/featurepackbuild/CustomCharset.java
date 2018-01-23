package org.wildfly.build.gradle.featurepackbuild;

import java.nio.charset.Charset;

interface CustomCharset {

	final Charset utf8 = Charset.forName( "UTF-8" );

}
