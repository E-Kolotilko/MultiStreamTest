package e_kolotilko.multi_stream.test.rabbit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import e_kolotilko.multi_stream.test.monitoring.CpuMonitorHolder;

public class SimpleSenderHolder {
    static Logger aLogger = LogManager.getLogger();
    
    public static final String HOST = "192.168.0.102";
    public static final String EXCHANGE_NAME = "border_cross";
    public static final String JSON_NAME_PERCENT = "percent";
    public static final String JSON_NAME_TIME = "time";
    
    static final SimpleSender sender;
    static {
        sender = new SimpleSender(HOST, EXCHANGE_NAME, JSON_NAME_PERCENT, JSON_NAME_TIME);
        CpuMonitorHolder.subToBorderOverflowEvent(sender);
        aLogger.info("Subbed rabbit worker to monitor");
        hasSubbed = true;
    }
    
    private static boolean hasSubbed;
    public static boolean hasSubbed() {
        return hasSubbed;
    }
}
