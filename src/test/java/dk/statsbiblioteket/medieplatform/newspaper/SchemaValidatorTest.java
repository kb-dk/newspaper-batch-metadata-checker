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
        Validator validator = new SchemaValidator("jpylizer.xsd");
        InputStream jpylizerFile = Thread.currentThread().getContextClassLoader()
                                         .getResourceAsStream(
                                                 "valid.xml");
        ResultCollector results = new ResultCollector("test", "0.1");
        validator.validate("valid.xml",jpylizerFile,results);
        System.out.println(results.toReport());
        Assert.assertTrue(results.isSuccess());

    }

    @Test
       public void testSchemaInvalidValidate()
               throws
               Exception {
           Validator validator = new SchemaValidator("jpylizer.xsd");
           InputStream jpylizerFile = Thread.currentThread().getContextClassLoader()
                                            .getResourceAsStream(
                                                    "schemaInvalid.xml");
           ResultCollector results = new ResultCollector("test", "0.1");
           validator.validate("schemaInvalid.xml",jpylizerFile,results);
           System.out.println(results.toReport());
           Assert.assertFalse(results.isSuccess());

       }
}
