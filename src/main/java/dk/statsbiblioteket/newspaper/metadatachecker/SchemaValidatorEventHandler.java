package dk.statsbiblioteket.newspaper.metadatachecker;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.DefaultTreeEventHandler;
import dk.statsbiblioteket.newspaper.metadatachecker.caches.DocumentCache;
import dk.statsbiblioteket.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Check xml data of known file postfixes against xsd schemas. */
public class SchemaValidatorEventHandler extends DefaultTreeEventHandler {
    /** A map from file postfix to a known schema for that file. */
    private static final Map<String, String> POSTFIX_TO_XSD;
    private static final Map<String, String> POSTFIX_TO_TYPE;
    private static final Map<String, String> POSTFIX_TO_MESSAGE_PREFIX;
    private DocumentCache documentCache;

    static {
        Map<String, String> postfixToXsd = new HashMap<>();
        postfixToXsd.put(".alto.xml", "alto-v2.0.xsd");
        postfixToXsd.put(".mix.xml", "mix.xsd");
        postfixToXsd.put(".mods.xml", "mods-3-1.xsd");
        postfixToXsd.put(".edition.xml", "mods-3-1.xsd");
        postfixToXsd.put(".film.xml", "film.xsd");
        postfixToXsd.put(".jpylyzer.xml", "jpylyzer.xsd");
        POSTFIX_TO_XSD = Collections.unmodifiableMap(postfixToXsd);

        Map<String, String> postfixToType = new HashMap<>();
        postfixToType.put(".alto.xml", "metadata");
        postfixToType.put(".mix.xml", "metadata");
        postfixToType.put(".mods.xml", "metadata");
        postfixToType.put(".edition.xml", "metadata");
        postfixToType.put(".film.xml", "metadata");
        postfixToType.put(".jpylyzer.xml", "jp2file");
        POSTFIX_TO_TYPE = Collections.unmodifiableMap(postfixToType);

        Map<String, String> postfixToMessagePrefix = new HashMap<>();
        postfixToMessagePrefix.put(".alto.xml", "2J: ");
        postfixToMessagePrefix.put(".mix.xml", "2K: ");
        postfixToMessagePrefix.put(".mods.xml", "2C: ");
        postfixToMessagePrefix.put(".edition.xml", "2D: ");
        postfixToMessagePrefix.put(".film.xml", "2E: ");
        postfixToMessagePrefix.put(".jpylyzer.xml", "2B: ");
        POSTFIX_TO_MESSAGE_PREFIX = Collections.unmodifiableMap(postfixToMessagePrefix);
    }

    /** Logger */
    private final Logger log = LoggerFactory.getLogger(getClass());
    /** The result collector results are collected in. */
    private final ResultCollector resultCollector;
    /** A map of parsed schemas for a given schema file name. */
    private Map<String, Schema> schemas = new HashMap<>();

    /**
     * Initialise the event handler with the collector to collect results in.
     *
     * @param resultCollector The collector to collect results in.
     */
    public SchemaValidatorEventHandler(ResultCollector resultCollector, DocumentCache documentCache) {
        log.debug("Initialising {}", getClass().getName());
        this.resultCollector = resultCollector;
        this.documentCache = documentCache;
    }

    @Override
    public void handleNodeBegin(NodeBeginsParsingEvent event) {
        // Do nothing
    }

    @Override
    public void handleNodeEnd(NodeEndParsingEvent event) {
        // Do nothing
    }

    @Override
    /**
     * For each attribute, if this is a known XML file postfix, check the appropriate schema for that XML file.
     * @event The attribute parsing event that is to be checked.
     */
    public void handleAttribute(AttributeParsingEvent event) {
        for (Map.Entry<String, String> entry : POSTFIX_TO_XSD.entrySet()) {
            if (event.getName().endsWith(entry.getKey())) {
                checkSchema(event, entry.getValue());
                break;
            }
        }
    }

