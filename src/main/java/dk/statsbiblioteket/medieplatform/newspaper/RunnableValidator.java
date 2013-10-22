package dk.statsbiblioteket.medieplatform.newspaper;

import dk.statsbiblioteket.medieplatform.autonomous.AbstractRunnableComponent;
import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.DataFileNodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.DataFileNodeEndsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.ParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.TreeIterator;
import dk.statsbiblioteket.util.xml.DOM;
import org.w3c.dom.Document;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

public class RunnableValidator
        extends AbstractRunnableComponent {

    private final Validator validator;
    private boolean atNinestars = false;

    protected RunnableValidator(Properties properties)
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
        validator = new ValidatorFactory(controlPoliciesDocument).createValidator();

    }

    protected RunnableValidator(Properties properties,
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
        while (iterator.hasNext()) {
            ParsingEvent event = iterator.next();
            switch (event.getType()) {
                case NodeBegin:
                    if (event instanceof DataFileNodeBeginsParsingEvent) {
                        isInDataFile = true;
                    }
                    break;
                case NodeEnd:
                    if (event instanceof DataFileNodeEndsParsingEvent) {
                        isInDataFile = false;
                    }
                    break;
                case Attribute:
                    if (isInDataFile) {
                        AttributeParsingEvent attributeParsingEvent = (AttributeParsingEvent) event;
                        if (event.getName().endsWith("/contents")) {
                            if (atNinestars) {
                                InputStream jpylizerOutput = jpylize(attributeParsingEvent.getData());
                                validator.validate(attributeParsingEvent.getName(), jpylizerOutput, resultCollector);
                            }
                        } else {
                            if (event.getName().endsWith("jpylizer.xml")) {
                                validator.validate(attributeParsingEvent.getName(), attributeParsingEvent.getData(), resultCollector);
                            }
                        }
                    }
                    break;
            }
        }
    }

    private InputStream jpylize(InputStream data) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream("jpylizer.xml");
    }
}
