package org.jenkinsci.plugins.rabbitmqconsumer.extensions;

import java.io.IOException;
import java.util.HashSet;
import java.util.logging.Logger;

import jenkins.model.Jenkins;
import hudson.ExtensionList;

import org.apache.tools.ant.ExtensionPoint;
import org.jenkinsci.plugins.rabbitmqconsumer.channels.ControlRMQChannel;

/**
 * RabbitMQ server operation class.
 *
 * @author rinrinne a.k.a. rin_ne
 */
public abstract class ServerOperator extends ExtensionPoint {

    private static final Logger LOGGER = Logger.getLogger(ServerOperator.class.toString());

    /**
     * Calls when channel is opened.
     *
     * @param controlChannel
     *            the control channel.
     * @throws IOException if ControlRMQChannel has somthing wrong.
     */
    public abstract void OnOpen(ControlRMQChannel controlChannel) throws IOException;

    /**
     * Calls when channel is closed.
     *
     * @param controlChannel
     *            the control channel.
     * @throws IOException if ControlRMQChannel has somthing wrong.
     */
    public abstract void OnCloseCompleted(ControlRMQChannel controlChannel) throws IOException;

    /**
     * Calls when channel for consumer is opened. (before binding queue)
     *
     * @param controlChannel
     *            the control channel.
     * @param queueName
     *            the queue name.
     * @param appIds
     *            the list of app ID.
     * @throws IOException if ControlRMQChannel has somthing wrong.
     */
    public abstract void OnOpenConsumer(ControlRMQChannel controlChannel,
            String queueName, HashSet<String> appIds) throws IOException;

    /**
     * Calls when channle for consumer is closed. (after unbinding queue)
     *
     * @param controlChannel
     *            the control channel.
     * @param queueName
     *            the queue name.
     * @param appIds
     *            the list of app ID.
     * @throws IOException if ControlRMQChannel has somthing wrong.
     */
    public abstract void OnClosedComsumer(ControlRMQChannel controlChannel,
            String queueName, HashSet<String> appIds) throws IOException;

    /**
     * Fires OnOpen event.
     *
     * @param controlChannel
     *            the control channel.
     * @throws IOException if ControlRMQChannel has somthing wrong.
     */
    public static void fireOnOpen(ControlRMQChannel controlChannel) throws IOException {
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
    public static void fireOnCloseCompleted(ControlRMQChannel controlChannel) throws IOException {
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
    public static void fireOnOpenConsumer(ControlRMQChannel controlChannel,
            String queueName, HashSet<String> appIds) throws IOException {
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
    public static void fireOnClosedConsumer(ControlRMQChannel controlChannel,
            String queueName, HashSet<String> appIds) throws IOException {
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
