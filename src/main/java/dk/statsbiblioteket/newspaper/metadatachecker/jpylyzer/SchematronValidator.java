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

/** Validator for Schematron. Validate the given xml against a schematron profile */
public class SchematronValidator
        implements Validator {

    private final Logger log = LoggerFactory.getLogger(SchematronValidator.class);
    private final ClassPathResource schemaResource;

    /**
     * Create a new schematron validator. Resolve the schematronPath on the classpath
     *
     * @param schematronPath the class path to the schematron profile
     */
    public SchematronValidator(String schematronPath) {
        schemaResource = new ClassPathResource(schematronPath);

    }

    @Override
    public boolean validate(String reference,
                            String contents,
                            ResultCollector resultCollector) {

        log.debug("Validating contents of {} via schematron {}", reference, schemaResource.getPath());

        boolean success = true;
        try {
            SchematronResourcePure schematron = new SchematronResourcePure(schemaResource);
            if (!schematron.isValidSchematron()) {
                success = false;
                return success;
            }

            Document document = DOM.stringToDOM(contents);


            SchematronOutputType result;
            try {
                result = schematron.applySchematronValidation(document);
            } catch (SchematronException e) {
                resultCollector
                        .addFailure(reference, "exception", getComponent(), e.getMessage(), Strings.getStackTrace(e));
                success = false;
                return success;
            }

            for (Object o : result.getActivePatternAndFiredRuleAndFailedAssert()) {
                if (o instanceof FailedAssert) {
                    success = false;
                    FailedAssert failedAssert = (FailedAssert) o;
                    resultCollector.addFailure(reference,
                                               "jp2file",
                                               getComponent(),
                                               failedAssert.getText(),
                                               failedAssert.getLocation(),
                                               failedAssert.getTest());
                }
                if (o instanceof ActivePattern) {
                    ActivePattern activePattern = (ActivePattern) o;
                    //do nothing
                }
                if (o instanceof FiredRule) {
                    FiredRule firedRule = (FiredRule) o;
                    //a rule that was run
                }
                if (o instanceof SuccessfulReport) {
                    SuccessfulReport successfulReport = (SuccessfulReport) o;
                    //ever?
                }
            }
            return success;
        } finally {
            if (!success) {
                log.warn("Failed validation of {} via schematron {}", reference, schemaResource.getPath());
            }

        }
    }

    /**
     * Get the name of this component for error reporting purposes
     *
     * @return
     */
    private String getComponent() {
        return "JPylizer_content_validator-" + getClass().getPackage().getImplementationVersion();
    }

}
