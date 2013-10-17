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

    public static void main(String[] args)
            throws
            Exception {

        Properties properties = parseArgs(args);

        RunnableComponent component = new MD5CheckerComponent(properties);


        CuratorFramework lockClient = CuratorFrameworkFactory.newClient(properties.getProperty("lockserver"),
                                                                        new ExponentialBackoffRetry(1000, 3));
        lockClient.start();
        BatchEventClient eventClient = createEventClient(properties);
        AutonomousComponent autonomous = new AutonomousComponent(component, properties, lockClient, eventClient, 1,
                                                                  toEvents(properties.getProperty("pastevents")),
                                                                  toEvents(properties.getProperty("pasteventsExclude")),
                                                                  toEvents(properties.getProperty("futureEvents")));
        Map<String, Boolean> result = autonomous.call();
        //TODO what to do with the result?
    }

    private static List<EventID> toEvents(String events) {
        String[] eventSplits = events.split(",");
        List<EventID> result = new ArrayList<>();
        for (String eventSplit : eventSplits) {
            try {
            result.add(EventID.valueOf(eventSplit.trim()));
            } catch (IllegalArgumentException e){
                //TODO log this
            }
        }
        return result;
    }

    private static BatchEventClient createEventClient(Properties properties) {
        return new BatchEventClientImpl(properties.getProperty("summa"), properties.getProperty("domsUrl"),
                                        properties.getProperty("domsUser"), properties.getProperty("domsPass"),
                                        properties.getProperty("pidGenerator"));
    }

    private static Properties parseArgs(String[] args)
            throws
            IOException {
        Properties properties = new Properties();
        for (int i = 0;
             i < args.length;
             i++) {
            String arg = args[i];
            if (arg.equals("-c")) {
                String configFile = args[i + 1];
                properties.load(new FileInputStream(configFile));
            }
        }
        return properties;
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
    public void doWorkOnBatch(Batch batch,
                              ResultCollector resultCollector)
            throws
            Exception {
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
