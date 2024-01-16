package it.unibo.sd.project.mastermind.rabbit;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class RPCClient implements AutoCloseable {

    private final Connection connection;
    private final Channel channel;
    private static final String EXCHANGE_NAME = "Web";

    public RPCClient() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(System.getenv("RABBIT_HOST"));

        connection = factory.newConnection();
        channel = connection.createChannel();
    }

    public void call(MessageType messageType, String message, Consumer<String> responseConsumer) {
        final String corrId = UUID.randomUUID().toString();
        String replyQueueName;
        try {
            replyQueueName = channel.queueDeclare().getQueue();
            AMQP.BasicProperties props = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(corrId)
                    .replyTo(replyQueueName)
                    .build();
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");
            channel.basicPublish(EXCHANGE_NAME,messageType.getType(),
                    props, message.getBytes(StandardCharsets.UTF_8));
            System.out.println("[x] Sent '" + messageType.getType() + "':'" + message + "'");
            channel.queuePurge(replyQueueName);
            channel.basicQos(1);
            channel.basicConsume(replyQueueName, true, (consumerTag, delivery) -> {
                if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                    responseConsumer.accept(new String(delivery.getBody(), StandardCharsets.UTF_8));
                }
            }, consumerTag -> {});
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void close() throws IOException {
        connection.close();
    }
}
