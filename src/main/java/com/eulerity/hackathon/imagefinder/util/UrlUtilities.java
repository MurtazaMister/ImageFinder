package com.eulerity.hackathon.imagefinder.util;

import lombok.extern.slf4j.Slf4j;

import java.net.MalformedURLException;
import java.net.URL;

@Slf4j
public class UrlUtilities {

    /**
     * This method concatenates baseUrl and currentUrl if the currentUrl is relative, otherwise checks if they are in
     * the same domain
     * @param baseUrl The page on which the url is found
     * @param currentUrl The url that is found
     * @param assertSameDomain To check if url belongs to the same domain
     * @return Fully formed {@code currentUrl} or {@code null} if not in the same domain
     * @throws MalformedURLException
     */
    public static String resolveUrl(String baseUrl, String currentUrl, boolean assertSameDomain) throws MalformedURLException {
        if(currentUrl.startsWith("http")) {
            if(assertSameDomain){
                return (isSameDomain(baseUrl, currentUrl)) ? currentUrl : null;
            }
            else{
                return currentUrl;
            }
        }
        else {
            return new URL(new URL(baseUrl), currentUrl).toString();
        }
    }

    /**
     * Checks if the two URLs are within the same domain
     * @param url1
     * @param url2
     * @return {@code true} if within the same domain else {@code false}
     * @throws MalformedURLException
     */
    public static boolean isSameDomain(String url1, String url2) throws MalformedURLException {
        URL base = new URL(url1);
        URL current = new URL(url2);

        return base.getHost().equalsIgnoreCase(current.getHost());
    }

    /**
     * Validates if the format of the URL is correct or not
     * @param url
     * @return {@code boolean} {@code true} if url is in valid format, {@code false} otherwise
     */
    public static boolean isValidURL(String url) {
        try{
            new URL(url);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

}
