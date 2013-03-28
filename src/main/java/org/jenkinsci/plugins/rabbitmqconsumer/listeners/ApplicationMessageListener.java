package org.jenkinsci.plugins.rabbitmqconsumer.listeners;

import net.sf.json.JSONObject;

/**
 * Listener interface for Application message. Note that this interface should
 * be implemented to extension (c.f. descriptor).
 * 
 * @author rinrinne a.k.a. rin_ne
 */
public interface ApplicationMessageListener {
    /**
     * Gets name.
     * 
     * @return the name.
     */
    String getName();

    /**
     * Gets application id.
     * 
     * @return the application id.
     */
    String getAppId();

    /**
     * Calls when binds to queue.
     * 
     * @param queueName
     *            the queue name.
     */
    void onBind(String queueName);

    /**
     * Calls when unbinds from queue.
     * 
     * @param queueName
     *            the queue name.
     */
    void onUnbind(String queueName);

    /**
     * Calls when message arrives.
     * 
     * @param queueName
     *            the queue name.
     * @param json
     *            the content of message.
     */
    void onReceive(String queueName, JSONObject json);
}
