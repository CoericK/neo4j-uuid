GraphAware Neo4j UUID
=====================

[![Build Status](https://travis-ci.org/graphaware/neo4j-uuid.png)](https://travis-ci.org/graphaware/neo4j-uuid) | <a href="http://graphaware.com/downloads/" target="_blank">Downloads</a> | <a href="http://graphaware.com/site/uuid/latest/apidocs/" target="_blank">Javadoc</a> | Latest Release: 2.3.3.37.8

GraphAware UUID is a simple library that transparently assigns a UUID to newly created nodes and relationships in the graph and makes sure nobody
can (accidentally or intentionally) change or delete them.

Getting the Software
--------------------

### Server Mode

When using Neo4j in the <a href="http://docs.neo4j.org/chunked/stable/server-installation.html" target="_blank">standalone server</a> mode,
you will need the <a href="https://github.com/graphaware/neo4j-framework" target="_blank">GraphAware Neo4j Framework</a> and GraphAware Neo4j UUID .jar files (both of which you can <a href="http://graphaware.com/downloads/" target="_blank">download here</a>) dropped
into the `plugins` directory of your Neo4j installation. After changing a few lines of config (read on) and restarting Neo4j, the module will do its magic.

### Embedded Mode / Java Development

Java developers that use Neo4j in <a href="http://docs.neo4j.org/chunked/stable/tutorials-java-embedded.html" target="_blank">embedded mode</a>
and those developing Neo4j <a href="http://docs.neo4j.org/chunked/stable/server-plugins.html" target="_blank">server plugins</a>,
<a href="http://docs.neo4j.org/chunked/stable/server-unmanaged-extensions.html" target="_blank">unmanaged extensions</a>,
GraphAware Runtime Modules, or Spring MVC Controllers can include use the UUID as a dependency for their Java project.

#### Releases

Releases are synced to <a href="http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22uuid%22" target="_blank">Maven Central repository</a>. When using Maven for dependency management, include the following dependency in your pom.xml.

    <dependencies>
        ...
        <dependency>
            <groupId>com.graphaware.neo4j</groupId>
            <artifactId>uuid</artifactId>
            <version>2.3.3.37.8</version>
        </dependency>
        ...
    </dependencies>

#### Snapshots

To use the latest development version, just clone this repository, run `mvn clean install` and change the version in the
dependency above to 2.3.3.37.9-SNAPSHOT.

#### Note on Versioning Scheme

The version number has two parts. The first four numbers indicate compatibility with Neo4j GraphAware Framework.
 The last number is the version of the UUID library. For example, version 2.1.3.11.1 is version 1 of the UUID library
 compatible with GraphAware Neo4j Framework 2.1.3.11.


Setup and Configuration
--------------------

### Server Mode

Edit neo4j.properties to register the UUID module:

```
com.graphaware.runtime.enabled=true

#UIDM becomes the module ID:
com.graphaware.module.UIDM.1=com.graphaware.module.uuid.UuidBootstrapper

#optional, default is uuid:
com.graphaware.module.UIDM.uuidProperty=uuid

#optional, default is all nodes:
com.graphaware.module.UIDM.node=hasLabel('Label1') || hasLabel('Label2')

#optional, default is no relationships:
com.graphaware.module.UIDM.relationship=isType('Type1')

#optional, default is uuidIndex
com.graphaware.module.UIDM.uuidIndex=uuidIndex

#optional, default is uuidRelIndex
com.graphaware.module.UIDM.uuidRelationshipIndex=uuidRelIndex
```

Note that "UIDM" becomes the module ID. 

`com.graphaware.module.UIDM.uuidProperty` is the property name that will be used to store the assigned UUID on nodes and relationships. The default is "uuid".

`com.graphaware.module.UIDM.node` specifies either a fully qualified class name of [`NodeInclusionPolicy`](http://graphaware.com/site/framework/latest/apidocs/com/graphaware/common/policy/NodeInclusionPolicy.html) implementation,
or a Spring Expression Language expression determining, which nodes to assign a UUID to. The default is to assign the
UUID property to every node which isn't internal to the framework.

`com.graphaware.module.UIDM.relationship` specifies either a fully qualified class name of [`RelationshipInclusionPolicy`](http://graphaware.com/site/framework/latest/apidocs/com/graphaware/common/policy/RelationshipInclusionPolicy.html) implementation,
or a Spring Expression Language expression determining, which relationships to assign a UUID to. The default is **not** to assign the
UUID property to any relationship. If you want to assign UUID to all relationship, please use `com.graphaware.module.UIDM.relationship=com.graphaware.runtime.policy.all.IncludeAllBusinessRelationships`

`com.graphaware.module.UIDM.uuidIndex` is the index name that will be used to index nodes based on their UUID. The default is "uuidIndex".

`com.graphaware.module.UIDM.uuidRelationshipIndex` is the index name that will be used to index relationships based on their UUID. The default is "uuidRelIndex".

### Embedded Mode / Java Development

To use the UUID module programmatically, register the module like this

```java
 GraphAwareRuntime runtime = GraphAwareRuntimeFactory.createRuntime(database);  //where database is an instance of GraphDatabaseService
 UuidModule module = new UuidModule("UUIDM", UuidConfiguration.defaultConfiguration());
 runtime.registerModule(module);
 runtime.start();
```

Alternatively:
```java
 GraphDatabaseService database = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(pathToDb)
    .loadPropertiesFromFile(this.getClass().getClassLoader().getResource("neo4j.properties").getPath())
    .newGraphDatabase();
 
 //make sure neo4j.properties contain the lines mentioned in previous section
```

Using GraphAware UUID
---------------------

Apart from the configuration described above, the GraphAware UUID module requires nothing else to function. It will assign a UUID to nodes configured,
and will prevent modifications to the UUID or deletion of the UUID property from these nodes by not allowing the transaction to commit.
You can also retrieve a node by UUID.

### Server Mode

In Server Mode, a node can be retrieved by its UUID via the REST API.

You can issue GET requests to `http://your-server-address:7474/graphaware/uuid/{moduleId}/node/{uuid}` to get the node ID for a given uuid.

GET requests to `http://your-server-address:7474/graphaware/uuid/{moduleId}/relationship/{uuid}` will get the relationship ID for a given uuid.

{moduleId} is the module ID the UUID Module was registered with. You can omit this part of the URL, in which case "UIDM" is assumed as the default value.
If no node exists with the given UUID, a 404 status code will be returned.

### Java API

To use the Java API to find a node by its UUID, please instantiate `UuidReader` and use the method `getNodeIdByUuid` or `getRelationshipByUuid`

```
 UuidConfiguration configuration = getStartedRuntime(database).getModule(moduleId, UuidModule.class).getConfiguration();
 UuidReader reader = UuidReader(configuration, database);
 Node node = getNodeIdByUuid(uuid);
 Relationship rel = getRelationshipByUuid(uuid);
```

Please refer to Javadoc for more detail.

License
-------

Copyright (c) 2016 GraphAware

GraphAware is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with this program.
If not, see <http://www.gnu.org/licenses/>.
