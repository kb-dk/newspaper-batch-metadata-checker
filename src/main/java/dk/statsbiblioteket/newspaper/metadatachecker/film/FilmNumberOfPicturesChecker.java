package dk.statsbiblioteket.newspaper.metadatachecker.film;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.newspaper.metadatachecker.checker.MetadataFailureType;
import dk.statsbiblioteket.newspaper.metadatachecker.checker.XmlAttributeChecker;
import dk.statsbiblioteket.util.xml.XPathSelector;
import org.w3c.dom.Document;

public class FilmNumberOfPicturesChecker extends XmlAttributeChecker {
    private final XPathSelector xPathSelector;

    public FilmNumberOfPicturesChecker(ResultCollector resultCollector, XPathSelector xPathSelector) {
        super(resultCollector, MetadataFailureType.METADATA);
        this.xPathSelector = xPathSelector;
    }

    public void validate(AttributeParsingEvent event, Document doc) {

    }
}
