package dk.statsbiblioteket.newspaper.metadatachecker;

import org.testng.annotations.Test;

public class MetadataCheckerTest {
    @Test(enabled = false)
    public void testMain() throws Exception {
        MetadataChecker.main("-c",Thread.currentThread().getContextClassLoader().getResource("config.properties").getPath());
    }
}
