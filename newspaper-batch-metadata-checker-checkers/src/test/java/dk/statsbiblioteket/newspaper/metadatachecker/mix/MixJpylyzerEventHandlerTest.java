package dk.statsbiblioteket.newspaper.metadatachecker.mix;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.newspaper.metadatachecker.mockers.JpylyzerMocker;
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

public class MixJpylyzerEventHandlerTest {

    private ResultCollector resultCollector = null;

    @BeforeTest
    public void setUp() {
        resultCollector = new ResultCollector("test", "test");
    }

    @Test
    public void testJpylyzerGood() throws ParseException, SQLException, IOException {
        setUp();
        final String batchId = "400022028241";
        final String film = "1";
        final String avisID = "adresseavisen1759";
        final String publishDate = "1795-06";
        final String pictureNumber = "0006";
        final Batch batch = new Batch();
        batch.setBatchID(batchId);
        batch.setRoundTripNumber(1);
        int width = 9304;
        int height = 11408;
        int size = width * height;
        AttributeParsingEvent event = MixerMockup.getMixPageAttributeParsingEvent(
                film,
                avisID,
                publishDate,
                pictureNumber,
                batch,
                width,
                height,
                400,
                "7ed748249def3bcaadd825ae17dc817a",
                size, "microfilm");
        Document doc1 = DOM.streamToDOM(event.getData());

        AttributeParsingEvent event2 = JpylyzerMocker.getJpylyzerXmlAttributeParsingEvent(
                film, avisID, batch, size, width, height);
        Document doc2 = DOM.streamToDOM(event2.getData());

        XmlAttributeChecker handler = new MixJpylyzerEventHandler(resultCollector);

        handler.validate(event, doc1);
        handler.validate(event2, doc2);

        String report = resultCollector.toReport();
        assertTrue(resultCollector.isSuccess(), report);
    }

    @Test
    public void testJpylyzerSizeBad() throws ParseException, SQLException, IOException {
        setUp();
        final String batchId = "400022028241";
        final String film = "1";
        final String avisID = "adresseavisen1759";
        final String publishDate = "1795-06";
        final String pictureNumber = "0006";
        final Batch batch = new Batch();
        batch.setBatchID(batchId);
        batch.setRoundTripNumber(1);
        int width = 9304;
        int height = 11408;
        int size = width * height;
        AttributeParsingEvent event = MixerMockup.getMixPageAttributeParsingEvent(
                film,
                avisID,
                publishDate,
                pictureNumber,
                batch,
                width,
                height,
                400,
                "7ed748249def3bcaadd825ae17dc817a",
                size, "microfilm");
        Document doc1 = DOM.streamToDOM(event.getData());

        AttributeParsingEvent event2 = JpylyzerMocker.getJpylyzerXmlAttributeParsingEvent(
                film, avisID, batch, size - 1, width, height);
        Document doc2 = DOM.streamToDOM(event2.getData());

        XmlAttributeChecker handler = new MixJpylyzerEventHandler(resultCollector);

        handler.validate(event, doc1);
        handler.validate(event2, doc2);

        String report = resultCollector.toReport();
        assertFalse(resultCollector.isSuccess(), report);
        assertTrue(
                report.contains("The file size from jpylyzer does not match what is reported in the mix file"),
                report);
    }



    @Test
    public void testJpylyzerWidthBad() throws ParseException, SQLException, IOException {
        setUp();
        final String batchId = "400022028241";
        final String film = "1";
        final String avisID = "adresseavisen1759";
        final String publishDate = "1795-06";
        final String pictureNumber = "0006";
        final Batch batch = new Batch();
        batch.setBatchID(batchId);
        batch.setRoundTripNumber(1);
        int width = 9304;
        int height = 11408;
        int size = width * height;
        AttributeParsingEvent event = MixerMockup.getMixPageAttributeParsingEvent(
                film,
                avisID,
                publishDate,
                pictureNumber,
                batch,
                width,
                height,
                400,
                "7ed748249def3bcaadd825ae17dc817a",
                size, "microfilm");
        Document doc1 = DOM.streamToDOM(event.getData());

        AttributeParsingEvent event2 = JpylyzerMocker.getJpylyzerXmlAttributeParsingEvent(
                film, avisID, batch, size, width-1, height);
        Document doc2 = DOM.streamToDOM(event2.getData());

        XmlAttributeChecker handler = new MixJpylyzerEventHandler(resultCollector);

        handler.validate(event, doc1);
        handler.validate(event2, doc2);

        String report = resultCollector.toReport();
        assertFalse(resultCollector.isSuccess(), report);
        assertTrue(
                report.contains("The picture width from jpylyzer does not match what is reported in the mix file"),
                report);
    }


    @Test
    public void testJpylyzerHeightBad() throws ParseException, SQLException, IOException {
        setUp();
        final String batchId = "400022028241";
        final String film = "1";
        final String avisID = "adresseavisen1759";
        final String publishDate = "1795-06";
        final String pictureNumber = "0006";
        final Batch batch = new Batch();
        batch.setBatchID(batchId);
        batch.setRoundTripNumber(1);
        int width = 9304;
        int height = 11408;
        int size = width * height;
        AttributeParsingEvent event = MixerMockup.getMixPageAttributeParsingEvent(
                film,
                avisID,
                publishDate,
                pictureNumber,
                batch,
                width,
                height,
                400,
                "7ed748249def3bcaadd825ae17dc817a",
                size, "microfilm");
        Document doc1 = DOM.streamToDOM(event.getData());

        AttributeParsingEvent event2 = JpylyzerMocker.getJpylyzerXmlAttributeParsingEvent(
                film, avisID, batch, size, width, height-1);
        Document doc2 = DOM.streamToDOM(event2.getData());

        XmlAttributeChecker handler = new MixJpylyzerEventHandler(resultCollector);

        handler.validate(event, doc1);
        handler.validate(event2, doc2);

        String report = resultCollector.toReport();
        assertFalse(resultCollector.isSuccess(), report);
        assertTrue(
                report.contains("The picture height from jpylyzer does not match what is reported in the mix file"),
                report);
    }

}
