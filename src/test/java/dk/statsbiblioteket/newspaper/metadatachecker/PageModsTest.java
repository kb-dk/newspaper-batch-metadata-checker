package dk.statsbiblioteket.newspaper.metadatachecker;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.Event;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.newspaper.mfpakintegration.configuration.MfPakConfiguration;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.InconsistentDatabaseException;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.MfPakDAO;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.NewspaperBatchOptions;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.NewspaperDateRange;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.NewspaperEntity;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.NewspaperTitle;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.testng.Assert.assertTrue;

/**
 * Tests for the validity of page-mods metadata are grouped in this class because they are logically connected, even
 * if they are actually distributed in two different places in the code ie
 * mods.sch and the class
 * ModsXPathEventHandler
 *
 */
public class PageModsTest {

    /**
     * Test that we can validate a valid page mods file.
     */
    @Test
    public void testPageModsGoodSch() {
        ResultCollector resultCollector = new ResultCollector("foo", "bar");
        SchematronValidatorEventHandler handler = new SchematronValidatorEventHandler(resultCollector, null);
        AttributeParsingEvent modsEvent = new AttributeParsingEvent("B400022028241-RT1/400022028241-14/1795-06-15-01/AdresseContoirsEfterretninger-1795-06-15-01-0010B.mods.xml") {
            @Override
            public InputStream getData() throws IOException {
                return Thread.currentThread().getContextClassLoader().getResourceAsStream("goodData/goodpagemods.mods.xml");
            }

            @Override
            public String getChecksum() throws IOException {
                return null;
            }
        };
        handler.handleAttribute(modsEvent);
        System.out.println(resultCollector.toReport());
        assertTrue(resultCollector.isSuccess());
    }


    /**
     * Test that we test an invalid page mods file.
     */
    @Test
    public void testPageModsBad1Sch() {
        ResultCollector resultCollector = new ResultCollector("foo", "bar");
        SchematronValidatorEventHandler handler = new SchematronValidatorEventHandler(resultCollector, null);
        AttributeParsingEvent modsEvent = new AttributeParsingEvent("AdresseContoirsEfterretninger-1795-06-15-01-0010B.mods.xml") {
            @Override
            public InputStream getData() throws IOException {
                return Thread.currentThread().getContextClassLoader().getResourceAsStream("badData/bad1.mods.xml");
            }

            @Override
            public String getChecksum() throws IOException {
                return null;
            }
        };
        handler.handleAttribute(modsEvent);
        String report = resultCollector.toReport();
        assertTrue(report.contains("2C-1:"));
        assertTrue(report.contains("2C-3:"));
        assertTrue(report.contains("2C-4:"));
        assertTrue(report.contains("2C-6:"));
        assertTrue(report.contains("2C-9:"));
        assertTrue(report.contains("2C-10:"));
        System.out.println(report);
    }

    /**
     * Test that we test an invalid page mods file.
     */
    @Test
    public void testPageModsBad2Sch() {
        ResultCollector resultCollector = new ResultCollector("foo", "bar");
        SchematronValidatorEventHandler handler = new SchematronValidatorEventHandler(resultCollector, null);
        AttributeParsingEvent modsEvent = new AttributeParsingEvent("AdresseContoirsEfterretninger-1795-06-15-01-0010B.mods.xml") {
            @Override
            public InputStream getData() throws IOException {
                return Thread.currentThread().getContextClassLoader().getResourceAsStream("badData/bad2.mods.xml");
            }

            @Override
            public String getChecksum() throws IOException {
                return null;
            }
        };
        handler.handleAttribute(modsEvent);

        String report = resultCollector.toReport();
        assertTrue(report.contains("2C-4:"), report);
        assertTrue(report.contains("2C-6:"), report);
        assertTrue(report.contains("2C-9:"), report);
        assertTrue(report.contains("2C-10:"), report);

        System.out.println(report);
    }


