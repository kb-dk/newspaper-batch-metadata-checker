package dk.statsbiblioteket.newspaper.metadatachecker.film;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.newspaper.metadatachecker.checker.FailureType;
import dk.statsbiblioteket.newspaper.metadatachecker.checker.XmlAttributeChecker;
import dk.statsbiblioteket.newspaper.metadatachecker.checker.XmlFileChecker;
import dk.statsbiblioteket.newspaper.mfpakintegration.batchcontext.BatchContext;
import dk.statsbiblioteket.util.xml.DOM;
import dk.statsbiblioteket.util.xml.XPathSelector;

/**
 * Handles the checking of film.xml files. The concrete checks are delegated to the individual film attribute
 * checker.
 */
public class FilmXmlChecker extends XmlFileChecker {


    private List<XmlAttributeChecker> checkers;

    public FilmXmlChecker(ResultCollector resultCollector, BatchContext context, Document batchXmlStructure) {
        super(resultCollector);
        XPathSelector xpathSelector = DOM.createXPathSelector("avis",
                "http://www.statsbiblioteket.dk/avisdigitalisering/microfilm/1/0/");
        checkers = new ArrayList<>();
        checkers.add(new FilmNumberOfPicturesChecker(resultCollector, xpathSelector, batchXmlStructure));
        checkers.add(new FilmDateVsEditionsChecker(resultCollector, xpathSelector, batchXmlStructure));
        checkers.add(new FilmIDAgainstFilmNodenameChecker(resultCollector, xpathSelector));
        checkers.add(new FilmNewspaperTitlesChecker(resultCollector, FailureType.METADATA, context));
        checkers.add(new FilmDateRangeAgainstMfpakChecker(resultCollector, context));
    }

    @Override
    protected List<XmlAttributeChecker> createCheckers() {
        return checkers;
    }


}
