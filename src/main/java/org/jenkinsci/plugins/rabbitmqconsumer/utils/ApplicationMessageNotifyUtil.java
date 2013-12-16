package org.jenkinsci.plugins.rabbitmqconsumer.utils;

import hudson.ExtensionList;

import java.util.HashSet;
import java.util.logging.Logger;

import jenkins.model.Jenkins;

import org.jenkinsci.plugins.rabbitmqconsumer.listeners.ApplicationMessageListener;

/**
 * Utility class to notify application message to listener.
 *
 * @author rinrinne a.k.a. rin_ne
 */
@Deprecated
public final class ApplicationMessageNotifyUtil {
    private static final Logger LOGGER = Logger.getLogger(ApplicationMessageNotifyUtil.class.getName());

    /**
     * Constructor.
     */
    @Deprecated
    private ApplicationMessageNotifyUtil() {
    }

    /**
     * Fires OnReceive event.
     *
     * @param appIds
     *            the hashset of application ids.
     * @param queueName
     *            the queue name.
     * @param json
     *            the json object.
     */
    @Deprecated
    public static void fireOnReceive(HashSet<String> appIds, String queueName, String contentType, byte[] body) {
        LOGGER.entering("DefaultApplicationMessageListener", "fireOnReceive");
        for (ApplicationMessageListener l : getAllListeners()) {
            if (appIds.contains(l.getAppId())) {
                l.onReceive(queueName, contentType, body);
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
    @Deprecated
    public static void fireOnBind(HashSet<String> appIds, String queueName) {
        LOGGER.entering("DefaultApplicationMessageListener", "fireOnBind");
        for (ApplicationMessageListener l : getAllListeners()) {
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
    @Deprecated
    public static void fireOnUnbind(HashSet<String> appIds, String queueName) {
        LOGGER.entering("DefaultApplicationMessageListener", "fireOnUnbind");
        for (ApplicationMessageListener l : getAllListeners()) {
            if (appIds.contains(l.getAppId())) {
                l.onUnbind(queueName);
            }
        }
    }

    /**
     * Gets all listeners implements {@link ApplicationMessageListener}.
     *
     * @return the extension list implements {@link ApplicationMessageListener}.
     */
    @Deprecated
    public static ExtensionList<ApplicationMessageListener> getAllListeners() {
        return Jenkins.getInstance().getExtensionList(ApplicationMessageListener.class);
    }
}
