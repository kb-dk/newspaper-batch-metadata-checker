package dk.statsbiblioteket.medieplatform.newspaper;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.InputStream;

public class SchematronValidatorTest {
    @Test
    public void testValidate()
            throws
            Exception {
        InputStream jpylizerFile = Thread.currentThread().getContextClassLoader()
                                         .getResourceAsStream(
                                                 "AdresseContoirsEfterretninger-1795-06-13-01-0006.xml");
        SchematronValidator validator = new SchematronValidator("sb-jp2-demands.sch");
        ResultCollector results = new ResultCollector("test", "0.1");
        validator.validate("AdresseContoirsEfterretninger-1795-06-13-01-0006.xml",jpylizerFile,results);
        System.out.println(results.toReport());
        Assert.assertTrue(results.isSuccess());


    }
}
