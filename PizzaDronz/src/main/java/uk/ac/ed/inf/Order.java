package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;

/**
 * This class is used to store the order data.
 */
public class Order {

    @JsonProperty("orderNo")
    private String orderNo;
    @JsonProperty("orderDate")
    private Date orderDate;
    @JsonProperty("customer")
    private String customer;
    @JsonProperty("creditCardNumber")
    private String creditCardNumber;
    @JsonProperty("creditCardExpiry")
    private String creditCardExpiry;
    @JsonProperty("cvv")
    private String cvv;
    @JsonProperty("priceTotalInPence")
    private int priceTotalInPence;
    @JsonProperty("orderItems")
    private String[] orderItems;

    private OrderOutcome outcome = null;

    private Restaurant orderRestaurant = null;


    public enum OrderOutcome{
        Delivered,
        ValidButNotDelivered,
        InvalidCardNumber,
        InvalidExpiryDate,
        InvalidCvv,
        InvalidTotal,
        InvalidPizzaNotDefined,
        InvalidPizzaCount,
        InvalidPizzaCombinationMultipleSuppliers,
        Invalid
    }

    /**
     *
     * @param restaurants AN array of the participating restaurants.
     * @param pizzaOrders An array stored the pizza names ordered.
     * @return int The total delivery fee calculated, if the pizza name is not found from the restaurant's menu return -1, if pizza is not found at all return -2.
     * @error InvalidPizzaCombinationException is caught when the pizza name is not found from the restaurant's menu, OrdersNotFromGivenRestaurantsException is caught when the pizza is not found from all given restaurant's menu.
     */
    private int getDeliveryCost(Restaurant[] restaurants, String[] pizzaOrders){
        //Used to check if the restaurant which the order belong is found
        boolean found = false;
        //One pound of the delivery fee to begin calculating
        int cost = 100;
        for (Restaurant restaurant : restaurants){
            for (Menu menu : restaurant.getMenu()){
                if (menu.getName().equals(pizzaOrders[0])){
                    orderRestaurant = restaurant;
                    found = true;
                    break;
                }
            }
            if (found){
                for (String pizzaOrdered : pizzaOrders){
                    //Now it's used to see if the pizza is found in this restaurant's menu
                    found = false;
                    for (Menu menu : orderRestaurant.getMenu()){
                        if (menu.getName().equals(pizzaOrdered)){
                            cost += menu.getPriceInPence();
                            found = true;
                            break;
                        }
                    }
                    //If the pizza name is not found from this restaurant's menu, then it's not from this restaurant, return -1
                    if (!(found)){
                        //System.err.println("Invalid Pizza Combination");
                        return -1;
                    }
                }
                return cost;
            }
        }
        //If the pizza is not found in all the given restaurant's menus, then return -2
        //System.err.println("Orders Not From Given Restaurants");
        return -2;
    }

    /**
     *
     * @param serverBaseAddress the url provide the address the order stored.
     * @return The orders read from the url address.
     * @throws IOException is cought when the url is wrong.
     */
    public static Order[] getOrdersFromRestServer(URL serverBaseAddress) throws IOException {
        return new ObjectMapper().readValue(serverBaseAddress,Order[].class);
    }

    /**
     * set the order outcome to Delivered
     */
    public void OrderDelivered(){
        outcome = OrderOutcome.Delivered;
    }

    /**
     * Check the order and change the order outcome and return it.
     * @param restaurants restaurant list stored all the restaurants.
     * @return The order outcome of this order.
     */
    public OrderOutcome generateOrderState(Restaurant[] restaurants){
        if (outcome != null){
            return outcome;
        }
        if (creditCardNumber.length() != 16){
            outcome = OrderOutcome.InvalidCardNumber;
            return outcome;
        }
        if (!((Character.isDigit(creditCardExpiry.charAt(0))) && (Character.isDigit(creditCardExpiry.charAt(1)))
                && (Character.isDigit(creditCardExpiry.charAt(3))) && (Character.isDigit(creditCardExpiry.charAt(4))))){
            outcome = OrderOutcome.InvalidExpiryDate;
            return outcome;
        }
        if ((creditCardExpiry.length() != 5) || (creditCardExpiry.charAt(2) != '/')){
            outcome = OrderOutcome.InvalidExpiryDate;
            return outcome;
        }
        if (((creditCardExpiry.charAt(3)  - '0') * 10 + ( creditCardExpiry.charAt(4) - '0') < 23)){
            outcome = OrderOutcome.InvalidExpiryDate;
            return outcome;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(orderDate);
        if ((( creditCardExpiry.charAt(3) - '0') * 10 + ( creditCardExpiry.charAt(4) - '0') == 23)){
            if (((( creditCardExpiry.charAt(0) - '0') * 10 + ( creditCardExpiry.charAt(1) - '0')) < cal.get(Calendar.MONTH))
                    || ((( creditCardExpiry.charAt(0) - '0') * 10 + ( creditCardExpiry.charAt(1) - '0')) > 12)){
                outcome = OrderOutcome.InvalidExpiryDate;
                return outcome;
            }
        }
        if (cvv.length() != 3){
            outcome = OrderOutcome.InvalidCvv;
            return outcome;
        }
        if (orderItems.length < 1 || orderItems.length > 4){
            outcome = OrderOutcome.InvalidPizzaCount;
            return outcome;
        }
        int deliveryCost = getDeliveryCost(restaurants, orderItems);
        if (deliveryCost == -1){
            outcome = OrderOutcome.InvalidPizzaCombinationMultipleSuppliers;
            return outcome;
        }
        if (deliveryCost == -2){
            outcome = OrderOutcome.InvalidPizzaNotDefined;
            return outcome;
        }
        if (deliveryCost != priceTotalInPence){
            outcome = OrderOutcome.InvalidTotal;
            return outcome;
        }
        outcome = OrderOutcome.ValidButNotDelivered;
        return outcome;
    }

    /**
     *
     * @return the order outcome.
     */
    public OrderOutcome getOutcome(){
        return outcome;
    }

    /**
     *
     * @return get the restaurant this order belongs to.
     */

    public Restaurant getOrderRestaurant(){return orderRestaurant;}

    public int getPizzaNumber(){return orderItems.length;}

    public String getOrderNo() {
        return orderNo;
    }

    public int getPriceTotalInPence() {
        return priceTotalInPence;
    }
}
