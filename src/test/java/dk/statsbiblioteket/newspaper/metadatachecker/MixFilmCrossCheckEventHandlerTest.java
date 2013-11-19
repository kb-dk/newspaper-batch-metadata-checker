package dk.statsbiblioteket.newspaper.metadatachecker;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

public class MixFilmCrossCheckEventHandlerTest {

    @Test
    public void goodTest() {
        ResultCollector resultCollector = new ResultCollector("foo", "bar");

        final String batchId = "400022028241";
        final String film = "1";
        final String avisID = "adresseavisen1759";
        final String publishDate = "1795-06-13";
        final String pictureNumber = "0006";
        final Batch batch = new Batch();
        batch.setBatchID(batchId);
        batch.setRoundTripNumber(1);
        AttributeParsingEvent mixEvent = MixerMockup.getMixPageAttributeParsingEvent(
                film, avisID, publishDate, pictureNumber, batch, 2286, 2864, 400, "");
        AttributeParsingEvent filmEvent = FilmMocker.getFilmXmlAttributeParsingEvent(film, avisID, "Adresse Contoirs Efterretninger", 
                "1980-11-01", "1978-10-10", "1978-12-30", batch, 400);
        
        MixFilmCrossCheckEventHandler handler = new MixFilmCrossCheckEventHandler(resultCollector);
        String goodPath = "B" + batchId + "-RT1/" + batchId + "-" + film; 
        handler.handleNodeBegin(new NodeBeginsParsingEvent(goodPath));
        handler.handleAttribute(filmEvent);
        handler.handleAttribute(mixEvent);
        handler.handleNodeEnd(new NodeEndParsingEvent(goodPath));


        assertTrue(resultCollector.isSuccess());
    }
    
    @Test
    public void badMixResolutionTest() {
        ResultCollector resultCollector = new ResultCollector("foo", "bar");

        final String batchId = "400022028241";
        final String film = "1";
        final String avisID = "adresseavisen1759";
        final String publishDate = "1795-06-13";
        final String pictureNumber = "0006";
        final Batch batch = new Batch();
        batch.setBatchID(batchId);
        batch.setRoundTripNumber(1);
        AttributeParsingEvent mixBadResEvent = MixerMockup.getMixPageAttributeParsingEvent(
                film, avisID, publishDate, pictureNumber, batch, 2286, 2864, 300, "");
        AttributeParsingEvent filmEvent = FilmMocker.getFilmXmlAttributeParsingEvent(film, avisID, "Adresse Contoirs Efterretninger", 
                "1980-11-01", "1978-10-10", "1978-12-30", batch, 400);
        
        MixFilmCrossCheckEventHandler handler = new MixFilmCrossCheckEventHandler(resultCollector);
        String goodPath = "B" + batchId + "-RT1/" + batchId + "-" + film; 
        handler.handleNodeBegin(new NodeBeginsParsingEvent(goodPath));
        handler.handleAttribute(filmEvent);
        handler.handleAttribute(mixBadResEvent);
        handler.handleNodeEnd(new NodeEndParsingEvent(goodPath));

        assertFalse(resultCollector.isSuccess());
        String report = resultCollector.toReport();
        assertTrue(report.contains("2K-5:"));
        assertTrue(report.contains("2K-6:"));
        
    }

    @Test
    public void NotInFilmBadMixResolutionTest() {
        ResultCollector resultCollector = new ResultCollector("foo", "bar");

        final String batchId = "400022028241";
        final String film = "1";
        final String avisID = "adresseavisen1759";
        final String publishDate = "1795-06-13";
        final String pictureNumber = "0006";
        final Batch batch = new Batch();
        batch.setBatchID(batchId);
        batch.setRoundTripNumber(1);
        AttributeParsingEvent mixBadResEvent = MixerMockup.getMixPageAttributeParsingEvent(
                film, avisID, publishDate, pictureNumber, batch, 2286, 2864, 300, "");
        AttributeParsingEvent filmEvent = FilmMocker.getFilmXmlAttributeParsingEvent(film, avisID, "Adresse Contoirs Efterretninger", 
                "1980-11-01", "1978-10-10", "1978-12-30", batch, 400);
        
        MixFilmCrossCheckEventHandler handler = new MixFilmCrossCheckEventHandler(resultCollector);
        String notInFilmPath = "B" + batchId + "-RT1/" + "WORKSHIFT-ISO-TARGET"; 
        handler.handleNodeBegin(new NodeBeginsParsingEvent(notInFilmPath));
        handler.handleAttribute(filmEvent);
        handler.handleAttribute(mixBadResEvent);
        handler.handleNodeEnd(new NodeEndParsingEvent(notInFilmPath));

        assertTrue(resultCollector.isSuccess());
        
    }
}
