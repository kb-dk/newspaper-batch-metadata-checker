package dk.statsbiblioteket.newspaper.metadatachecker;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 */
public class ModsXPathEventHandlerTest {

    /**
     * Test that we can validate a valid page mods file.
     */
    @Test
    public void testPageModsBad() {
        ResultCollector resultCollector = new ResultCollector("foo", "bar");
        ModsXPathEventHandler handler = new ModsXPathEventHandler(resultCollector);
        AttributeParsingEvent modsEvent = new AttributeParsingEvent("AdresseContoirsEfterretninger-1795-06-15-01-0010B.mods.xml") {
            @Override
            public InputStream getData() throws IOException {
                return Thread.currentThread().getContextClassLoader().getResourceAsStream("badData/bad1.mods.xml");
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
