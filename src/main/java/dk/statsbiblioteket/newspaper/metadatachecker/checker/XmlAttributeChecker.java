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

    public XmlAttributeChecker(ResultCollector resultCollector, FailureType failureType) {
        this.resultCollector = resultCollector;
        this.failureType = failureType;
    }

    protected void addFailure(AttributeParsingEvent event, String description) {
        resultCollector.addFailure(
                event.getName(), failureType.value(), getClass().getName(), description);
    }

    public abstract void validate(AttributeParsingEvent event, Document doc);


    /**
     * Indicates whether the encountered event is relevant to check.
     */
    public boolean shouldCheckEvent(AttributeParsingEvent event){
        return true;
    }
}
