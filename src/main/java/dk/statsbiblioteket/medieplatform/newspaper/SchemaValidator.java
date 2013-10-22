package dk.statsbiblioteket.medieplatform.newspaper;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class SchemaValidator
        implements Validator {
    private final URL schemaURL;


    public SchemaValidator() {
        schemaURL = Thread.currentThread().getContextClassLoader().getResource("jpylizer.xsd");
    }

    private String getComponent() {
        return "JPylizer_schema_validator-"+ getClass().getPackage().getImplementationVersion();
    }

    @Override
    public boolean validate(final String reference,
                            InputStream xml,
                            final ResultCollector resultCollector) {
        Source xmlFile = new StreamSource(xml);
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = null;
        final boolean[] valid = {true};
        try {
            schema = schemaFactory.newSchema(schemaURL);
        } catch (SAXException e) {
            resultCollector.addFailure(reference, "framework", getComponent(), "Failed to read schema", e.toString());
            return false;
        }

        try {
            javax.xml.validation.Validator validator = schema.newValidator();
            validator.setErrorHandler(new ErrorHandler() {
                @Override
                public void warning(SAXParseException exception)
                        throws
                        SAXException {
                    //Ignore
                }

                @Override
                public void error(SAXParseException exception)
                        throws
                        SAXException {
                    resultCollector.addFailure(reference, "schemaCheck", getComponent(), exception.toString());
                    valid[0] = false;

                }

                @Override
                public void fatalError(SAXParseException exception)
                        throws
                        SAXException {
                    resultCollector.addFailure(reference, "schemaCheck", getComponent(), exception.toString());
                    valid[0] = false;
                }
            });
            validator.validate(xmlFile);
        } catch (SAXException e) {
            //ignore, the error have already been logged as a fatal error
        } catch (IOException e) {
            resultCollector.addFailure(reference, "schemaCheck", getComponent(), e.toString());
            valid[0] = false;
        }

        return valid[0];
    }




}
