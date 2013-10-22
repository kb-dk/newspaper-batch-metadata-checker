package dk.statsbiblioteket.medieplatform.newspaper;

/**
 * Use this class to create new instances of the Validator
 */
public class ValidatorFactory {


    /**
     * Construct a new factory
     */
    public ValidatorFactory() {
    }

    /**
     * Create a validator
     * @return a new validator
     */
    public Validator createValidator(){
        return new DelegatingValidator(new SchemaValidator("jpylizer.xsd"),new SchematronValidator("sb-jp2-demands.sch"));

    }
}
