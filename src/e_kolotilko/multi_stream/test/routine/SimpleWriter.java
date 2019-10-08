package e_kolotilko.multi_stream.test.routine;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

public class SimpleWriter {

	public void writeText(HttpServletResponse response, String text) {
		response.setContentType("text/plain");
		try (PrintWriter writer = response.getWriter()) {
			writer.print(text);
			writer.flush();
		} catch (IOException e) {
			System.out.println("Exception while writing text. Error: "+e);
		}
	}
	
	public void writeSingleImage(HttpServletResponse response, byte[] image, String mime) {
        response.setContentType(mime);
        try(OutputStream out = response.getOutputStream()) {
            out.write(image);
            out.flush();
        }
        catch (Exception e) {
            System.out.println("Exception while writing image. Error: " + e);
        }
	}
	
}
