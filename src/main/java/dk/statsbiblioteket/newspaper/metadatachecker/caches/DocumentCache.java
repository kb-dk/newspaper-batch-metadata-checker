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

    public TimeSensitiveCache<String,Document> documentCache = new TimeSensitiveCache<>(10000,true,20);

    public DocumentCache() {
    }

    public synchronized Document getDocument(AttributeParsingEvent event)
            throws IOException {
        Document document = documentCache.get(event.getName());
        if (document == null) {
            log.debug("Parsing event '{}' as DOM, because not found in cache", event.getName());
            document = DOM.streamToDOM(event.getData(), true);
            documentCache.put(event.getName(), document);
        }
        return document;
    }

    public synchronized void cacheDocument(AttributeParsingEvent event, Document document) {
        documentCache.put(event.getName(), document);
    }
}
