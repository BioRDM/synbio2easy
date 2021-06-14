/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.synbio.dom;

/**
 *
 * @author tzielins
 */
public enum Command {

    DEPOSIT {
        @Override
        public String getGuidanceText() {
            StringBuffer textBuf = new StringBuffer();
            textBuf.append("You selected the DEPOSIT operation%n%n");
            textBuf.append("The deposit operation provides the ability to batch "
                    + "upload SBOL document files into a %ntarget SynBioHub server instance.%n");
            textBuf.append("The operation requires the following parameters:%n");
            textBuf.append("\t1. A directory containing valid SBOL document files you wish to upload%n");
            textBuf.append("\t2. A target SynBioHub server (https://synbiohub.org by default)%n");
            textBuf.append("\t3. An active user account on the target SynBioHub server%n");

            return textBuf.toString();
        }
    },
    UPDATE {
        @Override
        public String getGuidanceText() {
            StringBuffer textBuf = new StringBuffer();
            textBuf.append("You selected the UPDATE operation%n%n");
            textBuf.append("The update operation provides the ability to bulk update "
                    + "SBOL designs in an existing %ncollection on a target SynBioHub server instance.%n");
            textBuf.append("The operation requires the following parameters:%n");
            textBuf.append("\t1. An MS Excel file containing the metadata you wish to submit%n");
            textBuf.append("\t1.1. The MS Excel file must contain a 'display_id' column, "
                    + "along with one or %n\t     more optional columns comprising 'attachment_filename', "
                    + "'description' and 'notes'%n");
            textBuf.append("\t1.2. The filename specified in the 'attachment_filename' "
                    + "column will be uploaded %n\t     and attached to the corresponding "
                    + "design in the target SynBioHub server%n");
            textBuf.append("\t1.3. The text in the 'description' and 'notes' columns "
                    + "will be appended to the %n\t     appropriate fields of the corresponding "
                    + "design in the target SynBioHub server%n");
            textBuf.append("\t2. A target SynBioHub server (https://synbiohub.org by default)%n");
            textBuf.append("\t3. An active user account on the target SynBioHub server%n");

            return textBuf.toString();
        }
    },
    GENERATE {
        @Override
        public String getGuidanceText() {
            StringBuffer textBuf = new StringBuffer();
            textBuf.append("You selected the GENERATE operation%n%n");
            textBuf.append("The generate operation provides the ability to create "
                    + "the library of Cyanosource plasmid design SBOL document files. %n");
            textBuf.append("The operation requires the following parameters:%n");
            textBuf.append("\t1. A template SBOL document file from which to generate the plasmid designs with alternative flank sequences%n");
            textBuf.append("\t2. An MS Excel file containing the flank sequences for the designs you wish to generate%n");
            textBuf.append("\t3. An output directory where the newly created plasmid design SBOL document files will be placed%n");

            return textBuf.toString();
        }
    },
    CLEAN {
        @Override
        public String getGuidanceText() {
            StringBuffer textBuf = new StringBuffer();
            textBuf.append("You selected the CLEAN operation%n%n");
            textBuf.append("The clean operation provides the ability to sanitise "
                    + "an SBOL file originating in SynBioHub that you wish to upload again. %n");
            textBuf.append("The operation requires the following parameters:%n");
            textBuf.append("\t1. An original SBOL document file from SynBioHub to be cleaned%n");
            textBuf.append("\t2. An output file where the cleaned SBOL document will be saved%n");
            textBuf.append("\t3. A namespace for the cleaned SBOL document%n");
            textBuf.append("\t4. Whether to remove existing SynBioHub collection details from the cleaned SBOL document%n");

            return textBuf.toString();
        }
    };

    public abstract String getGuidanceText();
}
