package org.jenkinsci.plugins.rabbitmqconsumer;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import mockit.Delegate;
import mockit.Invocation;
import mockit.Mock;
import mockit.MockUp;

import org.jenkinsci.plugins.rabbitmqconsumer.channels.AbstractRMQChannel;
import org.jenkinsci.plugins.rabbitmqconsumer.channels.ConsumeRMQChannel;
import org.jenkinsci.plugins.rabbitmqconsumer.extensions.MessageQueueListener;
import org.jenkinsci.plugins.rabbitmqconsumer.listeners.RMQChannelListener;
import org.jenkinsci.plugins.rabbitmqconsumer.listeners.RMQConnectionListener;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;

/**
 * A utility class to declare mock.
 *
 * @author rinrinne a.k.a. rin_ne
 */
public class Mocks {

    public static final Stack<Consumer> consumerPool = new Stack<Consumer>();
    public static final List<String> responseArray = new CopyOnWriteArrayList<String>();
    public static final Set<MessageQueueListener> mqListenerSet = new CopyOnWriteArraySet<MessageQueueListener>();


    public static final class ChannelMock extends MockUp<Channel> {
        @Mock
        public void close() {
        }

        @Mock
        public void basicAck(long deliveryTag, boolean multiple) {
        }

        @Mock
        public String basicConsume(Invocation invocation, String queue, boolean autoAck, Consumer callback) {
            consumerPool.push(callback);
            return "consumerTag";
        }
    }

    public static final class RMQConnectionMock extends MockUp<RMQConnection> {
        @Mock
        public void close(Invocation invocation) {
            invocation.proceed();
            RMQConnection conn = invocation.getInvokedInstance();
            conn.shutdownCompleted(null);
        }
    }

    public static final class ComsumeRMQChannelMock extends MockUp<ConsumeRMQChannel> {

        @Mock
        public boolean isEnableDebug() {
            return false;
        }

        @Mock
        public void close(Invocation invocation) {
            invocation.proceed();
            ConsumeRMQChannel ch = invocation.getInvokedInstance();
            ch.shutdownCompleted(null);
        }
    }

    public static final class RMQChannelListenerMock implements RMQChannelListener {
        public void onOpen(AbstractRMQChannel rmqChannel) {
            System.out.println("Open ConsumeRMQChannel.");
        }
        public void onCloseCompleted(AbstractRMQChannel rmqChannel) {
            System.out.println("Closed ConsumeRMQChannel.");
        }
    }

    public static final class RMQConnectionListenerMock implements RMQConnectionListener {
        public void onOpen(RMQConnection rmqConnection) {
            System.out.println("Open RMQConnection.");
        }
        public void onCloseCompleted(RMQConnection rmqConnection) {
            System.out.println("Closed RMQConnection.");
        }
    }

    public static final class MessageQueueListenerMock extends MessageQueueListener {

        private final String name;
        private final String appId;

        public MessageQueueListenerMock(String name, String appId) {
            this.name = name;
            this.appId = appId;
        }

        @Override
        public String getName() {
            return name;
        }
        @Override
        public String getAppId() {
            return appId;
        }
        @Override
        public void onBind(String queueName) {
            System.out.println("Bind queue: " + queueName);
        }
        @Override
        public void onUnbind(String queueName) {
            System.out.println("Unbind queue: " + queueName);
        }
        @Override
        public void onReceive(String queueName, String contentType, Map<String, Object> headers, byte[] body) {
            System.out.println("Received: " + queueName);
            responseArray.add(getName());
        }
    }

    public static final class OnBindDelegation implements Delegate<MessageQueueListener> {
        void fireOnBind(HashSet<String> appIds, String queueName) {
            for (MessageQueueListener l : mqListenerSet) {
                if (appIds.contains(l.getAppId())) {
                    l.onBind(queueName);
                }
            }
        }
    }

    public static final class OnUnbindDelegation implements Delegate<MessageQueueListener> {
        void fireOnUnbind(HashSet<String> appIds, String queueName) {
            for (MessageQueueListener l : mqListenerSet) {
                if (appIds.contains(l.getAppId())) {
                    l.onUnbind(queueName);
                }
            }
        }
    }

    public static final class OnReceiveDelegation implements Delegate<MessageQueueListener> {
        void fireOnReceive(String appId,
                String queueName,
                String contentType,
                Map<String, Object> headers,
                byte[] body) {
            for (MessageQueueListener l : mqListenerSet) {
                if (appId.equals(l.getAppId())) {
                    l.onReceive(queueName, contentType, headers, body);
                }
            }
        }
    }
}
