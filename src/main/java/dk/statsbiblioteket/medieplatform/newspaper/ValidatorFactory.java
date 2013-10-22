package dk.statsbiblioteket.medieplatform.newspaper;

import org.w3c.dom.Document;

/** Use this class to create new instances of the Validator */
public class ValidatorFactory {


    /**
     * Construct a new factory from the given control policies
     * @param controlPolicies rdf scape control policies detailing the things to validate
     */
    public ValidatorFactory(Document controlPolicies) {
    }

    /**
     * Create a validator
     *
     * @return a new validator
     */
    public Validator createValidator() {
        return new DelegatingValidator(new SchemaValidator("jpylizer.xsd"),
                                       new SchematronValidator("sb-jp2.sch"));

    }
}
