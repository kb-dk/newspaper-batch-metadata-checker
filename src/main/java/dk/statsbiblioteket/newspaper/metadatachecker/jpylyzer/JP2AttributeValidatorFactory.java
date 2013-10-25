package dk.statsbiblioteket.newspaper.metadatachecker.jpylyzer;

import org.w3c.dom.Document;

/** Use this class to create new instances of the Validator */
public class JP2AttributeValidatorFactory {


    private final Document controlPolicies;

    /**
     * Construct a new factory from the given control policies
     * @param controlPolicies rdf scape control policies detailing the things to validate
     *
     */
    public JP2AttributeValidatorFactory(Document controlPolicies) {
        this.controlPolicies = controlPolicies;
    }

    /**
     * Create a validator
     *
     * @return a new validator
     */
    public AttributeValidator createValidator() {
        return new DelegatingAttributeValidator(new SchemaAttributeValidator("jpylizer.xsd"),
                                       new SchematronAttributeValidator("sb-jp2.sch"));

    }
}