/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.synbio.handler;

import org.apache.maven.artifact.versioning.ComparableVersion;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author jhay
 */
public class ComparableVersionTest {

    @Test
    public void testVersionNameSorting() {
        ComparableVersion version1_1 = new ComparableVersion("1.1");
        ComparableVersion version1_2 = new ComparableVersion("1.2");
        ComparableVersion version1_3 = new ComparableVersion("1.3");

        assertTrue(version1_1.compareTo(version1_2) < 0);
        assertTrue(version1_3.compareTo(version1_2) > 0);

        ComparableVersion version1_1_0 = new ComparableVersion("1.1.0");
        assertEquals(0, version1_1.compareTo(version1_1_0));
        
        ComparableVersion version1_1_alpha = new ComparableVersion("1.1-alpha");
        assertTrue(version1_1.compareTo(version1_1_alpha) > 0);

        ComparableVersion version1_1_beta = new ComparableVersion("1.1-beta");
        ComparableVersion version1_1_milestone = new ComparableVersion("1.1-milestone");
        ComparableVersion version1_1_rc = new ComparableVersion("1.1-rc");
        ComparableVersion version1_1_snapshot = new ComparableVersion("1.1-snapshot");

        assertTrue(version1_1_alpha.compareTo(version1_1_beta) < 0);
        assertTrue(version1_1_beta.compareTo(version1_1_milestone) < 0);
        assertTrue(version1_1_rc.compareTo(version1_1_snapshot) < 0);
        assertTrue(version1_1_snapshot.compareTo(version1_1) < 0);
    }
    
}
