# SynBio Toolkit
A collection of tools written in Java for interacting with SynBioHub

## Purpose
The aim of this toolkit is to provide convenient solutions for streamlining common data management tasks performed by biological researchers who wish to upload their designs into an instance of [SynBioHub](https://github.com/SynBioHub/synbiohub), for example the public [synbiohub.org](https://synbiohub.org/) repository.

## Features

### SBOL Generator
The first component of the toolkit is a library containing utility methods for generating SBOL documents and [ComponentDefinition](https://dissys.github.io/sbol-owl/sbol-owl.html#ComponentDefinition) entities from pre-defined SBOL templates. It also provides methods for updating [SequenceAnnotation](https://dissys.github.io/sbol-owl/sbol-owl.html#SequenceAnnotation) elements within existing component definitions. Another principal function that the library provides is to 'flatten' the parent-child hierarchy of component definitions and their child sequences into one top-level component definition, which enables full visibility of all elements in the design on the SynBioHub visualisation web UI.

### Excel to SBOL Transformer
Many biological researchers rely on MS Excel to 

### SynBioHub CLI Client


## Building the CLI Application

In NetBeans, right-click on the project in the Projects explorer window, and select 'Set Configuration' -> 'cli' from the context menu.

## Launching the CLI Application

```
java -jar target/sbol-toolkit-web-1.0.0-SNAPSHOT.war --collection-url=http://localhost:7777/user/Johnny/a_random_id/a_random_id_collection/1 --username=<email> password=<password> --dir-path=D:\temp\sbol\codA_Km_0081_slr1130.xml --file-ext-filter=xml --overwrite=true
```
To see the CLI help command run `java -jar target/sbol-toolkit-web-1.0.0-SNAPSHOT.war --help`.

