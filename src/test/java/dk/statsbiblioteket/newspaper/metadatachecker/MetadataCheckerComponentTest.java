package dk.statsbiblioteket.newspaper.metadatachecker;

import org.testng.Assert;
import org.testng.annotations.Test;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;

/**
 * Test Metadata checker
 */
public class MetadataCheckerComponentTest {

    private static final String EXPECTED_REPORT
            = "<result tool=\"Metadata_checker_component\" version=\"0.1\" xmlns=\"http://schemas.statsbiblioteket.dk/result/\"><outcome>Failure</outcome><date>[date]</date><failures><failure><filereference>B400022028241-RT1/400022028241-14/AdresseContoirsEfterretninger-400022028241-14.film.xml</filereference><type>metadata</type><component>Metadata_checker_component</component><description>Failure validating XML data from 'B400022028241-RT1/400022028241-14/AdresseContoirsEfterretninger-400022028241-14.film.xml': Line 27 Column 21: Content is not allowed in trailing section.</description></failure><failure><filereference>B400022028241-RT1/400022028241-14/1795-06-13-01/AdresseContoirsEfterretninger-1795-06-13-01.edition.xml</filereference><type>metadata</type><component>Metadata_checker_component</component><description>Failure validating XML data from 'B400022028241-RT1/400022028241-14/1795-06-13-01/AdresseContoirsEfterretninger-1795-06-13-01.edition.xml': Line 1 Column 53: cvc-elt.1: Cannot find the declaration of element 'mods:mods'.</description></failure><failure><filereference>B400022028241-RT1/400022028241-14/1795-06-13-01/AdresseContoirsEfterretninger-1795-06-13-01-0006.alto.xml</filereference><type>metadata</type><component>Metadata_checker_component</component><description>Failure validating XML data from 'B400022028241-RT1/400022028241-14/1795-06-13-01/AdresseContoirsEfterretninger-1795-06-13-01-0006.alto.xml': Line 2 Column 180: cvc-elt.1: Cannot find the declaration of element 'altox'.</description></failure><failure><filereference>B400022028241-RT1/400022028241-14/1795-06-13-01/AdresseContoirsEfterretninger-1795-06-13-01-0006.mix.xml</filereference><type>metadata</type><component>Metadata_checker_component</component><description>Failure validating XML data from 'B400022028241-RT1/400022028241-14/1795-06-13-01/AdresseContoirsEfterretninger-1795-06-13-01-0006.mix.xml': Line 63 Column 52: cvc-datatype-valid.1.2.1: 'hest' is not a valid value for 'integer'.</description></failure><failure><filereference>B400022028241-RT1/400022028241-14/1795-06-13-01/AdresseContoirsEfterretninger-1795-06-13-01-0006.mods.xml</filereference><type>metadata</type><component>Metadata_checker_component</component><description>Failure validating XML data from 'B400022028241-RT1/400022028241-14/1795-06-13-01/AdresseContoirsEfterretninger-1795-06-13-01-0006.mods.xml': Line 9 Column 16: The end-tag for element type \"mods:part\" must end with a '&gt;' delimiter.</description></failure></failures></result>";

    @Test
    /**
     * Test checking metadata on two batches.
     * One that fails on one of each type of metadata, and one that succeeds.
     */
    public void testDoWorkOnBatch() throws Exception {
        MetadataCheckerComponent metadataCheckerComponent = new MockupIteratorSuper(System.getProperties());

        // Run on first batch 1
        ResultCollector result =
                new ResultCollector(metadataCheckerComponent.getComponentName(), metadataCheckerComponent.getComponentVersion());
        Batch batch = new Batch("400022028241");
        batch.setRoundTripNumber(1);
        metadataCheckerComponent.doWorkOnBatch(batch, result);

        // Assert one error
        Assert.assertFalse(result.isSuccess(), result.toReport() + "\n");
        Assert.assertEquals(result.toReport().replaceAll("<date>[^<]*</date>", "<date>[date]</date>"), EXPECTED_REPORT);

        // Run on second batch
        result = new ResultCollector(metadataCheckerComponent.getComponentName(), metadataCheckerComponent.getComponentVersion());
        batch = new Batch("400022028241");
        batch.setRoundTripNumber(2);
        metadataCheckerComponent.doWorkOnBatch(batch, result);

        // Assert no errors
        Assert.assertTrue(result.isSuccess(), result.toReport() + "\n");
    }
}
