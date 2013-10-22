package dk.statsbiblioteket.newspaper.metadatachecker;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.EventHandlerFactory;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.TreeEventHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * A factory for checks to do on metadata.
 */
public class MetadataChecksFactory implements EventHandlerFactory {
    /** The result collector to collect errors in. */
    private ResultCollector resultCollector;

    /**
     * Initialise the MetadataChecksFactory with a result collector to collect errors in.
     *
     * @param resultCollector The result collector to collect errors in.
     */
    public MetadataChecksFactory(ResultCollector resultCollector) {
        this.resultCollector = resultCollector;
    }

    /**
     * Add all metadata checking event handlers.
     * @return The list of metadata checking event handlers.
     */
    @Override
    public List<TreeEventHandler> createEventHandlers() {
        ArrayList<TreeEventHandler> treeEventHandlers = new ArrayList<>();
        treeEventHandlers.add(new SchemaValidatorEventHandler(resultCollector));
        return treeEventHandlers;
    }
}