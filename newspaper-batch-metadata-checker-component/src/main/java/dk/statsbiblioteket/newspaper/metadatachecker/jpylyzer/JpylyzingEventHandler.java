package dk.statsbiblioteket.newspaper.metadatachecker.jpylyzer;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.DataFileNodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.DataFileNodeEndsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.InMemoryAttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.InjectingTreeEventHandler;
import dk.statsbiblioteket.util.Bytes;
import dk.statsbiblioteket.util.Streams;
import dk.statsbiblioteket.util.Strings;
import dk.statsbiblioteket.util.console.ProcessRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/** The jpylyzer metadata content checker checker */
public class JpylyzingEventHandler extends InjectingTreeEventHandler {

    /** Name of content file in jp2 virtual folder */
    public static final String CONTENTS = "/contents";
    /** Folder where the batch lives. Used because jpylyzer must work on the absolute path to the file*/
    private final String batchFolder;
    /** Logger */
    private final Logger log = LoggerFactory.getLogger(JpylyzingEventHandler.class);
    /** The result collector results are collected in. */
    private final ResultCollector resultCollector;
    /** Double flags indicating if we are in a virtual jp2 folder */
    private static final ThreadLocal<String> datafile = new ThreadLocal<String>();
    private static final ThreadLocal<Boolean> isInDataFile = new ThreadLocal<Boolean>();


    /** Path to the jpylyzer executable */
    private final String jpylyzerPath;


    /**
     * Extended constructor for the validator. This should be used if we want the validator to be able to
     * execute jpylyzer.
     *
     * @param resultCollector the result collector
     * @param batchFolder     the folder where the batches live
     * @param jpylyzerPath    path to the jpylyzer executable
     */
    public JpylyzingEventHandler(ResultCollector resultCollector,
                                 String batchFolder,
                                 String jpylyzerPath) {

        this.batchFolder = batchFolder;
        this.resultCollector = resultCollector;
        if (jpylyzerPath != null){
        this.jpylyzerPath = jpylyzerPath;
        } else {
            this.jpylyzerPath = "jpylyzer.py";
        }

    }

    /**
     * Node begins. Sets the isInDataFile flag and the "datafile" if this node is a data file node
     *
     * @param event the node begins event
     */
    @Override
    public void handleNodeBegin(NodeBeginsParsingEvent event) {
        if (event instanceof DataFileNodeBeginsParsingEvent) {
            isInDataFile.set(true);
            datafile.set(event.getName());
        }
    }

    /**
     * Node ends. Clears the isInDataFile flag if this node is a data file node.
     *
     * @param event the node ends event
     */
    @Override
    public void handleNodeEnd(NodeEndParsingEvent event) {
        if (event instanceof DataFileNodeEndsParsingEvent) {
            isInDataFile.set(false);
            datafile.set(null);
        }
    }

    /**
     * Attribute event.
     * If we are in a data file node, and the event is for a file called "/contents" and the flag "atNinestars" is set
     * we will run jpylyzer and validate the output.
     * If we are in a data file node, and the event is for a file called "*.jpylyzer.xml", validate the contents
     *
     * @param event the attribute event
     */
    @Override
    public void handleAttribute(AttributeParsingEvent event) {
        try {
            if (isInDataFile.get() != null && isInDataFile.get()) {

                if (event.getName().endsWith(CONTENTS)) {
                    log.debug("Encountered event {}", event.getName());

                    File filePath = new File(batchFolder, datafile.get());
                    byte[] jpylizerOutput = toByteArray(jpylize(filePath));
                    pushEvent(new InMemoryAttributeParsingEvent(getJpylyzerName(datafile.get()),
                            jpylizerOutput,
                            md5sum(jpylizerOutput)));

                }
            }
        } catch (IOException e) {
            resultCollector.addFailure(event.getName(),
                                       "exception",
                                       getClass().getSimpleName(),
                                       "Unexpected error: " + e.getMessage(),
                                       Strings.getStackTrace(e));
        }
    }

    protected String getJpylyzerName(String jp2Name) {
        return jp2Name.replaceFirst("\\.jp2$", ".jpylyzer.xml");
    }

    private String md5sum(byte[] bytes) {
        try {
            return Bytes.toHex(MessageDigest.getInstance("MD5").digest(bytes)).toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            throw new Error("MD5 not known");
        }
    }

    private byte[] toByteArray(InputStream jpylizerOutput) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        Streams.pipe(jpylizerOutput, byteStream);
        return byteStream.toByteArray();
    }

    /** Get the component name */
    private String getComponentName() {
        return "jpylyzerprofile" + getClass().getPackage().getImplementationVersion();
    }

    @Override
    public void handleFinish() {
        //Anything to do here?
    }

    /**
     * run jpylyzer on the given file and return the xml report as an inputstream.
     *
     * @param dataPath the path to the jp2 file
     *
     * @return the jpylyzer xml report
     * @throws IOException if the execution of jpylyzer failed in some fashion (not invalid file, if the program
     *                     returned non-zero returncode)
     */
    private InputStream jpylize(File dataPath) throws IOException {


        log.info("Running jpylyzer on file {}", dataPath);


        ProcessRunner runner = new ProcessRunner(jpylyzerPath, dataPath.getAbsolutePath());
        Map<String, String> myEnv = getJenkinsEnvironment();
        runner.setEnviroment(myEnv);
        runner.setOutputCollectionByteSize(Integer.MAX_VALUE);

        //this call is blocking
        runner.run();

        //we could probably do something more clever with returning the output while the command is still running.
        if (runner.getReturnCode() == 0) {
            return runner.getProcessOutput();
        } else {
            throw new IOException("failed to run jpylyzer, returncode:" + runner.getReturnCode() + ", stdOut:"
                                  + runner.getProcessOutputAsString() + " stdErr:" + runner.getProcessErrorAsString());
        }
    }

    private Map<String, String> getJenkinsEnvironment() {
        Map<String, String> sysEnv = System.getenv();
        return sysEnv;
    }

}
