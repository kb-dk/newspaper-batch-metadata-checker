package dk.statsbiblioteket.newspaper.metadatachecker.film;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.newspaper.metadatachecker.checker.FailureType;
import dk.statsbiblioteket.newspaper.metadatachecker.checker.XmlAttributeChecker;
import dk.statsbiblioteket.newspaper.metadatachecker.checker.XmlFileChecker;
import dk.statsbiblioteket.util.xml.DOM;
import dk.statsbiblioteket.util.xml.XPathSelector;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;

public class FilmXmlChecker extends XmlFileChecker {
    private List<XmlAttributeChecker> checkers;

    public FilmXmlChecker(ResultCollector resultCollector, Document batchXmlStructure) {
        super(resultCollector, FailureType.METADATA);

        XPathSelector xpathSelector = DOM.createXPathSelector("avis",
                "http://www.statsbiblioteket.dk/avisdigitalisering/microfilm/1/0/");
        checkers = new ArrayList<>();
        checkers.add(new FilmNumberOfPicturesChecker(resultCollector, xpathSelector, batchXmlStructure));
        checkers.add(new FilmDateVsEditionsChecker(resultCollector, xpathSelector, batchXmlStructure));
    }

    @Override
    protected List<XmlAttributeChecker> createCheckers() {
        return checkers;
    }


}
