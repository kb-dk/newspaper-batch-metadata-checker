package dk.statsbiblioteket.newspaper.metadatachecker;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.filesystem.FileAttributeParsingEvent;
import dk.statsbiblioteket.newspaper.mfpakintegration.batchcontext.BatchContext;
import dk.statsbiblioteket.newspaper.mfpakintegration.batchcontext.BatchContextUtils;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.InconsistentDatabaseException;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.MfPakDAO;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.NewspaperBatchOptions;
import dk.statsbiblioteket.util.xml.DOM;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests for the validity of page-mods metadata are grouped in this class because they are logically connected, even
 * if they are actually distributed in two different places in the code ie
 * mods.sch and the class
 * ModsXPathEventHandler
 *
 */
public class PageModsTest {

    private Document goodBatchXmlStructure;

    @BeforeTest
    public void setUp() {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("assumed-valid-structure.xml");
        goodBatchXmlStructure = DOM.streamToDOM(is);
    }


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

        assertTrue(resultCollector.isSuccess(),resultCollector.toReport());
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
        assertTrue(report.contains("2C-3:"),report);
        assertTrue(report.contains("2C-4:"),report);
        assertTrue(report.contains("2C-6:"),report);
        assertTrue(report.contains("2C-9:"),report);
        assertTrue(report.contains("2C-10:"), report);
    }

    /**
     * Test that we test an invalid page mods file.
     */
    @Test
    public void testPageModsBad2Sch() {
        ResultCollector resultCollector = new ResultCollector("foo", "bar");
        SchematronValidatorEventHandler handler = new SchematronValidatorEventHandler(resultCollector, null);
        AttributeParsingEvent modsEvent = new AttributeParsingEvent("AdresseContoirsEfterretninger-1795-06-15-01-0003B.mods.xml") {
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

    }

    /**
     * Returns a mock MfPakDAO instance.
     * @param batch the batch against which to mock.
     * @param optionB7 whether or not Option B7 (Section Titles) has been specified for this batch.
     * @return a mock dao.
     * @throws SQLException
     */
    private MfPakDAO getMockMfPakDAO(Batch batch, boolean optionB7) throws SQLException {
        MfPakDAO dao = mock(MfPakDAO.class);
        NewspaperBatchOptions options = mock(NewspaperBatchOptions.class);
        when(options.isOptionB7()).thenReturn(optionB7);
        when(dao.getNewspaperID(batch.getBatchID())).thenReturn("adresseavisen1759");
        when(dao.getBatchOptions(anyString())).thenReturn(options);
        return dao;
    }

    /**
     * Test that we can validate a valid page mods file.
     * @throws SQLException 
     * @throws InconsistentDatabaseException 
     */
    @Test
    public void testPageModsGoodXpath() throws InconsistentDatabaseException, SQLException {
        ResultCollector resultCollector = new ResultCollector("foo", "bar");
        Batch batch = new Batch();
        batch.setBatchID("400022028241");
        batch.setRoundTripNumber(10);
        MfPakDAO dao = getMockMfPakDAO(batch, true);
        BatchContext context = BatchContextUtils.buildBatchContext(dao, batch);
        ModsXPathEventHandler handler = new ModsXPathEventHandler(resultCollector, context, goodBatchXmlStructure);
        String editionNodeName = "B400022028240-RT1/400022028240-14/1795-06-15-01";
        handler.handleNodeBegin(new NodeBeginsParsingEvent(editionNodeName));
        AttributeParsingEvent modsEvent = new AttributeParsingEvent("AdresseContoirsEfterretninger-1795-06-15-01-0003B.mods.xml") {
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
       * Test that a mods file with section title is identified as invalid when Option B7 has not been chosen.
       * @throws SQLException
       * @throws InconsistentDatabaseException
       */
      @Test
      public void testPageModsGoodXpathBadOptionB7() throws InconsistentDatabaseException, SQLException {
          ResultCollector resultCollector = new ResultCollector("foo", "bar");
          Batch batch = new Batch();
          batch.setBatchID("400022028241");
          batch.setRoundTripNumber(10);
          MfPakDAO dao = getMockMfPakDAO(batch, false);
          BatchContext context = BatchContextUtils.buildBatchContext(dao, batch);
          ModsXPathEventHandler handler = new ModsXPathEventHandler(resultCollector, context, goodBatchXmlStructure);
          String editionNodeName = "B400022028240-RT1/400022028240-14/1795-06-15-01";
          handler.handleNodeBegin(new NodeBeginsParsingEvent(editionNodeName));
          AttributeParsingEvent modsEvent = new AttributeParsingEvent("AdresseContoirsEfterretninger-1795-06-15-01-0003B.mods.xml") {
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
          assertFalse(resultCollector.isSuccess(), report);
          assertTrue(report.contains("2C-1:"), report);
      }

    /**
     * Test that we can test an invalid mods file.
     * @throws SQLException 
     * @throws InconsistentDatabaseException 
     */
    @Test
    public void testPageModsBad1Xpath() throws InconsistentDatabaseException, SQLException {
        ResultCollector resultCollector = new ResultCollector("foo", "bar");
        Batch batch = new Batch();
        batch.setBatchID("400022028241");
        batch.setRoundTripNumber(10);
        MfPakDAO dao = getMockMfPakDAO(batch, true);
        BatchContext context = BatchContextUtils.buildBatchContext(dao, batch);
        ModsXPathEventHandler handler = new ModsXPathEventHandler(resultCollector, context, goodBatchXmlStructure);
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
        assertTrue(report.contains("2C-4:"),report);
        assertTrue(report.contains("2C-5:"),report);
        assertTrue(report.contains("2C-11:"), report);
    }

    /**
     * Test that we can validate an edition directory against a batch structure for consistency
     * of expected brik files.
     * @throws FileNotFoundException
     * @throws SQLException
     */
    @Test
    public void testBrikConsistencyGood() throws FileNotFoundException, SQLException {
        String dataDirS = "goodData/editionDirWithValidBrik";
        ResultCollector resultCollector = new ResultCollector("foo", "bar");
        iterateDataDir(dataDirS, resultCollector);
        assertTrue(resultCollector.isSuccess(), resultCollector.toReport());
    }

    /**
      * Test with bad data where the brik file is not referred to in any mods file.
      */
     @Test
     public void testWithBadBrikData1() throws SQLException, FileNotFoundException {
         String dataDirS = "badData/editionDirsWithBadBrik/brikWithNoReferent";
         ResultCollector resultCollector = new ResultCollector("foo", "bar");
         iterateDataDir(dataDirS, resultCollector);
         final String message = resultCollector.toReport();
         assertFalse(resultCollector.isSuccess(), message);
         assertTrue(message.contains("2C-10"), message);
     }


     /**
      * Test with bad data where the brik file is referred to in one mods file but not the other for the same
      * scan.
      */
     @Test
     public void testWithBadBrikData2() throws SQLException, FileNotFoundException {
         String dataDirS = "badData/editionDirsWithBadBrik/brikWithPartialReferent";
         ResultCollector resultCollector = new ResultCollector("foo", "bar");
         iterateDataDir(dataDirS, resultCollector);
         final String message = resultCollector.toReport();
         assertFalse(resultCollector.isSuccess(), message);
         assertTrue(message.contains("2C-10"), message);
     }


     /**
      * Test with bad data where the brik file is missing.
      */
     @Test
     public void testWithBadBrikData3() throws SQLException, FileNotFoundException {
         String dataDirS = "badData/editionDirsWithBadBrik/modsWithNoBrik";
         ResultCollector resultCollector = new ResultCollector("foo", "bar");
         iterateDataDir(dataDirS, resultCollector);
         final String message = resultCollector.toReport();
         assertFalse(resultCollector.isSuccess(), message);
         assertTrue(message.contains("2C-10"), message);
     }



    private void iterateDataDir(String dataDirS, ResultCollector resultCollector) throws FileNotFoundException, SQLException {
        Batch batch = new Batch();
        batch.setBatchID("400022028241");
        batch.setRoundTripNumber(1);
        File dataDir = new File(Thread.currentThread().getContextClassLoader().getResource(dataDirS).getPath());
        String editionNodeName = "B400022028241-RT1/400022028241-1/1795-06-15-02";
        Document batchStructure = DOM.streamToDOM(new FileInputStream(new File(dataDir, "structure.xml")));
        MfPakDAO dao = getMockMfPakDAO(batch, true);
        BatchContext context = BatchContextUtils.buildBatchContext(dao, batch);
        ModsXPathEventHandler handler = new ModsXPathEventHandler(resultCollector, context, batchStructure);
        File fileDir = new File(dataDir, "1795-06-15-02");
        handler.handleNodeBegin(new NodeBeginsParsingEvent(editionNodeName));
        for (File attributeFile: fileDir.listFiles()) {
            final String name = editionNodeName + "/" + attributeFile.getName();
            if (attributeFile.getName().contains("mods")) {
                FileAttributeParsingEvent modsEvent = new FileAttributeParsingEvent(name, attributeFile);
                handler.handleAttribute(modsEvent);
            }
        }
        handler.handleNodeEnd(new NodeEndParsingEvent(editionNodeName));
    }


}
