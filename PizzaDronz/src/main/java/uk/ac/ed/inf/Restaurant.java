package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;

/**
 * This class is used to store the restaurant data from REST.
 */
public class Restaurant {


    @JsonProperty("name")
    private String name;
    @JsonProperty("longitude")
    private double longitude;
    @JsonProperty("latitude")
    private double latitude;
    @JsonProperty("menu")
    private Menu[] menu;

    /**
     * This is a getter for the menu instance.
     * @return The menu stored for this restaurant.
     */
    public Menu[] getMenu(){
        return menu;
    }

    public String getName() {
        return name;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    /**
     *
     * @param serverBaseAddress The URL which we should get the restaurants data from.
     * @return Restaurant[] An array of restaurants which is read from the REST.
     * @throws IOException On input error.
     */
    public static Restaurant[] getRestaurantsFromRestServer(URL serverBaseAddress) throws IOException {
        return new ObjectMapper().readValue(serverBaseAddress,Restaurant[].class);
    }
}