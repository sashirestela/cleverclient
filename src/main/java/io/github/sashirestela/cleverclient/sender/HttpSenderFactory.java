package io.github.sashirestela.cleverclient.sender;

import io.github.sashirestela.cleverclient.support.CleverClientException;
import io.github.sashirestela.cleverclient.support.ReturnType;
import io.github.sashirestela.cleverclient.support.ReturnType.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.function.Supplier;

/**
 * Factory for the abstrac class HttpSender.
 */
public class HttpSenderFactory {

    private static Logger logger = LoggerFactory.getLogger(HttpSenderFactory.class);

    private static HttpSenderFactory factory = null;

    private EnumMap<Category, Supplier<HttpSender>> sendersMap;

    private HttpSenderFactory() {
        sendersMap = new EnumMap<>(Category.class);
        sendersMap.put(Category.ASYNC_STREAM, HttpAsyncStreamSender::new);
        sendersMap.put(Category.ASYNC_LIST, HttpAsyncListSender::new);
        sendersMap.put(Category.ASYNC_GENERIC, HttpAsyncGenericSender::new);
        sendersMap.put(Category.ASYNC_OBJECT, HttpAsyncObjectSender::new);
        sendersMap.put(Category.ASYNC_BINARY, HttpAsyncBinarySender::new);
        sendersMap.put(Category.ASYNC_PLAIN_TEXT, HttpAsyncPlainTextSender::new);
        sendersMap.put(Category.SYNC_STREAM, HttpSyncStreamSender::new);
        sendersMap.put(Category.SYNC_LIST, HttpSyncListSender::new);
        sendersMap.put(Category.SYNC_GENERIC, HttpSyncGenericSender::new);
        sendersMap.put(Category.SYNC_OBJECT, HttpSyncObjectSender::new);
        sendersMap.put(Category.SYNC_BINARY, HttpSyncBinarySender::new);
        sendersMap.put(Category.SYNC_PLAIN_TEXT, HttpSyncPlainTextSender::new);
    }

    public static HttpSenderFactory get() {
        if (factory == null) {
            factory = new HttpSenderFactory();
        }
        return factory;
    }

    /**
     * Instances a HttpSender concrete class based on the return type.
     * 
     * @param returnType The method return type.
     * @return A HttpSender concrete class.
     */
    public HttpSender createSender(ReturnType returnType) {
        HttpSender sender = null;
        var category = returnType.category();
        if (category != null && sendersMap.containsKey(category)) {
            sender = sendersMap.get(category).get();
            logger.debug("Created Sender : {}", sender.getClass().getSimpleName());
        } else {
            throw new CleverClientException("Unsupported return type {0}.", returnType.getFullClassName(), null);
        }
        return sender;
    }

}
