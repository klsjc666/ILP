package uk.ac.ed.inf;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URL;

/**
 * This is a singleton class used to read the data from REST.
 */
public class ReadREST {

    private static ReadREST readREST = null;
    public CentralAreaPoint[] centralAreaPoints;
    public NoFlyZone[] noFlyZones;
    private static String baseUrl = "https://ilp-rest.azurewebsites.net/";

    /**
     * The constructor will read all the data from given URL, we read the centralArea and noFlyZones data.
     */
    private ReadREST(){
        try {
            centralAreaPoints = new ObjectMapper().readValue(new URL(baseUrl + "centralArea") , CentralAreaPoint[].class);
            noFlyZones = new ObjectMapper().readValue(new URL(baseUrl + "noFlyZones") , NoFlyZone[].class);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * This method is used to get its own instance, if it's null, then create a new one.
     * @return An instance of ReadREST.
     */
    public static ReadREST getReadREST(){
        if (readREST == null){
            readREST = new ReadREST();
        }
        return readREST;
    }

}
