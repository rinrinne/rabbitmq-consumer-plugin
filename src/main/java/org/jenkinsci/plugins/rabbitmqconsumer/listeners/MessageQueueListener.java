package org.jenkinsci.plugins.rabbitmqconsumer.listeners;

import java.util.HashSet;
import java.util.Map;
import java.util.logging.Logger;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import jenkins.model.Jenkins;

/**
 * Listener for message queue.
 *
 * @author rinrinne a.k.a. rin_ne
 */
public abstract class MessageQueueListener implements ExtensionPoint {
    private static final Logger LOGGER = Logger.getLogger(MessageQueueListener.class.getName());

    /**
     * Gets name.
     *
     * @return the name.
     */
    public abstract String getName();

    /**
     * Gets application id.
     *
     * @return the application id.
     */
    public abstract String getAppId();

    /**
     * Calls when binds to queue.
     *
     * @param queueName
     *            the queue name.
     */
    public abstract void onBind(String queueName);

    /**
     * Calls when unbinds from queue.
     *
     * @param queueName
     *            the queue name.
     */
    public abstract void onUnbind(String queueName);

    /**
     * Calls when message arrives.
     *
     * @param queueName
     *            the queue name.
     * @param headers
     *            the map of headers.
     * @param json
     *            the content of message.
     */
    public abstract void onReceive(String queueName, String contentType, Map<String, Object> headers, byte[] body);

    /**
     * Fires OnReceive event.
     *
     * @param appIds
     *            the hashset of application ids.
     * @param queueName
     *            the queue name.
     * @param contentType
     *            the type of content.
     * @param headers
     *            the map of headers.
     * @param body
     *            the message body.
     */
    public static void fireOnReceive(HashSet<String> appIds,
            String queueName,
            String contentType,
            Map<String, Object> headers,
            byte[] body) {
        LOGGER.entering("MessageQueueListener", "fireOnReceive");
        for (MessageQueueListener l : all()) {
            if (appIds.contains(l.getAppId())) {
                l.onReceive(queueName, contentType, headers, body);
            }
        }
    }

    /**
     * Fires OnBind event.
     *
     * @param appIds
     *            the hashset of application ids.
     * @param queueName
     *            the queue name.
     */
    public static void fireOnBind(HashSet<String> appIds, String queueName) {
        LOGGER.entering("MessageQueueListener", "fireOnBind");
        for (MessageQueueListener l : all()) {
            if (appIds.contains(l.getAppId())) {
                l.onBind(queueName);
            }
        }
    }

    /**
     * Fires OnUnbind event.
     *
     * @param appIds
     *            the hashset of application ids.
     * @param queueName
     *            the queue name.
     */
    public static void fireOnUnbind(HashSet<String> appIds, String queueName) {
        LOGGER.entering("MessageQueueListener", "fireOnUnbind");
        for (MessageQueueListener l : all()) {
            if (appIds.contains(l.getAppId())) {
                l.onUnbind(queueName);
            }
        }
    }

    /**
     * Gets all listeners.
     *
     * @return the extension list.
     */
    public static ExtensionList<MessageQueueListener> all() {
        return Jenkins.getInstance().getExtensionList(MessageQueueListener.class);
    }
}
