package org.jenkinsci.plugins.rabbitmqconsumer.utils;

import hudson.ExtensionList;

import java.util.HashSet;
import java.util.logging.Logger;

import jenkins.model.Jenkins;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.jenkinsci.plugins.rabbitmqconsumer.listeners.ApplicationMessageListener;

/**
 * Utility class to notify application message to listener.
 * 
 * @author rinrinne a.k.a. rin_ne
 */
public final class ApplicationMessageNotifyUtil {
    private static final Logger LOGGER = Logger.getLogger(ApplicationMessageNotifyUtil.class.getName());

    /**
     * Constructor.
     */
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
    public static void fireOnReceive(HashSet<String> appIds, String queueName, Object json) {
        LOGGER.entering("DefaultApplicationMessageListener", "fireOnReceive");
        for (ApplicationMessageListener l : getAllListeners()) {
            if (appIds.contains(l.getAppId())) {
                try {
                    JSONObject jsonObj = (JSONObject) JSONSerializer.toJSON(json);
                    l.onReceive(queueName, jsonObj);
                } catch (JSONException e) {
                    LOGGER.warning(e.toString());
                }
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
    public static ExtensionList<ApplicationMessageListener> getAllListeners() {
        return Jenkins.getInstance().getExtensionList(ApplicationMessageListener.class);
    }
}
