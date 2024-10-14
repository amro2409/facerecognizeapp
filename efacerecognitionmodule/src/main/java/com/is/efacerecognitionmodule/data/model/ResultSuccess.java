package com.is.efacerecognitionmodule.data.model;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ResultSuccess implements Serializable {
    private  String name;
    private  float distance;
    private  String time;
    private  String status;
    private  Bitmap image;

    public ResultSuccess(String name, float distance) {
        this.name = name;
        this.distance = distance;
        this.time = new SimpleDateFormat("HH:mm", Locale.ENGLISH).format(new Date());
        this.status = "success";
    }

    public ResultSuccess setName(String name) {
        this.name = name;
        return this;
    }

    public ResultSuccess setDistance(float distance) {
        this.distance = distance;
        return this;
    }

    public ResultSuccess setTime(String time) {
        this.time = time;
        return this;
    }

    public ResultSuccess setStatus(String status) {
        this.status = status;
        return this;
    }

    public ResultSuccess setImage(Bitmap image) {
        this.image = image;
        return this;
    }

    public String getName() {
        return name;
    }

    public Bitmap getImage() {
        return image;
    }

    public float getDistance() {
        return distance;
    }

    public String getStatus() {
        return status;
    }

    public String getTime() {
        return time;
    }

}
