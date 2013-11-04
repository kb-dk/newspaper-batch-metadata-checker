package dk.statsbiblioteket.newspaper.metadatachecker;

import com.phloc.commons.io.resource.ClassPathResource;
import com.phloc.schematron.SchematronException;
import com.phloc.schematron.pure.SchematronResourcePure;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.DefaultTreeEventHandler;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.TreeEventHandler;
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

import javax.xml.validation.Schema;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Check xml data against the detailed schematron specifications.
 */
public class SchematronValidatorEventHandler extends DefaultTreeEventHandler {

    /** Logger */
    private final Logger log = LoggerFactory.getLogger(getClass());

    /** A map from file postfix to a known schema for that file. */
    private static final Map<String, String> POSTFIX_TO_XSD;

    /**
     * Statically initialise the Map to the hardcoded names of the schematron files.
     */
    static {  //TODO uncomment these as they are created
        Map<String, String> postfixToSch = new HashMap<>(5);
        //postfixToSch.put(".alto.xml", "alto.sch");
        //postfixToSch.put(".mix.xml", "mix.sch");
        postfixToSch.put(".mods.xml", "mods.sch");
        postfixToSch.put(".edition.xml", "edition-mods.sch");
        //postfixToSch.put(".film.xml", "film.sch");
        POSTFIX_TO_XSD = Collections.unmodifiableMap(postfixToSch);
    }
    /** The result collector results are collected in. */
    private final ResultCollector resultCollector;

    /** A map of parsed schemas for a given schema file name. */
    static Map<String, SchematronResourcePure> schematrons = new HashMap<>();

    /**
     * Initialise the event handler with the collector to collect results in.
     * @param resultCollector The collector to collect results in.
     */
    public SchematronValidatorEventHandler(ResultCollector resultCollector) {
        log.debug("Initialising {}", getClass().getName());
        this.resultCollector = resultCollector;
    }

    @Override
    public void handleAttribute(AttributeParsingEvent event) {
        for (Map.Entry<String, String> entry : POSTFIX_TO_XSD.entrySet()) {
               if (event.getName().endsWith(entry.getKey())) {
                   schematronValidate(event, entry.getValue());
                   break;
               }
           }
    }

    private void schematronValidate(AttributeParsingEvent event, String schematronFile) {
        Document doc = null;
        try {
            doc = DOM.streamToDOM(event.getData());
            if (doc == null) {
               resultCollector.addFailure(
                       event.getName(),
                       "metadata",
                       getClass().getName(),
                       "Exception parsing xml metadata from " + event.getName(),
                       event.getName()
               );
            return;
            }
        } catch (IOException e) {
            resultCollector.addFailure(
                    event.getName(),
                    "metadata",
                    getClass().getName(),
                    "Exception reading metadata. Error was " + e
                            .toString(),
                    Strings.getStackTrace(e)
            );
            return;
        }
        SchematronOutputType result = null;
        try {
            final SchematronResourcePure schematron = getSchematron(schematronFile);
            result = schematron.applySchematronValidation(doc);
        } catch (SchematronException e) {
            resultCollector.addFailure(
                    event.getName(),
                    "schematron",
                    getClass().getName(),
                    "Schematron Exception. Error was " + e
                            .toString(),
                    Strings.getStackTrace(e)
            );
            return;
        }
        for (Object o : result.getActivePatternAndFiredRuleAndFailedAssert()) {
            if (o instanceof FailedAssert) {
                FailedAssert failedAssert = (FailedAssert) o;
                resultCollector.addFailure(event.getName(),
                        "metadata",
                        getClass().getName(),
                        failedAssert.getText(),
                        "Location: '" + failedAssert.getLocation() + "'",
                        "Test: '" + failedAssert.getTest() + "'");
            }
        }
    }

    /**
     * Lazy initialiser for the Schematron instances we need.
     * @param schematronFile the file to read from.
     * @return The resulting Schematron.
     */
    private SchematronResourcePure getSchematron(String schematronFile) {
        if (schematrons.get(schematronFile) == null) {
            ClassPathResource schemaResource = new ClassPathResource(schematronFile);
            schematrons.put(schematronFile, new SchematronResourcePure(schemaResource));
        }
        return schematrons.get(schematronFile);
    }

}
