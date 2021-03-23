# SynBio Toolkit
A collection of tools written in Java for interacting with SynBioHub

## Purpose
The aim of this toolkit is to provide convenient solutions for streamlining common data management tasks performed by biological researchers who wish to upload their designs into an instance of [SynBioHub](https://github.com/SynBioHub/synbiohub), for example the public [synbiohub.org](https://synbiohub.org/) repository. There are two main components in the toolkit: 'SBOL Transformer' and 'SynBioHub CLI Client'.

## SBOL Transformer Library Features
### SBOL Generation
The first component of the toolkit is a library containing utility methods for generating SBOL documents and [ComponentDefinition](https://dissys.github.io/sbol-owl/sbol-owl.html#ComponentDefinition) entities from pre-defined SBOL templates. It also provides methods for updating [SequenceAnnotation](https://dissys.github.io/sbol-owl/sbol-owl.html#SequenceAnnotation) elements within existing component definitions. Another principal function that the library provides is to \'flatten\' the parent-child hierarchy of component definitions and their child sequences into one top-level component definition, which enables full visibility of all elements in the design on the SynBioHub visualisation web UI.

### Excel to SBOL Conversion
Many biological researchers rely on MS Excel spreadsheets to store their experimental data. Another feature of the library is the ability to convert xslx files into SBOL documents. Utility reader methods are offered that transform each row in a specified spreadsheet into a new SBOL component definition corresponding to the template structure defined previously. The current implementations of this transformer interface are specific to the data format and plasmid designs appropriate for a particular lab in the authors' research centre, but the option to extend these functions for other use cases will be available.

## SynBioHub CLI Client
Depositing multiple data files into a repository can be very tedious, repetitive and is a poor use of time for researchers. The SynBioHub CLI Client minimises obstacles to FAIR biological research data management by providing a command line tool that can be used to easily upload any number of SBOL files, metadata and attachments into a SynBioHub instance. Metadata and attachments for existing designs in SynBioHub can also be edited in batch processing using the client. The client interacts with the [SynBioHub API](https://wiki.synbiohub.org/api-docs) to perform these functions. The program is run as an [executable JAR](https://docs.spring.io/spring-boot/docs/2.4.4/reference/html/using-spring-boot.html#using-boot-running-as-a-packaged-application).

To run the program, execute the following command:
```shell
java -jar synbio-toolkit.jar
```
The user will then be asked to select from `deposit` or `update` actions.

### Data Deposition
To deposit data into SynBioHub, the user must specify the file path to the directory containing the SBOL files and/or sub-directories they wish to upload, along with details of the destination server and collection. The CLI client requires certain parameters to be specified at runtime, and will prompt the user for the following inputs:

| Parameter            | Description           | Default  |
| :-------------------- |---------------------| --------:|
| Directory path       | The path in the file system to the directory containing files to upload     | -      |
| File extension       | The file extension string to identify files to upload, for example '.xml'   |  \'.\*\' |
| Multiple collections | Whether to create multiple collections                                      |   -    |
| Sub-folders          | If multiple collections, create a new collection per sub-directory          |   -    |
| Create collection    | \'Y\' to create a new collection to upload the files into, or \'N\' to upload the files to an existing collection                            |   -    |
| Name                 | If creating a new collection, a name to assign                              |   -    |
| URL                  | If creating a new collection, this should be the URL of the SynBioHub server. Otherwise, provide the URL of the existing collection                                                           |   -    |
| Version              | If uploading to an existing collection, specify the version                 |   -    |
| Overwrite            | If uploading to an existing collection, \'Y\' to overwrite or \'N\' to merge  | Merge  |
| Username             | Your username for the target SynBioHub server (usually your email address)    |   -    |
| Password             | The password for your user on the target SynBioHub server                     |   -    |


### Data Updates


## Development
### Spring Boot
The CLI Client is a Spring Boot application. Entry to the program is through the [ApplicationRunner](https://docs.spring.io/spring-boot/docs/2.4.4/api/org/springframework/boot/ApplicationRunner.html) interface.
### Building the CLI Application

In NetBeans, right-click on the project in the Projects explorer window, and select 'Set Configuration' -> 'cli' from the context menu.

### Launching the CLI Application

```
java -jar target/sbol-toolkit-web-1.0.0-SNAPSHOT.war --collection-url=http://localhost:7777/user/Johnny/a_random_id/a_random_id_collection/1 --username=<email> password=<password> --dir-path=D:\temp\sbol\codA_Km_0081_slr1130.xml --file-ext-filter=xml --overwrite=true
```
To see the CLI help command run `java -jar target/sbol-toolkit-web-1.0.0-SNAPSHOT.war --help`.