    /**
     * Test that we can validate a valid page mods file.
     */
    @Test
    public void testPageModsGoodXpath() {
        ResultCollector resultCollector = new ResultCollector("foo", "bar");
        MfPakConfiguration configuration = new MfPakConfiguration();
        Batch batch = new Batch();
        batch.setBatchID("400022028241");
        batch.setRoundTripNumber(10);
        StubMfPakDAO dao = new StubMfPakDAO(configuration);
        dao.setNewspaperID("adressecontoirsefterretninger");
        ModsXPathEventHandler handler = new ModsXPathEventHandler(resultCollector, dao, batch);
        AttributeParsingEvent modsEvent = new AttributeParsingEvent("AdresseContoirsEfterretninger-1795-06-15-01-0010B.mods.xml") {
            @Override
            public InputStream getData() throws IOException {
                return Thread.currentThread().getContextClassLoader().getResourceAsStream("goodData/goodpagemods.mods.xml");
            }

            @Override
            public String getChecksum() throws IOException {
                return null;
            }
        };
        handler.handleAttribute(modsEvent);
        String report = resultCollector.toReport();
        assertTrue(resultCollector.isSuccess(), report);
    }

    /**
     * Test that we can test and invalid mods file.
     */
    @Test
    public void testPageModsBad1Xpath() {
        ResultCollector resultCollector = new ResultCollector("foo", "bar");
        MfPakConfiguration configuration = new MfPakConfiguration();
        Batch batch = new Batch();
        batch.setBatchID("400022028241");
        batch.setRoundTripNumber(10);
        StubMfPakDAO dao = new StubMfPakDAO(configuration);
        dao.setNewspaperID("adressecontoirsefterretninger123");
        ModsXPathEventHandler handler = new ModsXPathEventHandler(resultCollector, dao, batch);
        AttributeParsingEvent modsEvent = new AttributeParsingEvent("AdresseContoirsEfterretninger-1795-06-15-01-0010B.mods.xml") {
            @Override
            public InputStream getData() throws IOException {
                return Thread.currentThread().getContextClassLoader().getResourceAsStream("badData/bad1.mods.xml");
            }

            @Override
            public String getChecksum() throws IOException {
                return null;
            }
        };
        handler.handleAttribute(modsEvent);
        String report = resultCollector.toReport();
        assertTrue(report.contains("2C-4:"));
        assertTrue(report.contains("2C-5:"));
        assertTrue(report.contains("2C-11:"));
        System.out.println(report);
    }



    public static class StubMfPakDAO extends MfPakDAO {

        private String newspaperID;

        public StubMfPakDAO(MfPakConfiguration configuration) {
            super(configuration);
        }

        @Override
        public List<Batch> getAllBatches() throws SQLException {
            return null;
        }

        @Override
        public Batch getBatchByBarcode(String barcode) throws SQLException {
            return null;
        }

        public void setNewspaperID(String newspaperID) {
            this.newspaperID = newspaperID;
        }

        @Override
        public String getNewspaperID(String barcode) throws SQLException {
            return newspaperID;
        }

        @Override
        public Event getEvent(String batchBarcode, String eventStatus) throws SQLException {
            return null;
        }

        @Override
        public NewspaperEntity getNewspaperEntity(String newspaperID, Date date) throws
                                                                                 SQLException,
                                                                                 InconsistentDatabaseException {
            return null;
        }

        @Override
        public List<NewspaperDateRange> getBatchDateRanges(String batchID) throws SQLException {
            return new ArrayList<>(Arrays.asList(new NewspaperDateRange(new Date(0),new Date())));

        }

        @Override
        public List<NewspaperTitle> getBatchNewspaperTitles(String batchID) throws SQLException {
            NewspaperTitle title = new NewspaperTitle();
            title.setTitle("Kiøbenhavns Kongelig alene priviligerede Adresse-Contoirs Efterretninger");
            title.setDateRange(new NewspaperDateRange(new Date(Long.MIN_VALUE),new Date()));
            return Arrays.asList(title);
        }

        @Override
        public NewspaperBatchOptions getBatchOptions(String batchID) throws
                                                                     SQLException,
                                                                     InconsistentDatabaseException {
            return null;
        }
    }

}
