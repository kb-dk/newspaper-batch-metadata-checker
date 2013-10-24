package dk.statsbiblioteket.newspaper.metadatachecker;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.EventHandlerFactory;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.TreeEventHandler;
import dk.statsbiblioteket.newspaper.metadatachecker.jpylyzer.JpylyzerValidatorEventHandler;

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

    /**
     * Initialise the MetadataChecksFactory with a result collector to collect errors in.
     *
     * @param resultCollector The result collector to collect errors in.
     */
    public MetadataChecksFactory(ResultCollector resultCollector) {
        this.resultCollector = resultCollector;
    }

    //TODO document
    public MetadataChecksFactory(ResultCollector resultCollector,
                                 boolean atNinestars,
                                 String scratchFolder,
                                 String jpylyzerPath,
                                 String controlPoliciesPath) {
        this(resultCollector);
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
        return treeEventHandlers;
    }
}