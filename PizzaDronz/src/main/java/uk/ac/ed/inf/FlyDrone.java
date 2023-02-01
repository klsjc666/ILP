package uk.ac.ed.inf;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * This class is used to fly the drone, check the battery, go throw all the orders and finally write the results into the files.
 */
public class FlyDrone {

    private static Restaurant[] restaurants;
    private static Order[] orders;
    private static NoFlyZone[] noFlyZones;
    private static int battery;
    private static LngLat position;
    private static boolean takeOrder;

    private static final LngLat appletonTower = new LngLat(-3.186874,55.944494);
    private static File deliveriesFile;
    private static File flightpathFile;
    private static File droneFile;

    private static FileWriter deliveries;
    private static FileWriter flightpath;
    private static FileWriter drone;
    private static final int capacity = 4;
    private static int currentLoad;
    private static final String AppletonTower = "appletonTower";

    /**
     * The constructor which take the url and date and create all the output files, delete all the previous files with the same name
     * @param baseURL the provided base url
     * @param date the date we are processing
     */
    public FlyDrone(String baseURL, String date){
        try{
        restaurants = Restaurant.getRestaurantsFromRestServer(new URL(baseURL + "/restaurants"));
        orders = Order.getOrdersFromRestServer(new URL(baseURL + "/orders/" + date));
        }catch (IOException e){
            System.err.println("Invalid URL");
            e.printStackTrace();
        }
        currentLoad = 0;
        battery = 2000;
        position = new LngLat(-3.186874,55.944494);
        takeOrder = true;
        deliveriesFile = new File("outputs/deliveries-" + date + ".json");
        flightpathFile = new File("outputs/flightpath-" + date + ".json");
        droneFile = new File("outputs/drone-" + date + ".geojson");
        deliveriesFile.delete();
        flightpathFile.delete();
        droneFile.delete();
        try{
            deliveriesFile.createNewFile();
            flightpathFile.createNewFile();
            droneFile.createNewFile();
            deliveries = new FileWriter("outputs/deliveries-" + date + ".json");
            flightpath = new FileWriter("outputs/flightpath-" + date + ".json");
            drone = new FileWriter("outputs/drone-" + date + ".geojson");
        }catch (IOException e){
            System.err.println("The file generation is wrong");
            e.printStackTrace();
        }
    }

    /**
     * This method generates all the best route between restaurants and appleton tower.
     * @return a map with the restaurants' name and the destination name and their corresponding best route.
     */
    private static Map<String,ArrayList<LngLat>> generateRestaurantPath(){
        Map<String,ArrayList<LngLat>> allPaths = new HashMap<>();
        for (Restaurant restaurant : restaurants){
            allPaths.put(restaurant.getName(), FindPath.generateGoodFlightpath(new LngLat(restaurant.getLongitude(),restaurant.getLatitude()), appletonTower));
        }
        return allPaths;
    }

