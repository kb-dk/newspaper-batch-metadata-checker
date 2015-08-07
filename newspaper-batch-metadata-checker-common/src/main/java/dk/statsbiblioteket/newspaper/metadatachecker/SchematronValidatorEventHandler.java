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
import java.util.HashMap;
import java.util.Map;

/** Check xml data against the detailed schematron specifications. */
public class SchematronValidatorEventHandler extends DefaultTreeEventHandler {

    /** A map from file postfix to a known schema for that file. */
    private  final Map<String, AttributeSpec> attributeConfigs;
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
     * @param documentCache the cache to get the attribute xml documents from. Prevents double reads and parses of nasty xml documents
     *
     */
    public SchematronValidatorEventHandler(ResultCollector resultCollector, DocumentCache documentCache, Map<String, AttributeSpec> attributeConfigs) {
        this.attributeConfigs = attributeConfigs;
        log.debug("Initialising {}", getClass().getName());
        this.resultCollector = resultCollector;
        this.documentCache = documentCache;
    }

    @Override
    public void handleAttribute(AttributeParsingEvent event) {
        for (Map.Entry<String, AttributeSpec> entry : attributeConfigs.entrySet()) {
            if (event.getName().endsWith(entry.getKey())) {
                AttributeSpec attributeConfig = entry.getValue();
                if (attributeConfig.getSchematronFile() != null) {
                    schematronValidate(event, attributeConfig);
                    break;
                }
            }
        }
    }

    private void schematronValidate(AttributeParsingEvent event,
                                    AttributeSpec attributeSpec) {
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
            final SchematronResourcePure schematron = getSchematron(attributeSpec.getSchematronFile());
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
                                           attributeSpec.getType(),
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

}
