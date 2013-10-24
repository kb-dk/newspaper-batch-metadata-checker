package dk.statsbiblioteket.newspaper.metadatachecker;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.newspaper.metadatachecker.jpylyzer.SchematronAttributeValidator;
import dk.statsbiblioteket.util.Streams;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

//TODO document
public class SchematronValidatorTest {
    @Test
    public void testValidateValid()
            throws
            Exception {
        byte[] jpylizerFile = getByteArrayOutputStream(Thread.currentThread().getContextClassLoader()
                                         .getResourceAsStream("valid.xml"));
        SchematronAttributeValidator validator = new SchematronAttributeValidator("sb-jp2.sch");
        ResultCollector results = new ResultCollector("test", "0.1");
        validator.validate("valid.xml",jpylizerFile,results);
        System.out.println(results.toReport());
        Assert.assertTrue(results.isSuccess());


    }
    private byte[] getByteArrayOutputStream(InputStream jpylizerOutput)
             throws
             IOException {
         ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
         Streams.pipe(jpylizerOutput, byteStream);
         return byteStream.toByteArray();
     }


    @Test
    public void testValidateInvalid()
            throws
            Exception {
        byte[] jpylizerFile = getByteArrayOutputStream(Thread.currentThread().getContextClassLoader()
                                                  .getResourceAsStream("invalid.xml"));
        SchematronAttributeValidator validator = new SchematronAttributeValidator("sb-jp2.sch");
        ResultCollector results = new ResultCollector("test", "0.1");
        validator.validate("invalid.xml",jpylizerFile,results);
        System.out.println(results.toReport());
        Assert.assertFalse(results.isSuccess());
        //TODO assert the specific error, use String.contains

    }

}
