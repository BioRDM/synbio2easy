# End2End test workflow 

The workflow bellow walks through all the available operations using the input files
from the examples folder.

You should be able to just copy paste the listed commands, changing only 
personal parameters as your username and password.

### Installation
* check if java version 11 or higher is available in the system, type in a terminal
```shell
java -version
```
You should see a version text. In case of error follow a guide how to install java on your machine.

* download SynBio2Easy.jar and examples.zip from the latest release [SynBio2Easy](https://github.com/BioRDM/synbio2easy/releases)
* extract files from examples.zip into a folder, we assume it is called 'examples'
* copy the SynBio2Easy.jar into the 'examples' folder
* navigate in your terminal into examples folder

## Generate

***Remember to navigate to the examples folder which contains the program and the test files***

In this scenario, the user intends to generate files describing their library of designs in SBOL document format. 
* A template SBOL document is provided: `lib_template.xml`
* the MS Excel spreadsheet `lib_def.xlsx` containing details for each component design

The program generates 3 new ComponentDefinitions based on the template with the provided metadata and concrete sequences for
the template's abstract components.

We will start the tool, specify where the input files are located, and used the default values for most of the parameters.

1.  Type
```
java -jar SynBio2Easy.jar generate
```
2.	Type in the name of the template file, `lib_template.xml` then <ENTER> when asked to specify the template file
3.	Type in the name of the Excel file that defines the new components, `lib_def.xlsx` then <ENTER>
4.	Just press <ENTER> to accept the default `library` filename prefix (the output file will be called library.1.xml)
5.	Just press <ENTER> to accept the default `1.0` version string
6.	Just press <ENTER> to accept the default output directory (`library` in our case).
7.  Press ENTER on other questions to use the default silent errors handling

The program creates a file `library.1.xml` under `examples/library` folder.

Check how the details in Excel file were used to create the 3 plasmids.
For example the authors columns became creators. The name and displayId were constructed using simple templating.

Alternatively you can use the inlined command parameters as bellow, avoiding the interactive prompt:

```
java -jar SynBio2Easy.jar generate `
--output-dir=library --template-file=lib_template.xml --meta-file=lib_def.xlsx `
--filename-prefix=library --version=1.0 --stop-missing-metadata=N 
```

In the rest of the workflow we will use the inlined command parameters,
but you can always start the program without any and answer the guided questions.

We also assume that you used the same parameters as us in all the previous steps.

## Flatten

In this scenario, the user intends to flatten the hierarchy of components and sequence annotations contained in an SBOL document. 
Such 'flattened', simple annotated sequences can then be visualized or exported to GenBank.

We will use `libary/library.1.xml` from the previous example as the input.

```
java -jar SynBio2Easy.jar flatten --input-file=library/library.1.xml --output-file=library/library_flattened.1.xml --all-roots=Y --suffix=_flat
```

A new file `library/library_flattened.1.xml` is created, which contains the 'flattened' 3 plasmids designs.
Upload them to SynBioHub and check how they are rendered.
The original template plasmid cannot be flattened as it does not contain the concrete sequences for its flanks.

## Annotate

In this scenario, the user intends to enrich their ComponentDefinitions with extra descriptive metadata 
added into an existing SBOL file. 

We will use the flattened document as input: `library/library_flattened.1.xml`
And the metadata excel file: `flat_annotation.xlsx`.

The matching between excel and sbol record is based on displayId value.
Check how "templating" and excel functions were used to generate values.

```
java -jar SynBio2Easy.jar annotate `
--input-file=library/library_flattened.1.xml --meta-file=flat_annotation.xlsx `
--output-file=library/library_flattened.1.xml --stop-missing-metadata=N `
--stop-missing-id=N --overwrite=N
```

Check the overwritten library_flattened.1.xml it should contained appended information
in its sbh:description and sbh:notes fields.

## Deposit

In this scenario, the user intends to upload files describing their library of designs.
All files in our `library` directory will be deposited into a new collection in SynBioHub.

```
java -jar SynBio2Easy.jar deposit `
--dir=library --file-extension=.xml --multi=N `
--create-new=Y --name="synbio-test" --version=1.0 --url=https://synbiohub.org/ 
```

You will be prompted for your email (not username) and password for SynBioHub.

Make a note of the newly created collection:
https://synbiohub.org/user/YOURLOGIN/synbio_test/synbio_test_collection/1.0

Navigate to it, it contains both the original and flattened designs from both files.


## SynBio2Table

In this scenario, the user intends to get information about the designs from a SynBioHub collection in a table form.

The main usage is generation of a 'metadata' template for batch updates in SynBioHub.
as the generated file contains the definitons displayIds, versions, descriptions and etc.

Please change the url to the uploaded collection 
(you should only need to replace YOURLOGIN with the correct one)

```
java -jar SynBio2Easy.jar synbio2table `
--output-file=library/collection_table.xlsx `
--url=https://synbiohub.org/user/YOURLOGIN/synbio_test/synbio_test_collection/1.0 
```

You will be prompted for your email (not username) and password for SynBioHub.

The generated file contains the headers in the form needed by the UPDATE operation.

## Update

In this scenario, the intention is to attach files containing supporting data do plasmids definitions in SynBioHub
along with information what those files are, additional enriched metadata.

We will use the Excel file: `update.xlsx`
that contains details of the files to be attached and new notes information.
Such files can be generated with SYNBIO2TABLE command and then edited as needed.

The examples folder contains also the sequence files that will be uploaded.

The target collection for update will be the one recently created
https://synbiohub.org/user/YOURLOGIN/synbio_test/synbio_test_collection/1.0

please replace YOURLOGIN with suitable value bellow

```
java -jar SynBio2Easy.jar update `
--meta-file=update.xlsx --overwrite=N `
--url=https://synbiohub.org/user/YOURLOGIN/synbio_test/synbio_test_collection/1.0 
```

Check in SynBioHub how the selected plasmids (for example sll0558-codA) have a sequences attached and described in notes.

## Clean

In this scenario, the user intends to re-use an SBOL document that they have downloaded from SynBioHub.
Such documents even if coudl be quickly edidted in a text editor to provide some
missing details or change annotations are no longer valid for re-submission because of namespace adjustments. 

The cleaning process allows them to be deposited in SynBioHub again. 

An input SBOL document file to be sanitised is provided, so please navigate to the created collection:
https://synbiohub.org/user/YOURLOGIN/synbio_test/synbio_test_collection/1.0

Click download in the toolbar and select sbol document as the format, save it as `synbio_test_collection.xml`
under `examples`

```
java -jar SynBio2Easy.jar clean `
--input-file=synbio_test_collection.xml --output-file=synbio_test_collection_cleaned.xml --namespace=http://biordm.sbs.ed.ac.uk --remove-collections=Y
```

The output document is saved under `synbio_test_collection_cleaned.xml`
You can upload it to a new collection in SynBioHub using the web interface.
