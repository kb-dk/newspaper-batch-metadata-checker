package dk.statsbiblioteket.newspaper.metadatachecker.crosscheck;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.TreeEventHandler;

/**
 * Checks all the interfile consistency checks implied in Appendix 2C-10
 *
 */
public class BrikConsistencyCheckHandler implements TreeEventHandler {

    private ResultCollector resultCollector;
    private Batch batch;

    public BrikConsistencyCheckHandler(ResultCollector resultCollector, Batch batch) {
        this.resultCollector = resultCollector;
        this.batch = batch;
    }

    @Override
    public void handleNodeBegin(NodeBeginsParsingEvent nodeBeginsParsingEvent) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void handleNodeEnd(NodeEndParsingEvent nodeEndParsingEvent) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void handleAttribute(AttributeParsingEvent attributeParsingEvent) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void handleFinish() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
