import java.net.*;

/** 
 * @author Rafael Gameiro nº50677
 * @author Rui Santos nº50833
 */
public class URLparse {
	
	private String host;
	private String path;
	private String filename;
	private String ip;
	private String port;
	
	public URLparse(String args) throws Exception {
		URL url = new URL(args);
		String[] pathArray = url.getPath().split("/");
		String[] queryArray = url.getQuery().split("=");
		String[] ipArray = queryArray[1].split("&");
		host = url.getHost();
		path = url.getPath();
		filename = pathArray[3];
		ip = ipArray[0];
		port = queryArray[2];
	}
	
	/**
	 * @return URL host
	 */
	public String getHost() {
		return host;
	}
	
	/**
	 * @return URL path
	 */
	public String getPath() {
		return path;
	}
	
	/**
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}
	
	/**
	 * @return URL IP
	 */
	public String getIP() {
		return ip;
	}
	
	/**
	 * @return URL Port
	 */
	public String getPort() {
		return port;
	}
	
}

