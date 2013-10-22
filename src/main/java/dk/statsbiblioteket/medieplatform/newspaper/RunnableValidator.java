package dk.statsbiblioteket.medieplatform.newspaper;

import dk.statsbiblioteket.medieplatform.autonomous.AbstractRunnableComponent;
import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.DataFileNodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.DataFileNodeEndsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.ParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.TreeIterator;
import dk.statsbiblioteket.util.Strings;
import dk.statsbiblioteket.util.console.ProcessRunner;
import dk.statsbiblioteket.util.xml.DOM;
import org.w3c.dom.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

/**
 * The validator as a runnable component
 */
public class RunnableValidator
        extends AbstractRunnableComponent {

    private final Validator validator;
    private boolean atNinestars = false;
    private final String jpylyzerPath;

    /**
     * Create a new runnable validator. It uses the optional property field "controlPolicies" to denote the path to
     * the control policies
     * @param properties the properties
     * @throws FileNotFoundException if the control policies file cannot be read
     */
    public RunnableValidator(Properties properties)
            throws
            FileNotFoundException {
        super(properties);
        String controlPolicies = getProperties().getProperty("controlPolicies");
        Document controlPoliciesDocument;
        if (controlPolicies != null){
            controlPoliciesDocument = DOM.streamToDOM(new FileInputStream(controlPolicies));
        } else {
            controlPoliciesDocument = DOM.streamToDOM(Thread.currentThread().getContextClassLoader().getResourceAsStream("defaultControlPolicies.xml"));
        }
        jpylyzerPath = getProperties().getProperty("jpylyzerPath","src/main/extras/jpylyzer-1.10.1/jpylyzer.py");

        validator = new ValidatorFactory(controlPoliciesDocument,properties).createValidator();


    }

    /**
     * Constructor. Use this to set the atNinestars field
     * @param properties
     * @param atNinestars if true, it will attempt to run jpylizer when encountering a .jp2 file, instead of expecting
     *                    such jpylizer file to be in the tree
     * @throws FileNotFoundException if the control policies cannot be found
     * @see #RunnableValidator(java.util.Properties)
     */
    public RunnableValidator(Properties properties,
                                boolean atNinestars)
            throws
            FileNotFoundException {
        this(properties);
        this.atNinestars = atNinestars;
    }

    @Override
    public String getComponentName() {
        return "JPylizer_jpeg2k_validator";
    }

    @Override
    public String getComponentVersion() {
        return getClass().getPackage().getImplementationVersion();
    }

    @Override
    public String getEventID() {
        return "Data_Validated";  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void doWorkOnBatch(Batch batch,
                              ResultCollector resultCollector)
            throws
            Exception {


        TreeIterator iterator = createIterator(batch);
        boolean isInDataFile = false;
        String datafile = null;
        while (iterator.hasNext()) {
            ParsingEvent event = iterator.next();
            switch (event.getType()) {
                case NodeBegin:
                    if (event instanceof DataFileNodeBeginsParsingEvent) {
                        isInDataFile = true;
                        datafile = event.getName();
                    }
                    break;
                case NodeEnd:
                    if (event instanceof DataFileNodeEndsParsingEvent) {
                        isInDataFile = false;
                        datafile = null;
                    }
                    break;
                case Attribute:
                    if (isInDataFile) {
                        AttributeParsingEvent attributeParsingEvent = (AttributeParsingEvent) event;
                        if (event.getName().endsWith("/contents")) {
                            if (atNinestars) {
                                File filePath = new File(getProperties().getProperty("scratch"),
                                                         datafile);
                                InputStream jpylizerOutput = jpylize(filePath);
                                validator.validate(datafile, Strings.flush(jpylizerOutput), resultCollector);
                            }
                        } else {
                            if (event.getName().endsWith("jpylizer.xml")) {

                                validator.validate(datafile,
                                                   Strings.flush(attributeParsingEvent.getData()), resultCollector);
                            }
                        }
                    }
                    break;
            }
        }
    }

    private InputStream jpylize(File dataPath) {

        ProcessRunner runner = new ProcessRunner(jpylyzerPath, dataPath.getAbsolutePath());
        runner.setOutputCollectionByteSize(Integer.MAX_VALUE);
        runner.run();
        if (runner.getReturnCode() == 0){
            return runner.getProcessOutput();
        } else {
            throw new RuntimeException("failed to run jpylyzer, returncode:"+runner.getReturnCode()+", stdOut:"+runner.getProcessOutputAsString()+" stdErr:"+runner.getProcessErrorAsString());
        }
    }
}
