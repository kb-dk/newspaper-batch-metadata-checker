package dk.statsbiblioteket.newspaper.metadatachecker.mockers;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import java.io.ByteArrayInputStream;
import java.io.InputStream;


public class AttributeParsingEventMocker extends AttributeParsingEvent {
    private String DATA = "testing testing testing";
    private String md5Checksum = "8050d75a0aca4f7fd5387d8f8cfc82b3";

    public AttributeParsingEventMocker() {
        super("mockup AttributeParsingEvent");
    }

    public AttributeParsingEventMocker(String md5Checksum) {
        super("mockup AttributeParsingEvent");
        this.md5Checksum = md5Checksum;
    }

    public String getChecksum() {
        return md5Checksum;
    }

    public InputStream getData() {
        return new ByteArrayInputStream(DATA.getBytes());
    }
}
