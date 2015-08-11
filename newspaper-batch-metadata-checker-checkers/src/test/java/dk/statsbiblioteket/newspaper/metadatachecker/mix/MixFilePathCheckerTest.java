package dk.statsbiblioteket.newspaper.metadatachecker.mix;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.newspaper.metadatachecker.mockers.MixerMockup;
import dk.statsbiblioteket.newspaper.metadatachecker.checker.XmlAttributeChecker;
import dk.statsbiblioteket.util.xml.DOM;
import org.testng.annotations.BeforeMethod;
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

    @BeforeMethod
    public void setupResultCollector() {
        resultCollector = new ResultCollector("test", "test");
    }

    @Test
    public void testXpathValidationObjectIdentifierWorkshift() throws ParseException, SQLException, IOException {
        final String batchId = "400022028241";
        final Batch batch = new Batch();
        batch.setBatchID(batchId);
        batch.setRoundTripNumber(1);
        AttributeParsingEvent event2 = MixerMockup.getMixWorkshiftIso(
                "000001", "0001", batch, "aac20a9ace772bc5a92b7d7b00048b91", 15, "iso-film-target");

        Document doc = DOM.streamToDOM(event2.getData());

        XmlAttributeChecker handler = new MixFilePathChecker(resultCollector);

        handler.validate(event2, doc);

        String report = resultCollector.toReport();
        assertTrue(resultCollector.isSuccess(), report);
    }

    @Test
    public void testXpathValidationObjectIdentifierWorkshiftBad() throws ParseException, SQLException, IOException {
        final String batchId = "400022028241";
        final Batch batch = new Batch();
        batch.setBatchID(batchId);
        batch.setRoundTripNumber(1);
        MixerMockup.MixWorkshiftIsoAttributeParsingEvent event = MixerMockup.getMixWorkshiftIso(
                "000001", "0001", batch, "aac20a9ace772bc5a92b7d7b00048b91", 15, "iso-film-target");

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
        final String batchId = "400022028241";
        final String film = "1";
        final String avisID = "adresseavisen1759";
        final String publishDate = "1795-06";
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
                15, "microfilm");

        Document doc = DOM.streamToDOM(event.getData());

        XmlAttributeChecker handler = new MixFilePathChecker(resultCollector);

        handler.validate(event, doc);


        String report = resultCollector.toReport();
        assertTrue(resultCollector.isSuccess(), report);
    }

    @Test
    public void testXpathValidationObjectIdentifierBad() throws ParseException, SQLException, IOException {
        final String batchId = "400022028241";
        final String film = "1";
        final String avisID = "adresseavisen1759";
        final String publishDate = "1795-06";
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
                15, "microfilm");
        event.setPictureNumber(event.getPictureNumber() + 1);

        Document doc = DOM.streamToDOM(event.getData());

        XmlAttributeChecker handler = new MixFilePathChecker(resultCollector);

        handler.validate(event, doc);


        String report = resultCollector.toReport();
        assertTrue(report.contains("2K-14:"),report);
        assertTrue(report.contains("2K-1:"),report);

        assertFalse(resultCollector.isSuccess(), report);
    }

    @Test
    public void testXpathSourceInformationBad() throws ParseException, SQLException, IOException {
        final String batchId = "400022028241";
        final String film = "1";
        final String avisID = "adresseavisen1759";
        final String publishDate = "1795-06";
        final String pictureNumber = "0006";
        final Batch batch = new Batch();
        batch.setBatchID(batchId);
        batch.setRoundTripNumber(1);
        String sourceType = "iso-film-target"; //Note: This source type is bad, it should be "microfilm"
        AttributeParsingEvent event = MixerMockup
                .getMixPageAttributeParsingEvent(film, avisID, publishDate, pictureNumber, batch, 9304, 11408, 400,
                                                 "7ed748249def3bcaadd825ae17dc817a", 15, sourceType);

        Document doc = DOM.streamToDOM(event.getData());

        XmlAttributeChecker handler = new MixFilePathChecker(resultCollector);

        handler.validate(event, doc);

        String report = resultCollector.toReport();
        assertTrue(report.contains("2K-12:"),report);
        assertFalse(resultCollector.isSuccess(), report);
    }

    @Test
    public void testXpathWorkshiftSourceInformationBad() throws ParseException, SQLException, IOException {
        final String batchId = "400022028241";
        final Batch batch = new Batch();
        batch.setBatchID(batchId);
        batch.setRoundTripNumber(1);
        String badSourceType = "microfilm"; // Note: This sourceType is bad, it should be iso-film-target
        AttributeParsingEvent event = MixerMockup
                .getMixWorkshiftIso("000001", "0001", batch, "7ed748249def3bcaadd825ae17dc817a", 11408, badSourceType);

        Document doc = DOM.streamToDOM(event.getData());

        XmlAttributeChecker handler = new MixFilePathChecker(resultCollector);

        handler.validate(event, doc);

        String report = resultCollector.toReport();
        assertTrue(report.contains("2K-12:"),report);
        assertFalse(resultCollector.isSuccess(), report);
    }
}
