package org.jenkinsci.plugins.rabbitmqconsumer.publishers;

/**
 * A factory class for RabbitMQ publish channel.
 * 
 * @author rinrinne a.k.a. rin_ne
 */
public class PublishChannelFactory {

    private PublishChannelFactory() {
    }

    /**
     * Gets {@link PublishChannel}.
     * Note that you should not keep this instance.
     *
     * @return a instance.
     */
    public static PublishChannel getPublishChannel() {
        return org.jenkinsci.plugins.rabbitmqconsumer.RMQManager.getInstance().getPublishChannel();
    }
}
