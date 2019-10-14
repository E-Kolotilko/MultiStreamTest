package e_kolotilko.multi_stream.test.view;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;

import e_kolotilko.multi_stream.test.rabbit.SimpleSenderHolder;

/**
 * This servlet was a test of 
 * 
 * 1. using "multipart/x-mixed-replace" mime type with text to achieve stream-like behavior
 * Result: server can send strings converted to bytes and client can accumulate it rather naturally 
 * using XMLHttpRequest
 * 
 * 2. Receiving messages from rabbitmq (before moving on to front-end solution   
 *
 */
@SuppressWarnings("serial")
public class BorderCrossWeirdTest extends HttpServlet {
    static final String BOUNDARY = "myboundary";
    static final String NL = "\r\n";
    static final String NL_DOUBLE = NL+NL;
    static final String HEAD = "--"+BOUNDARY + NL +
          "Content-Type: text/plain; charset=UTF-8" + NL 
          + "Content-Length: ";
    
    ConnectionFactory factory;

    @Override
    public void init() throws ServletException {
        super.init();
        factory = new ConnectionFactory();
        factory.setHost(SimpleSenderHolder.HOST);
        factory.setUsername("reader_tester");
        factory.setPassword("reader");
        factory.setAutomaticRecoveryEnabled(true);
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse response) {
        response.setHeader("Cache-Control", 
                "no-store, no-cache, must-revalidate, pre-check=0, post-check=0, max-age=0");
        response.setContentType("multipart/x-mixed-replace;boundary="+BOUNDARY);
        try (Connection connection = factory.newConnection();
                Channel channel = connection.createChannel();
                OutputStream out = response.getOutputStream()) {
            channel.exchangeDeclare(SimpleSenderHolder.EXCHANGE_NAME, "fanout");
            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, SimpleSenderHolder.EXCHANGE_NAME, "");
            while (true) {
                GetResponse rabbitResponse = null;
                try {
                    rabbitResponse = channel.basicGet(queueName, true);
                    System.out.println("Basic get done");
                }
                catch (IOException e) {
                    System.out.println("Basic get exception:");
                }
                if (rabbitResponse!=null) {
                    out.write(rabbitResponse.getBody());
                    out.write('\n');
                    out.flush();
                    String message = new String(rabbitResponse.getBody(), "UTF-8");
                    System.out.println(message);
                }
                
                Thread.sleep(2000);
            }
            
        }
        catch (IOException e) {
            //Nothing to do. We expect client to break connection
            //System.out.println("Thread is stopping as expected");
            //BUT! If misconfigured, rabbit can deny actions with same IOException
            e.printStackTrace();
        }
        catch (Exception e) {
            //Unexpected. Logging
            System.out.println("Exception caught. Error:" + e);
        }
    }
    
}
