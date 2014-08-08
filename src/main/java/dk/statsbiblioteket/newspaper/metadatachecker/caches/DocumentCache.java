package dk.statsbiblioteket.newspaper.metadatachecker.caches;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.util.caching.TimeSensitiveCache;
import dk.statsbiblioteket.util.xml.DOM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import java.io.IOException;

public class DocumentCache {
    static Logger log = LoggerFactory.getLogger(DocumentCache.class);

    public TimeSensitiveCache<String,Document> documentCache = new TimeSensitiveCache<>(100,true,10);

    public DocumentCache() {
    }

    public synchronized Document getDocument(AttributeParsingEvent event, Boolean namespaceAware)
            throws IOException {
        Document document = documentCache.get(event.getName());
        if (document == null) {
            document = DOM.streamToDOM(event.getData(), namespaceAware);
            documentCache.put(event.getName(), document);
        }
        return document;
    }
}
