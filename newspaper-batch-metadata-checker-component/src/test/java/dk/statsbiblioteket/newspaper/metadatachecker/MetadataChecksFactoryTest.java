package dk.statsbiblioteket.newspaper.metadatachecker;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.TreeEventHandler;
import dk.statsbiblioteket.newspaper.metadatachecker.film.FilmXmlChecker;
import dk.statsbiblioteket.newspaper.metadatachecker.jpylyzer.JpylyzingEventHandler;
import dk.statsbiblioteket.newspaper.metadatachecker.mix.MixXmlFileChecker;
import dk.statsbiblioteket.newspaper.metadatachecker.mockers.MFPakMocker;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MetadataChecksFactoryTest {

    @Test
    public void testCreateEventHandlers() throws Exception {
        MetadataChecksFactory factoryForAll = new MetadataChecksFactory(new ResultCollector("sdfsd", "sdfds"),
                true,
                "batchFolder",
                "jpylyzerPath", MFPakMocker.getMFPak(),
                new Batch("4000"),
                null,
                null);
        final List<TreeEventHandler> eventHandlers = factoryForAll.createEventHandlers();
        Assert.assertEquals(eventHandlers.size(), 11);
        int i = 0;
        Assert.assertTrue(eventHandlers.get(i++) instanceof ChecksumCheckEventHandler);
        Assert.assertTrue(eventHandlers.get(i++) instanceof JpylyzingEventHandler);
        Assert.assertTrue(eventHandlers.get(i++) instanceof SchemaValidatorEventHandler);
        Assert.assertTrue(eventHandlers.get(i++) instanceof SchematronValidatorEventHandler);
        Assert.assertTrue(eventHandlers.get(i++) instanceof ModsXPathEventHandler);
        Assert.assertTrue(eventHandlers.get(i++) instanceof AltoXPathEventHandler);
        Assert.assertTrue(eventHandlers.get(i++) instanceof AltoMixCrossCheckEventHandler);
        Assert.assertTrue(eventHandlers.get(i++) instanceof EditionModsEventHandler);
        Assert.assertTrue(eventHandlers.get(i++) instanceof FilmXmlChecker);
        Assert.assertTrue(eventHandlers.get(i++) instanceof MixXmlFileChecker);
        Assert.assertTrue(eventHandlers.get(i++) instanceof MixFilmCrossCheckEventHandler);

    }

    @Test
    public void testCreateEventHandlersWithDisabled() throws Exception {
        Set<MetadataChecksFactory.Checks> disabled = new HashSet<>();
        disabled.add(MetadataChecksFactory.Checks.CHECKSUM);
        disabled.add(MetadataChecksFactory.Checks.MIX_FILM);
        MetadataChecksFactory factoryForAll = new MetadataChecksFactory(new ResultCollector("sdfsd", "sdfds"),
                true,
                "batchFolder",
                "jpylyzerPath", MFPakMocker.getMFPak(),
                new Batch("4000"),
                null,
                disabled);
        final List<TreeEventHandler> eventHandlers = factoryForAll.createEventHandlers();
        Assert.assertEquals(eventHandlers.size(), 9);
        int i = 0;
        Assert.assertTrue(eventHandlers.get(i++) instanceof JpylyzingEventHandler);
        Assert.assertTrue(eventHandlers.get(i++) instanceof SchemaValidatorEventHandler);
        Assert.assertTrue(eventHandlers.get(i++) instanceof SchematronValidatorEventHandler);
        Assert.assertTrue(eventHandlers.get(i++) instanceof ModsXPathEventHandler);
        Assert.assertTrue(eventHandlers.get(i++) instanceof AltoXPathEventHandler);
        Assert.assertTrue(eventHandlers.get(i++) instanceof AltoMixCrossCheckEventHandler);
        Assert.assertTrue(eventHandlers.get(i++) instanceof EditionModsEventHandler);
        Assert.assertTrue(eventHandlers.get(i++) instanceof FilmXmlChecker);
        Assert.assertTrue(eventHandlers.get(i++) instanceof MixXmlFileChecker);
    }

    public void testCreateEventHandlersWithDisabledNotAtNinestars() throws Exception {
        Set<MetadataChecksFactory.Checks> disabled = new HashSet<>();
        disabled.add(MetadataChecksFactory.Checks.CHECKSUM);
        disabled.add(MetadataChecksFactory.Checks.MIX_FILM);
        MetadataChecksFactory factoryForAll = new MetadataChecksFactory(new ResultCollector("sdfsd", "sdfds"),
                false,
                "batchFolder",
                "jpylyzerPath", MFPakMocker.getMFPak(),
                new Batch("4000"),
                null,
                disabled);
        final List<TreeEventHandler> eventHandlers = factoryForAll.createEventHandlers();
        Assert.assertEquals(eventHandlers.size(), 8);
        int i = 0;
        Assert.assertTrue(eventHandlers.get(i++) instanceof SchemaValidatorEventHandler);
        Assert.assertTrue(eventHandlers.get(i++) instanceof SchematronValidatorEventHandler);
        Assert.assertTrue(eventHandlers.get(i++) instanceof ModsXPathEventHandler);
        Assert.assertTrue(eventHandlers.get(i++) instanceof AltoXPathEventHandler);
        Assert.assertTrue(eventHandlers.get(i++) instanceof AltoMixCrossCheckEventHandler);
        Assert.assertTrue(eventHandlers.get(i++) instanceof EditionModsEventHandler);
        Assert.assertTrue(eventHandlers.get(i++) instanceof FilmXmlChecker);
        Assert.assertTrue(eventHandlers.get(i++) instanceof MixXmlFileChecker);
    }
}