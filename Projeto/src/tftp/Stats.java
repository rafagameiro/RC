package tftp;

public class Stats {

	private static final int INIT_TIMEOUT = 500;

	private static int totalDataBlocks = 0;
	private static int totalAcks = 0;
	private static int totalBytes = 0;
	private static int minRtt = 1000;
	private static int rtt = 0;
	private static int maxRtt = 0;
	private static int minTimeout = 1000;
	private static int computedTimeout = 0;
	private static int maxTimeout = 0;
	private static long startTime = 0;
	
	private static double estimatedRTT;
	private static double devRTT;


	Stats() {
		startTime = System.currentTimeMillis();
	}

	// any other methods
	
	/**
	 * Increase the number of block that were sent.
	 */
	void increaseDataBlocks() {
		totalDataBlocks++;
	}
	
	/**
	 * Increase the number of acknowledge messages received
	 */
	void increaseAcks() {
		totalAcks++;
	}
	
	/**
	 * It sums the value in bytes to the value in totalBytes
	 * 
	 * @param bytes number of bytes read from the file
	 */
	void increaseBytes(int bytes) {
		totalBytes += bytes;
	}
	
	/**
	 * Checks if the RTT value is superior or inferior to
	 * the maxRTT and minRTT values, respectively
	 * If it is, the new RTT value, becomes the maxRTT or the the minRTT
	 * 
	 * @param rtt new calculated RTT value
	 */
	void setRtt(double rtt) {
		if(rtt > maxRtt)
			maxRtt = (int) rtt;
		else if(rtt < minRtt)
			minRtt = (int) rtt;
		
		Stats.rtt += rtt;
	}
	
	/**
	 * Checks if the timeout value is superior or inferior to
	 * the maxTimeout and minTimeout values, respectively
	 * If it is, the new timeout value, becomes the maxTimeout or the the minTimeout 
	 * 
	 * @param timeout new calculated timeout value
	 */
	void setTimeout(double timeout) {
		if(timeout > maxTimeout)
			maxTimeout = (int) timeout;
		else if(timeout < minTimeout)
			minTimeout = (int) timeout;
	}
	
	/**
	 * The value in rtt becomes the new value for rtt variable
	 * 
	 * @param rtt last calculated RTT value
	 */
	void setAvgRtt() {
		Stats.rtt /= totalAcks;
	}
	
	/**
	 * The value in timeout becomes the new value for timeout variable
	 * 
	 * @param timeout last calculated timeout value
	 */
	void setComputedTimeout(double timeout) {
		computedTimeout = (int) timeout;
	}
	
	double calculateInitialTimeout(double rtt) {
		double timeout;
		
		estimatedRTT = rtt;
		devRTT = estimatedRTT / 2;
		timeout = estimatedRTT + 4*devRTT;
		
		setRtt(estimatedRTT);
		setTimeout(timeout);
		
		return timeout;
	}
	
	/**
	 * Calculates the next timeout to be set, so then the packet transfer
	 * between server and this program, can be optimised without throwing a SocketTimeoutException
	 * 
	 * @param currRTT RTT calculated from the last transfer between this program and server
	 * @return the next timeout value that should be set
	 */
	double calculateTimeout(double currRTT) {
		// TODO Auto-generated method stub
		double timeoutVal;
		
		estimatedRTT = estimatedRTT * 0.875 + 0.125 * currRTT;
		devRTT = 0.75 * devRTT + 0.25 * Math.abs(currRTT - estimatedRTT);
		timeoutVal = estimatedRTT + 4 * devRTT;

		setRtt(estimatedRTT);
		setTimeout(timeoutVal);
		
		return timeoutVal;
	}

	
	void printReport() {
		// compute time spent sending data blocks
		int milliSeconds = (int) (System.currentTimeMillis() - startTime);
		float speed = (float) (totalBytes * 8.0 / milliSeconds / 1000); // M bps
		System.out.println("\nTransfer stats:");
		System.out.println("\nFile size:\t\t\t " + totalBytes);
		System.out.printf("End-to-end transfer time:\t %.3f s\n", (float) milliSeconds / 1000);
		System.out.printf("End-to-end transfer rate:\t %.3f M bps\n", speed);
		System.out.println("Number of data messages sent:\t " + totalDataBlocks);
		System.out.println("Number of Acks received:\t " + totalAcks);

		// if you implement the dynamic timeout option, uncomment these two extra lines
		 System.out.printf("rtt - min, average, max: \t %d  %d  %d ms \n", minRtt, rtt, maxRtt);
		 System.out.printf("timeOut - min, last, max: \t %d  %d  %d ms \n\n", minTimeout, computedTimeout, maxTimeout);

		// With GBN and different block sizes, uncommente this line
		// System.out.println("window size: "+windowSize+" block size: "+blockSize;

	}
}
