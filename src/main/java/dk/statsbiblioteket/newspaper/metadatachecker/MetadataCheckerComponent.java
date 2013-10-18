package dk.statsbiblioteket.newspaper.metadatachecker;

import dk.statsbiblioteket.medieplatform.autonomous.AbstractRunnableComponent;
import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.ParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.TreeIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Properties;

/**
 * Check Metadata of all nodes
 */
public class MetadataCheckerComponent
        extends AbstractRunnableComponent {

    private Logger log = LoggerFactory.getLogger(getClass());

    public MetadataCheckerComponent(Properties properties) {
        super(properties);
    }

    @Override
    public String getComponentName() {
        return "Metadata_checker_component";

    }

    @Override
    public String getComponentVersion() {
        return "0.1";
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
    public void doWorkOnBatch(Batch batch, ResultCollector resultCollector) throws Exception {
        //TODO: This is probably handled best by using the event framework from Jeppe and Mikis. Move this to framework?

        TreeIterator iterator = createIterator(batch);
        while (iterator.hasNext()) {
            ParsingEvent next = iterator.next();
            switch (next.getType()) {
                case NodeBegin: {
                    break;
                }
                case NodeEnd: {
                    break;
                }
                case Attribute: {
                    AttributeParsingEvent attributeEvent = (AttributeParsingEvent) next;
                    break;
                }
            }

        }
        resultCollector.setTimestamp(new Date());
    }

}
