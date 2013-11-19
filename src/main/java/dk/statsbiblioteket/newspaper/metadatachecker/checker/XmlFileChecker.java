package dk.statsbiblioteket.newspaper.metadatachecker.checker;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.DefaultTreeEventHandler;
import dk.statsbiblioteket.util.xml.DOM;
import org.w3c.dom.Document;

import java.io.IOException;
import java.util.List;

/**
 * Implements the generic functionality for checking the content of a xml file by delegating the actual
 * detailed checks to a set of <code>XmlAttributeChecker</code>s.
 */
public abstract class XmlFileChecker extends DefaultTreeEventHandler {
    protected final ResultCollector resultCollector;
    private FailureType failureType;
    private List<XmlAttributeChecker> checkers;


    public XmlFileChecker(ResultCollector resultCollector, FailureType failureType) {
        this.resultCollector = resultCollector;
        this.failureType = failureType;
    }

    @Override
    public void handleAttribute(AttributeParsingEvent event) {
        boolean shouldCheck = false;
        for (XmlAttributeChecker xmlAttributeChecker : getCheckers()) {
            if (xmlAttributeChecker.shouldCheckEvent(event)) {
                shouldCheck = true;
                break;
            }
        }
        if (shouldCheck) {
            doValidate(event);
        }
    }

    private void doValidate(AttributeParsingEvent event) {
        Document doc;
        try {
            doc = DOM.streamToDOM(event.getData(), true);
            if (doc == null) {
                addFailure(event, "Could not parse xml from " + event.getName());
                return;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (XmlAttributeChecker checker : getCheckers()) {
            if (checker.shouldCheckEvent(event)) {
                checker.validate(event, doc);
            }
        }
    }

    private void addFailure(AttributeParsingEvent event, String description) {
        resultCollector.addFailure(
                event.getName(), failureType.value(), getClass().getName(), description);
    }

    /** Must be implemented by subclasse providing the concrete list of <code>XmlAttributeChecker</code>s. */
    protected abstract List<XmlAttributeChecker> createCheckers();

    public synchronized List<XmlAttributeChecker> getCheckers() {
        if (checkers == null) {
            checkers = createCheckers();
        }
        return checkers;
    }
}
