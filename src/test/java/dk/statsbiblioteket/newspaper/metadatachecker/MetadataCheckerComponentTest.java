package dk.statsbiblioteket.newspaper.metadatachecker;

import org.testng.Assert;
import org.testng.annotations.Test;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;

/**
 * Test Metadata checker
 */
public class MetadataCheckerComponentTest {
    @Test
    /**
     * Test checking metadata on two batches
     */
    public void testDoWorkOnBatch() throws Exception {
        MetadataCheckerComponent metadataCheckerComponent = new MockupIteratorSuper(System.getProperties());

        // Run on first batch 1
        ResultCollector result =
                new ResultCollector(metadataCheckerComponent.getComponentName(), metadataCheckerComponent.getComponentVersion());
        Batch batch = new Batch("400022028241");
        batch.setRoundTripNumber(1);
        metadataCheckerComponent.doWorkOnBatch(batch, result);

        // Assert no errors
        Assert.assertTrue(result.isSuccess(), result.toReport() + "\n");

        // Run on second batch
        result = new ResultCollector(metadataCheckerComponent.getComponentName(), metadataCheckerComponent.getComponentVersion());
        batch = new Batch("400022028241");
        batch.setRoundTripNumber(2);
        metadataCheckerComponent.doWorkOnBatch(batch, result);

        // Assert no errors
        Assert.assertTrue(result.isSuccess(), result.toReport() + "\n");
    }
}
