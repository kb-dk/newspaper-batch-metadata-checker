package dk.statsbiblioteket.newspaper.metadatachecker;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.newspaper.metadatachecker.jpylyzer.AttributeValidator;
import dk.statsbiblioteket.newspaper.metadatachecker.jpylyzer.SchemaAttributeValidator;
import dk.statsbiblioteket.util.Streams;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

//TODO document
public class SchemaValidatorTest {
    @Test
    public void testValidate()
            throws
            Exception {
        AttributeValidator attributeValidator = new SchemaAttributeValidator("jpylizer.xsd");
        byte[] jpylizerFile = getByteArrayOutputStream(Thread.currentThread().getContextClassLoader()
                                                             .getResourceAsStream("valid.xml"));
        ResultCollector results = new ResultCollector("test", "0.1");
        attributeValidator.validate("valid.xml",jpylizerFile,results);
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
    public void testSchemaInvalidValidate()
            throws
            Exception {
        AttributeValidator attributeValidator = new SchemaAttributeValidator("jpylizer.xsd");
        byte[] jpylizerFile = getByteArrayOutputStream(Thread.currentThread().getContextClassLoader()
                                                             .getResourceAsStream("schemaInvalid.xml"));
        ResultCollector results = new ResultCollector("test", "0.1");
        attributeValidator.validate("schemaInvalid.xml", jpylizerFile, results);
        System.out.println(results.toReport());
        Assert.assertFalse(results.isSuccess());
        //TODO assert the specific error, use String.contains


    }
}
