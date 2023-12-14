package it.unibo.sd.project.mastermind.rabbit;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

public class RPCServer implements Runnable {
    private static final String EXCHANGE_NAME = "Web";
    private final Map<MessageType, Function<String, String>> map;

    public RPCServer(Map<MessageType, Function<String,String>> map) {
        this.map = map;
    }

    @Override
    public void run() {
        try {
            startServer();
        } catch (IOException | TimeoutException e) {
            System.out.println(e.getMessage());
        }
    }

    private void startServer() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(System.getenv("RABBIT_HOST"));
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            channel.exchangeDeclare(EXCHANGE_NAME, "direct");
            var callbacks = createCallbackMap(channel);

            System.out.println("[x] Awaiting RPC requests");

            Object monitor = new Object();
            callbacks.forEach((queue, callback) -> {
                try {
                    channel.basicConsume(
                            queue,
                            false,
                            getDeliverCallback(callback, channel, monitor),
                            consumerTag -> { });
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }
            });

            //Wait and be prepared to consume the message from RPC client.
            while (true) {
                synchronized (monitor) {
                    try {
                        monitor.wait();
                    } catch (InterruptedException e) {
                        System.err.println(e.getMessage());
                    }
                }
            }
        }
    }

    private Map<String, Function<String,String>> createCallbackMap(Channel channel) {
        Map<String, Function<String,String>> callbackMap = new HashMap<>();
        map.forEach((t,c)-> {
            try {
                String queueName = channel.queueDeclare().getQueue();
                channel.queueBind(queueName, EXCHANGE_NAME, t.getType());
                callbackMap.put(queueName, c);
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        });
        return callbackMap;
    }

    private DeliverCallback getDeliverCallback(Function<String,String> callback, Channel channel, Object monitor){
       return  (consumerTag, delivery) -> {
            AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(delivery.getProperties().getCorrelationId())
                    .build();

            String response = "";
            try {
                response = callback.apply(new String(delivery.getBody(), "UTF-8"));
            } catch (RuntimeException e) {
                System.out.println(" [.] " + e);
            } finally {
                System.out.println("RPC server responding " + response + " in queue " + delivery.getProperties().getReplyTo());
                channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, response.getBytes("UTF-8"));
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

                //RabbitMq consumer worker thread notifies the RPC server owner thread
                synchronized (monitor) {
                    monitor.notify();
                }
            }
        };
    }
}