package dk.statsbiblioteket.newspaper.metadatachecker;

import com.phloc.commons.io.resource.ClassPathResource;
import com.phloc.schematron.SchematronException;
import com.phloc.schematron.pure.SchematronResourcePure;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.DefaultTreeEventHandler;
import dk.statsbiblioteket.newspaper.metadatachecker.caches.DocumentCache;
import dk.statsbiblioteket.util.Strings;
import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.SchematronOutputType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Check xml data against the detailed schematron specifications. */
public class SchematronValidatorEventHandler extends DefaultTreeEventHandler {

    /** A map from file postfix to a known schema for that file. */
    private  final Map<String, String> POSTFIX_TO_XSD;
    private  final Map<String, String> POSTFIX_TO_TYPE;
    private DocumentCache documentCache;


    /** A map of parsed schemas for a given schema file name. */
    static Map<String, SchematronResourcePure> schematrons = new HashMap<>();
    /** Logger */
    private final Logger log = LoggerFactory.getLogger(getClass());
    /** The result collector results are collected in. */
    private final ResultCollector resultCollector;

    /**
     * Initialise the event handler with the collector to collect results in.
     * @param resultCollector     The collector to collect results in.
     * @param postfix_to_xsd
     * @param postfix_to_type
     *
     */
    public SchematronValidatorEventHandler(ResultCollector resultCollector, DocumentCache documentCache, Map<String, String> postfix_to_xsd, Map<String, String> postfix_to_type) {
        POSTFIX_TO_XSD = postfix_to_xsd;
        POSTFIX_TO_TYPE = postfix_to_type;
        log.debug("Initialising {}", getClass().getName());
        this.resultCollector = resultCollector;
        this.documentCache = documentCache;
    }

    public SchematronValidatorEventHandler(ResultCollector resultCollector, DocumentCache documentCache) {
        this.resultCollector = resultCollector;
        this.documentCache = documentCache;
        POSTFIX_TO_XSD = new HashMap<>();
        POSTFIX_TO_XSD.put(".alto.xml", "alto.sch");
        POSTFIX_TO_XSD.put(".mix.xml", "mix.sch");
        POSTFIX_TO_XSD.put(".mods.xml", "mods.sch");
        POSTFIX_TO_XSD.put(".edition.xml", "edition-mods.sch");
        POSTFIX_TO_XSD.put(".film.xml", "film.sch");
        POSTFIX_TO_XSD.put(".jpylyzer.xml", "sb-jp2.sch");

        POSTFIX_TO_TYPE = new HashMap<>();
        POSTFIX_TO_TYPE.put(".alto.xml", "metadata");
        POSTFIX_TO_TYPE.put(".mix.xml", "metadata");
        POSTFIX_TO_TYPE.put(".mods.xml", "metadata");
        POSTFIX_TO_TYPE.put(".edition.xml", "metadata");
        POSTFIX_TO_TYPE.put(".film.xml", "metadata");
        POSTFIX_TO_TYPE.put(".jpylyzer.xml", "jp2file");

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
        Document doc;
        try {
            doc = documentCache.getDocument(event);
            if (doc == null) {
                resultCollector.addFailure(event.getName(),
                                           "exception",
                                           getClass().getSimpleName(),
                                           "Exception parsing xml metadata",
                                           event.getName());
                return;
            }
        } catch (IOException e) {
            resultCollector.addFailure(event.getName(),
                                       "exception",
                                       getClass().getSimpleName(),
                                       "Exception reading metadata. Error was " + e.toString(),
                                       Strings.getStackTrace(e));
            return;
        }
        SchematronOutputType result;
        try {
            final SchematronResourcePure schematron = getSchematron(schematronFile);
            result = schematron.applySchematronValidation(doc);
        } catch (SchematronException e) {
            resultCollector.addFailure(event.getName(),
                                       "exception",
                                       getClass().getSimpleName(),
                                       "Schematron Exception. Error was " + e.toString(),
                                       Strings.getStackTrace(e));
            return;
        }
        for (Object o : result.getActivePatternAndFiredRuleAndFailedAssert()) {
            if (o instanceof FailedAssert) {
                FailedAssert failedAssert = (FailedAssert) o;
                String message = failedAssert.getText();
                if (message == null) {
                    message = "";
                }
                message = message.trim().replaceAll("\\s+", " ");
                resultCollector.addFailure(event.getName(),
                                           getType(event.getName()),
                                           getClass().getSimpleName(), message,
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
