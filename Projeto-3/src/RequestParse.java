
/** 
 * @author Rafael Gameiro nº50677
 * @author Rui Santos nº50833
 */
public class RequestParse {
	
	private String n;
	private String port;
	private String[] arrayIP;
	
	public RequestParse(String args) {
		String s = args;
		String[] word = s.split("=");
		String[] arrayLocation = word[0].split("i");
		n = arrayLocation[0].substring(1, arrayLocation[0].length() - 1);
		arrayIP = word[1].split("&");
		port = word[2];
	}
	
	/**
	 * @return the filename
	 */
	public String getFilename() {
		return n;
	}
	
	/**
	 * @return URL IP
	 */
	public String getIP() {
		return arrayIP[0];
	}
	
	/**
	 * @return URL port
	 */
	public String getPort() {
		return port;
	}
}
