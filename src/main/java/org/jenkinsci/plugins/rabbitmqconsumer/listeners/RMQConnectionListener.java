package org.jenkinsci.plugins.rabbitmqconsumer.listeners;

import org.jenkinsci.plugins.rabbitmqconsumer.RMQConnection;

/**
 * Listener interface for {@link RMQConnection}.
 * 
 * @author rinrinne a.k.a. rin_ne
 */
public interface RMQConnectionListener {
    /**
     * Calls when closing process for connection is completed.
     * 
     * @param rmqConnection
     *            the closed connection.
     */
    void onCloseCompleted(RMQConnection rmqConnection);

    /**
     * Calls when connection is opend.
     * 
     * @param rmqConnection
     *            the connection.
     */
    void onOpen(RMQConnection rmqConnection);
}
