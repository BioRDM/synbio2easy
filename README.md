# SynBio Toolkit
A collection of biologist-friendly tools for interacting with SBOL and SynBioHub

## Purpose
The aim of this toolkit is to provide convenient solutions for streamlining common data management tasks performed by biological researchers who work with designs libraries rather than individual sbol files. For example, it helps with multifiles upload to an instance of [SynBioHub](https://github.com/SynBioHub/synbiohub) like the public [synbiohub.org](https://synbiohub.org/) repository or permits batch metadata data update. There are two main components in the toolkit: \'SBOL Transformer\' and \'SynBioHub CLI Client\'.

## SBOL Transformer Library Features
### SBOL Generation
The first component of the toolkit is a library containing utility methods for generating SBOL documents and [ComponentDefinition](https://dissys.github.io/sbol-owl/sbol-owl.html#ComponentDefinition) entities from pre-defined SBOL templates. It also provides methods for updating [SequenceAnnotation](https://dissys.github.io/sbol-owl/sbol-owl.html#SequenceAnnotation) elements within existing component definitions. Another principal function that the library provides is to \'flatten\' the parent-child hierarchy of component definitions and their child sequences into one top-level component definition, which enables full visibility of all elements in the design on the SynBioHub visualisation web UI.

### SBOL Annotation
Many biological researchers rely on MS Excel spreadsheets to store their experimental data. Another feature of the library is the ability to convert descriptions in xslx files into annotations inside SBOL documents. The current implementations of this transformer interface are specific to the data format and plasmid designs appropriate for a particular lab in the authors' research centre, but the option to extend these functions for other use cases will be available.

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
To update existing data in SynBioHub, the user must specify the file path to an Excel file that contains data organised into four columns as follows:

| display_id            | attachment_filename           | description  | notes  |
| :-------------------- |---------------------| --------|--------|
| The sbol2:displayID of the design to update          | File path to an attachment file           | String content to append to the existing description  | String content to append to the existing notes  |

The CLI client requires certain parameters to be specified at runtime, and will prompt the user for the following inputs:

| Parameter            | Description           | Default  |
| :-------------------- |:---------------------------------------------------| --------:|
| Filename             | The path in the file system to the Excel file containing design update data | -  |
| URL                  | The URL of the existing collection in SynBioHub to update                   |  - |
| Username             | Your username for the target SynBioHub server (usually your email address)    |   -    |
| Password             | The password for your user on the target SynBioHub server                     |   -    |

### Examples
#### Generate
##### Windows PowerShell
```shell
& "C:\Java\jdk-16.0.1\bin\java" -jar .\target\SynBioHub-CLI.jar generate `
--output-dir=examples/library --template-file=examples/template.xml --meta-file=examples/library_def.xlsx `
--filename-prefix=library --stop-missing-metadata=N --version=1.0
```

##### Mac/Linux Terminal
```shell
/usr/bin/java -jar ./target/SynBioHub-CLI.jar generate \
--output-dir=examples/library --template-file=examples/template.xml --meta-file=examples/library_def.xlsx \
--filename-prefix=library --stop-missing-metadata=N --version=1.0
```

#### Flatten
##### Windows PowerShell
```shell
& "C:\Java\jdk-16.0.1\bin\java" -jar .\target\SynBioHub-CLI.jar flatten `
--input-file=examples/library/library.1.xml --output-file=examples/library/library_flattened.1.xml `
--all-roots=Y --suffix=_flat
```

##### Mac/Linux Terminal
```shell
/usr/bin/java -jar ./target/SynBioHub-CLI.jar flatten \
--input-file=examples/library/library.1.xml --output-file=examples/library/library_flattened.1.xml \
--all-roots=Y --suffix=_flat
```

#### Annotate
##### Windows PowerShell
```shell
& "C:\Java\jdk-16.0.1\bin\java" -jar .\target\SynBioHub-CLI.jar annotate `
--input-file=examples/library/library_flattened.1.xml --meta-file=examples/flat_annotation.xlsx `
--output-file=examples/library/library_flattened_annotated.1.xml --stop-missing-metadata=N `
--stop-missing-id=N --overwrite=N
```

##### Mac/Linux Terminal
```shell
/usr/bin/java -jar ./target/SynBioHub-CLI.jar annotate \
--input-file=examples/library/library_flattened.1.xml --meta-file=examples/flat_annotation.xlsx \
--output-file=examples/library/library_flattened_annotated.1.xml --stop-missing-metadata=N \
--stop-missing-id=N --overwrite=N
```

#### Deposit
##### Windows PowerShell
```shell
& "C:\Java\jdk-16.0.1\bin\java" -jar .\target\SynBioHub-CLI.jar deposit `
--username=j.hay@epcc.ed.ac.uk --dir=examples/upload --file-extension=.xml --multi=N `
--create-new=Y --name="library 14-07-21" --url=https://synbiohub.org/ --version=1.0
```

##### Mac/Linux Terminal
```shell
/usr/bin/java -jar ./target/SynBioHub-CLI.jar deposit \
--username=j.hay@epcc.ed.ac.uk --dir=examples/upload --file-extension=.xml --multi=N \
--create-new=Y --name="library 14-07-21" --url=https://synbiohub.org/ --version=1.0
```

#### Template4Update
##### Windows PowerShell
```shell
& "C:\Java\jdk-16.0.1\bin\java" -jar .\target\SynBioHub-CLI.jar template4update `
--output-file=examples/library/template_4_update.xlsx `
--url=https://synbiohub.org/user/jhay/library_14_07_21/library_14_07_21_collection/1.0 `
--username=j.hay@epcc.ed.ac.uk
```

##### Mac/Linux Terminal
```shell
/usr/bin/java -jar ./target/SynBioHub-CLI.jar template4update \
--output-file=examples/library/template_4_update.xlsx \
--url=https://synbiohub.org/user/jhay/library_14_07_21/library_14_07_21_collection/1.0 \
--username=j.hay@epcc.ed.ac.uk
```

#### Update
##### Windows PowerShell
```shell
& "C:\Java\jdk-16.0.1\bin\java" -jar .\target\SynBioHub-CLI.jar update `
--meta-file=examples/library/template_4_update.xlsx `
--url=https://synbiohub.org/user/jhay/library_14_07_21/library_14_07_21_collection/1.0 `
--username=j.hay@epcc.ed.ac.uk
```

##### Mac/Linux Terminal
```shell
/usr/bin/java -jar ./target/SynBioHub-CLI.jar update \
--meta-file=examples/library/template_4_update.xlsx \
--url=https://synbiohub.org/user/jhay/library_14_07_21/library_14_07_21_collection/1.0 \
--username=j.hay@epcc.ed.ac.uk
```

#### Clean
##### Windows PowerShell
```shell
& "C:\Java\jdk-16.0.1\bin\java" -jar .\target\SynBioHub-CLI.jar clean `
--input-file=examples/library/library_downloaded.1.xml --output-file=examples/library/library_downloaded_cleaned.1.xml `
--namespace=http://biordm.sbs.ed.ac.uk --remove-collections=Y
```

##### Mac/Linux Terminal
```shell
/usr/bin/java -jar ./target/SynBioHub-CLI.jar clean \
--input-file=examples/library/library_downloaded.1.xml --output-file=examples/library/library_downloaded_cleaned.1.xml \
--namespace=http://biordm.sbs.ed.ac.uk --remove-collections=Y
```

## Development
### Spring Boot
The CLI Client is a Spring Boot application. Entry to the program is through the [ApplicationRunner](https://docs.spring.io/spring-boot/docs/2.4.4/api/org/springframework/boot/ApplicationRunner.html) interface.

### Building the CLI Application
In NetBeans, right-click on the project in the Projects explorer window, and select \'Run Maven\' -> \'Bundle\'

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
