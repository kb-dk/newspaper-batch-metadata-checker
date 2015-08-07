package dk.statsbiblioteket.newspaper.metadatachecker;

/**
 * Created by abr on 8/7/15.
 */
public class AttributeSpec {

    private String postfix;
    private String xsdFile;
    private String schematronFile;
    private String messagePrefix;
    private String type;

    public AttributeSpec(String postfix, String xsdFile, String schematronFile, String messagePrefix, String type) {
        this.postfix = postfix;
        this.xsdFile = xsdFile;
        this.schematronFile = schematronFile;
        this.messagePrefix = messagePrefix;
        this.type = type;
    }

    public String getPostfix() {
        return postfix;
    }

    public String getXsdFile() {
        return xsdFile;
    }

    public String getSchematronFile() {
        return schematronFile;
    }

    public String getMessagePrefix() {
        return messagePrefix;
    }

    public String getType() {
        return type;
    }
}
