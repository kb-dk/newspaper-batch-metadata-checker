package dk.statsbiblioteket.newspaper.metadatachecker.crosscheck;


import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.filesystem.FileAttributeParsingEvent;
import org.testng.annotations.Test;

import java.io.File;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**

 */
public class BrikConsistencyCheckHandlerTest {

    /**
     * Test with valid data.
     */
    @Test
    public void testWithGoodData() {
        String dataDirS = "goodData/editionDirWithValidBrik";
        ResultCollector resultCollector = new ResultCollector("foo", "bar");
        iterateNode(dataDirS, resultCollector);
        assertTrue(resultCollector.isSuccess(), resultCollector.toReport());
    }


    /**
     * Test with bad data where the brik file is not referred to in any mods file.
     */
    @Test
    public void testWithBadData1() {
        String dataDirS = "badData/editionDirsWithBadBrik/brikNoReferent";
        ResultCollector resultCollector = new ResultCollector("foo", "bar");
        iterateNode(dataDirS, resultCollector);
        final String message = resultCollector.toReport();
        assertFalse(resultCollector.isSuccess(), message);
        assertTrue(message.contains("2C-10"), message);
    }


    /**
     * Test with bad data where the brik file is referred to in one mods file but not the other for the same
     * scan.
     */
    @Test
    public void testWithBadData2() {
        String dataDirS = "badData/editionDirsWithBadBrik/brikWithPartialReferent";
        ResultCollector resultCollector = new ResultCollector("foo", "bar");
        iterateNode(dataDirS, resultCollector);
        final String message = resultCollector.toReport();
        assertFalse(resultCollector.isSuccess(), message);
        assertTrue(message.contains("2C-10"), message);
    }


    /**
     * Test with bad data where the brik file is missing.
     */
    @Test
    public void testWithBadData3() {
        String dataDirS = "badData/editionDirsWithBadBrik/modsWithNoBrik";
        ResultCollector resultCollector = new ResultCollector("foo", "bar");
        iterateNode(dataDirS, resultCollector);
        final String message = resultCollector.toReport();
        assertFalse(resultCollector.isSuccess(), message);
        assertTrue(message.contains("2C-10"), message);
    }





    private void iterateNode(String dataDirS, ResultCollector resultCollector) {
        Batch batch = new Batch();
        BrikConsistencyCheckHandler handler = new BrikConsistencyCheckHandler(resultCollector, batch);
        File dataDir = new File(Thread.currentThread().getContextClassLoader().getResource(dataDirS).getPath());
        String editionNodeName = "B400022028240-RT1/400022028240-14/1795-06-15-02";
        handler.handleNodeBegin(new NodeBeginsParsingEvent(editionNodeName));
        for (File attributeFile: dataDir.listFiles()) {
            final String name = editionNodeName + "/" + attributeFile.getName();
            if (attributeFile.getName().contains("mods")) {
                FileAttributeParsingEvent modsEvent = new FileAttributeParsingEvent(name, attributeFile);
                handler.handleAttribute(modsEvent);
            } else if (attributeFile.getName().contains("brik")) {
                handler.handleNodeBegin(new NodeBeginsParsingEvent(name));
                handler.handleAttribute(new FileAttributeParsingEvent(name + "/contents", attributeFile));
                handler.handleNodeEnd(new NodeEndParsingEvent(name));
            }
        }
        handler.handleNodeEnd(new NodeEndParsingEvent(editionNodeName));
    }

}
