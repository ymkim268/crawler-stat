
import java.io.IOException;
import java.util.Set;
import java.util.regex.Pattern;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.parser.BinaryParseData;
import edu.uci.ics.crawler4j.url.WebURL;


public class MyCrawler extends WebCrawler {
	
	private static final Pattern filters = Pattern.compile(
            ".*(\\.(css|(css\\?.*)|js|(js\\?.*)|json|mid|mp2|mp3|mp4|wav|avi|mov|mpeg|ram|m4v" +
            "|rm|smil|wmv|swf|wma|zip|rar|gz|(print\\.html)))$");
    
    // private static final Pattern myPatterns = Pattern.compile(".*(\\.(html|pdf))$");
    
    private static final Pattern imgPatterns = Pattern.compile(".*(\\.(bmp|gif|jpe?g|png|tiff?))$");
    
    CrawlStat myCrawlStat;

    public MyCrawler() throws IOException {
    	myCrawlStat = new CrawlStat();
    }

	/**
	* This method receives two parameters. The first parameter is the page
	* in which we have discovered this new url and the second parameter is
	* the new url. You should implement this function to specify whether
	* the given url should be crawled or not (based on your crawling logic).
	* In this example, we are instructing the crawler to ignore urls that
	* have css, js, git, ... extensions and to only accept urls that start
	* with "http://www.ics.uci.edu/". In this case, we didn't need the
	* referringPage parameter to make the decision.
	*/
	@Override
	public boolean shouldVisit(Page referringPage, WebURL url) {
		String href = url.getURL().toLowerCase();
		
		String domain1 = "https://www.foxnews.com/";
	    String domain2 = "http://www.foxnews.com/";
		
	    boolean filter = (!filters.matcher(href).matches() || imgPatterns.matcher(href).matches())
                && (href.startsWith(domain1) || href.startsWith(domain2)); 

		try {
			myCrawlStat.urlsCSV.appendRow(href);
			if(filter) {
				myCrawlStat.urlsCSV.appendRow("OK");
			} else {
				myCrawlStat.urlsCSV.appendRow("N_OK");
			}
			myCrawlStat.urlsCSV.endRow();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return filter;
	}

    /**
     * This function is called when a page is fetched and ready
     * to be processed by your program.
     */
    @Override
    public void visit(Page page) {   	
        String url = page.getWebURL().getURL();
        System.out.println("URL: " + url);
        
        myCrawlStat.incVisit();

        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            String text = htmlParseData.getText();
            String html = htmlParseData.getHtml();
            Set<WebURL> links = htmlParseData.getOutgoingUrls();

            System.out.println("Text length: " + text.length());
            System.out.println("Html length: " + html.length());
            System.out.println("Number of outgoing links: " + links.size());
            
            myCrawlStat.incTotalURLExtracted(links.size());
            myCrawlStat.dumpToExtractedURLSet(links);
            
            try {
            	myCrawlStat.visitCSV.appendRow(url);
            	myCrawlStat.visitCSV.appendRow(Double.toString((page.getContentData().length)/1024.0) + " KB");
            	myCrawlStat.incFileSize((page.getContentData().length)/1024.0);
            	myCrawlStat.visitCSV.appendRow(Integer.toString(links.size()));
            	myCrawlStat.visitCSV.appendRow(page.getContentType().split(";")[0]);
            	myCrawlStat.incContentType(page.getContentType().split(";")[0]);
            	myCrawlStat.visitCSV.endRow();
            } catch (IOException e) {
            	e.printStackTrace();
            }
            
        }
        
        // for image binary content type
        if(page.getParseData() instanceof BinaryParseData) {
            try {
            	myCrawlStat.visitCSV.appendRow(url);
            	myCrawlStat.visitCSV.appendRow(Double.toString((page.getContentData().length)/1024.0) + " KB");
            	myCrawlStat.incFileSize((page.getContentData().length)/1024.0);
            	myCrawlStat.visitCSV.appendRow(Integer.toString(0));
            	myCrawlStat.visitCSV.appendRow(page.getContentType().split(";")[0]);
            	myCrawlStat.incContentType(page.getContentType().split(";")[0]);
            	myCrawlStat.visitCSV.endRow();
            } catch (IOException e) {
            	e.printStackTrace();
            }
        }
    }
    
    
    /**
     * This function is called once the header of a page is fetched. It can be
     * overridden by sub-classes to perform custom logic for different status
     * codes. For example, 404 pages can be logged, etc.
     *
     * @param webUrl WebUrl containing the statusCode
     * @param statusCode Html Status Code number
     * @param statusDescription Html Status COde description
     */
	@Override
    protected void handlePageStatusCode(WebURL webUrl, int statusCode, String statusDescription) {
        // Do nothing by default
        // Sub-classed can override this to add their custom functionality
		
		myCrawlStat.incFetchAttempted();
		try {
			if(webUrl.toString().contains(",")) {
	    		String replaceUrl = webUrl.toString().replaceAll("(,)+", "-");
	    		myCrawlStat.fetchCSV.appendRow(replaceUrl);
	    	} else {
	    		myCrawlStat.fetchCSV.appendRow(webUrl.toString());
	    	}
			myCrawlStat.fetchCSV.appendRow(Integer.toString(statusCode));
			myCrawlStat.fetchCSV.endRow();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		myCrawlStat.incFetch(statusCode, statusDescription);
    }
	
	/**
     * This function is called before processing of the page's URL
     * It can be overridden by subclasses for tweaking of the url before processing it.
     * For example, http://abc.com/def?a=123 - http://abc.com/def
     *
     * @param curURL current URL which can be tweaked before processing
     * @return tweaked WebURL
     */
    protected WebURL handleUrlBeforeProcess(WebURL curURL) {
    	String url = curURL.getURL();
    	if(url.contains(",")) {
    		// replace comma with - to prevent throwing error
    		String replaceUrl = url.replaceAll("(,)+", "-");
    		curURL.setURL(replaceUrl);
    	}
    	
        return curURL;
    }
	
	/**
     * The CrawlController instance that has created this crawler instance will
     * call this function just before terminating this crawler thread. Classes
     * that extend WebCrawler can override this function to pass their local
     * data to their controller. The controller then puts these local data in a
     * List that can then be used for processing the local data of crawlers (if needed).
     *
     * @return currently NULL
     */
	@Override
    public Object getMyLocalData() {
		return myCrawlStat;
    }
}
