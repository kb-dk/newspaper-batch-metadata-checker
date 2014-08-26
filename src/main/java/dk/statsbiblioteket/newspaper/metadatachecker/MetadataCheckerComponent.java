package dk.statsbiblioteket.newspaper.metadatachecker;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.TreeProcessorAbstractRunnableComponent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.ParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.EventRunner;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.MultiThreadedEventRunner;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.TreeEventHandler;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.MfPakDAO;
import dk.statsbiblioteket.util.xml.DOM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executors;

/** Check Metadata of all nodes. */
public class MetadataCheckerComponent
        extends TreeProcessorAbstractRunnableComponent {
    private Logger log = LoggerFactory.getLogger(getClass());
    private final MfPakDAO mfPakDAO;
    private final Set<MetadataChecksFactory.Checks> disabledChecks;

    /**
     * Initialise metadata checker component. For used properties {@link TreeProcessorAbstractRunnableComponent#createIterator}.
     *
     * property fields that are used
     * <ul>
     * <li>atNinestars:boolean:default false: indicates if we are at ninestars and jpylyzer should be executed. The
     * fields below are only relevant if this one is true</li>
     * <li>jpylyzerPath:String: no default: the path to the jpylyzer executable</li>
     * <li>scratch: String: no default: Path to the folder containing the batches</li>
     * <li>controlPolicies: String: default null: The path to the control policies. Optional</li>
     * </ul>
     *
     * @param properties Properties for initialising component.
     * @param mfPakDAO a DAO object from which one can read relevant external properties of a batch.
     */
    public MetadataCheckerComponent(Properties properties, MfPakDAO mfPakDAO, Set<MetadataChecksFactory.Checks> disabledChecks) {
        super(properties);
        this.mfPakDAO = mfPakDAO;
        if (disabledChecks == null){
            disabledChecks = new HashSet<>();
        }
        this.disabledChecks = disabledChecks;
    }

    @Override
    public String getEventID() {
        return "Metadata_checked";
    }

    @Override
    /**
     * For each attribute in batch: Check metadata contents.
     *
     * @param batch The batch to check
     * @param resultCollector Collector to get the result.
     */
    public void doWorkOnBatch(Batch batch,
                              ResultCollector resultCollector)
            throws
            Exception {
        log.info("Starting validation of '{}'", batch.getFullID());

        InputStream batchXmlStructureStream = retrieveBatchStructure(batch);
        if (batchXmlStructureStream == null){
            throw new RuntimeException("Failed to resolve batch manifest from data collector");
        }

        Document batchXmlStructure = DOM.streamToDOM(batchXmlStructureStream);


        MetadataChecksFactory metadataChecksFactory = getMetadataChecksFactory(
                batch,
                resultCollector,
                batchXmlStructure);
        List<TreeEventHandler> eventHandlers = metadataChecksFactory.createEventHandlers();
        EventRunner eventRunner = new MultiThreadedEventRunner(createIterator(batch),
                eventHandlers,
                resultCollector,
                new MultiThreadedEventRunner.EventCondition() {
                    @Override
                    public boolean shouldFork(ParsingEvent event) {
                        String[] splits = event.getName().split("/");
                        return splits.length == 4;
                    }

                    @Override
                    public boolean shouldJoin(ParsingEvent event) {
                        String[] splits = event.getName().split("/");
                        return splits.length == 3;
                    }
                },
                Executors.newFixedThreadPool(Integer.parseInt(getProperties().getProperty(ConfigConstants.THREADS_PER_BATCH,
                        "1"))));
        eventRunner.run();
        log.info("Done validating '{}', success: {}", batch.getFullID(), resultCollector.isSuccess());
    }

    protected MetadataChecksFactory getMetadataChecksFactory(Batch batch, ResultCollector resultCollector,
                                                           Document batchXmlStructure) {
        boolean atNinestars =
                Boolean.parseBoolean(getProperties().getProperty(ConfigConstants.AT_NINESTARS, Boolean.FALSE.toString()));
        MetadataChecksFactory metadataChecksFactory;

        if (atNinestars) {
            String jpylyzerPath = getProperties().getProperty(ConfigConstants.JPYLYZER_PATH);
            String batchFolder = getProperties().getProperty(ConfigConstants.ITERATOR_FILESYSTEM_BATCHES_FOLDER);
            metadataChecksFactory = new MetadataChecksFactory(resultCollector,
                                                              true,
                                                              batchFolder,
                                                              jpylyzerPath,
                    mfPakDAO, batch, batchXmlStructure,disabledChecks);
        } else {
            metadataChecksFactory = new MetadataChecksFactory(resultCollector, mfPakDAO, batch, batchXmlStructure,disabledChecks);
        }
        return metadataChecksFactory;
    }

}
