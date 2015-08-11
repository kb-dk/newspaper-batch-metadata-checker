package dk.statsbiblioteket.newspaper.metadatachecker.mix;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.newspaper.metadatachecker.mockers.MixerMockup;
import dk.statsbiblioteket.newspaper.metadatachecker.checker.XmlAttributeChecker;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.MfPakDAO;
import dk.statsbiblioteket.util.xml.DOM;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class MixShipmentDateCheckerTest {

    private ResultCollector resultCollector = null;

    @BeforeMethod
    public void setupResultCollector() {
        resultCollector = new ResultCollector("test", "test");
    }

    @Test
    public void testXpathValidationScannedDate() throws ParseException, SQLException, IOException {
        final String batchId = "400022028241";
        final String film = "1";
        final String avisID = "adresseavisen1759";
        final String publishDate = "1795-06";
        final String pictureNumber = "0006";
        final Batch batch = new Batch();
        batch.setBatchID(batchId);
        batch.setRoundTripNumber(1);
        AttributeParsingEvent event = MixerMockup.getMixPageAttributeParsingEvent(
                film, avisID, publishDate, pictureNumber, batch, 9304, 11408, 400, "7ed748249def3bcaadd825ae17dc817a",15,
                "microfilm");


        MfPakDAO mfpakDao = mock(MfPakDAO.class);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date shipmentDate = formatter.parse("2010-01-02");
        when(mfpakDao.getBatchShipmentDate(batchId)).thenReturn(shipmentDate);

        Document doc = DOM.streamToDOM(event.getData());
        XmlAttributeChecker checker = new MixShipmentDateChecker(
                resultCollector, mfpakDao.getBatchShipmentDate(batchId));

        checker.validate(event, doc);

        String report = resultCollector.toReport();
        assertTrue(resultCollector.isSuccess(), report);
    }

    @Test
    public void testXpathValidationScannedBeforeShipment() throws ParseException, SQLException, IOException {
        final String batchId = "400022028241";
        final String film = "1";
        final String avisID = "adresseavisen1759";
        final String publishDate = "1795-06";
        final String pictureNumber = "0006";
        final Batch batch = new Batch();
        batch.setBatchID(batchId);
        batch.setRoundTripNumber(1);
        AttributeParsingEvent event = MixerMockup.getMixPageAttributeParsingEvent(
                film, avisID, publishDate, pictureNumber, batch, 9304, 11408, 400, "7ed748249def3bcaadd825ae17dc817a",15,
                "microfilm");

        MfPakDAO mfpakDao = mock(MfPakDAO.class);
        // The in the mix file is 2010-11-11
        Date shipmentDate = new Date();
        when(mfpakDao.getBatchShipmentDate(batchId)).thenReturn(shipmentDate);

        Document doc = DOM.streamToDOM(event.getData());
        XmlAttributeChecker checker = new MixShipmentDateChecker(
                resultCollector,
                mfpakDao.getBatchShipmentDate(batchId));

        checker.validate(event, doc);

        String report = resultCollector.toReport();
        assertFalse(resultCollector.isSuccess(), report);
        assertTrue(report.contains("2K-15:"));
    }


}
