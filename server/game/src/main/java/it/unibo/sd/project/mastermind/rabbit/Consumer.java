package it.unibo.sd.project.mastermind.rabbit;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class Consumer {

    private final String EXCHANGE_NAME;
    private final Map<MessageType, DeliverCallback> callback;

    public Consumer(String exchange_name, Map<MessageType, DeliverCallback> callback) {
        EXCHANGE_NAME = exchange_name;
        this.callback = callback;
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
        callback.forEach((messageType, deliverCallback) -> {
            try {
                String queueName = channel.queueDeclare().getQueue();
                channel.queueBind(queueName, EXCHANGE_NAME, messageType.getType());
                channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
    }
}
