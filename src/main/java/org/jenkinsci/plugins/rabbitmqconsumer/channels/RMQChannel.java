package org.jenkinsci.plugins.rabbitmqconsumer.channels;

import com.rabbitmq.client.Channel;

/**
 * Interface class for passing channel to extension.
 *
 * @author rinrinne a.k.a. rin_ne
 */
public interface RMQChannel {

    /**
     * Gets instance of Channel, not RMQChannel.
     *
     * @return the instance of Channel.
     */
    Channel getChannel();
}
