package dk.statsbiblioteket.newspaper.metadatachecker;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.MfPakDAO;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;

/** Test Metadata checker */
public class MetadataCheckerComponentTest {

    @Test
    /**
     * Test checking metadata on a "bad" batch.
     */
    public void testDoWorkOnBatchBad() throws Exception {


        MfPakDAO mfPakDAO = mock(MfPakDAO.class);
        MetadataCheckerComponent metadataCheckerComponent = new MockupIteratorSuper(
                System.getProperties(), mfPakDAO);

        TestResultCollector result = new TestResultCollector(
                metadataCheckerComponent.getComponentName(), metadataCheckerComponent.getComponentVersion());
        Batch batch = new Batch("400022028240");
        batch.setRoundTripNumber(1);
        metadataCheckerComponent.doWorkOnBatch(batch, result);

        // Assert errors
        Assert.assertFalse(result.isSuccess(), result.toReport() + "\n");
        Assert.assertTrue(
                result.failures
                      .contains(
                              "Failure validating XML data from "
                              +
                              "'B400022028240-RT1/400022028240-14/AdresseContoirsEfterretninger-400022028240-14.film.xml': Line 27 "
                              +
                              "Column 21: Content is not allowed in trailing section."));
        Assert.assertTrue(
                result.failures
                      .contains(
                              "Failure validating XML data from "
                              +
                              "'B400022028240-RT1/400022028240-14/1795-06-13-01/AdresseContoirsEfterretninger-1795-06-13-01.edition"
                              +
                              ".xml': Line 1 Column 53: cvc-elt.1: Cannot find the declaration "
                              +
                              "of element 'mods:mods'."));
        Assert.assertTrue(
                result.failures
                      .contains(
                              "Failure validating XML data from "
                              +
                              "'B400022028240-RT1/400022028240-14/1795-06-13-01/AdresseContoirsEfterretninger-1795-06-13-01-0006"
                              +
                              ".alto.xml': Line 2 Column 180: cvc-elt.1: Cannot find the "
                              +
                              "declaration of element 'altox'."));
        Assert.assertTrue(
                result.failures
                      .contains(
                              "Failure validating XML data from "
                              +
                              "'B400022028240-RT1/400022028240-14/1795-06-13-01/AdresseContoirsEfterretninger-1795-06-13-01-0006"
                              +
                              ".mods.xml': Line 9 Column 16: The end-tag for element type "
                              +
                              "\"mods:part\" must end with a '>' "
                              +
                              "delimiter."));
        Assert.assertTrue(
                result.failures
                      .contains(
                              "Failure validating XML data from "
                              +
                              "'B400022028240-RT1/400022028240-14/1795-06-13-01/AdresseContoirsEfterretninger-1795-06-13-01-0006"
                              +
                              ".mix.xml': Line 63 Column 52: cvc-datatype-valid.1.2.1: 'hest' is"
                              +
                              " not a valid value for 'integer'."));
    }

    @Test
    /**
     * Test checking on a "good" batch.
     */
    public void testDoWorkOnBatchGood() throws Exception {
        MfPakDAO mfPakDAO = MFPakMocker.getMFPak();
        MetadataCheckerComponent metadataCheckerComponent = new MockupIteratorSuper(System.getProperties(), mfPakDAO);
        TestResultCollector result = new TestResultCollector(
                metadataCheckerComponent.getComponentName(), metadataCheckerComponent.getComponentVersion());
        Batch batch = new Batch("400022028241");
        batch.setRoundTripNumber(1);
        metadataCheckerComponent.doWorkOnBatch(batch, result);
        Assert.assertTrue(result.isSuccess(), result.toReport() + "\n");
    }

}
