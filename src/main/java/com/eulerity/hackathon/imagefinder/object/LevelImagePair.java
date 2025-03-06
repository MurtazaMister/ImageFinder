package com.eulerity.hackathon.imagefinder.object;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Support class built for storing the depth of the URL being parsed, and images associated with it.
 */
public class LevelImagePair {
    int level;
    HashSet<Image> images;

    public LevelImagePair(int level) {
        this.level = level;
        images = new LinkedHashSet<>();
    }

    public void add(Image image) {
        images.add(image);
    }
}
