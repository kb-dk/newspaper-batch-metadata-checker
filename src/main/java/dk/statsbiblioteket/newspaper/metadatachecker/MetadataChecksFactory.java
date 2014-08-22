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

/** A factory for checks to do on metadata. */
public class MetadataChecksFactory
        implements EventHandlerFactory {
    /** The result collector to collect errors in. */
    private ResultCollector resultCollector;
    private boolean atNinestars = false;
    private String batchFolder;
    private String jpylyzerPath;
    private String controlPoliciesPath;
    private BatchContext batchContext;
    private Document batchXmlStructure;

    /**
     * Initialise the MetadataChecksFactory with a result collector to collect errors in.
     *
     * @param resultCollector The result collector to collect errors in.
     * @param mfPakDAO        a DAO object from which one can read relevant external properties of a batch.
     * @param batch           a batch object representing the batch being analysed.
     */
    public MetadataChecksFactory(ResultCollector resultCollector,
                                 MfPakDAO mfPakDAO,
                                 Batch batch,
                                 Document batchXmlStructure) {
        this.resultCollector = resultCollector;
        this.batchXmlStructure = batchXmlStructure;
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
     *
     * @param resultCollector     the result collector to collect errors in
     * @param atNinestars         should be true, sets the framework to run in the ninestars context
     * @param batchFolder         the folder where the batches lie
     * @param jpylyzerPath        the path to the jpylyzer executable. If null, jpylyzer will be used from the PATH
     * @param controlPoliciesPath the control policies for the validators. If null, default values are used
     * @param mfPakDAO            a DAO object from which one can read relevant external properties of a batch.
     * @param batch               a batch object representing the batch being analysed.
     */
    public MetadataChecksFactory(ResultCollector resultCollector,
                                    boolean atNinestars,
                                    String batchFolder,
                                    String jpylyzerPath,
                                    String controlPoliciesPath,
                                    MfPakDAO mfPakDAO,
                                    Batch batch,
                                    Document batchXmlStructure) {
        this(resultCollector, mfPakDAO, batch,batchXmlStructure);
        this.atNinestars = atNinestars;
        this.batchFolder = batchFolder;
        this.jpylyzerPath = jpylyzerPath;
        this.controlPoliciesPath = controlPoliciesPath;
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
            treeEventHandlers.add(new ChecksumCheckEventHandler(resultCollector));

            //This thing adds virtual jpylyzer.xml nodes
            treeEventHandlers.add(new JpylyzingEventHandler(resultCollector, batchFolder, jpylyzerPath));
        }
        treeEventHandlers.add(new SchemaValidatorEventHandler(resultCollector, documentCache));
        treeEventHandlers.add(new SchematronValidatorEventHandler(resultCollector, controlPoliciesPath, documentCache));
        treeEventHandlers.add(new ModsXPathEventHandler(resultCollector, batchContext, batchXmlStructure, documentCache));
        treeEventHandlers.add(new AltoXPathEventHandler(resultCollector, documentCache));
        treeEventHandlers.add(new AltoMixCrossCheckEventHandler(resultCollector, documentCache));
        treeEventHandlers.add(new EditionModsEventHandler(resultCollector, batchContext, documentCache));
        treeEventHandlers.add(new FilmXmlChecker(resultCollector, batchContext, batchXmlStructure, documentCache));
        treeEventHandlers.add(new MixXmlFileChecker(resultCollector, batchContext, batchXmlStructure, documentCache));
        treeEventHandlers.add(new MixFilmCrossCheckEventHandler(resultCollector, documentCache));
        return treeEventHandlers;
    }
}