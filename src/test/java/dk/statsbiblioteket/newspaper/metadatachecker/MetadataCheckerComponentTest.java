package dk.statsbiblioteket.newspaper.metadatachecker;

import dk.statsbiblioteket.newspaper.mfpakintegration.configuration.MfPakConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;

/** Test Metadata checker */
public class MetadataCheckerComponentTest {

    @Test
    /**
     * Test checking metadata on two batches.
     * One that fails on one of each type of metadata, and one that succeeds.
     */
    public void testDoWorkOnBatchBad()
            throws
            Exception {


        MetadataCheckerComponent metadataCheckerComponent = new MockupIteratorSuper(System.getProperties(),
                new PageModsTest.StubMfPakDAO(new MfPakConfiguration()));

        // Run on first batch 1
        TestResultCollector result = new TestResultCollector(metadataCheckerComponent.getComponentName(),
                metadataCheckerComponent.getComponentVersion());
        Batch batch = new Batch("400022028241");
        batch.setRoundTripNumber(1);
        metadataCheckerComponent.doWorkOnBatch(batch, result);

        // Assert errors
        Assert.assertFalse(result.isSuccess(), result.toReport() + "\n");
        Assert.assertTrue(result.failures.contains(
                "Failure validating XML data from " +
                        "'B400022028241-RT1/400022028241-14/AdresseContoirsEfterretninger-400022028241-14.film.xml': Line 27 " +
                        "Column 21: Content is not allowed in trailing section."));
        Assert.assertTrue(result.failures.contains(
                "Failure validating XML data from " +
                        "'B400022028241-RT1/400022028241-14/1795-06-13-01/AdresseContoirsEfterretninger-1795-06-13-01.edition" +
                        ".xml': Line 1 Column 53: cvc-elt.1: Cannot find the declaration of element 'mods:mods'."));
        Assert.assertTrue(result.failures.contains(
                "Failure validating XML data from " +
                        "'B400022028241-RT1/400022028241-14/1795-06-13-01/AdresseContoirsEfterretninger-1795-06-13-01-0006" +
                        ".alto.xml': Line 2 Column 180: cvc-elt.1: Cannot find the declaration of element 'altox'."));
        Assert.assertTrue(result.failures.contains(
                "Failure validating XML data from " +
                        "'B400022028241-RT1/400022028241-14/1795-06-13-01/AdresseContoirsEfterretninger-1795-06-13-01-0006" +
                        ".mods.xml': Line 9 Column 16: The end-tag for element type \"mods:part\" must end with a '>' " +
                        "delimiter."));
        Assert.assertTrue(result.failures.contains(
                "Failure validating XML data from " +
                        "'B400022028241-RT1/400022028241-14/1795-06-13-01/AdresseContoirsEfterretninger-1795-06-13-01-0006" +
                        ".mix.xml': Line 63 Column 52: cvc-datatype-valid.1.2.1: 'hest' is not a valid value for 'integer'."));
    }

    @Test
    /**
     * Test checking metadata on two batches.
     * One that fails on one of each type of metadata, and one that succeeds.
     */
    public void testDoWorkOnBatchGood()
            throws
            Exception {


        final PageModsTest.StubMfPakDAO mfPakDAO = new PageModsTest.StubMfPakDAO(new MfPakConfiguration());
        mfPakDAO.setNewspaperID("adressecontoirsefterretninger");
        MetadataCheckerComponent metadataCheckerComponent = new MockupIteratorSuper(System.getProperties(),
                mfPakDAO);

        // Run on first batch 1
        TestResultCollector result = new TestResultCollector(metadataCheckerComponent.getComponentName(),
                metadataCheckerComponent.getComponentVersion());
        Batch batch = new Batch("400022028241");
        batch.setRoundTripNumber(2);
        metadataCheckerComponent.doWorkOnBatch(batch, result);


        // Assert no errors
        Assert.assertTrue(result.isSuccess(), result.toReport() + "\n");
    }

}
