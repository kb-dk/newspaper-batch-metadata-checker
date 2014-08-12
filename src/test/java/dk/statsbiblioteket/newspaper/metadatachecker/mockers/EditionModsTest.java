package dk.statsbiblioteket.newspaper.metadatachecker.mockers;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.TreeEventHandler;
import dk.statsbiblioteket.newspaper.metadatachecker.caches.DocumentCache;
import dk.statsbiblioteket.newspaper.mfpakintegration.batchcontext.BatchContext;
import dk.statsbiblioteket.newspaper.mfpakintegration.batchcontext.BatchContextUtils;
import dk.statsbiblioteket.newspaper.metadatachecker.EditionModsEventHandler;
import dk.statsbiblioteket.newspaper.metadatachecker.SchematronValidatorEventHandler;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.MfPakDAO;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.NewspaperBatchOptions;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.NewspaperDateRange;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.NewspaperEntity;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests that the checks that the metadata for the edition follows the specification
 * actually do perform the required checks and catch failures to adhere to the specification.
 * See Appendix 2D – metadata per publication and edition.
 */
public class EditionModsTest {
    
    @BeforeMethod 
    public void nukeBatchContext() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Field contexts = BatchContextUtils.class.getDeclaredField("batchContexts");
        contexts.setAccessible(true);
        Map m = (Map) contexts.get(null);
        m.clear();
    }
    
    /** Test that we can validate a valid edition xml (mods) file. */
    @Test
    public void testEditionModsGood() throws SQLException, ParseException {
        DocumentCache documentCache = new DocumentCache();
        ResultCollector resultCollector = new ResultCollector("foo", "bar");
        SchematronValidatorEventHandler handler = new SchematronValidatorEventHandler(resultCollector, null);
        BatchContext context = BatchContextUtils.buildBatchContext(getMFPak(), getBatch());
        TreeEventHandler editionModsEventHandler = new EditionModsEventHandler(resultCollector, context, documentCache);
        AttributeParsingEvent editionEvent = new AttributeParsingEvent(
                "B400022028241-RT1/400022028241-14/1795-06-01/adresseavisen1759-1795-06-01.edition.xml") {
            @Override
            public InputStream getData() throws IOException {
                return Thread.currentThread()
                             .getContextClassLoader()
                             .getResourceAsStream(
                                     "goodData/adresseavisen1759-1795-06-01.edition.xml");
            }

            @Override
            public String getChecksum() throws IOException {
                return null;
            }
        };
        handler.handleAttribute(editionEvent);
        editionModsEventHandler.handleAttribute(editionEvent);

        assertTrue(resultCollector.isSuccess(), resultCollector.toReport());
    }

    /** Test that we can validate a valid edition xml (mods) file. */
    @Test
    public void testEditionModsBad() throws SQLException, ParseException {
        DocumentCache documentCache = new DocumentCache();
        ResultCollector resultCollector = new ResultCollector("foo", "bar");
        Batch batch = getBatch();
        TreeEventHandler schematronValidatorEventHandler = new SchematronValidatorEventHandler(resultCollector,null);
        BatchContext context = BatchContextUtils.buildBatchContext(getMFPak(), batch);
        TreeEventHandler editionModsEventHandler = new EditionModsEventHandler(resultCollector, context, documentCache);
        AttributeParsingEvent editionEvent = new AttributeParsingEvent(
                "B400022028241-RT1/400022028241-14/1795-06-01/adresseavisen1759-1795-06-01.edition.xml") {
            @Override
            public InputStream getData() throws IOException {
                return Thread.currentThread()
                             .getContextClassLoader()
                             .getResourceAsStream(
                                     "badData/adresseavisen1759-1795-06-01.edition.xml");
            }

            @Override
            public String getChecksum() throws IOException {
                return null;
            }
        };
        schematronValidatorEventHandler.handleAttribute(editionEvent);
        editionModsEventHandler.handleAttribute(editionEvent);

        String report = resultCollector.toReport();
        AssertJUnit.assertTrue(report,report.contains("<description>2D-1: avisID adressecontoirsefterretninger does not match avisID in MFPak 'adresseavisen1759'</description>"));
        AssertJUnit.assertTrue(report,report.contains("<description>2D-1: avisID adressecontoirsefterretninger does not match avisID in file structure 'adresseavisen1759'</description>"));
        AssertJUnit.assertTrue(report,report.contains("<description>2D-2: title Adresse Contoirs Efterretninger does not match title in MFPak 'Kiøbenhavns Kongelig alene priviligerede Adresse-Contoirs Efterretninger'</description>"));
        AssertJUnit.assertTrue(report,report.contains("<description>2D-3: Publication location 'Kobenhavn' does not match value 'København' from MFPak</description>"));
        AssertJUnit.assertTrue(report,report.contains("<description>2D-4: Date issued from file does not correspond to date in filename</description>"));
        AssertJUnit.assertTrue(report,report.contains("<description>2D-9: Edition number (2) in edition xml doesn't correspond to node edition number: 1795-06-01</description>"));

        assertFalse(resultCollector.isSuccess(),resultCollector.toReport());

    }

    private Batch getBatch() {
        return new Batch("400022028241");
    }

    private MfPakDAO getMFPak() throws SQLException, ParseException {

        MfPakDAO mfPakDAO = mock(MfPakDAO.class);
        when(mfPakDAO.getNewspaperID(anyString())).thenReturn("adresseavisen1759");
        NewspaperEntity entity = new NewspaperEntity();
        entity.setNewspaperTitle("Kiøbenhavns Kongelig alene priviligerede Adresse-Contoirs Efterretninger");
        entity.setNewspaperID("adresseavisen1759");
        entity.setPublicationLocation("København");
        entity.setNewspaperDateRange(new NewspaperDateRange(new SimpleDateFormat("yyyy").parse("1600"), new Date()));
        when(mfPakDAO.getBatchNewspaperEntities(anyString())).thenReturn(Arrays.asList(entity));
        NewspaperEntity entity2 = new NewspaperEntity();
        entity2.setPublicationLocation("København");
        entity2.setNewspaperID("adresseavisen1759");
        entity2.setNewspaperTitle("Kiøbenhavns Kongelig alene priviligerede Adresse-Contoirs Efterretninger");
        when(mfPakDAO.getNewspaperEntity(anyString(), any(Date.class))).thenReturn(entity2);
        NewspaperBatchOptions options = new NewspaperBatchOptions();
        options.setOptionB1(false);
        options.setOptionB2(false);
        options.setOptionB9(false);
        when(mfPakDAO.getBatchOptions(anyString())).thenReturn(options);
        when(mfPakDAO.getBatchShipmentDate(anyString())).thenReturn(new Date(0));
        return mfPakDAO;

    }

}
