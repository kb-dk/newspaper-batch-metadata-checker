package dk.statsbiblioteket.medieplatform.newspaper;

import com.phloc.commons.io.resource.ClassPathResource;
import com.phloc.schematron.SchematronException;
import com.phloc.schematron.pure.SchematronResourcePure;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.util.xml.DOM;
import org.oclc.purl.dsdl.svrl.ActivePattern;
import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.FiredRule;
import org.oclc.purl.dsdl.svrl.SchematronOutputType;
import org.oclc.purl.dsdl.svrl.SuccessfulReport;
import org.w3c.dom.Document;

import java.io.InputStream;

/**
 * Validator for Schematron. Validate the given xml against a schematron profile
 */
public class SchematronValidator implements Validator {


    private final ClassPathResource schemaResource;

    /**
     * Create a new schematron validator. Resolve the schematronPath on the classpath
     * @param schematronPath the class path to the schematron profile
     */
    public SchematronValidator(String schematronPath) {
        schemaResource =
                new ClassPathResource(schematronPath);

    }

    @Override
    public boolean validate(String reference,
                            InputStream contents,
                            ResultCollector resultCollector) {

        SchematronResourcePure schematron = new SchematronResourcePure(schemaResource);
        if (! schematron.isValidSchematron()){
            return false;
        }



        Document document = DOM.streamToDOM(contents);


        SchematronOutputType result;
        try {
            result = schematron.applySchematronValidation(document);
        } catch (SchematronException e) {
            addFailure(resultCollector, e);
            return false;
        }

        boolean success = true;
        for (Object o : result.getActivePatternAndFiredRuleAndFailedAssert()) {
            if (o instanceof FailedAssert) {
                success = false;
                FailedAssert failedAssert = (FailedAssert) o;
                //TODO find a better way to report the errors
                resultCollector.addFailure(reference,"schema","component",failedAssert.toString());
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
    }

    private void addFailure(ResultCollector resultCollector,
                            SchematronException e) {
        //To change body of created methods use File | Settings | File Templates.
    }
}
