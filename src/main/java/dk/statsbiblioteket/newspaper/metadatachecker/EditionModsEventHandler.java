package dk.statsbiblioteket.newspaper.metadatachecker;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.DefaultTreeEventHandler;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.MfPakDAO;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.NewspaperTitle;
import dk.statsbiblioteket.util.xml.DOM;
import dk.statsbiblioteket.util.xml.XPathSelector;
import org.w3c.dom.Document;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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
     *
     * @param resultCollector the result collector to collect errors in
     * @param mfPakDAO        a DAO object from which one can read relevant external properties of a batch.
     * @param batch           a batch object representing the batch being analysed.
     */
    public EditionModsEventHandler(ResultCollector resultCollector, MfPakDAO mfPakDAO, Batch batch) {
        this.resultCollector = resultCollector;
        this.mfPakDAO = mfPakDAO;
        this.batch = batch;
    }

    @Override
    public void handleAttribute(AttributeParsingEvent event) {
        if (event.getName()
                 .endsWith("edition.xml")) {
            doValidate(event);
        }
    }

    private void doValidate(AttributeParsingEvent event) {
        XPathSelector xpath = DOM.createXPathSelector("mods", "http://www.loc.gov/mods/v3");
        Document doc;
        try {
            doc = DOM.streamToDOM(event.getData(), true);
            if (doc == null) {
                resultCollector.addFailure(
                        event.getName(),
                        "metadata",
                        getClass().getName(),
                        "Could not parse xml from " + event.getName(),
                        event.getName());
                return;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String avisID = check2D_1(event, xpath, doc);
        Date editionDate = check2D_2(event, xpath, doc);
        check2D_3(event, xpath, doc, avisID, editionDate);


        /** 2D-4 Check Issue Date against event.getName() (file structure),
         * and check within expected interval against MFpak?
         */

        /** 2D-9 Check Edition Order against event.getName() (file structure),
         * and cross correlate with sibling editions that the numbers are sequential.
         */


    }

    private void check2D_3(AttributeParsingEvent event, XPathSelector xpath, Document doc, String avisID, Date editionDate) {
        //mfPakDAO.getNewspaperEntity()
        /** 2D-3 Check Publication Location against MFpak */

    }

    private Date check2D_2(AttributeParsingEvent event, XPathSelector xpath, Document doc) {
        /** 2D-2
         * Check Title (Newspaper title (MARC 245$a). Provided by the State and University Library, title may be
         * different for different periods in time, which will be specified by the State and University Library.
         * mods:mods/mods:titleInfo/mods:title
         * matches the titles we have shipped according to the lists we have delivered.
         * Check against MFpak.
         */


        Date editionDate = null;
        try {
            editionDate = getDateFromName(event.getName());
        } catch (ParseException e) {
            resultCollector.addFailure(
                    event.getName(), "metadata", getClass().getName(), "2D-2: No edition date found in this file");
            return null;
        }
        if (editionDate == null) {
            resultCollector.addFailure(
                    event.getName(), "metadata", getClass().getName(), "2D-2: No edition date found in this file");
            return null;
        } else {
            try {
                List<NewspaperTitle> titles = mfPakDAO.getBatchNewspaperTitles(batch.getBatchID());
                NewspaperTitle selected = null;
                for (NewspaperTitle title : titles) {
                    if (title.getDateRange()
                             .isIncluded(editionDate)) {
                        selected = title;
                        break;
                    }
                }
                if (selected == null) {
                    resultCollector.addFailure(
                            event.getName(),
                            "metadata",
                            getClass().getName(),
                            "2D-2: No title found in MFPak for this file");
                } else {
                    String avisTitle = xpath.selectString(doc, "/mods:mods/mods:titleInfo/mods:title");
                    if (avisTitle == null) {
                        resultCollector.addFailure(
                                event.getName(), "metadata", getClass().getName(), "2D-2: Title should exist");
                    } else {
                        if (!avisTitle.equals(selected.getTitle())) {
                            resultCollector.addFailure(
                                    event.getName(),
                                    "metadata",
                                    getClass().getName(),
                                    "2D-2: title "
                                    + avisTitle
                                    + " does not match title in MFPak '"
                                    + selected.getTitle()
                                                      + "'");
                        }
                    }
                }


            } catch (SQLException e) {
                resultCollector.addFailure(
                        event.getName(),
                        "metadata",
                        getClass().getName(),
                        "Could not connect to MFPak",
                        event.getName());
                return editionDate;
            }
        }
        return editionDate;
    }

    private String check2D_1(AttributeParsingEvent event, XPathSelector xpath, Document doc) {
        /** 2D-1
         * Check avisID (The unique ID for the newspaper concerned provided by the State and University Library)
         * mods:mods/mods:titleInfo/mods:title [@type=”uniform” authority=”Statens Avissamling”]
         * matches the ID in the file structure (event.getName()).
         */
        final String xpath2D1
                = "/mods:mods/mods:titleInfo[@type='uniform'][@authority='Statens Avissamling']/mods:title";
        String avisID = xpath.selectString(doc, xpath2D1);
        if (avisID == null) {
            resultCollector.addFailure(
                    event.getName(), "metadata", getClass().getName(), "2D-1: No avisID found in this file");
            return null;
        }
        String avisIDfromFileStructure = getAvisIDfromName(event.getName());
        String avisIDfromMFPak;
        try {
            avisIDfromMFPak = mfPakDAO.getNewspaperID(batch.getBatchID());
        } catch (SQLException e) {
            resultCollector.addFailure(
                    event.getName(), "metadata", getClass().getName(), "Could not connect to MFPak", event.getName());
            return avisID;
        }
        //TODO also existence checks

        if (!avisID.equals(avisIDfromFileStructure)) {
            resultCollector.addFailure(
                    event.getName(),
                    "metadata",
                    getClass().getName(),
                    "2D-1: avisID "
                    + avisID
                    + " does not match avisID in file structure '"
                    + avisIDfromFileStructure
                    + "'",
                    xpath2D1);
        }
        if (!avisID.equals(avisIDfromMFPak)) {
            resultCollector.addFailure(
                    event.getName(),
                    "metadata",
                    getClass().getName(),
                    "2D-1: avisID " + avisID + " does not match avisID in MFPak '" + avisIDfromMFPak + "'",
                    xpath2D1);
        }

        return avisID;

    }

    private Date getDateFromName(String name) throws ParseException {

        //B400022028241-RT1/400022028241-14/1795-06-13-01/AdresseContoirsEfterretninger-1795-06-13-01.edition.xml
        String[] splits = name.split("/");
        String file = splits[2];
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.parse(file);
    }

    private String getAvisIDfromName(String name) {
        //B400022028241-RT1/400022028241-14/1795-06-13-01/AdresseContoirsEfterretninger-1795-06-13-01.edition.xml
        String[] splits = name.split("/");
        String file = splits[splits.length - 1];
        String[] parts = file.split("-");
        return parts[0];
    }
}


