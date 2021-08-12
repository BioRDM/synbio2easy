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
* A template SBOL document is provided: `template.xml`
* the MS Excel spreadsheet `library_def.xlsx` containing details for each component design

The program generates 3 new ComponentDefinitions based on the template with the provided metadata and concrete sequences for
the template's abstract components.

We will start the tool, specify where the input files are located, and used the default values for most of the parameters.

1.      Type ```java -jar SynBio2Easy.jar generate```
2.	Type in the name of the template file, “template.xml” then <ENTER> when asked to specify the template file
3.	Type in the name of the Excel file that defines the new components, “library_def.xlsx” then <ENTER>
4.	Just press <ENTER> to accept the default ‘library’ filename prefix (the output file will be called library.1.xml)
5.	Just press <ENTER> to accept the default ‘1.0’ version string
6.	Just press <ENTER> to accept the default output directory (‘library’ in our case).
7.      Press ENTER on other questions to use the default silent errors handling

The program creates a file `library.1.xml` under `examples/library` folder.



