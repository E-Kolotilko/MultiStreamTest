package e_kolotilko.multi_stream.test.view;

import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import e_kolotilko.multi_stream.test.monitoring.CpuMonitorHolder;
import e_kolotilko.multi_stream.test.rabbit.SimpleSenderHolder;
import e_kolotilko.multi_stream.test.routine.SimpleWriter;
import e_kolotilko.multi_stream.test.routine.Utility;

@SuppressWarnings("serial")
public class Admin extends HttpServlet {
    static Logger aLogger = LogManager.getLogger();
    
    SimpleWriter writer = new SimpleWriter();
    
    @Override
    public void init() throws ServletException {
        super.init();
        aLogger.info("Poking rabbit... Has subbed:"+SimpleSenderHolder.hasSubbed());
    }
	
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
		  case "stopMonitor": {
		      CpuMonitorHolder.stopWorking(); 
			  message = "Monitor will be stopped after " + CpuMonitorHolder.getTriggerTime();
		  } break;
		  case "setBorderValue":{
		      String borderValueString = params.get("loadBorderValue");
		      if (borderValueString == null) {
		          message = "Border value not found in param string";
		          break;
		      }
		      try {
		          double borderValue = Double.parseDouble(borderValueString);
		          boolean valueIsSet = CpuMonitorHolder.setBorderLoad(borderValue);
		          if (valueIsSet) {
		              message = "New load border value is set";
		          }
		          else {
                      message = "New load border value is NOT set";
		          }
		      }
		      catch (NumberFormatException e) {
		          message = "Could not parse border load value";
		      }
		      break;
		  }
		  default: {
		      message = "Action is not recognised";
		  } break;
		}
		writer.writeText(response, message);
	}
	
}


