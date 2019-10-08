package e_kolotilko.multi_stream.test.routine;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

public class Utility {
	
	static public Map<String,String> processSimpleRequest(String requestString){
		Map<String,String> result = new TreeMap<String,String>();
		
		if ((requestString == null) || (!requestString.contains("="))) {
			return result;
		}
		
		String[] splitted = requestString.split("&");
		for (String pair : splitted) {
			String[] params = pair.split("=");
			if (params.length!=2) continue;
			result.put(params[0], params[1]);
		}
		
		return result;
	}
	
	static public String getBody(HttpServletRequest request) {
		String encoding = request.getCharacterEncoding();
		if (encoding == null) {
			encoding = StandardCharsets.UTF_8.name();
		}
		
		StringBuilder builder = new StringBuilder();
		String line;
		try (BufferedReader reader = 
				new BufferedReader( new InputStreamReader(request.getInputStream(),encoding))) {
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
		}
		catch (Exception e) {
			System.out.println("Exception while getting body"+e);
		}
		return builder.toString();
	}
	
}
