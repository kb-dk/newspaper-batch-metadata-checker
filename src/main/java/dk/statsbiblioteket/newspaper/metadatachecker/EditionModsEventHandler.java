package dk.statsbiblioteket.newspaper.metadatachecker;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.DefaultTreeEventHandler;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.MfPakDAO;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.NewspaperEntity;
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
    private static final String YYYY_MM_DD = "yyyy-MM-dd";
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
        try {
            String avisID = check2D_1(event, xpath, doc);
            Date editionDate = check2D_2(event, xpath, doc);
            check2D_3(event, xpath, doc, avisID, editionDate);
            check2D_4(event, xpath, doc, avisID, editionDate);
            check2D_9(event, xpath, doc, avisID, editionDate);

        } catch (SQLException e) {
            resultCollector.addFailure(
                    event.getName(), "metadata", getClass().getName(), "Could not connect to MFPak", event.getName());
        }


    }

    //TODO
    private void check2D_9(AttributeParsingEvent event, XPathSelector xpath, Document doc, String avisID,
                           Date editionDate) {
        /** 2D-9 Check Edition Order against event.getName() (file structure),
         * and cross correlate with sibling editions that the numbers are sequential.
         */


    }

    /** 2D-4 Check Issue Date against event.getName() (file structure), */
    private void check2D_4(AttributeParsingEvent event, XPathSelector xpath, Document doc, String avisID,
                           Date editionDate) {
        String dateIssuedString = xpath.selectString(doc, "/mods:mods/mods:originInfo/mods:dateIssued");
        try {
            Date dateIssued = new SimpleDateFormat(YYYY_MM_DD).parse(dateIssuedString);
            if (!dateIssued.equals(editionDate)) {
                resultCollector.addFailure(
                        event.getName(),
                        "metadata",
                        getClass().getName(),
                        "2D-4: Date issued from file does not correspond to date in filename");
            }
        } catch (ParseException e) {
            resultCollector.addFailure(
                    event.getName(),
                    "metadata",
                    getClass().getName(),
                    "2D-4: Date issued from file is not of the form '" + YYYY_MM_DD + "'");

        }


    }

    /**
     * 2D-3 Check Publication Location against MFpak
     *
     * @param event the parsing event
     * @param xpath the xpath selector
     * @param doc the edition mods document
     * @param avisID the avisID
     * @param editionDate the editionDate
     *
     * @throws SQLException if communication with MFPak failed
     */
    private void check2D_3(AttributeParsingEvent event, XPathSelector xpath, Document doc, String avisID,
                           Date editionDate) throws SQLException {

        NewspaperEntity newspaperEntity = mfPakDAO.getNewspaperEntity(avisID, editionDate);
        if (newspaperEntity == null) {
            resultCollector.addFailure(
                    event.getName(),
                    "metadata",
                    getClass().getName(),
                    "2D-3: Failed to retrieve a publication location from MFPak for this batch");
            return;
        }
        String editionLocation = xpath.selectString(doc, "/mods:mods/mods:originInfo/mods:place/mods:placeTerm");
        if (editionLocation == null) {
            resultCollector.addFailure(
                    event.getName(), "metadata", getClass().getName(), "2D-3: Failed to resolve publication location");
        }
        if (!newspaperEntity.getPublicationLocation()
                            .equals(editionLocation)) {
            resultCollector.addFailure(
                    event.getName(),
                    "metadata",
                    getClass().getName(),
                    "2D-3: Publication location '"
                    + editionLocation
                    + "' does not match value '"
                    + newspaperEntity.getPublicationLocation()
                    + "' from MFPak");
        }

    }

    /** 2D-2
     * Check edition date against MFPak.
     * Check that the full title matches the value from MFPak
     */
    private Date check2D_2(AttributeParsingEvent event, XPathSelector xpath, Document doc) throws SQLException {

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

            List<NewspaperEntity> titles = mfPakDAO.getBatchNewspaperEntities(batch.getBatchID());
            NewspaperEntity selected = null;
            for (NewspaperEntity title : titles) {
                if (title.getNewspaperDateRange()
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
                    if (!avisTitle.equals(selected.getNewspaperTitle())) {
                        resultCollector.addFailure(
                                event.getName(),
                                "metadata",
                                getClass().getName(),
                                "2D-2: title "
                                + avisTitle
                                + " does not match title in MFPak '"
                                + selected.getNewspaperTitle()
                                + "'");
                    }
                }
            }


        }
        return editionDate;
    }

    /** 2D-1
     * Check avisID (The unique ID for the newspaper concerned provided by the State and University Library)
     * mods:mods/mods:titleInfo/mods:title [@type=”uniform” authority=”Statens Avissamling”]
     * matches the ID in the file structure (event.getName()).
     * Check avisID matches the avisID in MFPak.
     */
    private String check2D_1(AttributeParsingEvent event, XPathSelector xpath, Document doc) throws SQLException {
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

        avisIDfromMFPak = mfPakDAO.getNewspaperID(batch.getBatchID());


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

    /**
     * Get the edition date from the name of a edition.xml file
     * @param name the name of the edition xml file
     * @return the edition date
     * @throws ParseException
     */
    private Date getDateFromName(String name) throws ParseException {

        //B400022028241-RT1/400022028241-14/1795-06-13-01/AdresseContoirsEfterretninger-1795-06-13-01.edition.xml
        String[] splits = name.split("/");
        String file = splits[2];
        final SimpleDateFormat dateFormat = new SimpleDateFormat(YYYY_MM_DD);
        return dateFormat.parse(file);
    }

    /**
     * Get the avisID from the edition.xml name
     * @param name the edition.xml file name
     * @return the avisID
     */
    private String getAvisIDfromName(String name) {
        //B400022028241-RT1/400022028241-14/1795-06-13-01/AdresseContoirsEfterretninger-1795-06-13-01.edition.xml
        String[] splits = name.split("/");
        String file = splits[splits.length - 1];
        String[] parts = file.split("-");
        return parts[0];
    }
}


