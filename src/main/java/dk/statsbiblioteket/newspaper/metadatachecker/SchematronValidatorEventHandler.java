package dk.statsbiblioteket.newspaper.metadatachecker;

import com.phloc.commons.io.resource.ClassPathResource;
import com.phloc.schematron.SchematronException;
import com.phloc.schematron.pure.SchematronResourcePure;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.DefaultTreeEventHandler;
import dk.statsbiblioteket.util.Strings;
import dk.statsbiblioteket.util.xml.DOM;
import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.SchematronOutputType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Check xml data against the detailed schematron specifications. */
public class SchematronValidatorEventHandler extends DefaultTreeEventHandler {

    /** A map from file postfix to a known schema for that file. */
    private static final Map<String, String> POSTFIX_TO_XSD;
    private static final Map<String, String> POSTFIX_TO_TYPE;

    /**
     * Statically initialise the Map to the hardcoded names of the schematron files.
     */
    static {  //TODO uncomment these as they are created
        Map<String, String> postfixToSch = new HashMap<>();
        //postfixToSch.put(".alto.xml", "alto.sch");
        //postfixToSch.put(".mix.xml", "mix.sch");
        postfixToSch.put(".mods.xml", "mods.sch");
        postfixToSch.put(".edition.xml", "edition-mods.sch");
        //postfixToSch.put(".film.xml", "film.sch");
        postfixToSch.put(".jpylyzer.xml", "sb-jp2.sch");
        POSTFIX_TO_XSD = Collections.unmodifiableMap(postfixToSch);

        Map<String, String> postfixToType = new HashMap<>();
        postfixToType.put(".alto.xml", "metadata");
        postfixToType.put(".mix.xml", "metadata");
        postfixToType.put(".mods.xml", "metadata");
        postfixToType.put(".edition.xml", "metadata");
        postfixToType.put(".film.xml", "metadata");
        postfixToType.put(".jpylyzer.xml", "jp2file");
        POSTFIX_TO_TYPE = Collections.unmodifiableMap(postfixToType);
    }

    /** A map of parsed schemas for a given schema file name. */
    static Map<String, SchematronResourcePure> schematrons = new HashMap<>();
    /** Logger */
    private final Logger log = LoggerFactory.getLogger(getClass());
    /** The result collector results are collected in. */
    private final ResultCollector resultCollector;

    /**
     * Initialise the event handler with the collector to collect results in.
     *
     * @param resultCollector     The collector to collect results in.
     * @param controlPoliciesPath path to the control policies. If null, use default control policies
     */
    public SchematronValidatorEventHandler(ResultCollector resultCollector,
                                           String controlPoliciesPath) {
        log.debug("Initialising {}", getClass().getName());
        this.resultCollector = resultCollector;

        Document controlPoliciesDocument;
        if (controlPoliciesPath != null) {
            try {
                controlPoliciesDocument = DOM.streamToDOM(new FileInputStream(controlPoliciesPath));
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Failed to load control policies from '" + controlPoliciesPath + "'", e);
            }
        } else {
            controlPoliciesDocument = DOM.streamToDOM(Thread.currentThread().getContextClassLoader()
                                                            .getResourceAsStream("defaultControlPolicies.xml"));
        }
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

    private void schematronValidate(AttributeParsingEvent event,
                                    String schematronFile) {
        Document doc = null;
        try {
            doc = DOM.streamToDOM(event.getData());
            if (doc == null) {
                resultCollector.addFailure(event.getName(),
                                           getType(event.getName()),
                                           getClass().getName(),
                                           "Exception parsing xml metadata from " + event.getName(),
                                           event.getName());
                return;
            }
        } catch (IOException e) {
            resultCollector.addFailure(event.getName(),
                                       getType(event.getName()),
                                       getClass().getName(),
                                       "Exception reading metadata. Error was " + e.toString(),
                                       Strings.getStackTrace(e));
            return;
        }
        SchematronOutputType result = null;
        try {
            final SchematronResourcePure schematron = getSchematron(schematronFile);
            result = schematron.applySchematronValidation(doc);
        } catch (SchematronException e) {
            resultCollector.addFailure(event.getName(),
                                       "schematron",
                                       getClass().getName(),
                                       "Schematron Exception. Error was " + e.toString(),
                                       Strings.getStackTrace(e));
            return;
        }
        for (Object o : result.getActivePatternAndFiredRuleAndFailedAssert()) {
            if (o instanceof FailedAssert) {
                FailedAssert failedAssert = (FailedAssert) o;
                resultCollector.addFailure(event.getName(),
                                           getType(event.getName()),
                                           getClass().getName(),
                                           failedAssert.getText(),
                                           "Location: '" + failedAssert.getLocation() + "'",
                                           "Test: '" + failedAssert.getTest() + "'");
            }
        }
    }

    /**
     * Lazy initialiser for the Schematron instances we need.
     *
     * @param schematronFile the file to read from.
     *
     * @return The resulting Schematron.
     */
    private SchematronResourcePure getSchematron(String schematronFile) {
        if (schematrons.get(schematronFile) == null) {
            ClassPathResource schemaResource = new ClassPathResource(schematronFile);
            schematrons.put(schematronFile, new SchematronResourcePure(schemaResource));
        }
        return schematrons.get(schematronFile);
    }

    private String getType(String name) {
        for (Map.Entry<String, String> stringStringEntry : POSTFIX_TO_TYPE.entrySet()) {
            if (name.endsWith(stringStringEntry.getKey())) {
                return stringStringEntry.getValue();
            }
        }
        return null;
    }


}
