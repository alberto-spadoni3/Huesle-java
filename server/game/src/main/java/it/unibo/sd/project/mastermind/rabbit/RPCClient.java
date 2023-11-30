package it.unibo.sd.project.mastermind.rabbit;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class RPCClient implements AutoCloseable {

    private Connection connection;
    private Channel channel;
    private static final String EXCHANGE_NAME = "Web";

    public RPCClient() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(System.getenv("RABBIT_HOST"));

        connection = factory.newConnection();
        channel = connection.createChannel();
    }

    public void call(MessageType messageType, String message, Consumer<String> responseConsumer) {
        final String corrId = UUID.randomUUID().toString();
        String replyQueueName = null;
        try {
            replyQueueName = channel.queueDeclare().getQueue();
            AMQP.BasicProperties props = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(corrId)
                    .replyTo(replyQueueName)
                    .build();
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");
            channel.basicPublish(EXCHANGE_NAME,messageType.getType(),
                    props, message.getBytes("UTF-8"));
            System.out.println(" [x] Sent '" + messageType.getType() + "':'" + message + "'");
            String ctag = null;
            channel.queuePurge(replyQueueName);
            channel.basicQos(1);
            ctag = channel.basicConsume(replyQueueName, true, (consumerTag, delivery) -> {
                if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                    responseConsumer.accept(new String(delivery.getBody(), "UTF-8"));
                }
            }, consumerTag -> {});
            //channel.basicCancel(ctag);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() throws IOException {
        connection.close();
    }
}
