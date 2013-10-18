package dk.statsbiblioteket.newspaper.autonomous.md5checker;

import dk.statsbiblioteket.medieplatform.autonomous.AbstractRunnableComponent;
import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.ParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.TreeIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dk.statsbiblioteket.util.Bytes;
import dk.statsbiblioteket.util.Checksums;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Check MD5 of all nodes
 */
public class MD5CheckerComponent
        extends AbstractRunnableComponent {

    private Logger log = LoggerFactory.getLogger(getClass());

    private Properties properties;

    public MD5CheckerComponent(Properties properties) {
        super(properties);
        this.properties = properties;
    }

    @Override
    public String getComponentName() {
        return "MD5_checker_component";

    }

    @Override
    public String getComponentVersion() {
        return "0.1";
    }

    @Override
    public String getEventID() {
        return "Checksums_checked";
    }

    @Override
    /**
     * For each attribute in batch: Calculate checksum and compare with metadata.
     *
     * @param batch The batch to check
     * @param resultCollector Collector to get the result.
     */
    public void doWorkOnBatch(Batch batch, ResultCollector resultCollector) throws Exception {
        TreeIterator iterator = createIterator(batch);
        while (iterator.hasNext()) {
            ParsingEvent next = iterator.next();
            switch (next.getType()) {
                case NodeBegin: {
                    break;
                }
                case NodeEnd: {
                    break;
                }
                case Attribute: {
                    AttributeParsingEvent attributeEvent = (AttributeParsingEvent) next;
                    String checksum = attributeEvent.getChecksum();
                    String calculatedChecksum = calculateChecksum(attributeEvent.getData());
                    if (!calculatedChecksum.equalsIgnoreCase(checksum)) {
                        resultCollector.addFailure(attributeEvent.getName(), "checksum", getFullName(),
                                                   "Expected checksum " + checksum + ", but was " + calculatedChecksum);
                    }
                    break;
                }
            }

        }
        resultCollector.setTimestamp(new Date());
    }

    private String calculateChecksum(InputStream stream) throws IOException {
        return Bytes.toHex(Checksums.md5(stream));
    }

}
