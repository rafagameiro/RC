import java.math.RoundingMode;
import java.text.DecimalFormat;

/** 
 * @author Rafael Gameiro nº50677
 * @author Rui Santos nº50833
 */

public class Stats {
	
	public static double start; 
	public static int totalBytes = 0;
	public static int requests = 0; 
	public static int totalHttpReply = 0;
	public static int averagePayload; 
	public static double totalTimeRequestReply;
	public static double averageTimeRequestReply;
	public static double timeRequestReply;
	public static DecimalFormat df;
	
	Stats(){
		df = new DecimalFormat("#.####");
        df.setRoundingMode(RoundingMode.CEILING);
		start = System.currentTimeMillis();
	}
	
	/**
	 * Total number of bytes downloaded (in bytes).
	 * 
	 * @param bytes number of bytes received from the server
	 */
	void setTotalBytes(int bytes) {
		totalBytes += bytes;
	}
	
	/**
	 * Number of requests performed by the client during the file transfer.
	 */
	void increaseRequests() {
		requests++;
	}
	
	/**
	 * Increment the number of HTTP replies.
	 */
	void increaseHttpReply() {
		totalHttpReply++;
	}
	
	/**
	 * Calculates the average size of the payload of each HTTP reply (in bytes).
	 */
	void setAverageHttpPayload() {
		averagePayload = totalBytes/totalHttpReply;
	}
	
	/**
	 * Initiates the counting time of request.
	 */
	void startRequestTime() {
		timeRequestReply = System.currentTimeMillis();
	}
	
	/**
	 * Gets the duration time of the reply.
	 * Adds this duration to the total time request/reply took.
	 */
	void setTotalTimeRequestReply() {
		timeRequestReply = System.currentTimeMillis() - timeRequestReply;
		totalTimeRequestReply += timeRequestReply;
	}
	
	/**
	 * Gets the average time of request/reply.
	 */
	void setAverageRequestReplyTime() {
		averageTimeRequestReply = totalTimeRequestReply / totalHttpReply;
	}
	
	/**
	 * Prints the statistics implemented.
	 */
	void statisticsProject() {
		setAverageHttpPayload();
		setAverageRequestReplyTime();
		double totalTime = (System.currentTimeMillis() - start)/1000;
        System.out.println("Total time elapsed:\t\t\t\t " + totalTime + " seconds");
        System.out.println("Total number of bytes downloaded:\t\t " + totalBytes + " bytes");
        System.out.println("End-to-end average bitrate:\t\t\t " + (int) (totalBytes/totalTime) + " bytes per second");
        System.out.println("Number of requests performed:\t\t\t " + requests + " requests");
        System.out.println("Average size of the payload of each HTTP reply:\t " + averagePayload + " bytes");
        System.out.println("Average time spent in each request/reply:\t " + df.format(averageTimeRequestReply) + " milliseconds");
    }
	
	
}
