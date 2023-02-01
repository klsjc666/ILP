package uk.ac.ed.inf;

import java.lang.Math;
import java.util.ArrayList;

/**
 * This record is used to store the data of any point in the map and also provide several functions related with those points.
 * @param lng This is used to store the longitude data for this point.
 * @param lat This is used to store the latitude data for this point.
 */
public record LngLat (double lng, double lat){

    private static final double MOVE_DISTANCE = 0.00015;

    private static final double[] angles = {0,22.5,45,67.5,90,112.5,135,157.5,180,202.5,225,247.5,270,292.5,315,337.5};

    /**
     * Check if the point is in the given polygon.
     * @param polygonPoints the polygon points.
     * @param point the current point.
     * @return weather this point is in the polygon.
     */
    private static boolean inPolygon(ArrayList<LngLat> polygonPoints, LngLat point){
        int crossNumber = 0;
        for (int i = 0; i < polygonPoints.size(); i++) {
            LngLat p1 = polygonPoints.get(i);
            LngLat p2 = polygonPoints.get((i+1)% polygonPoints.size());
            // no intersection if parallel (or same line)
            if (p1.lat == p2.lat)
                continue;
            // Either top / bottom then no intersection
            if (point.lat < Math.min(p1.lat, p2.lat))
                continue;
            if (point.lat >= Math.max(p1.lat, p2.lat))
                continue;
            double pointLng = (point.lat - p1.lat) * (p2.lng - p1.lng) / (p2.lat - p1.lat) + p1.lng;
            if (pointLng > point.lng)
                crossNumber++;

        }
        return (crossNumber % 2 == 1);
    }

    /**
     * Check if the point is on the polygon edge.
     * @param polygonPoints the polygon points.
     * @param point the current point.
     * @return weather this point is on the polygon edge.
     */
    private static boolean onPolygonEdge(ArrayList<LngLat> polygonPoints, LngLat point){
        for (int i = 0; i < polygonPoints.size(); i++) {
            LngLat p1 = polygonPoints.get(i);
            LngLat p2 = polygonPoints.get((i + 1) % polygonPoints.size());

            if (point.lat < Math.min(p1.lat, p2.lat))
                continue;

            if (point.lat > Math.max(p1.lat, p2.lat))
                continue;

            if (p1.lat == p2.lat) {
                double minLng = Math.min(p1.lng, p2.lng);
                double maxLng = Math.max(p1.lng, p2.lng);
                //return true if same line
                if ((point.lat == p1.lat) && (point.lng >= minLng && point.lng <= maxLng)) {
                    return true;
                }
            } else {
                double x = (point.lat - p1.lat) * (p2.lng - p1.lng) / (p2.lat - p1.lat) + p1.lng;
                if (x == point.lng)
                    return true;
            }
        }
        return false;
    }

    /**
     * Check if the point is contained in the polygon.
     * @param polygonPoints the polygon points.
     * @param point the current point.
     * @return weather this point is contained in the polygon.
     */
    public static boolean polygonContains(ArrayList<LngLat> polygonPoints, LngLat point){
        return inPolygon(polygonPoints, point) || onPolygonEdge(polygonPoints, point);
    }

    /**
     * This method can find out if this point is in the central area or not.
     * @return This is the boolean value which true means this point is in the central area false means not.
     */
    public boolean inCentralArea(){
        CentralAreaPoint[] centralAreaPoints = ReadREST.getReadREST().centralAreaPoints;
        ArrayList<LngLat> centralArea = new ArrayList<>();
        for (CentralAreaPoint centralAreaPoint : centralAreaPoints){
            centralArea.add(new LngLat(centralAreaPoint.getLongitude(),centralAreaPoint.getLatitude()));
        }
        return polygonContains(centralArea,this);
    }

    /**
     * This method can check if this point is in any of the no-fly zones.
     * @return weather the point is in the no-fly zone, true is in.
     */
    public boolean inNoFlyZone(){
        NoFlyZone[] noFlyZones = ReadREST.getReadREST().noFlyZones;
        ArrayList<LngLat> zoneLngLat;
        for (NoFlyZone noFlyZone : noFlyZones){
            zoneLngLat = new ArrayList<>();
            for (double[] singleZone : noFlyZone.getCoordinates()){
                zoneLngLat.add(new LngLat(singleZone[0],singleZone[1]));
            }
            if (polygonContains(zoneLngLat,this)){
                return true;
            }
        }
        return false;
    }

    /**
     * This method calculates the distance between this point and the given point
     * @param ll This is the other point which we need to calculate the distance to it.
     * @return The distance from this point to the given point.
     */
    public double distanceTo(LngLat ll){
        return Math.sqrt(Math.pow(ll.lng - lng,2) + Math.pow(ll.lat - lat,2));
    }

    /**
     * This method finds out if this point is close to the given point
     * @param ll The given point to find out if this point is close to it
     * @return A boolean value which shows whether this point is close to the given point
     */
    public boolean closeTo(LngLat ll){
        return distanceTo(ll) <= MOVE_DISTANCE;
    }

    /**
     * This method takes a direction and calculate the point of the next step
     * @param angle A double which is the direction angle
     * @return LngLat A point which is the next position on the given direction
     */
    public LngLat nextPosition(double angle) {
        return new LngLat(LngLat.MOVE_DISTANCE * Math.cos(Math.toRadians(angle)) + lng,
                LngLat.MOVE_DISTANCE * Math.sin(Math.toRadians(angle)) + lat
        );
    }


    /**
     * This method get the exact angle between this point and the given point.
     * @param ll the given point.
     * @return the exact angle between this point and the parameter.
     */
    public double angleBetween(LngLat ll){
        double angle = Math.toDegrees(Math.atan(Math.abs(ll.lat - lat)/Math.abs(ll.lng - lng)));
        if (ll.lng > lng){
            if (ll.lat < lat){
                angle = 360 - angle;
            }
        }else{
            if (ll.lat > lat){
                angle += 180 - angle;
            }else{
                angle += 180;
            }
        }
        return angle;
    }

    /**
     * This method get an exact angle between this point and the given point first and check all the directions drone is allowed to move and provide the closed direction
     * @param ll the given point
     * @return the closest direction
     */
    public double closedAngle(LngLat ll){
        double a = this.angleBetween(ll);
        double smallest = 360;
        int index = 0;
        int smallestIndex = 0;
        for (double angle : angles){
            if (Math.abs(angle - a) < smallest){
                smallest = Math.abs(angle - a);
                smallestIndex = index;
            }
            index ++;
        }
        return angles[smallestIndex];
    }
}