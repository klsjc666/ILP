package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NoFlyZone {

    @JsonProperty("name")
    private String name;
    @JsonProperty("coordinates")
    private double[][] coordinates;

    public double[][] getCoordinates() {
        return coordinates;
    }
}
