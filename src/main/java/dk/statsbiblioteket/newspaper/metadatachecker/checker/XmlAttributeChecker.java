package dk.statsbiblioteket.newspaper.metadatachecker.checker;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import org.w3c.dom.Document;

public abstract class XmlAttributeChecker {
    private final ResultCollector resultCollector;
    private final MetadataFailureType failureType;

    public XmlAttributeChecker(ResultCollector resultCollector, MetadataFailureType failureType ) {
        this.resultCollector = resultCollector;
        this.failureType = failureType;
    }

    protected void addFailure(AttributeParsingEvent event, String description) {
        resultCollector.addFailure(
                event.getName(), failureType.value(), getClass().getName(), description);
    }

    public abstract void validate(AttributeParsingEvent event, Document doc);
}
