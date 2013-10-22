package dk.statsbiblioteket.newspaper.metadatachecker;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.newspaper.metadatachecker.jpylyzer.SchemaValidator;
import dk.statsbiblioteket.newspaper.metadatachecker.jpylyzer.Validator;
import dk.statsbiblioteket.util.Strings;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SchemaValidatorTest {
    @Test
    public void testValidate()
            throws
            Exception {
        Validator validator = new SchemaValidator("jpylizer.xsd");
        String jpylizerFile = Strings.flush(Thread.currentThread().getContextClassLoader()
                                                  .getResourceAsStream("valid.xml"));
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
           String jpylizerFile = Strings.flush(Thread.currentThread().getContextClassLoader()
                                                     .getResourceAsStream("schemaInvalid.xml"));
           ResultCollector results = new ResultCollector("test", "0.1");
           validator.validate("schemaInvalid.xml",jpylizerFile,results);
           System.out.println(results.toReport());
           Assert.assertFalse(results.isSuccess());

       }
}
