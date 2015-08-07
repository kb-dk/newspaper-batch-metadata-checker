package dk.statsbiblioteket.newspaper.metadatachecker;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.newspaper.metadatachecker.mockers.AttributeParsingEventMocker;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

public class ChecksumCheckEventHandlerTest {
    @Test
    public void goodTest() {
        ResultCollector resultCollector = new ResultCollector("elf", "orc");

        AttributeParsingEvent attributeEvent = new AttributeParsingEventMocker();

        ChecksumCheckEventHandler handler = new ChecksumCheckEventHandler(resultCollector);
        handler.handleAttribute(attributeEvent);

        assertTrue(resultCollector.isSuccess());
    }

    @Test
    public void badTest() {
        ResultCollector resultCollector = new ResultCollector("imp", "bat");

        // Make event with bad checksum
        AttributeParsingEvent attributeEvent = new AttributeParsingEventMocker("8a6503");

        ChecksumCheckEventHandler handler = new ChecksumCheckEventHandler(resultCollector);
        handler.handleAttribute(attributeEvent);

        assertFalse(resultCollector.isSuccess());
        String report = resultCollector.toReport();
        assertTrue(report.contains("2F-O1:"));
    }
}
