package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class is used to store the points from the central area data in REST.
 */
public class CentralAreaPoint{

    @JsonProperty("name")
    private String name;
    @JsonProperty("longitude")
    private double longitude;
    @JsonProperty("latitude")
    private double latitude;

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }
}
