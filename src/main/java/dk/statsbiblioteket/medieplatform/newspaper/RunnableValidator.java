package dk.statsbiblioteket.medieplatform.newspaper;

import dk.statsbiblioteket.medieplatform.autonomous.AbstractRunnableComponent;
import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.DataFileNodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.DataFileNodeEndsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.ParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.TreeIterator;

import java.io.InputStream;
import java.util.Properties;

public class RunnableValidator
        extends AbstractRunnableComponent {

    private boolean atNinestars = false;

    protected RunnableValidator(Properties properties) {
        super(properties);

    }

    protected RunnableValidator(Properties properties,
                                boolean atNinestars) {
        super(properties);
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

        Validator validator = new ValidatorFactory().createValidator();
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
                        if (event.getName().endsWith("/contents")) {
                            AttributeParsingEvent attributeParsingEvent = (AttributeParsingEvent) event;
                            if (atNinestars) {
                                InputStream jpylizerOutput = jpylize(attributeParsingEvent.getData());
                                validator.validate(attributeParsingEvent.getName(), jpylizerOutput, resultCollector);
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
