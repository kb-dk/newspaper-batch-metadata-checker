package dk.statsbiblioteket.newspaper.metadatachecker;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.DefaultTreeEventHandler;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.MfPakDAO;
import dk.statsbiblioteket.util.xml.DOM;
import dk.statsbiblioteket.util.xml.XPathSelector;
import org.w3c.dom.Document;

import java.io.IOException;

/**
 * This class uses xpath to validate metadata requirements for edition xml (mods) files that do no fit into
 * the schematron paradigm.
 */
public class EditionModsEventHandler extends DefaultTreeEventHandler {
    private ResultCollector resultCollector;
    private MfPakDAO mfPakDAO;
    private Batch batch;

    /**
     * Constructor for this class.
     * @param resultCollector the result collector to collect errors in
     * @param mfPakDAO a DAO object from which one can read relevant external properties of a batch.
     * @param batch a batch object representing the batch being analysed.
     */
    public EditionModsEventHandler(ResultCollector resultCollector, MfPakDAO mfPakDAO, Batch batch) {
        this.resultCollector = resultCollector;
        this.mfPakDAO = mfPakDAO;
        this.batch = batch;
    }

    @Override
    public void handleAttribute(AttributeParsingEvent event) {
        if (event.getName().endsWith("edition.xml")) {
            doValidate(event);
        }
    }

    private void doValidate(AttributeParsingEvent event) {
        XPathSelector xpath = DOM.createXPathSelector("mods", "http://www.loc.gov/mods/v3");
        Document doc;
        try {
            doc = DOM.streamToDOM(event.getData(),true);
            if (doc == null) {
                resultCollector.addFailure(
                        event.getName(),
                        "metadata",
                        getClass().getName(),
                        "Could not parse xml from " + event.getName(),
                        event.getName()
                );
                return;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        /** 2D-1
         * Check avisID (The unique ID for the newspaper concerned provided by the State and University Library)
         * mods:mods/mods:titleInfo/mods:title [@type=”uniform” authority=”Statens Avissamling”]
         * matches the ID in the file structure (event.getName()).
         */
        final String xpath2D1 = "mods:mods/mods:titleInfo/mods:title [@type=”uniform” authority=”Statens Avissamling”]";
        String avisID = xpath.selectString(doc, xpath2D1);
        String avisIDfromFileStructure = event.getName();//TODO parse name
        if (avisID == null || !avisID.matches(avisIDfromFileStructure)) {
            resultCollector.addFailure(event.getName(),
                    "metadata",
                    getClass().getName(),
                    "2D-1: avisID " + avisID + " does not match avisID in file structure" + avisIDfromFileStructure,
                    xpath2D1
            );
        }
        /** 2D-2
         * Check Title (Newspaper title (MARC 245$a). Provided by the State and University Library, title may be
         * different for different periods in time, which will be specified by the State and University Library.
         * mods:mods/mods:titleInfo/mods:title
         * matches the titles we have shipped according to the lists we have delivered.
         * Check against MFpak.
         */
        //TODO
        /** 2D-3 Check Publication Location against MFpak */

        /** 2D-4 Check Issue Date against event.getName() (file structure),
         * and check within expected interval against MFpak?
         */

        /** 2D-9 Check Edition Order against event.getName() (file structure),
         * and cross correlate with sibling editions that the numbers are sequential.
         */


    }
}


