package e_kolotilko.multi_stream.test.view;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BorderCrossWeirdTest extends HttpServlet {
    static final String BOUNDARY = "myboundary";
    static final String NL = "\r\n";
    static final String NL_DOUBLE = NL+NL;
    static final String HEAD = "--"+BOUNDARY + NL +
          "Content-Type: text/plain; charset=UTF-8" + NL 
          + "Content-Length: ";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse response) {
        response.setHeader("Cache-Control", 
                "no-store, no-cache, must-revalidate, pre-check=0, post-check=0, max-age=0");
        response.setContentType("multipart/x-mixed-replace;boundary="+BOUNDARY);
        try (OutputStream out = response.getOutputStream()) {
            char test = '0';
            while (true) { 
                out.write(test);
                out.write('\n');
                out.flush();
                
                Thread.sleep(2000);
                if (++test>'z')
                    test='0';
            }
            
        }
        catch (IOException e) {
            //Nothing to do. We expect client to break connection
            System.out.println("Thread is stopping as expected");
        }
        catch (Exception e) {
            //Unexpected. Logging
            System.out.println("Exception caught. Error:" + e);
        }
    }
    
}
