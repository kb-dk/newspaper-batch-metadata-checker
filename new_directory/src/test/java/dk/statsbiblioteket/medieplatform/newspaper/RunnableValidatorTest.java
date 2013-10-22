package dk.statsbiblioteket.medieplatform.newspaper;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import junit.framework.Assert;
import org.testng.annotations.Test;

import java.util.Properties;

public class RunnableValidatorTest {
    @Test(groups = "integrationTest")
    public void testDoWorkOnBatch()
            throws
            Exception {

        Properties props = new Properties(System.getProperties());
        props.setProperty("scratch",System.getProperty("integration.test.newspaper.testdata")+"/small-test-batch/");

        RunnableValidator validator = new RunnableValidator(props, true);
        ResultCollector results = new ResultCollector(validator.getComponentName(), validator.getComponentVersion());
        validator.doWorkOnBatch(new Batch("400022028241"),results);

        System.out.println(results.toReport());

        Assert.assertTrue(results.isSuccess());

    }
}
