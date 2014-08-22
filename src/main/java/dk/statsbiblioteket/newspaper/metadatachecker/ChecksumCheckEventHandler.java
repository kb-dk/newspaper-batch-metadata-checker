package dk.statsbiblioteket.newspaper.metadatachecker;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.DefaultTreeEventHandler;
import dk.statsbiblioteket.util.Bytes;
import dk.statsbiblioteket.util.Checksums;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.InputStream;

public class ChecksumCheckEventHandler extends DefaultTreeEventHandler {
    private ResultCollector resultCollector;
    private Logger log = LoggerFactory.getLogger(getClass());
    private static final String CHECKSUM = "checksumChecker";


    /**
     * Constructor for this class.
     *
     * @param resultCollector The result collector to collect errors in
     */
    public ChecksumCheckEventHandler(ResultCollector resultCollector) {
        this.resultCollector = resultCollector;
    }

    @Override
    public void handleAttribute(AttributeParsingEvent attributeEvent) {
        String existingChecksum;
        try {
            existingChecksum = attributeEvent.getChecksum();
        } catch (IOException e) {
            log.warn("Error getting checksum in {}", attributeEvent.getName(), e);
            resultCollector.addFailure(attributeEvent.getName(), CHECKSUM, getClass().getSimpleName(),
                    "2F-O1: Error getting checksum: " + e.toString());
            return;
        }

        String calculatedChecksum;
        try {
            calculatedChecksum = calculateChecksum(attributeEvent.getData());
        } catch (IOException e) {
            log.warn("Error calculating checksum on data in {}", attributeEvent.getName(), e);
            resultCollector.addFailure(attributeEvent.getName(), CHECKSUM, getClass().getSimpleName(),
                    "2F-O1: Error calculating checksum on data: " + e.toString());
            return;
        }

        if (!calculatedChecksum.equalsIgnoreCase(existingChecksum)) {
            log.debug("Expected checksum {}, but was {} in {}", existingChecksum, calculatedChecksum,
                    attributeEvent.getName());
            resultCollector.addFailure(attributeEvent.getName(), CHECKSUM, getClass().getSimpleName(),
                    "2F-O1: Checksum mismatch. Value in md5-file: " + existingChecksum + "; calculated checksum: "
                            + calculatedChecksum);
        }
    }

    private String calculateChecksum(InputStream stream) throws IOException {
        return Bytes.toHex(Checksums.md5(stream));
    }
}
