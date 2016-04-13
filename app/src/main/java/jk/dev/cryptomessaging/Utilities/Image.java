package jk.dev.cryptomessaging.Utilities;

import android.graphics.Bitmap;

/**
 * Created by Jim on 13/4/2016.
 */
public class Image implements Comparable<Image> {
    private String path, category, date;
    private Bitmap thumb;

    public Image(String path, String category, String date, Bitmap thumb) {
        this.path = path;
        this.category = category;
        this.date = date;
        this.thumb = thumb;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Bitmap getThumb() {
        return thumb;
    }

    public void setThumb(Bitmap thumb) {
        this.thumb = thumb;
    }

    @Override
    public int compareTo(Image another) {
        return 0;
    }
}
