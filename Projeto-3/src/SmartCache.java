import java.util.HashSet;
import java.util.Set;

/**
 * 
 */

/** 
 * @author Rafael Gameiro nº50677
 * @author Rui Santos nº50833
 */
public class SmartCache {

	private Set<String> cacheContent;
	
	/**
	 * cache the program will use 
	 */
	public SmartCache() {
		// TODO Auto-generated constructor stub
		cacheContent = new HashSet<String>();
	}
	
	/**
	 * adds the filename to the cache
	 * @param filename
	 */
	public void addContent(String filename) {
		cacheContent.add(filename);
	}
	
	/**
	 * removes the filename in the cache
	 * @param filename
	 */
	public void removeContent(String filename) {
		cacheContent.remove(filename);
	}
	
	/**
	 * 
	 * @param filename
	 * @return true if the cache contains the filename 
	 * 		   false if the cache doesn't contain the filename
	 */
	public boolean hasContent(String filename) {
		return cacheContent.contains(filename);
	}
	

}
