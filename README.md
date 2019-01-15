This repository contains two plugins: one to create a custom WildFly server by assembling various feature packs, and one to create such feature packs.

# Gradle plugin for WildFly provisioning

*org.wildfly.build.provision* plugin. See also the [plugin page](https://plugins.gradle.org/plugin/org.wildfly.build.provision) on the Gradle Plugin Portal for details on how to apply the latest plugin version to your build.

## Minimum working `build.gradle` :

	plugins {
		id 'org.wildfly.build.provision' version '0.0.9'
	}
	
	provision {
	    variables['wildfly.version'] = '11.0.0.Final'
	}

Run with

 > gradle provision

You'll find a fully working copy of WildFly 11.0.0.Final in your `build/provisioned-wildfly` directory.


## More powerful example `build.gradle` :

    plugins {
       id 'org.wildfly.build.provision' version "0.0.9"
    }

	repositories {
		mavenLocal()
		mavenCentral()
		maven {
			name 'jboss-nexus'
			url "http://repository.jboss.org/nexus/content/groups/public/"
		}
	}
	
	provision {
		//Optional provisioning configuration:
		//configuration = "custom-server-provisioning.xml"

		//You can set variables to be injected in the provisioning.xml configuration;
		//The 'wildfly.version' variable is required by the default configuration:
		variables['wildfly.version'] = '11.0.0.Final'

		//The default is to add jboss-nexus automatically as you'll likely need it,
		//yet most builds will want to control such details explicitly.
		autoAddRepositories = false
		
		//Optional destination directory:
		//destinationDir = file("$buildDir/light-wildfly")
		//Overrides the version of an artifact:
		override( 'org.hibernate:hibernate-core' ) {
			version = '5.3.0.Beta1'
		}
		override( 'org.hibernate:hibernate-envers' ) {
			version = '5.3.0.Beta1'
		}
		//Overrides version, group, etc.. :
		override( 'org.hibernate.javax.persistence:hibernate-jpa-2.1-api' ) {
			groupId = 'javax.persistence'
			artifactId = 'javax.persistence-api'
			version = '2.2'
		}
	}


## Description

The `provision` task will download and install a WildFly server to the target directory, while
upgrading the two Hibernate ORM libraries and replacing the JPA 2.1 API with the standard JPA 2.2 API.

N.B. this example is not replacing all Hibernate ORM libraries so it wouldn't provide a reliable server!
Doing such an upgrade properly shouldn't be done via version overrides but using an appropriate
feature pack - using the more powerful 'configuration' property.
The overrides capability is meant for developers experimenting with changes to the WildFly server.

### Thin server

The created server will be of the "thin" kind: almost no jars and will take minimal space,
but the necessary jars will be stored in your local maven repository.

### Custom server

You can use a `server-provisioning.xml` to pick and choose only the feature packs you need;
this way you can materialize a slim server which only has the features you need.

Currently there isn't a large collection of feature packs but this will improve soon.

An example `server-provisioning.xml` could be:

    <server-provisioning xmlns="urn:wildfly:server-provisioning:1.1">
       <feature-packs>
	      <feature-pack groupId="org.wildfly" artifactId="wildfly-feature-pack" version="11.0.0.Final"/>
       </feature-packs>
    </server-provisioning>

The above will materialize a "full" WildFly 11.0.0.Final in the target directory.

A more complex example:

    <server-provisioning xmlns="urn:wildfly:server-provisioning:1.1">
       <feature-packs>
          <feature-pack groupId="org.wildfly" artifactId="wildfly-servlet-feature-pack" version="11.0.0.Final"/>
          <feature-pack groupId="org.hibernate.lucene-jbossmodules" artifactId="lucene-jbossmodules" version="5.5.5.hibernate05"/>
       </feature-packs>
    </server-provisioning>

The above example will materialize a smaller WildFly server (the "servlet" edition), but then
include as well the Apache Lucene libraries appropriately packaged as modules.

### Thin vs Full servers

By default the `<server-provisioning>` element produces a _thin_ server:
that's a very small server as it doesn't include a copy of all jar files,
rather referencing them from your local Maven repository.

(Yes it will use the Maven repository even during a Gradle build)

This is ideal for integration tests which need to produce and delete servers regularly,
and will transparently benefit from the Maven cache.

If you don't want to rely on the Maven cache you can produce a full copy
of the WildFly server by setting the `copy-module-artifacts=true` attribute
in the top element.

For example:

    <server-provisioning xmlns="urn:wildfly:server-provisioning:1.1" copy-module-artifacts="true">
      <feature-packs>
        <feature-pack groupId="org.wildfly" artifactId="wildfly-feature-pack" version="${project.version}">
          <contents>
            <filter pattern="copyright.txt" include="false"/>
            <filter pattern="README.txt" include="false"/>
            <filter pattern="docs/contrib/*" include="false"/>
          </contents>
        </feature-pack>
      </feature-packs>
    </server-provisioning>

For a full list of the options supported by `server-provisioning`, see also the
[Maven version of this plugin](https://github.com/wildfly/wildfly-build-tools/).

### Output directory

The default is to unpack a copy of the server in the "provisioned-wildfly" subdirectory of your build
directory.

Set the `destinationDir` property to any other directory to change this.

If you choose a `destinationDir` which is not in your build directory you might want to explicitly add a clean task:

    clean {
       delete file("/somehere/custom-wildfly")
    }

## Limitations

Provisioning is a powerful feature of the WildFly server which includes several other capabilities which have not
yet been implemented in this plugin.
Patches and help welcome!

## Maven users

This plugin is actually a fairly limited port to Gradle of an existing, more powerful and more mature Maven plugin.
Maven users should use [the original plugin](https://github.com/wildfly/wildfly-build-tools/).

## License

This software and its documentation are distributed under the Apache Software License 2.0.
Refer to LICENSE.txt for more information.
