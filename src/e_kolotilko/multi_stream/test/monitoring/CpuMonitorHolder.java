package e_kolotilko.multi_stream.test.monitoring;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.sun.management.OperatingSystemMXBean;

public class CpuMonitorHolder {

    static final CpuMonitor monitor;
    //don't really need to record a thread, but let's keep it for now
    protected static final Thread executionThread;
    static {
        monitor = new CpuMonitor();
        executionThread = new Thread(monitor);
        executionThread.start();
    }
    
    
    static public void stopWorking() {
        monitor.stopWorking();
    }
    
    /**
     * Get last measured value of CPU load
     * @return CPU load in percent, not rounded
     */
    static public double getLoadPercent() {
        return monitor.getLoadPercent();
    }
    
    /**
     * get CPU load border value. If CPU load if more than or equal to this, event will be triggered
     * @return CPU load border value
     */
    static public double getBorderLoad() {
        return monitor.getBorderLoad();
    }
    
    /**
     * set CPU load border value. If CPU load if more than or equal to this, event will be triggered
     * @param newBorder new trigger value in % between 0 and 100
     * @return true if assigned new value, else false
     */
    static public boolean setBorderLoad(double newBorder) {
        return monitor.setBorderLoad(newBorder);
    }
    
    /**
     * Get how often measurement will occur
     * @return minimum amount of time in milliseconds between 2 measurements
     */
    static public long getTriggerTime() {
        return monitor.getTriggerTime();
    }
    
    /**
     * Subscribe to CPU load updates
     * @param listener 
     */
    static public void subToLoadInfoUpdate(ICpuLoadGetter listener) {
        monitor.subToLoadInfoUpdate(listener);
    }
    /**
     * Unsubscribe from CPU load updates
     * @param listener
     */
    static public void unsubFromLoadInfoUpdate(ICpuLoadGetter listener) {
        monitor.unsubFromLoadInfoUpdate(listener);
    }
    
    /**
     * Subscribe to being notified about border overflow events
     * @param listener
     */
    static public void subToBorderOverflowEvent(IBorderEventGetter listener) {
        monitor.subToBorderOverflowEvent(listener);
    }
    /**
     * Unsubscribe from being notified about border overflow events
     * @param listener
     */
    static public void unsubFromBorderOverflowEvent(IBorderEventGetter listener) {
        monitor.unsubFromBorderOverflowEvent(listener);
    }
    
}


class CpuMonitor implements Runnable {
    static public final long TRIGGER_TIME_MIN=50;
    static public final long TRIGGER_TIME_DEFAULT = 1000;

    static public final double BORDER_LOAD_MIN=0;
    static public final double BORDER_LOAD_MAX=100;
    static public final double BORDER_LOAD_DEFAULT = 80;
    
    public CpuMonitor() {
        this(TRIGGER_TIME_DEFAULT,BORDER_LOAD_DEFAULT);
    }
    
    public CpuMonitor(long triggerTime) {
        this(triggerTime, BORDER_LOAD_DEFAULT);
    }
    
    public CpuMonitor(long triggerTime, double borderLoad) {
        this.triggerTime = (triggerTime<TRIGGER_TIME_MIN) ? TRIGGER_TIME_MIN : triggerTime;
        
        if (borderLoad<BORDER_LOAD_MIN) {
            this.borderLoad = BORDER_LOAD_MIN;
        }
        else if (borderLoad>BORDER_LOAD_MAX) {
            this.borderLoad = BORDER_LOAD_MAX;
        }
        else {
            this.borderLoad = borderLoad;
        }
    }
    
    protected boolean needToWork = true;    
    public boolean isWorking() {
        return true;
    }
    public void stopWorking() {
        needToWork = false;
    }
    
    /**
     * how often measurement will occur (in fact sleep time for now)
     */
    protected long triggerTime;
    public long getTriggerTime() { 
        return triggerTime; 
    }
    

    /**
     * Last measured value of CPU load
     */
    protected volatile double loadPercent;
    public double getLoadPercent() {
       return loadPercent; 
    }
    
    /**
     * CPU load border. If CPU load if more or equal to this, event will be triggered 
     */
    protected volatile double borderLoad;
    public double getBorderLoad() {
        return borderLoad;
    }
    
    /**
     * Set new value that will trigger CPU load overflow event
     * @param newBorder new trigger value in % between 0 and 100
     * @return true if assigned new value, else false
     */
    public boolean setBorderLoad(double newBorder) {
        if ((newBorder>=BORDER_LOAD_MIN) && (newBorder<=BORDER_LOAD_MAX)) {
            borderLoad = newBorder;
            return true;
        }
        return false;
    }

    //set of subscribers who want to know last measured CPU load
    protected List<ICpuLoadGetter> subsToLoad = new ArrayList<ICpuLoadGetter>();
    public void subToLoadInfoUpdate(ICpuLoadGetter listener) {
        if (listener != null) {
            subsToLoad.add(listener);
        }
    }
    public void unsubFromLoadInfoUpdate(ICpuLoadGetter listener) {
        if (listener != null) {
            subsToLoad.remove(listener);   
        }
    }
    
    //set of subscribers who want to get notified when CPU_load>=borderLoad
    protected List<IBorderEventGetter> subsToBorderOverflow = new ArrayList<IBorderEventGetter>();
    public void subToBorderOverflowEvent(IBorderEventGetter listener) {
        if (listener != null) {
            subsToBorderOverflow.add(listener); 
        }
    }
    public void unsubFromBorderOverflowEvent(IBorderEventGetter listener) {
        if (listener == null) {
            subsToBorderOverflow.remove(listener);   
        }
    }
    
    protected void notifySubToBorderOverflowEvent(double cpuLoad) {
        if (subsToBorderOverflow.isEmpty()) return;
        
        long now = System.currentTimeMillis();
        
        for (IBorderEventGetter sub : subsToBorderOverflow) {
            sub.getBorderEventData(cpuLoad,now);
        }
    }
    
    protected void notifySubsToLoad(double cpuLoad) {
        if (subsToLoad.isEmpty()) return;
        
        for (ICpuLoadGetter sub : subsToLoad) {
            sub.getCpuLoad(cpuLoad);
        }
    }
        
    
    public void run() {
        //TODO : check other ways, 'cause this is not recommended 
        //although this one seems to be the most stable on Windows 7.
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        double tempPercent;
        while (needToWork) {
            // What % load the overall system is at, from 0.0-1.0
            tempPercent = osBean.getSystemCpuLoad();
            tempPercent*=100;
            
            loadPercent = tempPercent;
            notifySubsToLoad(loadPercent);
            if (loadPercent>=borderLoad) {
                notifySubToBorderOverflowEvent(loadPercent);
            }
            
            try {
             Thread.sleep(triggerTime);
            }
            catch (Exception e) {
                System.out.println("Sleep is interrupted! Exception:" + e);
                System.out.println("Exiting...");
                needToWork = false;
            }
        }
    }
    
}
