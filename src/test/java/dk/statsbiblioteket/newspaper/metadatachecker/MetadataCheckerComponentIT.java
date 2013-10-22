package dk.statsbiblioteket.newspaper.metadatachecker;

import org.testng.annotations.Test;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.TreeIterator;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.EventHandlerFactory;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.EventRunner;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.filesystem.transforming.TransformingIteratorForFileSystems;

import java.io.File;
import java.io.FileInputStream;
import java.net.URISyntaxException;
import java.util.Properties;

import static org.testng.Assert.assertTrue;

/**
 */
public class MetadataCheckerComponentIT {

    private final static String TEST_BATCH_ID = "400022028241";

    /**
     * Tests that the BatchStructureChecker can parse a production like batch.
     */
    @Test(groups = "integrationTest")
    public void testMetadataCheck() throws Exception {
        String pathToProperties = System.getProperty("integration.test.newspaper.properties");
        Properties properties = new Properties();
        properties.load(new FileInputStream(pathToProperties));
        
        TreeIterator iterator = getIterator();
        EventRunner batchStructureChecker = new EventRunner(iterator);
        ResultCollector resultCollector = new ResultCollector("Batch Structure Checker", "v0.1");
        Batch batch = new Batch();
        batch.setBatchID(TEST_BATCH_ID);
        batch.setRoundTripNumber(1);

        EventHandlerFactory eventHandlerFactory = new MetadataChecksFactory(resultCollector);
        batchStructureChecker.runEvents(eventHandlerFactory.createEventHandlers());
        assertTrue(resultCollector.isSuccess());
        //Assert.fail();
    }

    /**
     * Creates and returns a iteration based on the test batch file structure found in the test/ressources folder.
     * @return A iterator the the test batch
     * @throws URISyntaxException
     */
    public TreeIterator getIterator() throws URISyntaxException {
        String pathToTestBatch = System.getProperty("integration.test.newspaper.testdata");
        File file = new File(pathToTestBatch + "/small-test-batch/");
        System.out.println(file);
        return new TransformingIteratorForFileSystems(file, "\\.", "\\.jp2$", ".md5");
    }
}
