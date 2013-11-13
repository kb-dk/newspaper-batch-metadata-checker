package dk.statsbiblioteket.newspaper.metadatachecker;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.TreeIterator;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.filesystem.transforming.TransformingIteratorForFileSystems;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.MfPakDAO;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Pattern;

public class MockupIteratorSuper extends MetadataCheckerComponent {

    /**
     * Constructor matching super. Super requires a properties to be able to initialise the tree iterator, if needed.
     * If you do not need the tree iterator, ignore properties.
     *
     * You can use properties for your own stuff as well
     *
     * @param properties properties.
     * @param mfPakDAO a DAO or stub.
     */
    public MockupIteratorSuper(Properties properties, MfPakDAO mfPakDAO) {
        super(properties, mfPakDAO);
    }

    /**
     * We override this method to be able to inject our own tree iterator
     * @param batch the batch to iterate on
     * @return a tree iterator
     */
    @Override
    protected TreeIterator createIterator(Batch batch) {
        File dataDir = new File(Thread.currentThread().getContextClassLoader().getResource("scratch").getFile());
        File batchDir = new File(dataDir, batch.getFullID());
        return new TransformingIteratorForFileSystems(batchDir, Pattern.quote("."),".*\\.jp2$",".md5");
    }

    @Override
    public InputStream retrieveBatchStructure(Batch batch) throws IOException {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream("assumed-valid-structure.xml");
    }
}
