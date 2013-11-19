package dk.statsbiblioteket.newspaper.metadatachecker.film;

import java.util.ArrayList;
import java.util.List;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.newspaper.metadatachecker.checker.XmlAttributeChecker;
import dk.statsbiblioteket.newspaper.metadatachecker.checker.XmlFileChecker;
import dk.statsbiblioteket.util.xml.DOM;
import dk.statsbiblioteket.util.xml.XPathSelector;

public class FilmXmlChecker extends XmlFileChecker {
    private List<XmlAttributeChecker> checkers;

    public FilmXmlChecker(ResultCollector resultCollector) {
        super(resultCollector);

        XPathSelector xpathSelector = DOM.createXPathSelector("avis",
                "http://www.statsbiblioteket.dk/avisdigitalisering/microfilm/1/0/");
        checkers = new ArrayList<>();
        checkers.add(new FilmNumberOfPicturesChecker(resultCollector, xpathSelector));
    }

    @Override
    protected List<XmlAttributeChecker> getCheckers() {
        return checkers;
    }

    @Override
    protected boolean shouldCheckEvent(AttributeParsingEvent event) {
        return event.getName().endsWith(".film.xml");
    }
}
