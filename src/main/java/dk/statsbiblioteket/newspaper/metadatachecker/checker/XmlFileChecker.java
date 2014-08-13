package dk.statsbiblioteket.newspaper.metadatachecker.checker;

import java.io.IOException;
import java.util.List;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.DefaultTreeEventHandler;
import dk.statsbiblioteket.newspaper.metadatachecker.caches.DocumentCache;
import dk.statsbiblioteket.util.Strings;
import org.w3c.dom.Document;

/**
 * Implements the generic functionality for checking the content of a xml file by delegating the actual
 * detailed checks to a set of <code>XmlAttributeChecker</code>s.
 */
public abstract class XmlFileChecker extends DefaultTreeEventHandler {
    protected final ResultCollector resultCollector;
    private List<XmlAttributeChecker> checkers;
    private DocumentCache documentCache;

    public XmlFileChecker(ResultCollector resultCollector, DocumentCache documentCache) {
        this.resultCollector = resultCollector;
        this.documentCache = documentCache;
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
            doc = documentCache.getDocument(event);
            if (doc == null) {
                resultCollector.addFailure(
                        event.getName(), "exception", getClass().getSimpleName(),
                        "Could not parse xml");
                return;
            }
        } catch (IOException e) {
            resultCollector.addFailure(
                    event.getName(), "exception", getClass().getSimpleName(),
                    "Unexpected error: " + e.toString(), Strings.getStackTrace(e));
            return;
        }

        for (XmlAttributeChecker checker : getCheckers()) {
            if (checker.shouldCheckEvent(event)) {
                checker.validate(event, doc);
            }
        }
    }

    /** Must be implemented by subclass providing the concrete list of <code>XmlAttributeChecker</code>s. */
    protected abstract List<XmlAttributeChecker> createCheckers();

    public synchronized List<XmlAttributeChecker> getCheckers() {
        if (checkers == null) {
            checkers = createCheckers();
        }
        return checkers;
    }

    @Override
    public void handleFinish() {
        for (XmlAttributeChecker checker: getCheckers()) {
            checker.finish();
        }
    }
}
