package e_kolotilko.multi_stream.test.monitoring;

/**
 * This interface must be implemented by classes that wish to be notified 
 * when CPU load crossed the predefined border
 */
public interface IBorderEventGetter {

    /**
     * Callback function for getting notified when border cross was detected
     * @param percent detected CPU load in percent, not rounded
     * @param time milliseconds, UNIX time
     */
	void getBorderEventData(double percent, long time);
	
}
