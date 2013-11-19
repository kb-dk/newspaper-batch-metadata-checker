package dk.statsbiblioteket.newspaper.metadatachecker.mockers;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.InMemoryAttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.TreeIterator;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.InjectingTreeEventHandler;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.TreeEventHandler;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.filesystem.transforming.TransformingIteratorForFileSystems;
import dk.statsbiblioteket.newspaper.metadatachecker.MetadataCheckerComponent;
import dk.statsbiblioteket.newspaper.metadatachecker.MetadataChecksFactory;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.MfPakDAO;
import dk.statsbiblioteket.util.Bytes;
import dk.statsbiblioteket.util.Streams;
import org.w3c.dom.Document;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

public class MockupIteratorSuper extends MetadataCheckerComponent {

    private MfPakDAO mfPakDAO;

    /**
     * Constructor matching super. Super requires a properties to be able to initialise the tree iterator, if needed.
     * If you do not need the tree iterator, ignore properties.
     *
     * You can use properties for your own stuff as well
     *
     * @param properties properties.
     * @param mfPakDAO   a DAO or stub.
     */
    public MockupIteratorSuper(Properties properties, MfPakDAO mfPakDAO) {
        super(properties, mfPakDAO);
        this.mfPakDAO = mfPakDAO;
    }

    /**
     * We override this method to be able to inject our own tree iterator
     *
     * @param batch the batch to iterate on
     *
     * @return a tree iterator
     */
    @Override
    protected TreeIterator createIterator(Batch batch) {
        File dataDir = new File(
                Thread.currentThread()
                      .getContextClassLoader()
                      .getResource("scratch")
                      .getFile());
        File batchDir = new File(dataDir, batch.getFullID());
        return new TransformingIteratorForFileSystems(batchDir, Pattern.quote("."), ".*\\.jp2$", ".md5");
    }

    @Override
    public InputStream retrieveBatchStructure(Batch batch) throws IOException {
        return Thread.currentThread()
                     .getContextClassLoader()
                     .getResourceAsStream("assumed-valid-structure.xml");
    }

    @Override
    protected MetadataChecksFactory getMetadataChecksFactory(Batch batch, ResultCollector resultCollector,
                                                             Document batchXmlStructure) {
        return new MetadataChecksFactory(resultCollector, mfPakDAO, batch, batchXmlStructure) {
            @Override
            public List<TreeEventHandler> createEventHandlers() {
                List<TreeEventHandler> defaultHandlers = super.createEventHandlers();
                defaultHandlers.add(
                        new InjectingTreeEventHandler() {
                            public static final String CONTENTS = "/contents";

                            @Override
                            public void handleNodeBegin(NodeBeginsParsingEvent event) {
                                //To change body of implemented methods use File | Settings | File Templates.
                            }

                            @Override
                            public void handleNodeEnd(NodeEndParsingEvent event) {
                                //To change body of implemented methods use File | Settings | File Templates.
                            }

                            @Override
                            public void handleAttribute(AttributeParsingEvent event) {
                                if (event.getName()
                                         .endsWith(CONTENTS)) {
                                    byte[] jpylizerOutput = new byte[0];
                                    try {
                                        String name = "jpylyzerFiles/" + event.getName()
                                                                              .replaceAll("^[^.]*/", "")
                                                                              .replaceAll(
                                                                                      Pattern.quote(CONTENTS),
                                                                                      ".jpylyzer.xml");
                                        InputStream resourceAsStream = Thread.currentThread()
                                                                             .getContextClassLoader()
                                                                             .getResourceAsStream(name);
                                        if (resourceAsStream != null) {
                                            jpylizerOutput = toByteArray(
                                                    resourceAsStream);
                                            pushInjectedEvent(
                                                    new InMemoryAttributeParsingEvent(
                                                            getJpylyzerName(event.getName()),
                                                            jpylizerOutput,
                                                            md5sum(jpylizerOutput)));

                                        }
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }


                                }
                            }

                            private byte[] toByteArray(InputStream jpylizerOutput) throws IOException {
                                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                                Streams.pipe(jpylizerOutput, byteStream);
                                return byteStream.toByteArray();
                            }

                            private String getJpylyzerName(String jp2Name) {
                                return jp2Name.replaceFirst(CONTENTS, ".jpylyzer.xml");
                            }

                            private String md5sum(byte[] bytes) {
                                try {
                                    return Bytes.toHex(
                                            MessageDigest.getInstance("MD5")
                                                         .digest(bytes))
                                                .toLowerCase();
                                } catch (NoSuchAlgorithmException e) {
                                    throw new Error("MD5 not known");
                                }
                            }

                            @Override
                            public void handleFinish() {
                                //To change body of implemented methods use File | Settings | File Templates.
                            }
                        });
                return defaultHandlers;
            }
        };
    }
}
