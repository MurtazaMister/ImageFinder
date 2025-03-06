package com.eulerity.hackathon.imagefinder.object;

import lombok.*;

import java.util.Objects;

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
        SVG,
        IMAGE
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Image image = (Image) obj;
        return Objects.equals(imageUrl, image.imageUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(imageUrl);
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
        if(imageUrl.toLowerCase().contains("favicon") || imageUrl.toLowerCase().contains(".ico")){
            imageObjectType = Image.Type.FAVICON;
        }
        else if(imageUrl.toLowerCase().contains("logo")){
            imageObjectType = Image.Type.LOGO;
        }
        else if(imageUrl.toLowerCase().contains(".gif")){
            imageObjectType = Image.Type.GIF;
        }
        else if(imageUrl.toLowerCase().contains(".svg")){
            imageObjectType = Image.Type.SVG;
        }
        else{
            imageObjectType = Image.Type.IMAGE;
        }
        return new Image(imageUrl, imageObjectType);
    }

}
