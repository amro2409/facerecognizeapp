package com.is.efacerecognitionmodule.data.model;

import android.graphics.Bitmap;
import android.graphics.RectF;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * An immutable result returned by a Classifier describing what was recognized.
 * <p>
 * A unique identifier for what has been recognized. Specific to the class, not the instance of
 * the object.
 * <p>
 * Display name for the recognition.
 * <p>
 * A sortable score for how good the recognition is relative to others. Lower should be better.
 * <p>
 * Optional location within the source image for the location of the recognized object.
 */
/*public class Recognition {
 *//**
 * A unique identifier for what has been recognized. Specific to the class, not the instance of
 * the object.
 *//*
    private final String id;

    *//**
 * Display name for the recognition.
 *//*
    private final String title;

    *//**
 * A sortable score for how good the recognition is relative to others. Lower should be better.
 *//*
    private final Float distance;
    private Object extraEmbed;

    *//**
 * Optional location within the source image for the location of the recognized object.
 *//*
    private RectF location;
    private Integer color;
    private Bitmap crop;

    public Recognition(final String id, final String title, final Float distance, final RectF location) {
        this.id = id;
        this.title = title;
        this.distance = distance;
        this.location = location;
        this.color = null;
        this.extraEmbed = null;
        this.crop = null;
    }

    public void setExtraEmbed(Object extraEmbed) {
        this.extraEmbed = extraEmbed;
    }

    public Object getExtraEmbed() {
        return this.extraEmbed;
    }

    public void setColor(Integer color) {
        this.color = color;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Float getDistance() {
        return distance;
    }

    public RectF getLocation() {
        return new RectF(location);
    }

    public void setLocation(RectF location) {
        this.location = location;
    }

    @NotNull
    @Override
    public String toString() {
        String resultString = "";
        if (id != null) {
            resultString += "[" + id + "] ";
        }

        if (title != null) {
            resultString += title + " ";
        }

        if (distance != null) {
            resultString += String.format(Locale.ENGLISH, "(%.1f%%) ", distance * 100.0f);
        }

        if (location != null) {
            resultString += location + " ";
        }

        return resultString.trim();
    }

    public Integer getColor() {
        return this.color;
    }

    public void setCrop(Bitmap crop) {
        this.crop = crop;
    }

    public Bitmap getCrop() {
        return this.crop;
    }
}*/

/**
 * An immutable result returned by a Classifier describing what was recognized.
 */
public class Recognition {
    /**
     * A unique identifier for what has been recognized. Specific to the class, not the instance of
     * the object.
     */
    private final String id;
    /**
     * Display name for the recognition.
     */
    private final String title;
    /**
     * A sortable score for how good the recognition is relative to others. Lower should be better.
     */
    private final float distance;

    /**
     * Optional location within the source image for the location of the recognized object.
     */
    private final RectF location;
    private final int color;
    private final Object extraEmbed;
    private final Bitmap crop;

    private Recognition(Builder builder) {
        this.id = builder.id;
        this.title = builder.title;
        this.distance = builder.confidence;
        this.location = builder.location;
        this.color = builder.color;
        this.extraEmbed = builder.extraEmbed;
        this.crop = builder.crop;
    }

    public String getId() {
        return id;
    }

    public float getDistance() {
        return distance;
    }

    public String getTitle() {
        return title;
    }

    public RectF getLocation() {
        return location;
    }

    public Object getExtraEmbed() {
        return extraEmbed;
    }

    public Bitmap getCrop() {
        return crop;
    }

    public int getColor() {
        return color;
    }

    public static class Builder {
        private String id;
        private String title;
        private float confidence;
        private RectF location;
        private int color;
        private Object extraEmbed;
        private Bitmap crop;

        public Builder() {

        }

        public Builder(@NotNull Recognition result) {
            this.id = result.id;
            this.title = result.title;
            this.confidence = result.distance;
            this.location = result.location;
            this.color = result.color;
            this.extraEmbed = result.extraEmbed;
            this.crop = result.crop;
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setConfidence(float confidence) {
            this.confidence = confidence;
            return this;
        }

        public Builder setLocation(RectF location) {
            this.location = location;
            return this;
        }

        public Builder setColor(int color) {
            this.color = color;
            return this;
        }

        public Builder setExtraEmbed(Object extraEmbed) {
            this.extraEmbed = extraEmbed;
            return this;
        }

        public Builder setCrop(Bitmap crop) {
            this.crop = crop;
            return this;
        }

        public Recognition create() {
            return new Recognition(this);
        }
    }

    @NotNull
    @Override
    public String toString() {
        String resultString = "";
        if (id != null) {
            resultString += "[" + id + "] ";
        }

        if (title != null) {
            resultString += title + " ";
        }

        if (distance > 0) {
            resultString += String.format(Locale.ENGLISH, "(%.1f%%) ", distance * 100.0f);
        }

        if (location != null) {
            resultString += location + " ";
        }

        return resultString.trim();
    }
}

