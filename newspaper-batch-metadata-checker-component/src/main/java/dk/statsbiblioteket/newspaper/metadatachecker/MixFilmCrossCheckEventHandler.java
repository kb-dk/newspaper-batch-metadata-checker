package dk.statsbiblioteket.newspaper.metadatachecker;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.DefaultTreeEventHandler;
import dk.statsbiblioteket.newspaper.metadatachecker.caches.DocumentCache;
import dk.statsbiblioteket.util.xml.DOM;
import dk.statsbiblioteket.util.xml.XPathSelector;

import org.w3c.dom.Document;

import java.io.IOException;

import static dk.statsbiblioteket.util.Strings.getStackTrace;

/** This class tests that the mix and film files agree on the sampling frequency */
public class MixFilmCrossCheckEventHandler extends DefaultTreeEventHandler {
    private final ResultCollector resultCollector;
    private final XPathSelector xpath;
    private String currentFilmNode = null;
    private Integer filmXmlSamplingFreq = null;
    private DocumentCache documentCache;

    public MixFilmCrossCheckEventHandler(ResultCollector resultCollector, DocumentCache documentCache) {
        this.resultCollector = resultCollector;
        this.documentCache = documentCache;

        xpath = DOM.createXPathSelector(
                "avis", "http://www.statsbiblioteket.dk/avisdigitalisering/microfilm/1/0/", 
                "mix", "http://www.loc.gov/mix/v20");

    }

    /**
     * Checks the begin even to see if we are in a film dir, if so enables checking
     *
     * @param event
     */
    @Override
    public void handleNodeBegin(NodeBeginsParsingEvent event) {
        if(currentFilmNode == null) {
            if(isFilmNode(event)) {
                currentFilmNode = event.getName();
            }
        }
    }

    /**
     * Checks to see if we are leaving a known film dir, resets state if we are
     *
     * @param event
     */
    @Override
    public void handleNodeEnd(NodeEndParsingEvent event) {
       if(currentFilmNode != null) {
            if(currentFilmNode.equals(event.getName())) {
                currentFilmNode = null;
            }
        }
    }

    /**
     * Checks if we are in a film dir, if so looks for AttributeParsingEvents that concerns 
     * film.xml and mix.xml files. 
     * If a film.xml file is encountered extracts it's declared sampling frequency
     * If a mix.xml file is encountered compares it's declared sampling frequency with the one found in film.
     * 
     * @param event 
     */
    @Override
    public void handleAttribute(AttributeParsingEvent event) {
        if(currentFilmNode != null) {
            if(event.getName().endsWith(".film.xml")) {
                setFilmSamplingFrequency(event);
            }
            
            if(event.getName().endsWith(".mix.xml")) {
                if(filmXmlSamplingFreq != null) {
                    checkMixFrequency(event);
                } else {
                    addFailure(event.getName(), "2K-7: Encountered a MIX file in a film dir, "
                            + "but no film-XML file has been seen!", event.getName());
                }
            } 
        }
        
    }

    /**
     * Check if we are at a film node. 
     * This is true if the path does not contain WORKSHIFT-ISO-TARGET
     * and have more that one path part (parts seperated by a '/')
     */
    private static boolean isFilmNode(NodeBeginsParsingEvent event) {
        boolean isFilmNode = true;
        
        if(event.getName().contains("WORKSHIFT-ISO-TARGET")) {
            isFilmNode = false;
        }
        
        if(event.getName().split("/").length <= 1) {
            // We are at the base (i.e. this is the batch/roundtrip node)
            isFilmNode = false;
        }
        
        return isFilmNode;
    }
    
    /**
     * Read the film sampling frequency from the film.xml file. 
     */
    private void setFilmSamplingFrequency(AttributeParsingEvent event) {
        final String filmFrequencyXPath = "/avis:reelMetadata/avis:captureResolutionOriginal";
        Document doc;
        try {
            doc = documentCache.getDocument(event);
            filmXmlSamplingFreq = xpath.selectInteger(doc, filmFrequencyXPath);
            
        } catch (IOException e) {
            resultCollector.addFailure(event.getName(), "exception", getClass().getSimpleName(),
                                       "Error processing FILM metadata: " + e.toString(), getStackTrace(e));
        }
    }

    /**
     * Read and verify the sampling frequencies from the Mix file against the one previously found in the mix file. 
     */
    private void checkMixFrequency(AttributeParsingEvent event) {
        final String xFreqXPath = "/mix:mix/mix:ImageAssessmentMetadata/mix:SpatialMetrics"
                + "/mix:xSamplingFrequency[mix:denominator=1]/mix:numerator";
        final String yFreqXPath = "mix:mix/mix:ImageAssessmentMetadata/mix:SpatialMetrics"
                + "/mix:ySamplingFrequency[mix:denominator=1]/mix:numerator"; 
        
        Document doc;
        try {
            doc = documentCache.getDocument(event);
            Integer xFrequency = xpath.selectInteger(doc, xFreqXPath);
            Integer yFrequency = xpath.selectInteger(doc, yFreqXPath);
            
            if(!xFrequency.equals(filmXmlSamplingFreq)) {
                addFailure(event.getName(), "2K-5: MIX X sampling frequency '" + xFrequency 
                        + "' is not equal to film sampling frequency '" 
                        + filmXmlSamplingFreq + "'", event.getName());
            }
            
            if(!yFrequency.equals(filmXmlSamplingFreq)) {
                addFailure(event.getName(), "2K-6: MIX Y sampling frequency '" + yFrequency 
                        + "' is not equal to film sampling frequency '" 
                        + filmXmlSamplingFreq + "'", event.getName());
            }
            
        } catch (IOException e) {
            resultCollector.addFailure(event.getName(), "exception", getClass().getSimpleName(),
                                       "Error processing MIX metadata: " + e.toString(), getStackTrace(e));
        }
    }

    private void addFailure(String reference, String description, String details) {
        resultCollector.addFailure(reference, "metadata", getClass().getSimpleName(), description, details);
    }
    
}