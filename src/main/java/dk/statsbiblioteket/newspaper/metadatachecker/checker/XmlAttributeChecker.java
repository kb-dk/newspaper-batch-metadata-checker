package dk.statsbiblioteket.newspaper.metadatachecker.checker;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.util.xml.DOM;
import dk.statsbiblioteket.util.xml.XPathSelector;
import org.w3c.dom.Document;

/**
 * Contains the general functionality for testing a event against a xml document (metadata xml file).
 * The concrete subclasses will implement the actual business logic. <p>
 *
 * This enables check to be broken down into small focused checks.  </p>
 */
public abstract class XmlAttributeChecker {
    protected static final XPathSelector XPATH = DOM.createXPathSelector(
            "mix",
            "http://www.loc.gov/mix/v20",
            "avis",
            "http://www.statsbiblioteket.dk/avisdigitalisering/microfilm/1/0/");
    private final ResultCollector resultCollector;
    private final FailureType failureType;

    /**
     * @param resultCollector The result collector to used for logging failures.
     * @param failureType The specific type of failure to register.
     */
    public XmlAttributeChecker(ResultCollector resultCollector, FailureType failureType) {
        this.resultCollector = resultCollector;
        this.failureType = failureType;
    }

    protected void addFailure(AttributeParsingEvent event, String description) {
        resultCollector.addFailure(
                event.getName(), failureType.value(), getClass().getName(), description);
    }

    /**
     * Makes the relevant checks for the concrete checker based on the supplied event and doc.
     * @param event The event to base the check on.
     * @param doc The xml document representing the metadata for the event (if any). The document has been generate
     *            based on the event data, but is pregenerated and injected to avoid every
     *            <code>XmlAttributeChecker</code> having to do this.
     */
    public abstract void validate(AttributeParsingEvent event, Document doc);


    /**
     * Indicates whether the encountered event is relevant to check.
     */
    public abstract boolean shouldCheckEvent(AttributeParsingEvent event);
}
