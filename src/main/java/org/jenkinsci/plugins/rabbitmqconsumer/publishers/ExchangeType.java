package org.jenkinsci.plugins.rabbitmqconsumer.publishers;

/**
 * A enum for exchange types.
 * 
 * @author rinrinne a.k.a. rin_ne
 */
public enum ExchangeType {
    /**
     *  direct type. routingkey is mandatory.
     */
    DIRECT,

    // fanout type. routingkey is optional.
    FANOUT,

    // fanout type. routingkey is mandatory. it accepts pattern.
    TOPIC
}
