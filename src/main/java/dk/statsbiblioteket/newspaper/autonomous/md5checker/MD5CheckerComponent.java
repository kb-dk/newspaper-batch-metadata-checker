package dk.statsbiblioteket.newspaper.autonomous.md5checker;

import com.netflix.curator.framework.CuratorFramework;
import com.netflix.curator.framework.CuratorFrameworkFactory;
import com.netflix.curator.retry.ExponentialBackoffRetry;
import dk.statsbibliokeket.newspaper.batcheventFramework.BatchEventClient;
import dk.statsbibliokeket.newspaper.batcheventFramework.BatchEventClientImpl;
import dk.statsbiblioteket.autonomous.AutonomousComponent;
import dk.statsbiblioteket.autonomous.ResultCollector;
import dk.statsbiblioteket.autonomous.RunnableComponent;
import dk.statsbiblioteket.doms.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.doms.iterator.common.ParsingEvent;
import dk.statsbiblioteket.doms.iterator.common.TreeIterator;
import dk.statsbiblioteket.doms.iterator.filesystem.IteratorForFileSystems;
import dk.statsbiblioteket.doms.iterator.filesystem.transforming.TransformingIteratorForFileSystems;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.Batch;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.EventID;
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
        implements RunnableComponent {


    private Properties properties;

    public MD5CheckerComponent(Properties properties) {
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
    public EventID getEventID() {
        // TODO: Hardcoded event IDs in framework? Not so hot!
        return EventID.Data_Archived;
    }

    private TreeIterator createIterator(Properties properties,
                                        Batch batch) {
        boolean useFileSystem = Boolean.parseBoolean(properties.getProperty("useFileSystem", "true"));
        if (useFileSystem) {
            File scratchDir = new File(properties.getProperty("scratch"));
            File batchDir = new File(scratchDir, "B" + batch.getBatchID() + "-RT" + batch.getRoundTripNumber());
            return new TransformingIteratorForFileSystems(batchDir, Pattern.quote("."), "\\.jp2$", ".md5");

        }
        throw new UnsupportedOperationException("Presently only supported for filesystems, sorry");
    }

    @Override
    /**
     * For each attribute in batch: Calculate checksum and compare with metadata.
     *
     * @param batch The batch to check
     * @param resultCollector Collector to get the result.
     */
    public void doWorkOnBatch(Batch batch, ResultCollector resultCollector) throws Exception {
        TreeIterator iterator = createIterator(properties, batch);
        resultCollector.setSuccess(true);
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
                    AttributeParsingEvent event = (AttributeParsingEvent) next;
                    String checksum = event.getChecksum();
                    String calculatedChecksum = calculateChecksum(event.getText());
                    if (!calculatedChecksum.equalsIgnoreCase(checksum)) {
                        resultCollector.setSuccess(false);
                        resultCollector.addFailure(event.getLocalname(), "checksum", getComponentName(),
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
