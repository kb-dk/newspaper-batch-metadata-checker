package dk.statsbiblioteket.newspaper.autonomous.md5checker;

import com.netflix.curator.framework.CuratorFramework;
import com.netflix.curator.framework.CuratorFrameworkFactory;
import com.netflix.curator.retry.ExponentialBackoffRetry;
import dk.statsbibliokeket.newspaper.batcheventFramework.BatchEventClient;
import dk.statsbibliokeket.newspaper.batcheventFramework.BatchEventClientImpl;

import dk.statsbiblioteket.autonomous.AutonomousComponent;
import dk.statsbiblioteket.autonomous.RunnableComponent;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.EventID;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Autonomous component executable for checking MD5 of all nodes.
 */
public class MD5Checker {
    public static void main(String[] args) throws Exception {
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
        for (Map.Entry<String, Boolean> entry : result.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }

    private static List<EventID> toEvents(String events) {
        String[] eventSplits = events.split(",");
        List<EventID> result = new ArrayList<>();
        for (String eventSplit : eventSplits) {
            try {
                result.add(EventID.valueOf(eventSplit.trim()));
            } catch (IllegalArgumentException e){
                System.err.println("Unknown event '" + eventSplit.trim() + "' ignored");
            }
        }
        return result;
    }

    private static BatchEventClient createEventClient(Properties properties) {
        return new BatchEventClientImpl(properties.getProperty("summa"), properties.getProperty("domsUrl"),
                                        properties.getProperty("domsUser"), properties.getProperty("domsPass"),
                                        properties.getProperty("pidGenerator"));
    }

    private static Properties parseArgs(String[] args) throws IOException {
        Properties properties = new Properties();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("-c")) {
                String configFile = args[i + 1];
                properties.load(new FileInputStream(configFile));
            }
        }
        return properties;
    }
}
