package dk.statsbiblioteket.medieplatform.newspaper;

public class ValidatorFactory {


    public ValidatorFactory() {
    }

    public Validator createValidator(){
        return new DelegatingValidator(new SchemaValidator());

    }
}