    /**
     * This method is used to write the result into the file.
     * @param fileContent all the needed data for writting into the file.
     */
    private static void writeFiles(ArrayList<ArrayList<Object>> fileContent){
        JSONArray pathArray = new JSONArray();
        JSONObject path;
        ArrayList<LngLat> droneFlightPath;
        int timeTick;

        var features = new ArrayList<Feature>();

        String orderNo;
        LngLat from;
        LngLat to;
        ArrayList<Point> fromPointtoPoint;
        for (ArrayList<Object> order : fileContent){
            orderNo = (String) (order.get(0));
            droneFlightPath = (ArrayList<LngLat>) (order.get(1));
            timeTick = (int) (order.get(2));
            fromPointtoPoint = new ArrayList<>();
            for (int i = 0; i < droneFlightPath.size()-1 ; i++){
                from = droneFlightPath.get(i);
                to = droneFlightPath.get(i+1);
                double turingAngle = from.closedAngle(to);
                path = new JSONObject();
                path.put("orderNo" , orderNo);
                path.put("fromLongitude" , from.lng());
                path.put("fromLatitude" , from.lat());
                path.put("angle" , from.closedAngle(to));
                path.put("toLongitude" , to.lng());
                path.put("toLatitude" , to.lat());
                path.put("ticksSinceStartOfCalculation" , timeTick);
                pathArray.add(path);
                fromPointtoPoint.add(Point.fromLngLat(from.lng(),from.lat()));
                fromPointtoPoint.add(Point.fromLngLat(to.lng(),to.lat()));
                LineString lineString = LineString.fromLngLats(fromPointtoPoint);
                var feature = Feature.fromGeometry(lineString);
                feature.addStringProperty("orderNo", orderNo);
                feature.addNumberProperty("angle", turingAngle);
                feature.addNumberProperty("ticksSinceStartOfCalculation", timeTick);
                features.add(feature);
            }

        }

        var featureCollection = FeatureCollection.fromFeatures(features);
        var finalGeoJson = featureCollection.toJson();

        JSONArray deliveryArray = new JSONArray();
        JSONObject delivery;
        String outcome;
        for (Order order : orders){
            outcome = order.getOutcome().toString();
            delivery = new JSONObject();
            delivery.put("orderNo", order.getOrderNo());
            delivery.put("outcome",outcome);
            delivery.put("costInPence",order.getPriceTotalInPence());
            deliveryArray.add(delivery);
        }
        try{
            deliveries.write(deliveryArray.toJSONString());
            deliveries.close();
            flightpath.write(pathArray.toJSONString());
            flightpath.close();
            drone.write(finalGeoJson);
            drone.close();
        }catch (IOException e){
            System.err.println("The file generation have some error");
            e.printStackTrace();
        }
    }

    /**
     * The method that contain runs the drone, fly it to deliver the pizzas and finally write the results into the file.
     */
    public static void fly(){
        Map<String , ArrayList<LngLat>> allRestaurantPath = generateRestaurantPath();
        Order.OrderOutcome outcome;
        ArrayList<ArrayList<Object>> fileContent = new ArrayList<>();
        ArrayList<Object> singleOrderRecord;
        ArrayList<LngLat> wholePath;
        ArrayList<LngLat> path;
        ArrayList<Integer> steps = new ArrayList<>();
        Map<Integer , String> stepsAndNames = new HashMap<>();
        ArrayList<String> closestRestaurant = new ArrayList<>();
        ArrayList<ArrayList<Order>> sortedOrder = new ArrayList<>();
        sortedOrder.add(new ArrayList<>());
        sortedOrder.add(new ArrayList<>());
        sortedOrder.add(new ArrayList<>());
        sortedOrder.add(new ArrayList<>());
        for (Restaurant restaurant : restaurants){
            steps.add(allRestaurantPath.get(restaurant.getName()).size());
            stepsAndNames.put(allRestaurantPath.get(restaurant.getName()).size(),restaurant.getName());
        }
        Collections.sort(steps);
        for (int number : steps){
            closestRestaurant.add(stepsAndNames.get(number));
        }
        for (Order order1 : orders) {
            outcome = order1.generateOrderState(restaurants);
            if (outcome == Order.OrderOutcome.ValidButNotDelivered) {
                sortedOrder.get(closestRestaurant.indexOf(order1.getOrderRestaurant().getName())).add(order1);
            }
        }
        for (ArrayList<Order> orders1 : sortedOrder){
            for (Order order : orders1) {
                singleOrderRecord = new ArrayList<>();
                wholePath = new ArrayList<>();
                path = allRestaurantPath.get(order.getOrderRestaurant().getName());
                if (battery >= 2 + path.size() * 2) {
                    battery = battery - 2 - path.size() * 2;
                    singleOrderRecord.add(order.getOrderNo());
                    for (int t = path.size() - 1; t > -1; t--) {
                        wholePath.add(path.get(t));
                    }
                    wholePath.addAll(path);
                    singleOrderRecord.add(wholePath);
                    singleOrderRecord.add((int) (new Date().getTime() / 1000));
                    order.OrderDelivered();
                    fileContent.add(singleOrderRecord);

                }
            }
        }
        writeFiles(fileContent);
    }



}
