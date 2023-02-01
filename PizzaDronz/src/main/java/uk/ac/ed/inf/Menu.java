package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class is used to store the menu data from REST.
 */
public class Menu {

    @JsonProperty("name")
    private String name;
    @JsonProperty("priceInPence")
    private int priceInPence;

    public String getName() {
        return name;
    }

    public int getPriceInPence() {
        return priceInPence;
    }
}
