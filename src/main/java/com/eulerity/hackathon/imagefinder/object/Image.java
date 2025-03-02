package com.eulerity.hackathon.imagefinder.object;

import lombok.*;

/**
 * Image class consisting of {@code imageUrl} and {@code type} of image
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
public class Image {

    public static enum Type{
        GIF,
        FAVICON,
        LOGO,
        IMAGE
    }

    private String imageUrl;
    private Type type;

    /**
     * Initializes and categorizes the Image object
     * @param imageUrl
     * @return {@code Image} object
     */
    public static Image processImage(String imageUrl) {
        Image.Type imageObjectType;
        if(imageUrl.contains("favicon")){
            imageObjectType = Image.Type.FAVICON;
        }
        else if(imageUrl.contains("logo")){
            imageObjectType = Image.Type.LOGO;
        }
        else if(imageUrl.contains(".gif")){
            imageObjectType = Image.Type.GIF;
        }
        else{
            imageObjectType = Image.Type.IMAGE;
        }
        return new Image(imageUrl, imageObjectType);
    }

}
