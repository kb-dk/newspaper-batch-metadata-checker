package dk.statsbiblioteket.newspaper.metadatachecker;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.EventHandlerFactory;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.TreeEventHandler;
import dk.statsbiblioteket.newspaper.metadatachecker.jpylyzer.JpylyzerValidatorEventHandler;
import dk.statsbiblioteket.newspaper.mfpakintegration.configuration.MfPakConfiguration;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.MfPakDAO;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/** A factory for checks to do on metadata. */
public class MetadataChecksFactory
        implements EventHandlerFactory {
    /** The result collector to collect errors in. */
    private ResultCollector resultCollector;
    private boolean atNinestars = false;
    private String scratchFolder;
    private String jpylyzerPath;
    private String controlPoliciesPath;
    private MfPakConfiguration mfPakConfiguration;
    private Batch batch;

    /**
     * Initialise the MetadataChecksFactory with a result collector to collect errors in.
     *
     * @param resultCollector The result collector to collect errors in.
     */
    public MetadataChecksFactory(ResultCollector resultCollector, MfPakConfiguration mfPakConfiguration, Batch batch) {
        this.resultCollector = resultCollector;
        this.mfPakConfiguration = mfPakConfiguration;
        this.batch = batch;
    }

    /**
     * Construct a metadata checks factory that is usable for ninestars
     * @param resultCollector the result collector to collect errors in
     * @param atNinestars should be true, sets the framework to run in the ninestars context
     * @param scratchFolder the folder where the batches lie
     * @param jpylyzerPath the path to the jpylyzer executable. If null, jpylyzer will be used from the PATH
     * @param controlPoliciesPath the control policies for the validators. If null, default values are used
     */
    public MetadataChecksFactory(ResultCollector resultCollector,
                                 boolean atNinestars,
                                 String scratchFolder,
                                 String jpylyzerPath,
                                 String controlPoliciesPath, MfPakConfiguration mfPakConfiguration, Batch batch) {
        this(resultCollector, mfPakConfiguration, batch);
        this.atNinestars = atNinestars;
        this.scratchFolder = scratchFolder;
        this.jpylyzerPath = jpylyzerPath;
        this.controlPoliciesPath = controlPoliciesPath;
    }

    /**
     * Add all metadata checking event handlers.
     *
     * @return The list of metadata checking event handlers.
     */
    @Override
    public List<TreeEventHandler> createEventHandlers() {
        ArrayList<TreeEventHandler> treeEventHandlers = new ArrayList<>();
        treeEventHandlers.add(new SchemaValidatorEventHandler(resultCollector));

        try {
            treeEventHandlers.add(new JpylyzerValidatorEventHandler(scratchFolder, resultCollector,
                                                                    controlPoliciesPath,
                                                                    jpylyzerPath,
                                                                    atNinestars));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        treeEventHandlers.add(new SchematronValidatorEventHandler(resultCollector));
        treeEventHandlers.add(new ModsXPathEventHandler(resultCollector, new MfPakDAO(mfPakConfiguration), batch ));
        return treeEventHandlers;
    }
}