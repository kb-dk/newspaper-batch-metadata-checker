package dk.statsbiblioteket.newspaper.metadatachecker.jpylyzer;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;

/** The schema validator. It validates the input against the schema given in the constructor */
public class SchemaAttributeValidator
        implements AttributeValidator {
    private static final String TYPE = "jp2file";
    private final URL schemaURL;

    private final Logger log = LoggerFactory.getLogger(SchemaAttributeValidator.class);
    private final Schema schema;

    /**
     * Construct a new schema validator. The schema name is found on the classpath.
     *
     * @param schemaName the classpath address of the schema to use
     */
    public SchemaAttributeValidator(String schemaName) {
        schemaURL = Thread.currentThread().getContextClassLoader().getResource(schemaName);
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);


        try {
            schema = schemaFactory.newSchema(schemaURL);
        } catch (SAXException e) {
            log.error("Failed to parse this schema '{}'",schemaURL,e);
            throw new RuntimeException(e);
        }

    }

    /** Get the name of this component for error reporting purposes */
    private String getComponent() {
        return getClass().getName() + "-" + getClass().getPackage().getImplementationVersion();
    }

    @Override
    public boolean validate(final String reference,
                            byte[] xml,
                            final ResultCollector resultCollector) {
        log.debug("Validating jpylyzer profile of '{}' via schema '{}'", reference, schemaURL.getPath());
        final boolean[] valid = {true};
        try {
            Source xmlFile = new StreamSource(new ByteArrayInputStream(xml));

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
                        resultCollector.addFailure(reference, TYPE, getComponent(), exception.toString());
                        valid[0] = false;

                    }

                    @Override
                    public void fatalError(SAXParseException exception)
                            throws
                            SAXException {
                        resultCollector.addFailure(reference, TYPE, getComponent(), exception.toString());
                        valid[0] = false;

                    }
                });
                validator.validate(xmlFile);
            } catch (SAXException e) {
                //From validator.validate javadoc: SAXException - If the ErrorHandler throws a SAXException or if a fatal error is found and the ErrorHandler returns normally.
                //ignore, the error have already been logged as a fatal error
            } catch (IOException e) {
                resultCollector.addFailure(reference, TYPE, getComponent(), e.toString());
                valid[0] = false;
            }

            return valid[0];
        } finally {
            if (!valid[0]) {
                log.warn("Failed validation of  jpylyzer profile of '{}' via schema '{}'", reference, schemaURL.getPath());
            }
        }
    }


}
