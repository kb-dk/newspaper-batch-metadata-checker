package dk.statsbiblioteket.medieplatform.newspaper;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.InputStream;

public class SchematronValidatorTest {
    @Test
    public void testValidateValid()
            throws
            Exception {
        InputStream jpylizerFile = Thread.currentThread().getContextClassLoader()
                                         .getResourceAsStream(
                                                 "valid.xml");
        SchematronValidator validator = new SchematronValidator("sb-jp2.sch");
        ResultCollector results = new ResultCollector("test", "0.1");
        validator.validate("valid.xml",jpylizerFile,results);
        System.out.println(results.toReport());
        Assert.assertTrue(results.isSuccess());


    }


    @Test
    public void testValidateInvalid()
            throws
            Exception {
        InputStream jpylizerFile = Thread.currentThread().getContextClassLoader()
                                         .getResourceAsStream(
                                                 "invalid.xml");
        SchematronValidator validator = new SchematronValidator("sb-jp2.sch");
        ResultCollector results = new ResultCollector("test", "0.1");
        validator.validate("invalid.xml",jpylizerFile,results);
        System.out.println(results.toReport());
        Assert.assertFalse(results.isSuccess());


    }

}
