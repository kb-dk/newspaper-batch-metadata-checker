package dk.statsbiblioteket.newspaper.metadatachecker;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.TreeEventHandler;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.MfPakDAO;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.NewspaperDateRange;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.NewspaperEntity;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.NewspaperTitle;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertFalse;

/**
 * Tests that the checks that the metadata for the edition follows the specification
 * actually do perform the required checks and catch failures to adhere to the specification.
 * See Appendix 2D – metadata per publication and edition.
 */
public class EditionModsTest {
    /** Test that we can validate a valid edition xml (mods) file. */
    @Test
    public void testEditionModsGood() throws SQLException {
        ResultCollector resultCollector = new ResultCollector("foo", "bar");
        SchematronValidatorEventHandler handler = new SchematronValidatorEventHandler(resultCollector, null);
        TreeEventHandler editionModsEventHandler = new EditionModsEventHandler(resultCollector, getMFPak(), getBatch());
        AttributeParsingEvent editionEvent = new AttributeParsingEvent(
                "B400022028241-RT1/400022028241-14/1795-06-13-01/adresseavisen1759-1795-06-13-01.edition.xml") {
            @Override
            public InputStream getData() throws IOException {
                return Thread.currentThread()
                             .getContextClassLoader()
                             .getResourceAsStream(
                                     "goodData/adresseavisen1759-1795-06-13-01.edition.xml");
            }

            @Override
            public String getChecksum() throws IOException {
                return null;
            }
        };
        handler.handleAttribute(editionEvent);
        editionModsEventHandler.handleAttribute(editionEvent);

        if ( ! resultCollector.isSuccess()){
            System.out
                  .println(resultCollector.toReport());
        }
        assertTrue(resultCollector.isSuccess());
    }

    /** Test that we can validate a valid edition xml (mods) file. */
    @Test
    public void testEditionModsBad() throws SQLException {
        ResultCollector resultCollector = new ResultCollector("foo", "bar");
        Batch batch = getBatch();
        TreeEventHandler schematronValidatorEventHandler = new SchematronValidatorEventHandler(resultCollector,null);
        TreeEventHandler editionModsEventHandler = new EditionModsEventHandler(resultCollector, getMFPak(), batch);
        AttributeParsingEvent editionEvent = new AttributeParsingEvent(
                "B400022028241-RT1/400022028241-14/1795-06-13-01/adresseavisen1759-1795-06-13-01.edition.xml") {
            @Override
            public InputStream getData() throws IOException {
                return Thread.currentThread()
                             .getContextClassLoader()
                             .getResourceAsStream(
                                     "badData/adresseavisen1759-1795-06-13-01.edition.xml");
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


        assertFalse(resultCollector.isSuccess());

    }

    private Batch getBatch() {
        return new Batch("400022028241");
    }

    private MfPakDAO getMFPak() throws SQLException {

        MfPakDAO mfPakDAO = mock(MfPakDAO.class);
        when(mfPakDAO.getNewspaperID(anyString())).thenReturn("adresseavisen1759");
        NewspaperTitle title = new NewspaperTitle();
        title.setTitle("Kiøbenhavns Kongelig alene priviligerede Adresse-Contoirs Efterretninger");
        title.setDateRange(new NewspaperDateRange(new Date(Long.MIN_VALUE), new Date()));
        when(mfPakDAO.getBatchNewspaperTitles(anyString())).thenReturn(Arrays.asList(title));
        NewspaperEntity entity = new NewspaperEntity();
        entity.setPublicationLocation("København");
        entity.setNewspaperID("adresseavisen1759");
        entity.setNewspaperTitle("Kiøbenhavns Kongelig alene priviligerede Adresse-Contoirs Efterretninger");
        when(mfPakDAO.getNewspaperEntity(anyString(), any(Date.class))).thenReturn(entity);
        return mfPakDAO;

    }

}
