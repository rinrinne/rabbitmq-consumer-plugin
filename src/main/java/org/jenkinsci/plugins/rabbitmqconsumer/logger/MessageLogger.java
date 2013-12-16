package org.jenkinsci.plugins.rabbitmqconsumer.logger;

import hudson.Extension;

import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;

import org.jenkinsci.plugins.rabbitmqconsumer.RabbitmqConsumeItem;
import org.jenkinsci.plugins.rabbitmqconsumer.listeners.MessageQueueListener;

/**
 * Extension for logging messages. This is debug purpose.
 *
 * @author rinrinne a.k.a. rin_ne
 */
@Extension
public class MessageLogger extends MessageQueueListener {
    private static final Logger LOGGER = Logger.getLogger(MessageLogger.class.getName());
    private static final String PLUGIN_NAME = "Message Logger for debug";

    /**
     * @inheritDoc
     * @return the name.
     */
    public String getName() {
        return PLUGIN_NAME;
    }

    /**
     * @inheritDoc
     * @return the application id.
     */
    public String getAppId() {
        return RabbitmqConsumeItem.DEBUG_APPID;
    }

    /**
     * @inheritDoc
     * @param queueName
     *            the queue name that bind to.
     */
    public void onBind(String queueName) {
        LOGGER.info("Bind to " + queueName);
    }

    /**
     * @inheritDoc
     * @param queueName
     *            the queue name that unbind from.
     */
    public void onUnbind(String queueName) {
        LOGGER.info("Unbind from " + queueName);
    }

    /**
     * @inheritDoc
     * @param queueName
     *            the queue name that receive from.
     * @param json
     *            the content of message.
     */
    public void onReceive(String queueName, String contentType, byte[] body) {
        String msg;
        try {
            msg = new String(body, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            msg = "<Unsupported Encoding>";
        }
        LOGGER.info("Receive: (" + contentType + ") " + msg);
    }
}
