package CashierView;

import java.util.HashMap;
import java.util.Map;

public class Order {
    private String name;
    private String price;
    private Map<String, Double> ing = new HashMap<>();
    private Integer quan;
    private Double total;

    Order(String name, String price, Integer quan, Map<String, Double> ing){
        this.name = name;
        this.price = price;
        this.ing = ing;
        this.quan = quan;
        this.total = Double.parseDouble(price.split(" ")[0])*quan;
    }

    public String getName(){
        return name;
    }

    public String getPrice(){
        return price;
    }

    public Double getTotal(){
        return total;
    }

    public Map<String, Double> getIng(){
        return ing;
    }

    public String getQuan(){
        return String.valueOf(quan);
    }

    public void setName(String name){
        this.name = name;
    }

    public void setPrice(String price){
        this.price = price;
    }

    public void setQuan(Integer quan){
        this.quan = quan;
        this.total = Double.parseDouble(price.split(" ")[0])*quan;
    }

}
