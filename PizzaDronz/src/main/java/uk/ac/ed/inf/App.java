package uk.ac.ed.inf;

import java.io.IOException;
import java.net.URL;

/**
 * mein method that firstly check the validation of the date and then fly the drones.
 */
public class App {
    public static void main(String[] args){
        if (ExceptionChecks.checkDate(args[0])){
            try{
                Order[] order = Order.getOrdersFromRestServer(new URL(args[1] + "/orders/" + args[0]));
                FlyDrone flyDrone = new FlyDrone(args[1],args[0]);
                flyDrone.fly();
            }catch (IOException ignore){
                System.err.println("The given url is wrong");
            }
        }
        //System.out.println(ReadREST.getReadREST().noFlyZones[0].coordinates[0][0]);
        //System.out.println(ReadREST.getReadREST().noFlyZones[0].coordinates[0][1]);
    }
}
