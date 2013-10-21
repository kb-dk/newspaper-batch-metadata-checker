package dk.statsbiblioteket.medieplatform.newspaper;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.InputStream;

public class SchemaValidatorTest {
    @Test
    public void testValidate()
            throws
            Exception {
        Validator validator = new SchemaValidator();
        InputStream jpylizerFile = Thread.currentThread().getContextClassLoader()
                                         .getResourceAsStream(
                                                 "AdresseContoirsEfterretninger-1795-06-13-01-0006.xml");
        ResultCollector results = new ResultCollector("test", "0.1");
        validator.validate("src/test/resources/AdresseContoirsEfterretninger-1795-06-13-01-0006.xml",jpylizerFile,results);
        System.out.println(results.toReport());
        Assert.assertTrue(results.isSuccess());

    }
}
