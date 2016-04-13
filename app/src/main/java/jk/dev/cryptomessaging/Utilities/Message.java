package jk.dev.cryptomessaging.Utilities;

import android.graphics.drawable.Drawable;

/**
 * Created by the awesome and extraordinary developer Jim on 18/1/2016.
 */
public class Message {
    private String fromName, message;
    private boolean isSelf;
    private boolean isImage = false;
    private Drawable drawable;

    public Message() {
    }

    public Message(String fromName, String message, boolean isSelf, boolean isImage,Drawable drawable) {
        this.fromName = fromName;
        this.message = message;
        this.isSelf = isSelf;
        this.isImage = isImage;
        this.drawable = drawable;
    }

    public Message(String fromName, String message, boolean isSelf) {
        this.fromName = fromName;
        this.message = message;
        this.isSelf = isSelf;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSelf() {
        return isSelf;
    }

    public void setSelf(boolean isSelf) {
        this.isSelf = isSelf;
    }

    public boolean isImage() {
        return isImage;
    }

    public void setImage(boolean image) {
        isImage = image;
    }

    public Drawable getDrawable() {
        return drawable;
    }

    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
    }
}