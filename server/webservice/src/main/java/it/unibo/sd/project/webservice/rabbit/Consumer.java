package it.unibo.sd.project.webservice.rabbit;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Consumer {

    private final String EXCHANGE_NAME;
    private final DeliverCallback callback;
    private final MessageType msgType;

    public Consumer(String exchange_name, DeliverCallback callback, MessageType type) {
        EXCHANGE_NAME = exchange_name;
        this.callback = callback;
        this.msgType = type;
        try {
            start();
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    private void start() throws IOException, TimeoutException {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(System.getenv("RABBIT_HOST"));
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, "direct");
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, EXCHANGE_NAME, msgType.getType());

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
        channel.basicConsume(queueName, true, callback, consumerTag -> { });
    }
}
