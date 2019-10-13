package e_kolotilko.multi_stream.test.rabbit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import e_kolotilko.multi_stream.test.monitoring.IBorderEventGetter;

public class SimpleSender implements IBorderEventGetter//, Runnable 
{
    static Logger aLogger = LogManager.getLogger();
    
    String host;
    String exchangeName;
    String jsonNamePercent;
    String jsonNameTime;
    ConnectionFactory factory;
    //Deque<String> messages = new LinkedList<>();
    
    public SimpleSender(String host, String exchangeName, String jsonNamePercent, String jsonNameTime) {
        this.host = host;
        this.exchangeName = exchangeName;
        this.jsonNamePercent =jsonNamePercent;
        this.jsonNameTime = jsonNameTime;
        factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setUsername("write_tester");
        factory.setPassword("111222333");
    }

    @Override
    public void getBorderEventData(double percent, long time) {
        JSONObject toSend = new JSONObject();
        toSend.put(jsonNamePercent, percent);
        toSend.put(jsonNameTime, time);
        String message = toSend.toString();
        //TODO: put to queue and notify
        synchronized (this) {
            try (Connection connection = factory.newConnection();
                    Channel channel = connection.createChannel()) {
                channel.exchangeDeclare(exchangeName, "fanout");
                channel.basicPublish(exchangeName, "", null, message.getBytes("UTF-8"));
               }
            catch (Exception e) {
                aLogger.error(e);
            }
        }
    }
    
}
