package org.jenkinsci.plugins.rabbitmqconsumer.channels;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.Arrays;
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
import mockit.Mocked;
import mockit.NonStrictExpectations;

import org.apache.commons.codec.CharEncoding;
import org.jenkinsci.plugins.rabbitmqconsumer.extensions.MessageQueueListener;
import org.jenkinsci.plugins.rabbitmqconsumer.listeners.RMQChannelListener;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;

public class ConsumeRMQChannelTest {

    RMQChannelListener chListener = new RMQChannelListener() {

        public void onOpen(AbstractRMQChannel rmqChannel) {
            System.out.println("Open ConsumeRMQChannel.");
        }

        public void onCloseCompleted(AbstractRMQChannel rmqChannel) {
            System.out.println("Closed ConsumeRMQChannel.");
        }
    };

    @Mocked
    MessageQueueListener mqListener = null;

    MessageQueueListener listener;
    final Set<MessageQueueListener> mqListenerSet = new CopyOnWriteArraySet<MessageQueueListener>();

    final Stack<Consumer> consumerPool = new Stack<Consumer>();
    final List<String> responseArray = new CopyOnWriteArrayList<String>();

    Connection connection;
    Channel channel;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        channel = new MockUp<Channel>() {
            @Mock
            public String basicConsume(Invocation invocation, String queue, boolean autoAck, Consumer callback) {
                consumerPool.push(callback);
                return "consumerTag";
            }
        }.getMockInstance();
        connection = new MockUp<Connection>() {
            @Mock
            Channel createChannel() {
                return channel;
            }
        }.getMockInstance();

        new MockUp<ConsumeRMQChannel>() {
            @Mock
            private boolean isEnableDebug() {
                return false;
            }

            @Mock
            public void close(Invocation invocation) {
                invocation.proceed();
                ConsumeRMQChannel ch = invocation.getInvokedInstance();
                ch.shutdownCompleted(null);
            }
        };

        listener = new MessageQueueListener() {

            @Override
            public String getName() {
                return "listener-1";
            }

            @Override
            public String getAppId() {
                return "app-1";
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
        };
        mqListenerSet.add(listener);

        listener = new MessageQueueListener() {

            @Override
            public String getName() {
                return "listener-2";
            }

            @Override
            public String getAppId() {
                return "app-2";
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
                responseArray.add(getName());
            }
        };
        mqListenerSet.add(listener);

        new NonStrictExpectations() {{
            channel.close();
            channel.basicAck(anyLong, anyBoolean);

            MessageQueueListener.fireOnBind((HashSet<String>) any, anyString);
            result = new Delegate() {
                void fireOnBind(HashSet<String> appIds, String queueName) {
                    for (MessageQueueListener l : mqListenerSet) {
                        if (appIds.contains(l.getAppId())) {
                            l.onBind(queueName);
                        }
                    }
                }
            };
            MessageQueueListener.fireOnUnbind((HashSet<String>) any, anyString);
            result = new Delegate() {
                void fireOnBind(HashSet<String> appIds, String queueName) {
                    for (MessageQueueListener l : mqListenerSet) {
                        if (appIds.contains(l.getAppId())) {
                            l.onUnbind(queueName);
                        }
                    }
                }
            };
            MessageQueueListener.fireOnReceive(anyString,
                    anyString,
                    anyString,
                    (Map<String, Object>) any,
                    (byte[]) any);
            result = new Delegate() {
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
            };
        }};
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testConsume() {
        HashSet<String> appIds = new HashSet<String>();
        appIds.addAll(Arrays.asList("app-1" ,"app-2", "app-3"));
        Envelope envelope = new Envelope(0L, false, "exchange-1", "test.app");

        AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
        builder.appId("app-1");
        builder.contentEncoding(CharEncoding.UTF_8);
        builder.contentType("application/json");

        AMQP.BasicProperties props = builder.build();

        ConsumeRMQChannel channel = new ConsumeRMQChannel("theQueue", appIds);
        channel.addRMQChannelListener(chListener);
        try {
            channel.open(connection);
            channel.consume();

            Consumer consumer = consumerPool.pop();
            consumer.handleDelivery("consumerTag", envelope, props, "Test message".getBytes());

            assertEquals(1, responseArray.size());
            assertThat("Unmatch consumed queue.", responseArray.get(0), is("listener-1"));
            channel.close();
        } catch (Exception ex) {
            fail(ex.toString());
        }
    }

}
