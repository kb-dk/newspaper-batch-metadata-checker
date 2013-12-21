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

public class MixChecksumCheckerTest {

    private ResultCollector resultCollector = null;

    @BeforeTest
    public void setUp() {
        resultCollector = new ResultCollector("test", "test");
    }

    @Test
    public void testChecksumGood() throws ParseException, SQLException, IOException {
        setUp();
        final String batchId = "400022028241";
        final String film = "1";
        final String avisID = "adresseavisen1759";
        final String publishDate = "1795-06";
        final String pictureNumber = "0006";
        final Batch batch = new Batch();
        batch.setBatchID(batchId);
        batch.setRoundTripNumber(1);
        AttributeParsingEvent event = MixerMockup.getMixPageAttributeParsingEvent(
                film, avisID, publishDate, pictureNumber, batch, 9304, 11408, 400, "7ed748249def3bcaadd825ae17dc817a",15);

        Document doc = DOM.streamToDOM(event.getData());

        XmlAttributeChecker handler = new MixChecksumChecker(resultCollector, DOM.streamToDOM(Thread.currentThread()
                                                                                             .getContextClassLoader().getResourceAsStream("assumed-valid-structure.xml")));

        handler.validate(event, doc);


        String report = resultCollector.toReport();
        assertTrue(resultCollector.isSuccess(), report);
    }

    @Test
      public void testChecksumBad() throws ParseException, SQLException, IOException {
          setUp();
          final String batchId = "400022028241";
          final String film = "1";
          final String avisID = "adresseavisen1759";
          final String publishDate = "1795-06";
          final String pictureNumber = "0006";
          final Batch batch = new Batch();
          batch.setBatchID(batchId);
          batch.setRoundTripNumber(1);
          AttributeParsingEvent event = MixerMockup.getMixPageAttributeParsingEvent(
                  film, avisID, publishDate, pictureNumber, batch, 9304, 11408, 400, "wrongSum",15);

          Document doc = DOM.streamToDOM(event.getData());

          XmlAttributeChecker handler = new MixChecksumChecker(resultCollector, DOM.streamToDOM(Thread.currentThread()
                                                                                               .getContextClassLoader().getResourceAsStream("assumed-valid-structure.xml")));

          handler.validate(event, doc);


          String report = resultCollector.toReport();
          assertFalse(resultCollector.isSuccess(), report);
      }

}
