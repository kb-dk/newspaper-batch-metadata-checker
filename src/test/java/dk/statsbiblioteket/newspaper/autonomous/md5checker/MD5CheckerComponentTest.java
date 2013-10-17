package dk.statsbiblioteket.newspaper.autonomous.md5checker;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import dk.statsbiblioteket.autonomous.ResultCollector;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.Batch;

import java.io.IOException;
import java.util.Properties;

/**
 * Test MD5 checker
 */
public class MD5CheckerComponentTest {
    private Properties properties;

    @Before
    public void setUp() throws Exception {
        properties = new Properties();
        properties.setProperty("useFileSystem", "true");
        properties.setProperty("scratch", Thread.currentThread().getContextClassLoader().getResource("scratch").getFile());
    }

    @Test
    public void testDoWorkOnBatch() throws Exception {
        MD5CheckerComponent md5CheckerComponent = new MD5CheckerComponent(properties);
        Batch batch = new Batch();
        batch.setBatchID(400022028241L);
        batch.setRoundTripNumber(1);
        ResultCollector resultCollector = new ResultCollector(md5CheckerComponent.getComponentName(),
                                                              md5CheckerComponent.getComponentVersion());
        md5CheckerComponent.doWorkOnBatch(batch, resultCollector);
        Assert.assertTrue("Expected success, but result was:\n" + resultCollector.toReport(), resultCollector.isSuccess());

    }
}
