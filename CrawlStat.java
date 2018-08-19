import java.io.IOException;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import edu.uci.ics.crawler4j.url.WebURL;

public class CrawlStat {
	
//	private static final Pattern filters = Pattern.compile(
//            ".*(\\.(css|js|(js\\?.*)|json|mid|mp2|mp3|mp4|wav|avi|mov|mpeg|ram|m4v" +
//            "|rm|smil|wmv|swf|wma|zip|rar|gz))$");
	
	private static final Pattern filters = Pattern.compile(
            ".*(\\.(css|(css\\?.*)|js|(js\\?.*)|json|mid|mp2|mp3|mp4|wav|avi|mov|mpeg|ram|m4v" +
            "|rm|smil|wmv|swf|wma|zip|rar|gz|(print\\.html)))$");
	
	// private static final Pattern myPatterns = Pattern.compile(".*(\\.(html|pdf))$");
    
    private static final Pattern imgPatterns = Pattern.compile(".*(\\.(bmp|gif|jpe?g|png|tiff?))$");
    
	CSV fetchCSV;
    CSV visitCSV;
    CSV urlsCSV;
	
	// Fetch Statistics
    private int totalFetchAttempted = 0;
    private int totalFetchSucceeded = 0;
    private int totalFetchAborted = 0;
    private int totalFetchFailed = 0;
    
    // Outgoing URLs Statistics
    private int totalURLExtracted = 0;
    // private int uniqueURLExtracted = 0;
    private int uniqueURLWithin = 0;
    private int uniqueURLOutside = 0;
    
    Set<String> extractedURLSet;
    private int nonUniqueURLExtracted = 0;
    
    // Status Codes    
    private Map<Integer, String> statusDescMap;
    private Map<Integer, Integer> statusMap;
    
    // File Sizes
    private int totalLess1 = 0;
    private int total1To10 = 0;
    private int total10To100 = 0;
    private int total100To1000 = 0;
    private int totalGreater1000 = 0;
    
    // Content Types
    private int totalText = 0;
    private int totalGif = 0;
    private int totalJpeg = 0;
    private int totalPng = 0;
    private int totalPdf = 0;
    
    // Debug
    int totalVisitTest = 0;
    
    public CrawlStat() throws IOException {
    	fetchCSV = new CSV("fetch.csv");
        visitCSV = new CSV("visit.csv");
        urlsCSV = new CSV("urls.csv");
        
        extractedURLSet = new HashSet<String>();
        
        statusDescMap = new HashMap<Integer, String>();
        statusMap = new HashMap<Integer, Integer>();
    }
    
    /*
     * Parse through the extracted urls to count :
     * 1. non-unique urls
     * 2. unique urls within/outside the domain
     */
    public void dumpToExtractedURLSet(Set<WebURL> links) {
    	Iterator<WebURL> it = links.iterator();
        while(it.hasNext()) {
        	String href = it.next().getURL().toLowerCase();
        	boolean isUnique = extractedURLSet.add(href);
        	if(!isUnique) {
        	 	nonUniqueURLExtracted++;
        	} else {
        	
        		String domain1 = "https://www.foxnews.com/";
        	    String domain2 = "http://www.foxnews.com/";
        	    
        	    boolean filter = (!filters.matcher(href).matches() || imgPatterns.matcher(href).matches())
                        && (href.startsWith(domain1) || href.startsWith(domain2)); 
        		
        	    // boolean filter = (myPatterns.matcher(href).matches() || imgPatterns.matcher(href).matches())
                //        && (href.startsWith(domain1) || href.startsWith(domain2)); 
        	    
        		if(filter) {
        			uniqueURLWithin++;
        		} else {
        			uniqueURLOutside++;
        		}
        	}
        }
    }
    
    
    // increment fetch attempted by crawler
    public void incFetchAttempted() {
        totalFetchAttempted++;
    }
    
    // increment total url extracted of each page visited by crawler
    public void incTotalURLExtracted(int count) {
    	totalURLExtracted += count;
    }
    
    // increment num of pages visited by crawler
    public void incVisit() {
    	totalVisitTest++;
    }
    
    // increment fetch succeeded and succeed and map accordingly to status code
    public void incFetch(int statusCode, String statusDescription) {
    	// update status description map
    	if(!statusDescMap.containsKey(statusCode)) {
    		statusDescMap.put(statusCode, statusDescription);
    	}
    	
    	// update status counter
    	if(statusMap.containsKey(statusCode)) {
    		statusMap.put(statusCode, statusMap.get(statusCode) + 1);
    	} else {
    		statusMap.put(statusCode, 1);
    	}
    	
    	if(statusCode < 200 ||statusCode > 299) {
    		totalFetchAborted++;
    	} else {
    		totalFetchSucceeded++;
    	}
    }
    
    // increment visited page accordingly to file size type (hard-coded values of range) 
    public void incFileSize(double fileSize) {
    	if(fileSize < 1) {
    		totalLess1++;
    	} else if (fileSize >= 1 && fileSize < 10) {
    		total1To10++;
    	} else if (fileSize >= 10 && fileSize < 100) {
    		total10To100++;
    	} else if (fileSize >= 100 && fileSize < 1000) {
    		total100To1000++;
    	} else {
    		totalGreater1000++;
    	}
    }
    
    // increment visited page accordingly to content type visited (hard-coded content types)
    public void incContentType(String contentType) {
    	if(contentType.equals("text/html")) {
    		totalText++;
    	} else if (contentType.equals("image/gif")) {
    		totalGif++;
    	} else if (contentType.equals("image/jpeg")) {
    		totalJpeg++;
    	} else if (contentType.equals("image/png")) {
    		totalPng++;
    	} else if (contentType.equals("application/pdf")) {
    		totalPdf++;
    	} else {
    		// do not increment
    	}
    }
    
    /* 
     * Below is set of 'get' functions to  return stat value for a crawler 
     * Used at Controller.java where it accumulates stat for each crawler
     */
    
    public int getTotalFetchAttempted() {
        return totalFetchAttempted;
    }
    
    public int getTotalFetchSucceeded() {
    	return totalFetchSucceeded;
    }

    public int getTotalFetchAborted() {
    	return totalFetchAborted;
    }
    
    public int getTotalFetchFailed() {
    	return totalFetchFailed;
    }
    
    public Map<Integer, String> getStatusDescMap() {
    	return statusDescMap;
    }
    
    public Map<Integer, Integer> getStatusMap() {
    	return statusMap;
    }
    
    public int getTotalURLExtracted() {
    	return totalURLExtracted;
    }
    
    public int getUniqueURLExtracted() {
    	return extractedURLSet.size();
    }
    
    public int getNonUniqueURLExtracted() {
    	return nonUniqueURLExtracted;
    }
    
    public int getUniqueURLWithin() {
    	return uniqueURLWithin;
    }
    
    public int getUniqueURLOutside() {
    	return uniqueURLOutside;
    }
    
    public int[] getTotalFileSize() {
    	int[] totalFileSizeArray = {totalLess1, total1To10, total10To100, total100To1000, totalGreater1000};
    	return totalFileSizeArray;	
    }
    
    public int[] getTotalContentType() {
    	int[] totalContentType = {totalText, totalGif, totalJpeg, totalPng, totalPdf};
    	return totalContentType;
    }
    
    public int getVisitTotal() {
    	return totalVisitTest;
    }

}