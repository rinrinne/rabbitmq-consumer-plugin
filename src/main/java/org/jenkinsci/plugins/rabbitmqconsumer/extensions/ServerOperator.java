package org.jenkinsci.plugins.rabbitmqconsumer.extensions;

import java.io.IOException;
import java.util.HashSet;
import java.util.logging.Logger;

import jenkins.model.Jenkins;
import hudson.ExtensionList;

import org.apache.tools.ant.ExtensionPoint;
import org.jenkinsci.plugins.rabbitmqconsumer.channels.RMQChannel;

/**
 * RabbitMQ server operation class.
 *
 * @author rinrinne a.k.a. rin_ne
 */
public abstract class ServerOperator extends ExtensionPoint {

    private static final Logger LOGGER = Logger.getLogger(ServerOperator.class.toString());

    /**
     * Calls when channel is opened.
     * You must not hold given values from control channel.
     *
     * @param controlChannel
     *            the control channel.
     * @throws IOException if ControlRMQChannel has somthing wrong.
     */
    public abstract void OnOpen(RMQChannel controlChannel) throws IOException;

    /**
     * Calls when channel is closed.
     * You must not hold given values from control channel.
     *
     * @param controlChannel
     *            the control channel.
     * @throws IOException if ControlRMQChannel has somthing wrong.
     */
    public abstract void OnCloseCompleted(RMQChannel controlChannel) throws IOException;

    /**
     * Calls when channel for consumer is opened. (before binding queue)
     * You must not hold given values from control channel.
     *
     * @param controlChannel
     *            the control channel.
     * @param queueName
     *            the queue name.
     * @param appIds
     *            the list of app ID.
     * @throws IOException if ControlRMQChannel has somthing wrong.
     */
    public abstract void OnOpenConsumer(RMQChannel controlChannel,
            String queueName, HashSet<String> appIds) throws IOException;

    /**
     * Calls when channle for consumer is closed. (after unbinding queue)
     * You must not hold given values from control channel.
     *
     * @param controlChannel
     *            the control channel.
     * @param queueName
     *            the queue name.
     * @param appIds
     *            the list of app ID.
     * @throws IOException if ControlRMQChannel has somthing wrong.
     */
    public abstract void OnClosedComsumer(RMQChannel controlChannel,
            String queueName, HashSet<String> appIds) throws IOException;

    /**
     * Fires OnOpen event.
     *
     * @param controlChannel
     *            the control channel.
     * @throws IOException if ControlRMQChannel has somthing wrong.
     */
    public static void fireOnOpen(RMQChannel controlChannel) throws IOException {
        LOGGER.entering("ServerOperator", "fireOnOpen");
        for (ServerOperator l : all()) {
            l.OnOpen(controlChannel);
        }
    }

    /**
     * Fires OnCloseCompleted event.
     *
     * @param controlChannel
     *            the control channel.
     * @throws IOException if ControlRMQChannel has somthing wrong.
     */
    public static void fireOnCloseCompleted(RMQChannel controlChannel) throws IOException {
        LOGGER.entering("ServerOperator", "fireOnCloseCompleted");
        for (ServerOperator l : all()) {
            l.OnCloseCompleted(controlChannel);
        }
    }

    /**
     * Fires OnBeforeBind event.
     *
     * @param controlChannel
     *            the control channel.
     * @param queueName
     *            the queue name.
     * @param appIds
     *            the list of app ID.
     * @throws IOException if ControlRMQChannel has somthing wrong.
     */
    public static void fireOnOpenConsumer(RMQChannel controlChannel,
            String queueName, HashSet<String> appIds) throws IOException {
        LOGGER.entering("ServerOperator", "fireOnOpenConsumer");
        for (ServerOperator l : all()) {
            l.OnOpenConsumer(controlChannel, queueName, appIds);
        }
    }

    /**
     * Fires OnAfterUnbind event.
     *
     * @param controlChannel
     *            the control channel.
     * @param queueName
     *            the queue name.
     * @param appIds
     *            the list of app ID.
     * @throws IOException if ControlRMQChannel has somthing wrong.
     */
    public static void fireOnClosedConsumer(RMQChannel controlChannel,
            String queueName, HashSet<String> appIds) throws IOException {
        LOGGER.entering("ServerOperator", "fireOnClosedConsumer");
        for (ServerOperator l : all()) {
            l.OnClosedComsumer(controlChannel, queueName, appIds);
        }
    }

    /**
     * Gets all listeners.
     *
     * @return the extension list.
     */
    public static ExtensionList<ServerOperator> all() {
        return Jenkins.getInstance().getExtensionList(ServerOperator.class);
    }
}
