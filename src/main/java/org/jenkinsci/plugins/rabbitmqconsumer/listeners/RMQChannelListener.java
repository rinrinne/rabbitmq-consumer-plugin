package org.jenkinsci.plugins.rabbitmqconsumer.listeners;

import org.jenkinsci.plugins.rabbitmqconsumer.RMQChannel;

/**
 * Listener interface for {@link RMQChannel}.
 * 
 * @author rinrinne a.k.a. rin_ne
 */
public interface RMQChannelListener {
    /**
     * Calls when close process for channel is completed.
     * 
     * @param rmqChannel
     *            the closed channel.
     */
    void onCloseCompleted(RMQChannel rmqChannel);

    /**
     * Calls when channel is opend.
     * 
     * @param rmqChannel
     *            the channel.
     */
    void onOpen(RMQChannel rmqChannel);
}
