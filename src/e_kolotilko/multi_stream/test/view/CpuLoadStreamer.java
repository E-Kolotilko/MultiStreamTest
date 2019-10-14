package e_kolotilko.multi_stream.test.view;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import e_kolotilko.multi_stream.test.monitoring.CpuMonitorHolder;
import e_kolotilko.multi_stream.test.monitoring.ICpuLoadGetter;
import e_kolotilko.multi_stream.test.routine.SimpleWriter;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * This servlet is supposed to stream MJPEG to receivers.
 * 
 */
@SuppressWarnings("serial")
public class CpuLoadStreamer extends HttpServlet implements ICpuLoadGetter {
    static SimpleWriter writer = new SimpleWriter();
    static public final Color FONT_COLOR = new Color(0,0,0);
    static public final Color BACK_COLOR = new Color(255,255,255);
    static public final int PIC_HIGHT = 500;
    static public final int PIC_WIDTH = 500;
    static public final int STRING_X = 70;
    static public final int STRING_Y = 300;
    static public final int FONT_SIZE = PIC_WIDTH/2;
    
    static final String NL = "\r\n";
    static final String NL_DOUBLE = NL+NL;
    static final String BOUNDARY = "myboundary";
    static final String HEAD = "--"+BOUNDARY + NL +
          "Content-Type: image/jpeg" + NL 
          + "Content-Length: ";
    
    //Max wait time for update on CPU load. Used to be sure that client is still connected 
    public static final long MAX_WAIT_TIME = 10000;
    //Simple sync object to make all clients receive update almost at the same time
    static Object globalSync = new Object();
    
    double loadPercent;
    //temporary image object. Used to get bytes for sending
    BufferedImage image = new BufferedImage(PIC_WIDTH, PIC_HIGHT, BufferedImage.TYPE_INT_RGB);
    Graphics tempGraph;
    //byte array representing an image
    byte[] imageBytes;
    
    /**
     * Callback used by monitor for providing an update on CPU load
     */
    @Override
    public void getCpuLoad(double loadPercent) {
        this.loadPercent = loadPercent;
        //TODO : move out to another thread to let monitor go fast
        setImageBytes(loadPercent);
        synchronized (globalSync) {
            globalSync.notifyAll();
        }
    }

    /**
     * Draw an image and set up new image array 
     * @param loadPercent
     */
    void setImageBytes(double loadPercent) {
        //
        //loadPercent = Math.round(loadPercent*100.0)/100.0;
        int percent = (int)Math.round(loadPercent);
        //
        tempGraph.setColor(BACK_COLOR);
        tempGraph.fillRect(0, 0, PIC_WIDTH, PIC_HIGHT);
        String loadString = String.valueOf(percent);
        tempGraph.setColor(FONT_COLOR);
        tempGraph.drawString(loadString, STRING_X, STRING_Y);
        byte[] newImageBytes = getBytesFromImage(image);
        this.imageBytes = newImageBytes;
    }
    
    @Override
    public void init() throws ServletException {
        super.init();        
        tempGraph = image.getGraphics();
        tempGraph.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,FONT_SIZE));
        setImageBytes(this.loadPercent);
        CpuMonitorHolder.subToLoadInfoUpdate(this);
    }
    
    @Override
    public void destroy() {
        super.destroy();
        CpuMonitorHolder.unsubFromLoadInfoUpdate(this);
        if (tempGraph!=null) {
            tempGraph.dispose();
        }
    }
    
    /**
     * Start a stream
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("Cache-Control", 
                "no-store, no-cache, must-revalidate, pre-check=0, post-check=0, max-age=0");
        response.setContentType("multipart/x-mixed-replace;boundary="+BOUNDARY);
        try (OutputStream out = response.getOutputStream()) {
            while (true) { //work until client disconnects   
                //writing even if nothing changed to test if client is still online
                if (imageBytes != null) {
                    out.write( (HEAD + imageBytes.length + NL_DOUBLE).getBytes(StandardCharsets.UTF_8) );
                    out.write(imageBytes);
                    out.flush();
                }     
                synchronized (globalSync) {
                    globalSync.wait(MAX_WAIT_TIME);
                }
            }
            
        }
        catch (IOException e) {
            //Nothing to do. We expect client to break connection
            System.out.println("Thread is stopping as expected"); //TODO : replace with logger
        }
        catch (Exception e) {
            //Unexpected. Logging
            System.out.println("Exception caught. Error:" + e);
        }
    }
        
    
    static protected byte[] getBytesFromImage(BufferedImage image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "jpeg", baos);
        } catch (IOException e) {
            System.out.println("Exception while converting image to bytes. Message:"+e);
            return null;
        }
        return baos.toByteArray();
    }
    
    
}
