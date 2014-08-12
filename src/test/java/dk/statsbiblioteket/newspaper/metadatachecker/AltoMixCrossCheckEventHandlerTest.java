package dk.statsbiblioteket.newspaper.metadatachecker;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.newspaper.metadatachecker.caches.DocumentCache;
import dk.statsbiblioteket.newspaper.metadatachecker.mockers.AltoMocker;
import dk.statsbiblioteket.newspaper.metadatachecker.mockers.MixerMockup;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

public class AltoMixCrossCheckEventHandlerTest {

    @Test
    public void goodTest() {
        ResultCollector resultCollector = new ResultCollector("foo", "bar");
        DocumentCache documentCache = new DocumentCache();

        final String batchId = "400022028241";
        final String film = "1";
        final String avisID = "adresseavisen1759";
        final String publishDate = "1795-06";
        final String pictureNumber = "0006";
        final Batch batch = new Batch();
        batch.setBatchID(batchId);
        batch.setRoundTripNumber(1);
        AttributeParsingEvent eventMix = MixerMockup.getMixPageAttributeParsingEvent(
                film, avisID, publishDate, pictureNumber, batch, 2286, 2864, 400, "", 15, "microfilm");
        AttributeParsingEvent eventAlto = AltoMocker.getAltoPageAttributeParsingEvent(
                film, avisID, publishDate, pictureNumber, batch, 2286, 2864);
        AltoMixCrossCheckEventHandler handler = new AltoMixCrossCheckEventHandler(resultCollector, documentCache);
        handler.handleNodeBegin(new NodeBeginsParsingEvent("good"));
        handler.handleAttribute(eventMix);
        handler.handleAttribute(eventAlto);
        handler.handleNodeEnd(new NodeEndParsingEvent("good"));


        assertTrue(resultCollector.isSuccess());


    }

    @Test
    public void badTest() {
        DocumentCache documentCache = new DocumentCache();
        final String batchId = "400022028241";
        final String film = "1";
        final String avisID = "adresseavisen1759";
        final String publishDate = "1795-06";
        final String pictureNumber = "0006";
        final Batch batch = new Batch();
        batch.setBatchID(batchId);
        batch.setRoundTripNumber(1);
        AttributeParsingEvent eventMix = MixerMockup.getMixPageAttributeParsingEvent(
                film, avisID, publishDate, pictureNumber, batch, 2286, 2864, 400,"",15, "microfilm");
        AttributeParsingEvent eventAlto = AltoMocker.getAltoPageAttributeParsingEvent(
                film, avisID, publishDate, pictureNumber, batch, 2285, 2863);
        ResultCollector resultCollector = new ResultCollector("foo", "bar");
        AltoMixCrossCheckEventHandler handler = new AltoMixCrossCheckEventHandler(resultCollector, documentCache);
        handler.handleNodeBegin(new NodeBeginsParsingEvent("bad"));
        handler.handleAttribute(eventMix);
        handler.handleAttribute(eventAlto);
        handler.handleNodeEnd(new NodeEndParsingEvent("bad"));


        assertFalse(resultCollector.isSuccess());


    }


}
