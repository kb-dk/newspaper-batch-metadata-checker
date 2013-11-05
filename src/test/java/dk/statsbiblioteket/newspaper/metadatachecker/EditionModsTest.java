package dk.statsbiblioteket.newspaper.metadatachecker;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.testng.Assert.assertTrue;

/**
 * Tests that the checks that the metadata for the edition follows the specification
 * actually do perform the required checks and catch failures to adhere to the specification.
 * See Appendix 2D – metadata per publication and edition.
 */
public class EditionModsTest {
    /**
     * Test that we can validate a valid edition xml (mods) file.
     */
    @Test
    public void testEditionModsGood() {
        ResultCollector resultCollector = new ResultCollector("foo", "bar");
        SchematronValidatorEventHandler handler = new SchematronValidatorEventHandler(resultCollector, null);
        AttributeParsingEvent editionEvent = new AttributeParsingEvent(
                "B400022028241-RT1/400022028241-14/1795-06-13-01/AdresseContoirsEfterretninger-1795-06-13-01.edition.xml") {
            @Override
            public InputStream getData() throws IOException {
                return Thread.currentThread().getContextClassLoader().getResourceAsStream(
                        "scratch/B400022028241-RT1/400022028241-14/1795-06-13-01/AdresseContoirsEfterretninger-1795-06-13-01.edition.xml");
            }

            @Override
            public String getChecksum() throws IOException {
                return null;
            }
        };
        handler.handleAttribute(editionEvent);
        System.out.println(resultCollector.toReport());
        assertTrue(resultCollector.isSuccess());
    }

    /**
     * TODO Test that we catch invalid edition xml (mods) files
     */

}
