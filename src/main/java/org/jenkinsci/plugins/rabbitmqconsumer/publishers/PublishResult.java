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
    
    /**
     * Create instance.
     *
     * @param isSuccess the value of whether publish is succeeded or not.
     * @param message the string of this result.
     */
    public PublishResult(boolean isSuccess, String message) {
        this.isSucess = isSuccess;
        this.message = message;
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
}