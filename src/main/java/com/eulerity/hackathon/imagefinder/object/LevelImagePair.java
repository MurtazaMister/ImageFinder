package com.eulerity.hackathon.imagefinder.object;

import java.util.concurrent.CopyOnWriteArrayList;

public class LevelImagePair {
    int level;
    CopyOnWriteArrayList<Image> images;

    public LevelImagePair(int level) {
        this.level = level;
        images = new CopyOnWriteArrayList<>();
    }

    public void add(Image image) {
        images.add(image);
    }
}
