# parasoft-findings-plugin

The Parasoft Findings Plugin project contains a plugin which allows publishing Parasoft findings to Jenkins.

Prerequisites for building:
--------------------------
 - Java 7 JDK
 - Maven 3

To build:
---------

mvn clean install

To run:
-------

mvn hpi:run


To perform a release:
--------------------

mvn release:prepare release:perform
