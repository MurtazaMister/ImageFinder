package com.eulerity.hackathon.imagefinder.util;

import lombok.extern.slf4j.Slf4j;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * URL Utilities class provides quick utility functions for URLs
 */
@Slf4j
public class UrlUtilities {

    public static String[] imageFormats = {
            ".jpg", ".jpeg", ".png", ".gif", ".webp", ".svg", ".bmp", ".apng", ".avif", ".ico"
    };

    /**
     * To make URLs consistent among different scenarios
     * @param url URL to be normalized
     * @return Normalized URL of type {@code String}
     */
    public static String normalizeUrl(String url) {
        if(url == null || url.isEmpty()) return null;
        if(url.endsWith("/")) return url;
        return url + "/";
    }

    /**
     * Normalizes the URL by removing unwanted trailing characters 
     * without affecting the core structure.
     * @param url URL to normalize
     * @return Normalized URL
     */
    public static String normalizeTrailingCharacters(String url) {
        if (url == null || url.isEmpty()) return null;

        String[] unwantedPatterns = {"#/", "./", "../"};

        boolean modified;
        do {
            modified = false;
            for (String pattern : unwantedPatterns) {
                if (url.endsWith(pattern)) {
                    url = url.substring(0, url.length() - pattern.length());
                    modified = true;
                }
            }
        } while (modified);

        return url;
    }

    /**
     * Checks if the given url is absolute or not
     * @param url URL
     * @return {@code true} if absolute URL is supplied, otherwise false
     */
    public static boolean isAbsoluteUrl(String url) {
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    /**
     * Checks whether the URL conforms to HTTP or HTTPS
     * @param url URL
     * @return {@code true} if yes, {@code false} otherwise
     */
    public static boolean isHttpOrHttps(String url) {
        try {
            String protocol = new URL(url).getProtocol();
            return protocol.equalsIgnoreCase("http") || protocol.equalsIgnoreCase("https");
        } catch (MalformedURLException e) {
            return false;
        }
    }

    /**
     * This method concatenates baseUrl and currentUrl if the currentUrl is relative, otherwise checks if they are in
     * the same domain
     * @param baseUrl The page on which the url is found
     * @param currentUrl The url that is found
     * @param assertSameDomain To check if url belongs to the same domain
     * @return Fully formed {@code currentUrl} or {@code null} if not in the same domain
     * @throws MalformedURLException Invalid URL Exception
     */
    public static String resolveUrl(String baseUrl, String currentUrl, boolean assertSameDomain) throws MalformedURLException {

        if(isAbsoluteUrl(currentUrl)) {
            if(isHttpOrHttps(currentUrl)) {
                if(assertSameDomain){
                    return (isSameDomain(baseUrl, currentUrl)) ? currentUrl : null;
                }
                else{
                    return currentUrl;
                }
            }
            else {
                return null;
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
     * @param url URL
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

    /**
     * Function to check whether a URL conforms to an image family or not
     * @param url
     * @return {@code true} if it does, {@code false} otherwise
     */
    public static boolean isImageUrl(String url) {
        for(String ext : imageFormats){
            if(url.contains(ext)) return true;
        }
        return false;
    }

}
