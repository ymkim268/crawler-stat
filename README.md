# crawler-stat

## Web Crawler using Crawler4j
* using crawler4j library to crawl a pre-selected news website
* gather fetch statistics from the crawl (output on the console)

## note:
* using crawler4j library (crawler4j-4.1-jar-with-dependencies.jar)
* news site crawled: https://wwww.foxnews.com/
* fetch.csv contains urls that it attemps to fetch
* visit.csv contains files it successfully downloads
* urls.csv contains all urls that were discovered including repeats


## Sample output on the console from Controller.java
```
Fetch Statistics
================
# fetches attempted: 19992
# fetches succeeded: 19000
# fetches aborted: 992
# fetches failed: 0
================

Outgoing URLs
================
Total URLs extracted: 2413551
# unique URLS extracted: 222289
# unique URLS within News Site: 66927
# unique URLS outside News Site: 155362
================

Status Codes
================
200 OK: 19000
301 Moved Permanetly: 884
302 Found: 13
404 Not Found: 85
410 Gone: 1
422 Unprocessable Entity: 7
503 Service Unavailable: 2
================

File Sizes:
================
< 1KB: 18
1KB ~ <10KB: 657
10KB ~ <100KB: 17927
100KB ~ <1MB: 384
<= 1MB: 8
================

Content Types:
================
text/html: 17770
image/gif: 19
image/jpeg: 1036
image/png: 165
application/pdf: 4
================
```