package com.eulerity.hackathon.imagefinder.service;

import com.eulerity.hackathon.imagefinder.config.ConfigLoader;
import com.eulerity.hackathon.imagefinder.object.Image;
import com.eulerity.hackathon.imagefinder.util.UrlUtilities;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class defines methods to recursively traverse pages, and subpages within
 * the same domain to extract all the images
 */
@Slf4j
public class ImageCrawlerService {

    // Rate limiting mechanism
    private final Object lock = new Object();
    private volatile long adaptiveDelay = 100; // ms
    private static final int MAX_HISTORY_SIZE = 5;
    private final Queue<Long> responseTimes = new LinkedList<>();

    // Concurrency mechanism
    private ExecutorService executorService = Executors.newFixedThreadPool(ConfigLoader.get("crawler.maxThreads", 8));
    // Object to store image objects for their respective webpage's URL
    private ConcurrentMap<String, CopyOnWriteArrayList<Image>> imageDb = new ConcurrentHashMap<>();
    private final AtomicInteger activeTaskCounter = new AtomicInteger(0);

    // Class parameters
    private boolean recursive;
    private int permissibleDepth;

    public ImageCrawlerService(boolean recursive) {
        this.recursive = recursive;
        this.permissibleDepth = ConfigLoader.get("crawler.defaultDepth", 0);
    }

    public ImageCrawlerService(boolean recursive, int permissibleDepth) {
        this.recursive = recursive;
        this.permissibleDepth = permissibleDepth;
    }

    /**
     * Entry point for ImageCrawlerService class
     * @param url URL of the webpage to be crawled
     * @return {@code ConcurrentMap<String, CopyOnWriteArrayList<Image>>} Map of {@code (url, List<Image>)}
     */
    public ConcurrentMap<String, CopyOnWriteArrayList<Image>> init(String url){
        log.info("Crawl initiate request for: {}", url);
        init(url, 0);

        synchronized (this) {
            while (activeTaskCounter.get() > 0) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        return imageDb;
    }

    /**
     * Overloaded entry point for ImageCrawlerService class to introduce the depth parameter
     * @param url URL of the webpage to be crawled
     * @param depth Current level of depth of this page
     */
    public void init(String url, int depth){
        if(depth > permissibleDepth) return;
        if(imageDb.containsKey(url)) return;

        imageDb.put(url, new CopyOnWriteArrayList<>());
        activeTaskCounter.incrementAndGet();
        crawl(url, depth);
    }

    /**
     * Crawls the current webpage for subpage URLs and images.<br>
     * Repeats the above step for subpages within the current page, until {@code depth} variable is not exhausted.
     * @param url URL of the page to be crawled
     * @param depth Current level of depth of the webpage
     */
    public void crawl(String url, int depth){
        executorService.submit(() -> {
            try {
                long tempDelay = adaptiveDelay;
                Thread.sleep(tempDelay);
                log.info("After delay: {} | Crawling initiates for: {}, depth: {}", tempDelay, url, depth);

                long startTime = System.currentTimeMillis();
                Document document = Jsoup.connect(url)
                        .header("User-Agent", "Mozilla/5.0")
                        .get();
                long responseTime = System.currentTimeMillis() - startTime;

                synchronized (lock) {
                    long previousAverage = responseTimes.stream().mapToLong(Long::longValue).sum() / responseTimes.size();
                    if (responseTimes.size() >= MAX_HISTORY_SIZE) {
                        responseTimes.poll();
                    }
                    responseTimes.add(responseTime);
                    long currentAverage = responseTimes.stream().mapToLong(Long::longValue).sum() / responseTimes.size();
                    double percentageChange = ((double) (currentAverage - previousAverage) / previousAverage );

                    adaptiveDelay = (long) ((double)adaptiveDelay * (1.0 + percentageChange));
                    adaptiveDelay = Math.max(50, Math.min(2500, adaptiveDelay));
                }

                if(recursive && depth < permissibleDepth){
                    List<String> subPageUrlsList = getSubPageUrls(document, url);
                    for(String subPageUrl : subPageUrlsList){
                        init(subPageUrl, depth+1);
                    }
                }
                crawlImages(document, url);
            } catch (Exception e) {
                log.error("Failed to process: {}\nException: {}", url, e.getMessage());
                // Exponential backoff on rejected requests
                synchronized (lock) {
                    adaptiveDelay = (long)Math.min(2500.0, (double)adaptiveDelay*1.25);
                }
            } finally {
                if(activeTaskCounter.decrementAndGet() == 0){
                    synchronized (ImageCrawlerService.this) {
                        ImageCrawlerService.this.notifyAll();
                    }
                    executorShutdownService();
                }
            }
        });
    }

    /**
     * Gets the list of all the URLs within the current subpage, and is within the same domain of the baseUrl
     * @param document HTML document of the page being crawled
     * @param baseUrl URL of the page being crawled
     * @return {@code List<String>}
     */
    public List<String> getSubPageUrls(Document document, String baseUrl){
        List<String> subPageUrls = new ArrayList<>();
        Elements links = document.select("a[href]");
        for(Element link : links){
            try {
                String url = UrlUtilities.normalizeUrl(UrlUtilities.resolveUrl(baseUrl, link.attr("href"), true));
                if(url != null) subPageUrls.add(url);
            }
            catch(Exception e) {
                log.error("Failed to resolve subpage url: {} | {}\nException: {}", baseUrl, link.attr("href"), e.getMessage());
            }
        }
        return subPageUrls;
    }

    /**
     * Adds {@link Image} objects of all the images on the page in the {@code imageDb}
     * @param document HTML document of the page being crawled
     * @param baseUrl URL of the page being crawled
     */
    public void crawlImages(Document document, String baseUrl){
        Elements images = document.select("img[src]");

        for(Element image : images){
            String imageUrl = image.attr("src");
            if(imageUrl.startsWith("data") || imageUrl.contains("svg")) continue;
            try {
                imageUrl = UrlUtilities.resolveUrl(baseUrl, imageUrl, false);
                if(imageUrl != null) {
                    imageDb.get(baseUrl).add(Image.processImage(imageUrl));
                }
            }
            catch(Exception e) {
                log.error("Failed to resolve image url: {} | {}\nException: {}", baseUrl, imageUrl, e.getMessage());
            }
        }
    }

    /**
     * Gracefully shuts down the executor service once all tasks are completed
     */
    public void executorShutdownService(){
        log.info("Executor service shut down initiated");
        executorService.shutdown();
        try {
            if(!executorService.awaitTermination(ConfigLoader.get("crawler.timeOut.seconds", 60), TimeUnit.SECONDS)) {
                executorService.shutdownNow();
                log.info("Executor service shut down complete");
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            log.info("Executor service shut down complete");
        }
    }

}
