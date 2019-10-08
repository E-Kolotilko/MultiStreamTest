package e_kolotilko.multi_stream.test.view;

import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import e_kolotilko.multi_stream.test.monitoring.CpuMonitorHolder;
import e_kolotilko.multi_stream.test.routine.SimpleWriter;
import e_kolotilko.multi_stream.test.routine.Utility;

@SuppressWarnings("serial")
public class Admin extends HttpServlet {
    SimpleWriter writer = new SimpleWriter();
        
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		writer.writeText(response, "Border now:" + CpuMonitorHolder.getBorderLoad());
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {				
		//String action = request.getParameter("action");
		
		String querryString = Utility.getBody(request);
		Map<String,String> params = Utility.processSimpleRequest(querryString);
		String action = params.get("action");
		
		if (action == null) {
			writer.writeText(response, "No action found");
			return;
		}
		String message;
		switch (action) {
		  case "stop": {
		      CpuMonitorHolder.stopWorking(); 
			  message = "Monitor will be stopped after " + CpuMonitorHolder.getTriggerTime();
		  } break;
		  default: {
		      message = "Action is not recognised";
		  } break;
		}
		writer.writeText(response, message);
	}
	
}


