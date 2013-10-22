package dk.statsbiblioteket.newspaper.metadatachecker.jpylyzer;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.DataFileNodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.DataFileNodeEndsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.TreeEventHandler;
import dk.statsbiblioteket.util.Strings;
import dk.statsbiblioteket.util.console.ProcessRunner;
import dk.statsbiblioteket.util.xml.DOM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class JpylyzerValidatorEventHandler
        implements TreeEventHandler {

    private final String scratchFolder;
    /** Logger */
    private final Logger log = LoggerFactory.getLogger(JpylyzerValidatorEventHandler.class);
    /** The result collector results are collected in. */
    private final ResultCollector resultCollector;
    private Validator validator;
    private boolean isInDataFile;
    private String datafile;
    private boolean atNinestars = false;
    private String jpylyzerPath;

    public JpylyzerValidatorEventHandler(String scratchFolder,
                                         ResultCollector resultCollector,
                                         String controlPoliciesPath,
                                         String jpylyzerPath)
            throws
            FileNotFoundException {
        this.scratchFolder = scratchFolder;
        this.resultCollector = resultCollector;
        this.jpylyzerPath = jpylyzerPath;


        Document controlPoliciesDocument;
        if (controlPoliciesPath != null) {
            controlPoliciesDocument = DOM.streamToDOM(new FileInputStream(controlPoliciesPath));
        } else {
            controlPoliciesDocument = DOM.streamToDOM(Thread.currentThread().getContextClassLoader()
                                                            .getResourceAsStream("defaultControlPolicies.xml"));
        }
        if (this.jpylyzerPath == null) {
            this.jpylyzerPath = "src/main/extras/jpylyzer-1.10.1/jpylyzer.py";
        }
        validator = new ValidatorFactory(controlPoliciesDocument).createValidator();

    }

    public JpylyzerValidatorEventHandler(String scratchFolder,
                                         ResultCollector resultCollector,
                                         String controlPoliciesPath,
                                         String jpylyzerPath,
                                         boolean atNinestars)
            throws
            FileNotFoundException {
        this(scratchFolder, resultCollector, controlPoliciesPath, jpylyzerPath);

        this.atNinestars = atNinestars;
    }

    @Override
    public void handleNodeBegin(NodeBeginsParsingEvent event) {
        if (event instanceof DataFileNodeBeginsParsingEvent) {
            isInDataFile = true;
            datafile = event.getName();
        }
    }

    @Override
    public void handleNodeEnd(NodeEndParsingEvent event) {
        if (event instanceof DataFileNodeEndsParsingEvent) {
            isInDataFile = false;
            datafile = null;
        }
    }

    @Override
    public void handleAttribute(AttributeParsingEvent event) {
        try {
            if (isInDataFile) {

                if (event.getName().endsWith("/contents")) {
                    log.debug("Encountered event {}",event.getName());
                    if (atNinestars) {
                        File filePath = new File(scratchFolder, datafile);
                        InputStream jpylizerOutput = jpylize(filePath);
                        validator.validate(datafile, Strings.flush(jpylizerOutput), resultCollector);
                    }
                } else {
                    if (event.getName().endsWith("jpylizer.xml")) {
                        validator.validate(datafile, Strings.flush(event.getData()), resultCollector);
                    }
                }
            }
        } catch (IOException e) {
            //TODO map exception to resultCollector
        }
    }

    @Override
    public void handleFinish() {
        //Anything to do here?
    }

    private InputStream jpylize(File dataPath) {

        log.info("Running jpylyzer on file {}", dataPath);
        ProcessRunner runner = new ProcessRunner(jpylyzerPath, dataPath.getAbsolutePath());
        runner.setOutputCollectionByteSize(Integer.MAX_VALUE);
        runner.run();
        if (runner.getReturnCode() == 0) {
            return runner.getProcessOutput();
        } else {
            throw new RuntimeException(
                    "failed to run jpylyzer, returncode:" + runner.getReturnCode() + ", stdOut:" + runner
                            .getProcessOutputAsString() + " stdErr:" + runner.getProcessErrorAsString());
        }
    }


}
