package com.is.efacerecognitionmodule.data.model;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class LocationModel {
   private final double allowedLatitude ; //
   private final double allowedLongitude ;
   private final double allowedRadius ;

    public LocationModel(double allowedLatitude, double allowedLongitude, double allowedRadius) {
        this.allowedLatitude = allowedLatitude;
        this.allowedLongitude = allowedLongitude;
        this.allowedRadius = allowedRadius;
    }

    public double getAllowedLatitude() {
        return allowedLatitude;
    }

    public double getAllowedLongitude() {
        return allowedLongitude;
    }

    public double getAllowedRadius() {
        return allowedRadius;
    }

    @NotNull
    @Override
    public String toString() {
        return "LocationModel{" +
                "allowedLatitude=" + allowedLatitude +
                ", allowedLongitude=" + allowedLongitude +
                ", allowedRadius=" + allowedRadius +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LocationModel)) return false;
        LocationModel that = (LocationModel) o;
        return Double.compare(that.allowedLatitude, allowedLatitude) == 0 &&
                Double.compare(that.allowedLongitude, allowedLongitude) == 0 &&
                Double.compare(that.allowedRadius, allowedRadius) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(allowedLatitude, allowedLongitude, allowedRadius);
    }
}
