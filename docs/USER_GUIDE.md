# Table of Contents {#table-of-contents .TOC-Heading}

[Initial screen -- Choose Operation 2](#initial-screen-choose-operation)

[Deposit 2](#deposit)

[General Notes on Deposit Operation
2](#general-notes-on-deposit-operation)

[Update 2](#update)

[Use Case 1 -- Deposit SBOL Files to a Single New Collection
3](#use-case-1-deposit-sbol-files-to-a-single-new-collection)

[Inputs 3](#inputs)

[Outputs 3](#outputs)

[Typical Errors 4](#typical-errors)

[Collection Already Exists 4](#collection-already-exists)

[Use Case 2 -- Deposit Files to Multiple New Collections
6](#use-case-2-deposit-files-to-multiple-new-collections)

[Inputs 6](#inputs-1)

[Outputs 6](#outputs-1)

[Typical Errors 7](#typical-errors-1)

[Collection(s) Already Exists 7](#collections-already-exists)

[Use Case 3 -- Deposit New SBOL Files to an Existing Collection
8](#use-case-3-deposit-new-sbol-files-to-an-existing-collection)

[Inputs 8](#inputs-2)

[Outputs 8](#outputs-2)

[Use Case 4 -- Deposit SBOL Files to Update an Existing Collection
10](#use-case-4-deposit-sbol-files-to-update-an-existing-collection)

[Inputs 10](#inputs-3)

[Outputs 10](#outputs-3)

[Use Case 5 -- Updating Existing Designs in an Existing Collection
12](#use-case-5-updating-existing-designs-in-an-existing-collection)

[Inputs 12](#inputs-4)

[Outputs 13](#outputs-4)

[Advanced Use 14](#advanced-use)

# Initial screen -- Choose Operation

![](media/image1.png){width="6.268055555555556in"
height="3.8555555555555556in"}

## Deposit

This operation is for uploading SBOL files into new or existing
collections, and updating or overwriting the objects in existing
collections. This is the operation used in Use Cases 1-4.

### General Notes on Deposit Operation

-   If no files are detected in the specified directory either because
    there are none present, or they are filtered out because of the file
    extension filter, then a new collection will still be created for
    Use Cases 1 and 2

## Update

The update operation is for adding more metadata and file attachments to
designs that already exist in a collection on a target SynBioHub server.
This operation is described in Use Case 5.

# Use Case 1 -- Deposit SBOL Files to a Single New Collection

In this common use case, the user intends to upload all files in a
specified directory into a new collection on the target SynBioHub
server. The user can specify a file extension filter to select only
particular types of files in the directory that they wish to deposit.

## Inputs

1.  Choose 'deposit'

2.  Enter the path to the directory containing the files you wish to
    upload, e.g. 'C:\\my_data\\cyano_source'. Alternatively, if your
    data are in the same directory as where the application was
    launched, press \<ENTER\>

3.  Enter the appropriate file name extension for the files you wish to
    upload, e.g. '.xml', or press \<ENTER\> to select files of all types

4.  Enter 'n' when asked to create multiple collections

5.  Enter 'y' when asked to create a new collection

6.  Enter a new name for the collection

7.  Enter the URL for the target SynBioHub server, or press \<ENTER\> to
    specify the default 'https://synbiohub.org'

8.  Enter a version number or press \<ENTER\> to accept the default
    '1.0' version

9.  Enter your SynBioHub username, usually your email address

10. Enter your SynBioHub password

## Outputs

-   The program will report the URL for the newly created

-   The program will report which files were successfully deposited and
    those which failed

-   Any files that failed to be deposited most likely failed because
    they contain invalid SBOL

![](media/image2.png){width="6.268055555555556in"
height="4.655555555555556in"}

## Typical Errors

### Collection Already Exists

If you choose to create a new collection, but you specify a name and
version for a collection that already exists in the server, you will
receive an "Invalid collection specified" error as shown below.

N.B. The arguments for 'new collection name' and 'version' are case
sensitive: therefore, a collection named "JHay Cyano [S]{.ul}ource" is
different than one named "JHay Cyano [s]{.ul}ource" and version
"[v]{.ul}1.0" is different than "[V]{.ul}1.0", for example.

![](media/image3.png){width="6.268055555555556in"
height="4.655555555555556in"}

# 

# 

# Use Case 2 -- Deposit Files to Multiple New Collections

In this use case, the user intends to upload the contents of each
sub-directory in a specified directory into a new collection on the
target SynBioHub server. The user can specify a file extension filter to
select only particular types of files in the sub-directories that they
wish to deposit. This feature is non-recursive, so only sub-directories
that are immediate children of the specified parent directory will be
traversed for their file contents to be uploaded.

## Inputs

1.  Choose 'deposit'

2.  Enter the path to the directory containing the files you wish to
    upload, e.g. 'C:\\my_data\\cyano_source'. Alternatively, if your
    data are in the same directory as where the application was
    launched, press \<ENTER\>

3.  Enter the appropriate file name extension for the files you wish to
    upload, e.g. '.xml', or press \<ENTER\> to select files of all types

4.  Enter 'Y' when asked to create multiple collections

5.  Enter a prefix that will be prepended to each sub-directory name to
    create the new collection names, or press \<ENTER\> for no prefix
    (and only the directory names)

6.  Enter the URL for the target SynBioHub server, or press \<ENTER\> to
    specify the default 'https://synbiohub.org'

7.  Enter a version number or press \<ENTER\> to accept the default
    '1.0' version

8.  Enter your SynBioHub username, usually your email address

9.  Enter your SynBioHub password

## Outputs

-   The program will report the new URL for each collection that was
    created for a sub-directory

-   The program will report which files were successfully deposited and
    those which failed

-   Any files that failed to be deposited most likely failed because
    they contain invalid SBOL

![](media/image4.png){width="6.268055555555556in"
height="4.655555555555556in"}

## Typical Errors

### Collection(s) Already Exists

If you specify a prefix and version for collections that already exist
in the server, you will receive an "Invalid collection specified" error
as shown below.

N.B. The arguments for 'new collection prefix' and 'version' are case
sensitive: therefore, a prefix named "JHay Multi Cyano [S]{.ul}ource" is
different than one named "JHay Multi Cyano [s]{.ul}ource" and version
"[v]{.ul}1.0" is different than "[V]{.ul}1.0", for example.

# Use Case 3 -- Deposit New SBOL Files to an Existing Collection

In this use case, the user intends to deposit new SBOL files in the
specified directory into an existing collection on the target SynBioHub
server. Any SBOL files that contain objects which already exist on the
server will not be overwritten, but the objects contained in new files
will be uploaded if they do not conflict with any existing objects.

## Inputs

1.  Choose 'deposit'

2.  Enter the path to the directory containing the files you wish to
    upload, e.g. 'C:\\my_data\\cyano_source'. Alternatively, if your
    data are in the same directory as where the application was
    launched, press \<ENTER\>

3.  Enter the appropriate file name extension for the files you wish to
    upload, e.g. '.xml', or press \<ENTER\> to select files of all types

4.  Enter 'n' when asked to create multiple collections

5.  Enter 'N' when asked to create a new collection

6.  Enter the URL for the existing collection, e.g.
    'https://synbiohub.org/user/jhay/JHay_Cyano_Source/JHay_Cyano_Source_collection/1.0'

7.  Enter 'n' when asked to overwrite designs if they exist

8.  Enter your SynBioHub username, usually your email address

9.  Enter your SynBioHub password

## Outputs

-   The program will report which files were successfully deposited and
    which files failed to be deposited

-   The files that failed to be deposited are the ones that contain
    objects that already exist on the server, or contain invalid SBOL

![](media/image5.png){width="6.268055555555556in"
height="4.655555555555556in"}

# Use Case 4 -- Deposit SBOL Files to Update an Existing Collection

In this use case, the user intends to deposit SBOL files in the
specified directory into an existing collection on the target SynBioHub
server. All objects contained in the SBOL files which already exist on
the server will be overwritten, so this sequence of commands should be
used with caution!

## Inputs

1.  Choose 'deposit'

2.  Enter the path to the directory containing the files you wish to
    upload, e.g. 'C:\\my_data\\cyano_source'. Alternatively, if your
    data are in the same directory as where the application was
    launched, press \<ENTER\>

3.  Enter the appropriate file name extension for the files you wish to
    upload, e.g. '.xml', or press \<ENTER\> to select files of all types

4.  Enter 'n' when asked to create multiple collections

5.  Enter 'N' when asked to create a new collection

6.  Enter the URL for the existing collection, e.g.
    'https://synbiohub.org/user/jhay/JHay_Cyano_Source/JHay_Cyano_Source_collection/1.0'

7.  Enter 'Y' when asked to overwrite designs if they exist

8.  Enter your SynBioHub username, usually your email address

9.  Enter your SynBioHub password

## Outputs

-   The program will report which files were successfully deposited and
    which files failed to be deposited

-   Any files that failed to be deposited most likely failed because
    they contain invalid SBOL

![](media/image6.png){width="6.268055555555556in"
height="4.655555555555556in"}

# Use Case 5 -- Updating Existing Designs in an Existing Collection

In this use case, the user intends to update existing designs in an
existing collection on the target SynBioHub server. There are three
elements of the designs in SynBioHub that can be updated using this
feature: files can be attached, and the 'description' and 'notes' text
fields can be appended to. It is anticipated that users may wish to
attach sequence data in other formats such as GenBank files, and append
extra text metadata to enhance the FAIRness of their data in SynBioHub.

The user provides a file in MS Excel format comprised of one mandatory
column and at least one of three optional columns. The mandatory column
is 'display_id', while the other three columns are
'attachment_filename', 'description' and 'notes'. The columns can be in
any order but they must be adjacent to each other (i.e. in a contiguous
block) and begin at column A of the worksheet. The values in the
'display_id' column must match the 'displayId' attribute of existing
designs in a collection on the target server. The 'attachment_filename'
column's values can be either absolute file paths to files on the user's
local machine, or simply file names of files that are relative to the
current working directory. An example is shown below.

![](media/image7.png){width="6.268055555555556in"
height="2.4506944444444443in"}

## Inputs

1.  Choose 'update'

2.  Enter the absolute path to the MS Excel file containing the entity
    display IDs, file attachments and metadata you wish to upload, e.g.
    'C:\\my_data\\cyano_source\\update_designs.xlsx'. Alternatively, if
    your Excel file is in the same directory as where the application
    was launched, enter the file name.

3.  Enter the URL for the existing collection containing the designs you
    wish to update, e.g.
    'https://synbiohub.org/user/jhay/JHay_Cyano_Source/JHay_Cyano_Source_collection/1.0'

4.  Enter your SynBioHub username, usually your email address

5.  Enter your SynBioHub password

## Outputs

-   The program will report which designs it found in the specified
    collection with display IDs matching those in the 'display_id'
    column of the spreadsheet

-   The program will also report which display IDs it could not find in
    the specified collection

![](media/image8.png){width="6.268055555555556in"
height="4.655555555555556in"}

# Advanced Use

Users who know precisely which parameters they require can invoke the
SynBioHub Client with those arguments in a prepared statement. For
instance, to skip the initial operation selection step, the user can
simply start the client with the 'deposit' or 'update' argument. If any
required parameters are missing from those supplied by the user for the
specified task, the user will be prompted for them. The CLI commands
that are equivalent to the user input prompt for each use case
demonstrated above are shown in the table below.

+----------+----------------------------------------------------------+
| Use Case | CLI Command                                              |
+==========+==========================================================+
| 1        | java -jar SynBioHub-CLI.jar deposit                      |
|          |                                                          |
|          | \--dir=D:\\temp\\sbol\\cyano_subset                      |
|          | \--file-extension=.xml                                   |
|          |                                                          |
|          | \--multi=false \--create-new=true \--name=\"JHay Cyano   |
|          | Source\"                                                 |
|          |                                                          |
|          | \--url=https://synbiohub.org \--version=1.0              |
|          |                                                          |
|          | \--username=j.hay\@epcc.ed.ac.uk                         |
+----------+----------------------------------------------------------+
| 2        | java -jar SynBioHub-CLI.jar deposit                      |
|          | \--dir=D:\\temp\\sbol \--file-extension=.xml             |
|          | \--multi=true \--create-new=true \--name=\"JHay Multi    |
|          | Cyano Source\" \--url=https://synbiohub.org              |
|          | \--version=1.0 \--username=j.hay\@epcc.ed.ac.uk          |
+----------+----------------------------------------------------------+
| 3        | java -jar SynBioHub-CLI.jar deposit                      |
|          | \--dir=D:\\temp\\sbol \--file-extension=.xml             |
|          | \--multi=false \--create-new=false                       |
|          |                                                          |
|          | \--url=[https://synbiohub.org/user/jhay/\                |
|          | JHay_Cyano_Source/JHay_                                  |
|          | Cyano_Source_collection/1.0](https://synbiohub.org/user/ |
|          | jhay/JHay_Cyano_Source/JHay_Cyano_Source_collection/1.0) |
|          |                                                          |
|          | \--username=j.hay\@epcc.ed.ac.uk \--overwrite=false      |
+----------+----------------------------------------------------------+
| 4        | java -jar SynBioHub-CLI.jar deposit                      |
|          | \--dir=D:\\temp\\sbol \--file-extension=.xml             |
|          | \--multi=false \--create-new=false \--url=               |
|          |                                                          |
|          | [https://synbiohub.org/user/jhay/JHay_Cyano_Source/\     |
|          | JHay_                                                    |
|          | Cyano_Source_collection/1.0](https://synbiohub.org/user/ |
|          | jhay/JHay_Cyano_Source/JHay_Cyano_Source_collection/1.0) |
|          |                                                          |
|          | \--username=j.hay\@epcc.ed.ac.uk \--overwrite=true       |
+----------+----------------------------------------------------------+
| 5        | java -jar SynBioHub-CLI.jar update                       |
|          | \--excel-file=update_designs.xlsx                        |
|          | \--url=[https://synbiohub.org/user/\                     |
|          | jhay/JHay_Cyano_Source/JHay_                             |
|          | Cyano_Source_collection/1.0](https://synbiohub.org/user/ |
|          | jhay/JHay_Cyano_Source/JHay_Cyano_Source_collection/1.0) |
|          |                                                          |
|          | \--username=j.hay\@epcc.ed.ac.uk                         |
+----------+----------------------------------------------------------+

Table : All-in-one CLI commands for running the example use cases

+----------------------+----------------------+----------------------+
| Command Option       | Description          | Examples             |
+======================+======================+======================+
| \--dir, \--d         | Directory to upload  | \--dir=C:\\m         |
|                      |                      | y_data\\cyano_source |
+----------------------+----------------------+----------------------+
| \--file-extension,   | File extension with  | \--f=.xml            |
| \--f                 | which to filter      |                      |
|                      | files in upload      | \                    |
|                      | directory            | --file-extension=.\* |
+----------------------+----------------------+----------------------+
| \--multi, \--m       | Flag to indicate     | \--multi=true        |
|                      | whether to upload    |                      |
|                      | child directories as | \--m=false           |
|                      | multiple collections |                      |
+----------------------+----------------------+----------------------+
| \--create-new, \--c  | Whether to create a  | \--create-new=true   |
|                      | new collection       |                      |
|                      |                      | \--c=false           |
+----------------------+----------------------+----------------------+
| \--url, \--l         | URL of the target    | \--url=h             |
|                      | SynBioHub server or  | ttps://synbiohub.org |
|                      | the existing         |                      |
|                      | collection           | \--l=[https://       |
|                      |                      | synbiohub.org/user/\ |
|                      |                      | jhay                 |
|                      |                      | /JHay_Cyano_Source/\ |
|                      |                      | JHay_Cyano_Source    |
|                      |                      | _collection/1.0](htt |
|                      |                      | ps://synbiohub.org/u |
|                      |                      | ser/jhay/JHay_Cyano_ |
|                      |                      | Source/JHay_Cyano_So |
|                      |                      | urce_collection/1.0) |
+----------------------+----------------------+----------------------+
| \--username, \--u    | User name of the     | <--username=         |
|                      | SynBioHub user       | j.hay@epcc.ed.ac.uk> |
|                      | (typically email     |                      |
|                      | address)             | -u=jo.bloggs\@ac.uk  |
+----------------------+----------------------+----------------------+
| \--version, \--v     | Version of the new   | \--version=1.0       |
|                      | collection to create |                      |
|                      |                      | \--v=v1.0.0-alpha    |
+----------------------+----------------------+----------------------+
| \--overwrite, \--o   | Whether to overwrite | \--overwrite=true    |
|                      | existing objects in  |                      |
|                      | the existing         | \--o=false           |
|                      | collection           |                      |
+----------------------+----------------------+----------------------+
| \--excel-file, \--e  | The file path to the | \--excel-file        |
|                      | Excel file           | =update_designs.xlsx |
|                      | containing the       |                      |
|                      | metadata for         | \-                   |
|                      | existing objects to  | -e=new_metadata.xlsx |
|                      | update               |                      |
+----------------------+----------------------+----------------------+

Table : Form and function of different command options