    /**
     * Given an attribute parsing event and a schema file name, extract the data from the event, and validate it
     * against
     * the schema.
     *
     * @param event      The attribute parsing event containing the data.
     * @param schemaFile The file name of the schema to check the data against.
     */
    private void checkSchema(AttributeParsingEvent event,
                             String schemaFile) {
        log.debug("Checking '{}' with schema '{}'", event.getName(), schemaFile);

            try {
                InputStream data = event.getData();
                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

                documentBuilderFactory.setSchema(getSchema(schemaFile));
                documentBuilderFactory.setNamespaceAware(true);
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                documentBuilder.setErrorHandler(new ErrorHandler() {
                    @Override
                    public void warning(SAXParseException exception) throws SAXException {
                    }

                    @Override
                    public void error(SAXParseException exception) throws SAXException {
                        throw exception;
                    }

                    @Override
                    public void fatalError(SAXParseException exception) throws SAXException {
                        throw exception;
                    }
                });

                Document doc = documentBuilder.parse(data);
                documentCache.cacheDocument(event, doc);

        } catch (SAXParseException e) {
            resultCollector.addFailure(event.getName(),
                                       getType(event.getName()),
                                       getClass().getSimpleName(),
                                       getMessagePrefix(event.getName()) + "Failure validating XML data: Line " +
                                               e.getLineNumber() + " Column " + e.getColumnNumber() + ": " + e
                                               .getMessage());
            log.debug("Error validating '{}' with schema '{}': Line {} Column {}: {}",
                      event.getName(),
                      schemaFile,
                      e.getLineNumber(),
                      e.getColumnNumber(),
                      e.getMessage(),
                      e);
        } catch (SAXException e) {
            resultCollector.addFailure(event.getName(),
                                       "exception",
                                       getClass().getSimpleName(),
                                       getMessagePrefix(
                                               event.getName()) + "Failure validating XML data: " + e.toString(),
                                       Strings.getStackTrace(e));
            log.debug("Error validating '{}' with schema '{}': {}", event.getName(), schemaFile, e.getMessage(), e);
        } catch (IOException e) {
            resultCollector.addFailure(event.getName(),
                                       "exception",
                                       getClass().getSimpleName(),
                                       getMessagePrefix(event.getName()) + "Failure reading data: " + e.toString(),
                                       Strings.getStackTrace(e));
            log.debug("IO error reading '{}' while validating with schema '{}'", event.getName(), schemaFile, e);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            resultCollector.addFailure(event.getName(),
                                       "exception",
                                       getClass().getSimpleName(),
                                       getMessagePrefix(event.getName()) + "Unexpected failure processing data from '" + event.getName() + "': " +
                                       e.toString(),
                                       sw.toString());
            log.error("Unexpected error while validating '{}' with schema '{}'", event.getName(), schemaFile, e);
        }
    }

    private String getType(String name) {
        for (Map.Entry<String, String> stringStringEntry : POSTFIX_TO_TYPE.entrySet()) {
            if (name.endsWith(stringStringEntry.getKey())) {
                return stringStringEntry.getValue();
            }
        }
        return null;
    }

    private String getMessagePrefix(String name) {
        for (Map.Entry<String, String> stringStringEntry : POSTFIX_TO_MESSAGE_PREFIX.entrySet()) {
            if (name.endsWith(stringStringEntry.getKey())) {
                return stringStringEntry.getValue();
            }
        }
        return "";
    }

    /**
     * Create a new validator for the schema in the given schema file. Note: Validators are not thread safe!
     *
     * @param schemaFile The file name of the schema to get a validator for.
     *
     * @return A validator for the given schema.
     * @throws SAXException If the schema fails to parse.
     */
    private Validator createValidator(String schemaFile) throws SAXException {
        Schema schema = getSchema(schemaFile);
        return schema.newValidator();
    }

    /**
     * Given a schema file name, get a parsed version of the schema from the classpath. Note that parsed schemas are
     * cached.
     *
     * @param schemaFile The filename of the schema.
     *
     * @return The parsed schema.
     * @throws SAXException If the schema fails to parse.
     */
    private synchronized Schema getSchema(String schemaFile) throws SAXException {
        if (schemas.get(schemaFile) == null) {
            log.debug("Cache miss for schema file {}", schemaFile);
            long start = System.currentTimeMillis();
            URL schemaUrl = getClass().getClassLoader().getResource(schemaFile);
            Schema schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(schemaUrl);
            schemas.put(schemaFile, schema);
            log.debug("Loaded schema {} in {} ms", schemaFile, System.currentTimeMillis() - start);
        }
        return schemas.get(schemaFile);
    }

    @Override
    public void handleFinish() {
        // Do nothing
    }
}
