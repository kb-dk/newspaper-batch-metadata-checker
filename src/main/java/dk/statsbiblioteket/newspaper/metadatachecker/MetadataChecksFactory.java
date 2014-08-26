package dk.statsbiblioteket.newspaper.metadatachecker;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.EventHandlerFactory;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.TreeEventHandler;
import dk.statsbiblioteket.newspaper.metadatachecker.caches.DocumentCache;
import dk.statsbiblioteket.newspaper.metadatachecker.film.FilmXmlChecker;
import dk.statsbiblioteket.newspaper.metadatachecker.jpylyzer.JpylyzingEventHandler;
import dk.statsbiblioteket.newspaper.metadatachecker.mix.MixXmlFileChecker;
import dk.statsbiblioteket.newspaper.mfpakintegration.batchcontext.BatchContext;
import dk.statsbiblioteket.newspaper.mfpakintegration.batchcontext.BatchContextUtils;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.MfPakDAO;
import org.w3c.dom.Document;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** A factory for checks to do on metadata. */
public class MetadataChecksFactory
        implements EventHandlerFactory {

    public enum Checks{
        ALTO_XPATH,ALTO_MIX,CHECKSUM,EDITION_MODS,MIX_FILM,MODS_XPATH,SCHEMATRON,SCHEMA_VALIDATOR,MIX_XML,FILM_XML,JPYLYZER;
    }
    /** The result collector to collect errors in. */
    private ResultCollector resultCollector;
    private boolean atNinestars = false;
    private String batchFolder;
    private String jpylyzerPath;
    private BatchContext batchContext;
    private Document batchXmlStructure;
    private final Set<Checks> disabledChecks;

    /**
     * Initialise the MetadataChecksFactory with a result collector to collect errors in.
     *  @param resultCollector The result collector to collect errors in.
     * @param mfPakDAO        a DAO object from which one can read relevant external properties of a batch.
     * @param batch           a batch object representing the batch being analysed.
     * @param disabledChecks  a set of enums detailing the checks to be disabled
     */
    public MetadataChecksFactory(ResultCollector resultCollector, MfPakDAO mfPakDAO, Batch batch,
                                 Document batchXmlStructure, Set<Checks> disabledChecks) {
        this.resultCollector = resultCollector;
        this.batchXmlStructure = batchXmlStructure;
        this.disabledChecks = disabledChecks;
        try {
            this.batchContext = BatchContextUtils.buildBatchContext(mfPakDAO, batch);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to obtain required information from database. "
                    + "Check database connection, and try again", e);
        }
        
        //FIXME Introduce checks on batch context
    }

    /**
     * Construct a metadata checks factory that is usable for ninestars
     * @param resultCollector     the result collector to collect errors in
     * @param atNinestars         should be true, sets the framework to run in the ninestars context
     * @param batchFolder         the folder where the batches lie
     * @param jpylyzerPath        the path to the jpylyzer executable. If null, jpylyzer will be used from the PATH
     * @param mfPakDAO            a DAO object from which one can read relevant external properties of a batch.
     * @param batch               a batch object representing the batch being analysed.
     * @param disabledChecks
     */
    public MetadataChecksFactory(ResultCollector resultCollector, boolean atNinestars, String batchFolder, String jpylyzerPath,
                                 MfPakDAO mfPakDAO, Batch batch, Document batchXmlStructure, Set<Checks> disabledChecks) {
        this(resultCollector, mfPakDAO, batch,batchXmlStructure, disabledChecks);
        this.atNinestars = atNinestars;
        this.batchFolder = batchFolder;
        this.jpylyzerPath = jpylyzerPath;
        this.batchXmlStructure = batchXmlStructure;

    }



    /**
     * Add all metadata checking event handlers.
     *
     * @return The list of metadata checking event handlers.
     */
    @Override
    public List<TreeEventHandler> createEventHandlers() {
        ArrayList<TreeEventHandler> treeEventHandlers = new ArrayList<>();
        DocumentCache documentCache = new DocumentCache();


        if (atNinestars) {
            if (!disabledChecks.contains(Checks.CHECKSUM)) {
                treeEventHandlers.add(new ChecksumCheckEventHandler(resultCollector));
            }

            //This thing adds virtual jpylyzer.xml nodes
            treeEventHandlers.add(new JpylyzingEventHandler(resultCollector, batchFolder, jpylyzerPath));
        }
        if (!disabledChecks.contains(Checks.ALTO_XPATH)) {
            treeEventHandlers.add(new SchemaValidatorEventHandler(resultCollector, documentCache));
        }
        if (!disabledChecks.contains(Checks.SCHEMATRON)) {
            treeEventHandlers.add(new SchematronValidatorEventHandler(resultCollector, documentCache));
        }
        if (!disabledChecks.contains(Checks.MODS_XPATH)) {
            treeEventHandlers.add(new ModsXPathEventHandler(resultCollector,
                    batchContext,
                    batchXmlStructure,
                    documentCache));
        }
        if (!disabledChecks.contains(Checks.ALTO_XPATH)) {
            treeEventHandlers.add(new AltoXPathEventHandler(resultCollector, documentCache));
        }
        if (!disabledChecks.contains(Checks.ALTO_MIX)) {
            treeEventHandlers.add(new AltoMixCrossCheckEventHandler(resultCollector, documentCache));
        }
        if (!disabledChecks.contains(Checks.EDITION_MODS)) {
            treeEventHandlers.add(new EditionModsEventHandler(resultCollector, batchContext, documentCache));
        }
        if (!disabledChecks.contains(Checks.FILM_XML)) {
            treeEventHandlers.add(new FilmXmlChecker(resultCollector, batchContext, batchXmlStructure, documentCache));
        }
        if (!disabledChecks.contains(Checks.MIX_XML)) {
            treeEventHandlers.add(new MixXmlFileChecker(resultCollector, batchContext, batchXmlStructure, documentCache));
        }
        if (!disabledChecks.contains(Checks.MIX_FILM)) {
            treeEventHandlers.add(new MixFilmCrossCheckEventHandler(resultCollector, documentCache));
        }

        return treeEventHandlers;
    }
}