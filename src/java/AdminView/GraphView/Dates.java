package AdminView.GraphView;

import java.util.Map;

public class Dates {
    // The output format is
    // Date: {Total: *total*; TotalIng: *hashmap of total ing*; TotalOrdered: *hashmap of ordered products that day}
    private String date;
    private String total;

    // The date constructor
    Dates(String date, double total){
        this.date = date;
        this.total = String.valueOf(total) + " P";
    }

    // Both Ing and Prod
    private String ing, totIng, prod, totProd;
    Dates(String ing, String totIng, String prod, String totProd){
        this.ing = ing;
        this.totIng = totIng;
        this.prod = prod;
        this.totProd = totProd;
    }

    // Only ing
    Dates(String ing, String totIng){
        this.ing = ing;
        this.totIng = totIng;
    }

    // Only prod
    Dates(String prod, String totProd, Integer x){
        this.prod = prod;
        this.totProd = totProd;
    }

    public String getDate(){
        return date;
    }

    public String getTotal(){
        return total;
    }

    public String getIng(){
        return ing;
    }

    public String getTotIng(){
        return totIng;
    }

    public String getProd(){
        return prod;
    }

    public String getTotProd(){
        return totProd;
    }






}
