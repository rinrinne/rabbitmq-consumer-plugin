package org.jenkinsci.plugins.rabbitmqconsumer;

import java.io.IOException;
import java.text.MessageFormat;
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
import org.jenkinsci.plugins.rabbitmqconsumer.channels.ControlRMQChannel;
import org.jenkinsci.plugins.rabbitmqconsumer.channels.RMQChannel;
import org.jenkinsci.plugins.rabbitmqconsumer.extensions.MessageQueueListener;
import org.jenkinsci.plugins.rabbitmqconsumer.extensions.ServerOperator;
import org.jenkinsci.plugins.rabbitmqconsumer.listeners.RMQChannelListener;
import org.jenkinsci.plugins.rabbitmqconsumer.listeners.RMQConnectionListener;
import java.util.logging.Logger;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;

/**
 * A utility class to declare mock.
 *
 * @author rinrinne a.k.a. rin_ne
 */
public class Mocks {

    private static final Logger LOGGER = Logger.getLogger(Mocks.class.getName());

    public static final Stack<Consumer> consumerPool = new Stack<Consumer>();
    public static final List<String> responseArray = new CopyOnWriteArrayList<String>();
    public static final Set<MessageQueueListener> mqListenerSet = new CopyOnWriteArraySet<MessageQueueListener>();
    public static final Set<ServerOperator> operatorSet = new CopyOnWriteArraySet<ServerOperator>();

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

    public static final class ConsumeRMQChannelMock extends MockUp<ConsumeRMQChannel> {

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

    public static final class ControlRMQChannelMock extends MockUp<ControlRMQChannel> {
        @Mock
        public void close(Invocation invocation) {
            invocation.proceed();
            ControlRMQChannel ch = invocation.getInvokedInstance();
            ch.shutdownCompleted(null);
        }
    }

    public static final class RMQChannelListenerMock implements RMQChannelListener {
        public void onOpen(AbstractRMQChannel rmqChannel) {
            LOGGER.info(MessageFormat.format("Open ConsumeRMQChannelMock channel {0} for {1}.",
                    rmqChannel.getChannel().getChannelNumber(), ((ConsumeRMQChannel)rmqChannel).getQueueName()));
        }
        public void onCloseCompleted(AbstractRMQChannel rmqChannel) {
            LOGGER.info(MessageFormat.format("Closed ConsumeRMQChannelMock channel {0} for {1}.",
                    rmqChannel.getChannel().getChannelNumber(), ((ConsumeRMQChannel)rmqChannel).getQueueName()));
        }
    }

    public static final class RMQConnectionListenerMock implements RMQConnectionListener {
        public void onOpen(RMQConnection rmqConnection) {
            LOGGER.info("Open RabbitMQ connection.");
        }
        public void onCloseCompleted(RMQConnection rmqConnection) {
            LOGGER.info("Closed RabbitMQ connection.");
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
            LOGGER.info(MessageFormat.format("<{0}> Bind queue: {1}", name, queueName));
        }
        @Override
        public void onUnbind(String queueName) {
            LOGGER.info(MessageFormat.format("<{0}> Unbind queue: {1}",name, queueName));
        }
        @Override
        public void onReceive(String queueName, String contentType, Map<String, Object> headers, byte[] body) {
            LOGGER.info(MessageFormat.format("<{0}> Received: {1}", name, queueName));
            responseArray.add(getName());
        }
    }

    public static final class ServerOperatorMock extends ServerOperator {

        @Override
        public void OnOpen(RMQChannel controlChannel) {
            LOGGER.info(MessageFormat.format("Open control channel {0}.", controlChannel.getChannel().getChannelNumber()));
        }

        @Override
        public void OnCloseCompleted(RMQChannel controlChannel) {
            LOGGER.info(MessageFormat.format("Closed control channel {0}.", controlChannel.getChannel().getChannelNumber()));
        }

        @Override
        public void OnOpenConsumer(RMQChannel controlChannel, String queueName, HashSet<String> appIds) {
            LOGGER.info(MessageFormat.format("Open consumer: queue \"{0}\" for {1}.",
                    queueName, appIds.toString()));
        }

        @Override
        public void OnClosedComsumer(RMQChannel controlChannel, String queueName, HashSet<String> appIds) {
            LOGGER.info(MessageFormat.format("Closed consumer: queue \"{0}\" for {1}.",
                    queueName, appIds.toString()));
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

    public static final class OnOpenDelegation implements Delegate<ServerOperator> {
        void fireOnOpen(ControlRMQChannel controlChannel) throws IOException {
            for (ServerOperator l : operatorSet) {
                l.OnOpen(controlChannel);
            }
        }
    }

    public static final class OnCloseCompletedDelegation implements Delegate<ServerOperator> {
        void fireOnCloseCompleted(ControlRMQChannel controlChannel) throws IOException {
            for (ServerOperator l : operatorSet) {
                l.OnCloseCompleted(controlChannel);
            }
        }
    }

    public static final class OnOpenConsumerDelegation implements Delegate<ServerOperator> {
        void fireOnOpenConsumer(ControlRMQChannel controlChannel, String queueName, HashSet<String> appIds) throws IOException {
            for (ServerOperator l : operatorSet) {
                l.OnOpenConsumer(controlChannel, queueName, appIds);
            }
        }
    }

    public static final class OnClosedConsumerDelegation implements Delegate<ServerOperator> {
        void fireOnClosedConsumer(ControlRMQChannel controlChannel, String queueName, HashSet<String> appIds) throws IOException {
            for (ServerOperator l : operatorSet) {
                l.OnClosedComsumer(controlChannel, queueName, appIds);
            }
        }
    }
}
