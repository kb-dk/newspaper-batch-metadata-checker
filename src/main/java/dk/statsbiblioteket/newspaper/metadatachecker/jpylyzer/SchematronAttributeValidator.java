package dk.statsbiblioteket.newspaper.metadatachecker.jpylyzer;

import com.phloc.commons.io.resource.ClassPathResource;
import com.phloc.schematron.SchematronException;
import com.phloc.schematron.pure.SchematronResourcePure;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.util.Strings;
import dk.statsbiblioteket.util.xml.DOM;
import org.oclc.purl.dsdl.svrl.ActivePattern;
import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.FiredRule;
import org.oclc.purl.dsdl.svrl.SchematronOutputType;
import org.oclc.purl.dsdl.svrl.SuccessfulReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;

/** Validator for Schematron. Validate the given xml against a schematron profile */
public class SchematronAttributeValidator
        implements AttributeValidator {

    private static final String TYPE = "jp2file";
    private final Logger log = LoggerFactory.getLogger(SchematronAttributeValidator.class);
    private final ClassPathResource schemaResource;
    private final SchematronResourcePure schematron;

    /**
     * Create a new schematron validator. Resolve the schematronPath on the classpath
     *
     * @param schematronPath the class path to the schematron profile
     */
    public SchematronAttributeValidator(String schematronPath) {
        schemaResource = new ClassPathResource(schematronPath);
        schematron = new SchematronResourcePure(schemaResource);
    }

    @Override
    public boolean validate(String reference,
                            byte[] contents,
                            ResultCollector resultCollector) {

        log.debug("Validating contents of '{}' via schematron '{}'", reference, schemaResource.getPath());

        boolean success = true;
        try {

            //TODO move to constructor and runtime exception
            if (!schematron.isValidSchematron()) {
                success = false;
                return success;
            }

            Document document = DOM.streamToDOM(new ByteArrayInputStream(contents));


            SchematronOutputType result;
            try {
                result = schematron.applySchematronValidation(document);
            } catch (SchematronException e) {
                resultCollector.addFailure(reference,
                                           TYPE,
                                           getComponent(),
                                           "Failed to validate jpylyzer output against schematron. Error was " + e
                                                   .toString(),
                                           Strings.getStackTrace(e));
                success = false;
                return success;
            }

            for (Object o : result.getActivePatternAndFiredRuleAndFailedAssert()) {
                if (o instanceof FailedAssert) {
                    success = false;
                    FailedAssert failedAssert = (FailedAssert) o;
                    resultCollector.addFailure(reference, TYPE,
                                               getComponent(),
                                               failedAssert.getText(),
                                               "Location: '"+failedAssert.getLocation()+"'",
                                               "Test: '"+failedAssert.getTest()+"'");
                }
                else if (o instanceof ActivePattern) {
                    ActivePattern activePattern = (ActivePattern) o;
                    //do nothing
                }
                else if (o instanceof FiredRule) {
                    FiredRule firedRule = (FiredRule) o;
                    //a rule that was run
                }
                else if (o instanceof SuccessfulReport) {
                    SuccessfulReport successfulReport = (SuccessfulReport) o;
                    //ever?
                } else {
                    //unknown type of o.
                    throw new RuntimeException("Unknown result from schematron library: "+o.getClass().getName());
                }
            }
            return success;
        } finally {
            if (!success) {
                log.warn("Failed validation of '{}' via schematron '{}'", reference, schemaResource.getPath());
            }

        }
    }

    /**
     * Get the name of this component for error reporting purposes
     *
     * @return
     */
    private String getComponent() {
        return getClass().getName() + "-" + getClass().getPackage().getImplementationVersion();
    }

}
