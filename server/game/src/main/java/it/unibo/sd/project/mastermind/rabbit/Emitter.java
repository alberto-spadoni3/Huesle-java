package it.unibo.sd.project.mastermind.rabbit;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;

public class Emitter {

    private final String EXCHANGE_NAME;

    public Emitter(String exchange_name) {
        EXCHANGE_NAME = exchange_name;
    }

    public void emit(MessageType messageType, String message){
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(System.getenv("RABBIT_HOST"));
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
                channel.exchangeDeclare(EXCHANGE_NAME, "direct");
                channel.basicPublish(EXCHANGE_NAME, messageType.getType(),
                null, message.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(" [x] Sent '" + messageType.getType() + "':'" + message + "'");
    }
}
