import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class Controller {
	
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		long startTime = System.nanoTime();
		
		String crawlStorageFolder = "src/data/crawl";
		int numberOfCrawlers = 7;
		
		CrawlConfig config = new CrawlConfig();
		config.setCrawlStorageFolder(crawlStorageFolder);
		
		config.setMaxDepthOfCrawling(16);
		//config.setMaxPagesToFetch(20000);
		config.setMaxPagesToFetch(50);
		config.setPolitenessDelay(1000);
		config.setUserAgentString("cs572Hw2Agent");
		
		/*
	     * Since images are binary content, we need to set this parameter to
	     * true to make sure they are included in the crawl.
	     */
		config.setIncludeBinaryContentInCrawling(true);
		
		/*
         * Instantiate the controller for this crawl.
         */		
		PageFetcher pageFetcher = new PageFetcher(config); 
		RobotstxtConfig robotstxtConfig = new RobotstxtConfig(); 
		RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher); 
		CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
		
		robotstxtConfig.setEnabled(false);
		
		/*
         * For each crawl, you need to add some seed urls. These are the first
         * URLs that are fetched and then the crawler starts following links
         * which are found in these pages
         */
		controller.addSeed("https://www.foxnews.com/");
		
		/*
         * Start the crawl. This is a blocking operation, meaning that your code
         * will reach the line after this only when crawling is finished.
         */
		controller.start(MyCrawler.class, numberOfCrawlers);
		
		// Local Data obtained from each crawler
		List<Object> crawlersLocalData = controller.getCrawlersLocalData();
		
		// Fetch Statistics
	    int totalFetchAttempted = 0;
	    int totalFetchSucceeded = 0;
	    int totalFetchAborted = 0; // according to assignment failed and aborted same
	    int totalFetchFailed = 0; // ignore
	    
	    // Outgoing URLs Statistics
	    int totalURLExtracted = 0;
	    int totalUniqueURLExtracted = 0;
	    int totalUniqueURLWithin = 0;
	    int totalUniqueURLOutside = 0;
	    
	    // Status Codes
	    Map<Integer, String> statusCodeMap = new HashMap<Integer, String>();
	    Map<Integer, Integer> statusCounterMap = new HashMap<Integer, Integer>();
	    
	    // File Size
	    int[] totalFileSize = new int[5];
	    String[] fileSizeArray = {"< 1KB", "1KB ~ <10KB", "10KB ~ <100KB",
	    		"100KB ~ <1MB", "<= 1MB"};
	    
	    // Content Types
	    int[] totalContentType = new int[5];
	    String[] contentTypeArray = {"text/html", "image/gif", "image/jpeg",
	    		"image/png", "application/pdf"};
	    
	    int totalVisitTest = 0;
	    for (Object localData : crawlersLocalData) {
            CrawlStat stat = (CrawlStat) localData;
            totalFetchAttempted += stat.getTotalFetchAttempted();
            totalFetchSucceeded += stat.getTotalFetchSucceeded();
            totalFetchAborted += stat.getTotalFetchAborted();
            totalFetchFailed += stat.getTotalFetchFailed();
            
            totalURLExtracted += stat.getTotalURLExtracted();
            totalUniqueURLExtracted += stat.getUniqueURLExtracted();
            totalUniqueURLWithin += stat.getUniqueURLWithin();
            totalUniqueURLOutside += stat.getUniqueURLOutside();
            
            totalVisitTest += stat.getVisitTotal();
            
            // merge all the status code -> desc encountered by the crawlers
            Map<Integer, String> statusDescMap = stat.getStatusDescMap();
            for(Map.Entry<Integer, String> e : statusDescMap.entrySet()) {
            	if(!statusCodeMap.containsKey(e.getKey())) {
            		statusCodeMap.put(e.getKey(), e.getValue());
            	}
            }
            // merge all status code encounters of crawler to a single map
            Map<Integer, Integer> statusMap = stat.getStatusMap();
            for(Map.Entry<Integer, Integer> e : statusMap.entrySet()) {
            	if(statusCounterMap.containsKey(e.getKey())) {
            		statusCounterMap.put(e.getKey(), statusCounterMap.get(e.getKey()) + e.getValue());
            	} else {
            		statusCounterMap.put(e.getKey(), e.getValue());
            	}
            }
            
            for(int i = 0; i < totalFileSize.length; ++i) {
            	totalFileSize[i] += (stat.getTotalFileSize())[i];
            }
            
            for(int i = 0; i < totalContentType.length; ++i) {
            	totalContentType[i] += (stat.getTotalContentType())[i];
            }
            
            try {
            	stat.fetchCSV.close();
            	stat.visitCSV.close();
            	stat.urlsCSV.close();
            } catch(IOException e) {
            	System.out.println("Failed to write to CSV file!");
            	e.printStackTrace();
            }
        }
	    
	    // Summary of Fetch Statistics Below
	    System.out.println("Fetch Statistics");
	    System.out.println("================");
	    System.out.println("# fetches attempted: " + totalFetchAttempted);
	    System.out.println("# fetches succeeded: " + totalFetchSucceeded);
	    System.out.println("# fetches aborted: " + totalFetchAborted);
	    System.out.println("# fetches failed: " + totalFetchFailed);
	    System.out.println("================\n");
	    
	    System.out.println("Outgoing URLs");
	    System.out.println("================");
	    System.out.println("Total URLs extracted: " + totalURLExtracted);
	    System.out.println("# unique URLS extracted: " + totalUniqueURLExtracted);
	    System.out.println("# unique URLS within News Site: " + totalUniqueURLWithin);
	    System.out.println("# unique URLS outside News Site: " + totalUniqueURLOutside);
	    System.out.println("================\n");
	    
	    System.out.println("Status Codes");
	    System.out.println("================");
	    for(Map.Entry<Integer, Integer> e : statusCounterMap.entrySet()) {
	    	System.out.println(e.getKey() + " " + statusCodeMap.get(e.getKey()) + ": " + e.getValue());
	    }
	    System.out.println("================\n");
	    
	    System.out.println("File Sizes:");
	    System.out.println("================");
	    for(int i = 0; i < totalFileSize.length; ++i) {
	    	System.out.println(fileSizeArray[i] + ": " + totalFileSize[i]);
	    }
	    System.out.println("================\n");
	    
	    System.out.println("Content Types:");
	    System.out.println("================");
	    for(int i = 0; i < totalContentType.length; ++i) {
	    	System.out.println(contentTypeArray[i] + ": " + totalContentType[i]);
	    }
	    System.out.println("================\n");
	    
	    System.out.println("Total Visit(): " + totalVisitTest);
	    int accFileSize = 0;
	    int accContentSize = 0;
	    for(int i = 0; i < 5; ++i) {
	    	accFileSize += totalFileSize[i];
	    	accContentSize += totalContentType[i];
	    }
	    System.out.println("Total File Sizes: " + accFileSize);
	    System.out.println("Total Content Types: " + accContentSize);
	    
	    double delta = (System.nanoTime() - startTime) / 1000000;
	    System.out.println( "Total Elasped Time: " + delta + " ms");
	    System.out.println( "Total Elasped Time: " + delta/1000 + " s");
	    System.out.println( "Total Elasped Time: " + delta/60000 + " min");
	}

}
