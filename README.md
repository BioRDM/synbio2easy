# SynBio2Easy 
A biologist-friendly tool for batch operations on SBOL designs with metadata input from Excel.

## Purpose
The aim of this toolkit is to provide convenient solutions for streamlining common data management 
tasks performed by biological researchers who work with designs libraries rather than individual sbol files. 

For example, it helps with multifiles upload to an instance of [SynBioHub](https://github.com/SynBioHub/synbiohub) 
like the public [synbiohub.org](https://synbiohub.org/) repository.

It permits batch metadata data update either off-line on a sbol document or online on records in SynBioHub. 

## Quick start

### Installation
* check if java version 11 or higher is available in the system, type in a terminal
```shell
java -version
```
You should see a version text. In case of error follow a guide how to install java on your machine.

* download SynBio2Easy.jar and examples.zip from the latest release [SynBio2Easy](https://github.com/BioRDM/synbio2easy/releases)
* extract files from examples.zip into a folder, we assume it is called 'examples'
* copy the SynBio2Easy.jar into the 'examples' folder

### Running the tool
* start a terminal/console window in your system
* navigate in the terminal to the 'examples' folder where you stored the SynBio2Easy.jar and examples files
* type
```shell
java -jar SynBio2Easy.jar
```
and press enter.

The simple, interactive, command line program starts. You can choose one of the required operations
and then you will be prompted for the necessary parameters and inputs. 
Each prompt contains short description of the parameters asked as well as sensible defaults.

### Example workflow
The best way to learn this tool is by trying it with our example workflow. 

Follow our step by step guide document [test-workflow](examples/README.md)
and examine the provided input files and the outcomes of each of the available operations.


## SynBio2Easy features

### Metadata input file

Metadata that describe the biological designs to be generated or annotated in SBOL documents and deposited in SynBioHub 
are specified for SynBio2Easy in a metadata input file. 
These data are expected to be in tabular format in an MS Excel spreadsheet file, 
including any of the column names as described in the list below.

* display_id: This is the only mandatory column in the spreadsheet. It is used as the primary key to match with component definition display IDs in SynBioHub and in SBOL documents.
* name: The optional name of the design, which will be displayed as free text in the record in SynBioHub
* version: The optional version of the component definition, which can be either numeric (e.g. 1.0) or free text (e.g. 1.0-alpha)
* attachment_filename: The absolute or relative (from the current working directory) path to a file to be attached to the design in SynBioHub
* summary: The short description that will be written in the design’s description property of the SBOL component definition.
* description: The text that will appear as the record description in SynBioHub (mutable description)
* notes: The notes that will appear on the record in SynBioHub
* author: The authors that will be listed in the design in the SBOL document
* key: An entirely optional column that can be used to store a unique identifier string for a design, which can then be interpolated by SynBio2Easy when it is referenced in other columns, such as in the ‘display_id’ and ‘name’ cells’ values

The cells in the spreadsheet columns support simple templating using keyword strings such as “{key}”, “{display_id}” and “{name}”: 
the SynBio2Easy interpolation engine can then construct the target string value with the relevant values 
from those cells in the same row. 
In addition, standard Excel formulas are supported, for example ‘concatenate’ which provides powerful 
ways to combine values from other cells.

Check the [examples](examples) folder for examples of metadata input files and the use of parameters and functions.

### Available operations

+-----------------------+----------------------------------------------+
| Command               | Behaviour and example use case               |
+=======================+==============================================+
| GENERATE              | Generates a series of designs based on an    |
|                       | SBOL template and \'concrete\' instance      |
|                       | parameters (including sub-components\'       |
|                       | sequences) specified in an Excel table       |
|                       |                                              |
|                       | Use case: generation of library of similar   |
|                       | designs                                      |
+-----------------------+----------------------------------------------+
| ANNOTATE              | Adds information (e.g. descriptions,         |
|                       | authors) to multiple component definitions   |
|                       | in an SBOL document using details defined in |
|                       | an Excel table                               |
|                       |                                              |
|                       | Use case: batch update of designs'           |
|                       | descriptions to change their status to       |
|                       | "tested" and add provenance                  |
+-----------------------+----------------------------------------------+
| FLATTEN               | Converts a tree of SBOL sub-components in a  |
|                       | design into a \'flattened\' component        |
|                       | definition with an annotated linear sequence |
|                       |                                              |
|                       | Use case: create an alternative              |
|                       | representation of a plasmid suitable for     |
|                       | export to GenBank file format                |
+-----------------------+----------------------------------------------+
| DEPOSIT               | Deposits files from a folder(s) into         |
|                       | SynBioHub collection(s)                      |
|                       |                                              |
|                       | Use case: Deposition of a large collection   |
|                       | of designs                                   |
+-----------------------+----------------------------------------------+
| UPDATE                | Adds information (e.g. notes, attachment     |
|                       | files) to multiple records in SynBioHub      |
|                       | using details defined in an Excel table.     |
|                       | Unlike ANNOTATE it is an online operation on |
|                       | a server.                                    |
|                       |                                              |
|                       | Use case: attach verified sequences to       |
|                       | designs' descriptions                        |
+-----------------------+----------------------------------------------+
| CLEAN                 | Removes annotations and namespaces specific  |
|                       | to SynBioHub from an SBOL document, so it    |
|                       | can be re-uploaded to SynBioHub              |
|                       |                                              |
|                       | Use case: quick edit of a SynBioHub          |
|                       | collection using a text editor and           |
|                       | downloaded XML file                          |
+-----------------------+----------------------------------------------+
| SYNBIO2TABLE          | Retrieves identity details of all members of |
|                       | a collection and saves them to an Excel file |
|                       | with headings for metadata columns supported |
|                       | by SynBio2Easy                               |
|                       |                                              |
|                       | Use case: preparation of input file for the  |
|                       | UPDATE operation                             |
+-----------------------+----------------------------------------------+

### CLI parameters

The CLI client requires certain parameters to be specified at runtime, and will prompt the user for the following inputs:

| Parameter            | Description           | Default  |
| :-------------------- |:---------------------------------------------------| --------:|
| Filename             | The path in the file system to the Excel file containing design update data | -  |
| URL                  | The URL of the existing collection in SynBioHub to update                   |  - |
| Username             | Your username for the target SynBioHub server (usually your email address)    |   -    |
| Password             | The password for your user on the target SynBioHub server                     |   -    |


| CLI Parameter (Long) | CLI Parameter (Short) | Description                                                                                                            | Default Value |
|----------------------|-----------------------|------------------------------------------------------------------------------------------------------------------------|---------------|
| --template-file      | -t                    | The absolute or relative path to the template file from which to generate the SBOL document library                    | -             |
| --meta-file          | -f                    | The absolute or relative path to the MS Excel file containing properties of constructs to be generated in SBOL library | -             |
| --filename-prefix    | -p                    | The filename for the generated SBOL document                                                                           | "library"     |
| --version            | -v                    | The version to assign to the designs generated in the SBOL documents                                                   | 1.0           |
| --output-dir         | -d                    | The absolute or relative path to the output directory where the generated SBOL document files will be created          | -             |
| --stop-missing-meta  | -m                    | Whether the generation process should halt if missing metadata is encountered for a design in the Excel file           | No            |

### Further reads

The [docs](docs) folder contains additional information.

For example a use-case document, which describes usage of the tool from the perspective
of biologist wanting to perform particular tasks. This document
contains also detailed installation instruction, including setting up Java environment.


## Development

### Spring Boot
The CLI Client is a Spring Boot application. 
Entry to the program is through the [ApplicationRunner](https://docs.spring.io/spring-boot/docs/2.4.4/api/org/springframework/boot/ApplicationRunner.html) interface.

### Building the CLI Application

This application depepends on another library [sbol2easy](https://github.com/BioRDM/sbol2easy).
Sbol2easy has a snapshot dependencies and is not available in maven central.

Before building the SynBio2Easy, sbol2easy must be cloned and build locally to be installed in the local maven repo.

After that step, SynBio2Easy can be built as ay typical maven project, 
```
maven clean install
```


### Updating the Documentation
Do your edits in the Word file, then convert to the Markdown version with Pandoc (https://medium.com/@ravinduk369/convert-a-ms-word-document-to-markdown-e0e99c41cfab) with:
```shell
pandoc -f docx -t markdown sbh_cli_user_guide.docx -o USER_GUIDE.md
```
### Creating the Certificate for jsign Executable Signing
```shell
keytool.exe -genkeypair -alias BioRDM -keyalg RSA -keystore keystore.jks
```
>> BioRDM
>> SynthSys
>> The University of Edinburgh
>> Edinburgh
>> Scotland
>> UK
