package dk.statsbiblioteket.newspaper.metadatachecker;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 */
public class SchematronValidatorEventHandlerTest {

    /**
     * Test that we can validate a valid page mods file.
     */
    @Test
    public void testPageModsGood() {
        ResultCollector resultCollector = new ResultCollector("foo", "bar");
        SchematronValidatorEventHandler handler = new SchematronValidatorEventHandler(resultCollector);
        AttributeParsingEvent modsEvent = new AttributeParsingEvent("B400022028241-RT1/400022028241-14/1795-06-15-01/AdresseContoirsEfterretninger-1795-06-15-01-0010B.mods.xml") {
            @Override
            public InputStream getData() throws IOException {
                return Thread.currentThread().getContextClassLoader().getResourceAsStream("scratch/B400022028241-RT1/400022028241-14/1795-06-15-01/AdresseContoirsEfterretninger-1795-06-15-01-0010B.mods.xml");
            }

            @Override
            public String getChecksum() throws IOException {
                return null;
            }
        };
        handler.handleAttribute(modsEvent);
        System.out.println(resultCollector.toReport());
    }

}
