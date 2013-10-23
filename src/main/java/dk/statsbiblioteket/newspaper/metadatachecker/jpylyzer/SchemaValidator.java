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
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;

/** The schema validator. It validates the input against the schema given in the constructor */
public class SchemaValidator
        implements Validator {
    private final URL schemaURL;

    private final Logger log = LoggerFactory.getLogger(SchemaValidator.class);

    /**
     * Construct a new schema validator. The schema name is found on the classpath.
     *
     * @param schemaName the classpath address of the schema to use
     */
    public SchemaValidator(String schemaName) {
        schemaURL = Thread.currentThread().getContextClassLoader().getResource(schemaName);
    }

    /** Get the name of this component for error reporting purposes */
    private String getComponent() {
        return "JPylizer_schema_validator-" + getClass().getPackage().getImplementationVersion();
    }

    @Override
    public boolean validate(final String reference,
                            String xml,
                            final ResultCollector resultCollector) {
        log.debug("Validating jpylyzer profile of '{}' via schema '{}'", reference, schemaURL.getPath());
        final boolean[] valid = {true};
        try {
            Source xmlFile = new StreamSource(new StringReader(xml));
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema;

            try {
                schema = schemaFactory.newSchema(schemaURL);
            } catch (SAXException e) {
                resultCollector
                        .addFailure(reference, "framework", getComponent(), "Failed to read schema", e.toString());
                valid[0] = false;
                return valid[0];
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
        } finally {
            if (!valid[0]) {
                log.warn("Failed validation of  jpylyzer profile of '{}' via schema '{}'", reference, schemaURL.getPath());
            }
        }
    }


}
