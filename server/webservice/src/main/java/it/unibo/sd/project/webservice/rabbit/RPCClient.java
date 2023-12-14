package it.unibo.sd.project.webservice.rabbit;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class RPCClient implements AutoCloseable {

    private final Connection connection;
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
        try {
            if(channel.isOpen()) {
                String replyQueueName = channel.queueDeclare().getQueue();
                AMQP.BasicProperties props = new AMQP.BasicProperties
                        .Builder()
                        .correlationId(corrId)
                        .replyTo(replyQueueName)
                        .build();
                channel.exchangeDeclare(EXCHANGE_NAME, "direct");
                channel.basicPublish(EXCHANGE_NAME, messageType.getType(),
                        props, message.getBytes("UTF-8"));
                System.out.println("[x] Sent '" + messageType.getType() + "':'" + message + "'");
                //channel.queuePurge(replyQueueName);
                channel.basicQos(1); // accept only one unack-ed message at a time
                channel.basicConsume(replyQueueName, false, (consumerTag, delivery) -> {
                    if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                        responseConsumer.accept(new String(delivery.getBody(), "UTF-8"));
                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    }
                }, consumerTag -> { });
            }else{
                channel = connection.createChannel();
                call(messageType,message,responseConsumer);
            }

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public void close() throws IOException {
        connection.close();
    }
}
