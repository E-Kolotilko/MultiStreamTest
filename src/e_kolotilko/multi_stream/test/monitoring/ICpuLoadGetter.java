package e_kolotilko.multi_stream.test.monitoring;

/**
 * This interface must be implemented by classes that wish to be notified 
 * of detected CPU load
 */
public interface ICpuLoadGetter {

    /**
     * Callback function for getting notified about measured CPU load
     * @param loadPercent detected CPU load in percent, not rounded 
     */
	void getCpuLoad(double loadPercent);
	
}
