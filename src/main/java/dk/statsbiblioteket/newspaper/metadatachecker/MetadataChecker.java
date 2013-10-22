package dk.statsbiblioteket.newspaper.metadatachecker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.statsbiblioteket.medieplatform.autonomous.RunnableComponent;
import dk.statsbiblioteket.medieplatform.autonomous.AutonomousComponentUtils;

import java.util.Map;
import java.util.Properties;

/** This component checks metadata for validity. */
public class MetadataChecker {


    private static Logger log = LoggerFactory.getLogger(MetadataChecker.class);

    /**
     * The class must have a main method, so it can be started as a command line tool
     *
     * @param args the arguments.
     *
     * @throws Exception
     * @see AutonomousComponentUtils#parseArgs(String[])
     */
    public static void main(String[] args) throws Exception {
        log.info("Starting with args {}", args);

        //Parse the args to a properties construct
        Properties properties = AutonomousComponentUtils.parseArgs(args);

        //make a new runnable component from the properties
        RunnableComponent component = new MetadataCheckerComponent(properties);

        Map<String, Boolean> result = AutonomousComponentUtils.startAutonomousComponent(properties, component);

        AutonomousComponentUtils.printResults(result);
    }
}