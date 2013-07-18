package org.jenkinsci.plugins.rabbitmqconsumer.publishers;

/**
 * A class to get result for publish.
 * 
 * @author rinrinne a.k.a. rin_ne
 * 
 */
public class PublishResult {
    
    private boolean isSucess;
    private String message;
    private String exchangeName;
    private String queueName;
    private ExchangeType exchangeType;
    private String routingKey;
    
    /**
     * Create instance.
     *
     * @param isSuccess the value of whether publish is succeeded or not.
     * @param message the string of this result.
     * @param exchangeName the exchange name.
     */
    public PublishResult(boolean isSuccess, String message, String exchangeName) {
        this(isSuccess, message, exchangeName, null, null, null);
    }

    /**
     * Create instance.
     *
     * @param isSuccess the value of whether publish is succeeded or not.
     * @param message the string of this result.
     * @param exchangeName the exchange name.
     * @param queueName the queue name.
     * @param exchangeType the exchange type.
     * @param routingKey the routing key.
     */
    public PublishResult(boolean isSuccess, String message, String exchangeName,
            String queueName, ExchangeType exchangeType, String routingKey) {
        this.isSucess = isSuccess;
        this.message = message;
        this.exchangeName = exchangeName;
        this.queueName = queueName;
        this.exchangeType = exchangeType;
        this.routingKey = routingKey;
    }
    
    /**
     * Gets result status.
     * @return true if publish is successful.
     */
    public boolean isSuccess() {
        return isSucess;
    }

    /**
     * Gets result message.
     * @return result message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Gets exchange name.
     * @return exchange name.
     */
    public String getExchangeName() {
        return exchangeName;
    }

    /**
     * Gets queue name.
     * @return queue name.
     */
    public String getQueueName() {
        return queueName;
    }

    /**
     * Gets exchange type.
     * @return exchange type.
     */
    public ExchangeType getExchangeType() {
        return exchangeType;
    }

    /**
     * Gets routing key.
     * @return routing key.
     */
    public String getRoutingKey() {
        return routingKey;
    }
}