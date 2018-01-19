# Gradle plugin for WildFly provisioning

*org.wildfly.build.provision* plugin. See also instruction on the [Gradle Plugin Portal](https://plugins.gradle.org/plugin/org.wildfly.build.provision).

## Example build.gradle

    plugins {
       id 'org.wildfly.build.provision' version "0.0.1"
    }

    repositories {
       mavenCentral()
       maven {
          name 'jboss-nexus'
          url "http://repository.jboss.org/nexus/content/groups/public/"
       }
    }
    
    provision {
       //Optional provisioning configuration:
       //configuration = "custom-server-provisioning.xml"
       
       //Optional destination directory:
       //destinationDir = file("$buildDir/light-wildfly")
    }


## Description

The `:provision` target will download and install a WildFly server to the target directory.

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

### Output directory

The default is to unpack a copy of the server in the "provisioned-wildfly" subdirectory of your build
directory.

Set the `destinationDir` property to any other directory to change this.

If you choose a `destinationDir` which is not in your build directory you might want to explicitly add a clean task:

    clean {
       delete file("/somehere/custom-wildfly")
    }

## Limitations

Provisioning is a powerful feature of the WildFly server which includes options to override dependencies,
inject parameters into templates, and more.
Such capabilities have not yet been exposed in this plugin.
Patches and help welcome!

## Maven users

This plugin is actually a port to Gradle of an existing Maven plugin.
Maven users should use [the original plugin](https://github.com/wildfly/wildfly-build-tools/).


## License

This software and its documentation are distributed under the Apache Software License 2.0.
Refer to LICENSE.txt for more information.
