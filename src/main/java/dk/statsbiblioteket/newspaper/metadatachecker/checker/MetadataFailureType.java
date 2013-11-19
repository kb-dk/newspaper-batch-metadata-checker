package dk.statsbiblioteket.newspaper.metadatachecker.checker;

public enum MetadataFailureType {
    METADATA("metadata");

    private final String value;

    MetadataFailureType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static MetadataFailureType fromValue(String v) {
        for (MetadataFailureType c: MetadataFailureType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
