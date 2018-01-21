package org.wildfly.build.gradle.provisioning;

public class MissingVersionException extends RuntimeException {
	public MissingVersionException(String message) {
		super( message );
	}
}
