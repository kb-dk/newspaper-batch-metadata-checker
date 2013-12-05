package dk.statsbiblioteket.newspaper.metadatachecker.mix;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.newspaper.metadatachecker.mockers.MixerMockup;
import dk.statsbiblioteket.newspaper.metadatachecker.checker.XmlAttributeChecker;
import dk.statsbiblioteket.util.xml.DOM;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class MixFilePathCheckerTest {

    private ResultCollector resultCollector = null;

    @BeforeTest
    public void setUp() {
        resultCollector = new ResultCollector("test", "test");
    }

    @Test
    public void testXpathValidationObjectIdentifierWorkshift() throws ParseException, SQLException, IOException {
        setUp();
        final String batchId = "400022028241";
        final Batch batch = new Batch();
        batch.setBatchID(batchId);
        batch.setRoundTripNumber(1);
        AttributeParsingEvent event2 = MixerMockup.getMixWorkshiftIso(
                "000001", "0001", batch, "aac20a9ace772bc5a92b7d7b00048b91", 15);

        Document doc = DOM.streamToDOM(event2.getData());

        XmlAttributeChecker handler = new MixFilePathChecker(resultCollector);

        handler.validate(event2, doc);

        String report = resultCollector.toReport();
        assertTrue(resultCollector.isSuccess(), report);
    }

    @Test
    public void testXpathValidationObjectIdentifierWorkshiftBad() throws ParseException, SQLException, IOException {
        setUp();
        final String batchId = "400022028241";
        final Batch batch = new Batch();
        batch.setBatchID(batchId);
        batch.setRoundTripNumber(1);
        MixerMockup.MixWorkshiftIsoAttributeParsingEvent event = MixerMockup.getMixWorkshiftIso(
                "000001", "0001", batch, "aac20a9ace772bc5a92b7d7b00048b91", 15);

        event.setPictureNumber("0002");
        Document doc = DOM.streamToDOM(event.getData());

        XmlAttributeChecker handler = new MixFilePathChecker(resultCollector);

        handler.validate(event, doc);

        String report = resultCollector.toReport();
        assertTrue(report.contains("2K-14:"),report);
        assertTrue(report.contains("2K-1:"),report);
        assertFalse(resultCollector.isSuccess(), report);
    }

    @Test
    public void testXpathValidationObjectIdentifier() throws ParseException, SQLException, IOException {
        setUp();
        final String batchId = "400022028241";
        final String film = "1";
        final String avisID = "adresseavisen1759";
        final String publishDate = "1795-06-13";
        final String pictureNumber = "0006";
        final Batch batch = new Batch();
        batch.setBatchID(batchId);
        batch.setRoundTripNumber(1);
        AttributeParsingEvent event = MixerMockup.getMixPageAttributeParsingEvent(
                film,
                avisID,
                publishDate,
                pictureNumber,
                batch,
                9304,
                11408,
                400,
                "7ed748249def3bcaadd825ae17dc817a",
                15);

        Document doc = DOM.streamToDOM(event.getData());

        XmlAttributeChecker handler = new MixFilePathChecker(resultCollector);

        handler.validate(event, doc);


        String report = resultCollector.toReport();
        assertTrue(resultCollector.isSuccess(), report);
    }

    @Test
    public void testXpathValidationObjectIdentifierBad() throws ParseException, SQLException, IOException {
        setUp();
        final String batchId = "400022028241";
        final String film = "1";
        final String avisID = "adresseavisen1759";
        final String publishDate = "1795-06-13";
        final String pictureNumber = "0006";
        final Batch batch = new Batch();
        batch.setBatchID(batchId);
        batch.setRoundTripNumber(1);

        MixerMockup.MixPageAttributeParsingEvent event = MixerMockup.getMixPageAttributeParsingEvent(
                film,
                avisID,
                publishDate,
                pictureNumber,
                batch,
                9304,
                11408,
                400,
                "7ed748249def3bcaadd825ae17dc817a",
                15);
        event.setPictureNumber(event.getPictureNumber() + 1);

        Document doc = DOM.streamToDOM(event.getData());

        XmlAttributeChecker handler = new MixFilePathChecker(resultCollector);

        handler.validate(event, doc);


        String report = resultCollector.toReport();
        assertTrue(report.contains("2K-14:"),report);
        assertTrue(report.contains("2K-1:"),report);

        assertFalse(resultCollector.isSuccess(), report);
    }


}
