package dk.statsbiblioteket.newspaper.metadatachecker;

import dk.statsbiblioteket.medieplatform.autonomous.AutonomousComponentUtils;
import dk.statsbiblioteket.medieplatform.autonomous.CallResult;
import dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants;
import dk.statsbiblioteket.medieplatform.autonomous.RunnableComponent;
import dk.statsbiblioteket.newspaper.mfpakintegration.configuration.MfPakConfiguration;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.MfPakDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
    public static void main(String... args) throws Exception {
        System.exit(doMain(args));
    }

    private static int doMain(String[] args) throws IOException {
        log.info("Starting with args {}", new Object[]{args});

        //Parse the args to a properties construct
        Properties properties = AutonomousComponentUtils.parseArgs(args);

        MfPakConfiguration mfPakConfiguration = new MfPakConfiguration();
        mfPakConfiguration.setDatabaseUrl(properties.getProperty(ConfigConstants.MFPAK_URL));
        mfPakConfiguration.setDatabaseUser(properties.getProperty(ConfigConstants.MFPAK_USER));
        mfPakConfiguration.setDatabasePassword(properties.getProperty(ConfigConstants.MFPAK_PASSWORD));

        //make a new runnable component from the properties
        RunnableComponent component = new MetadataCheckerComponent(properties, new MfPakDAO(mfPakConfiguration));

        CallResult result = AutonomousComponentUtils.startAutonomousComponent(properties, component);
        log.info(result.toString());
        return result.containsFailures();
    }
}