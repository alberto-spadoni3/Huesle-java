package it.unibo.sd.project.mastermind.rabbit;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class SimpleConsumer {

    private final String EXCHANGE_NAME;

    public SimpleConsumer(String exchange_name, MessageType msgType, DeliverCallback callback) {
        EXCHANGE_NAME = exchange_name;
        try {
            start(msgType, callback);
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    private void start(MessageType msgType, DeliverCallback callback) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(System.getenv("RABBIT_HOST"));
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, "direct");
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, EXCHANGE_NAME, msgType.getType());
        channel.basicConsume(queueName, true, callback, consumerTag -> { });
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
    }
}
