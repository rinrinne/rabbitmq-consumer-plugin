package org.jenkinsci.plugins.rabbitmqconsumer.listeners;

/**
 * Listener interface for Application message. Note that this interface should
 * be implemented to extension (c.f. descriptor).
 *
 * @author rinrinne a.k.a. rin_ne
 */
@Deprecated
public interface ApplicationMessageListener {
    /**
     * Gets name.
     *
     * @return the name.
     */
    @Deprecated
    String getName();

    /**
     * Gets application id.
     *
     * @return the application id.
     */
    @Deprecated
    String getAppId();

    /**
     * Calls when binds to queue.
     *
     * @param queueName
     *            the queue name.
     */
    @Deprecated
    void onBind(String queueName);

    /**
     * Calls when unbinds from queue.
     *
     * @param queueName
     *            the queue name.
     */
    @Deprecated
    void onUnbind(String queueName);

    /**
     * Calls when message arrives.
     *
     * @param queueName
     *            the queue name.
     * @param json
     *            the content of message.
     */
    @Deprecated
    void onReceive(String queueName, String contentType, byte[] body);
}
