package e_kolotilko.multi_stream.test.view;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import e_kolotilko.multi_stream.test.monitoring.CpuMonitorHolder;
import e_kolotilko.multi_stream.test.routine.SimpleWriter;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class CpuRequestOnce  extends HttpServlet {
    static SimpleWriter writer = new SimpleWriter();
    static final Color MAIN_COLOR = new Color(250,0,0);
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("image/jpeg");
        try (OutputStream out = response.getOutputStream()) {
            BufferedImage initialImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);            
            int load = (int) Math.round(CpuMonitorHolder.getLoadPercent());
            
            String test = String.valueOf(load);            
            Graphics tempGraph = initialImage.getGraphics();
            tempGraph.setColor(MAIN_COLOR);
            tempGraph.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,20));
            tempGraph.clearRect(0, 0, 200, 200);
            tempGraph.drawString(test, 50, 50);
            
            byte[] imageBytes = getBytesFromImage(initialImage);
            tempGraph.dispose();
            
            if (imageBytes != null) {
                out.write(imageBytes);
                out.flush();
            }
            else {
                try {
                    response.sendError(500, "Server failed to convert image");
                    return;
                } catch (IOException e) {
                    System.out.println("AND it was unable to report error properly!!! Message:"+e);
                    return;
                }
            }

        }
        catch (IOException e) {
            //Nothing to do. We expect this
            System.out.println("Thread is stopping as expected");
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
