package AdminView.Products;

import com.google.cloud.firestore.CollectionReference;

import java.util.HashMap;
import java.util.Map;

public class Prods {
    private Integer id;
    private String name;
    private String ing;
    private String stock;


    public Prods(Integer id, String pN, String st){
        this.id = id;
        name = pN;
        this.stock = st;
        this.ing = null;
    }

    public Prods(String pN, String st){
        ing = pN;
        this.stock = st;
        this.name = null;
    }

    public Prods(){
        this.name = null;
        this.stock = null;
    }

    public Integer getId(){
        return id;
    }
    public String getName(){
        return name;
    }
    public String getIng() {return ing;}
    public Object getStock(){
        return stock;
    }
    public void setProd(Object prod){
        this.name = (String) prod;
    }
    public void setStock(String stock){
        this.stock = stock;
    }
    public void setIng(String pN) {this.ing = pN;}

    public void save(CollectionReference cr, Object Ing){
        Map<String, Object> temp = new HashMap<>();
        temp.put("Name",name);
        temp.put("Stock",Double.parseDouble(((stock.toString()).split(" "))[0]));
        temp.put("Ingredients", Ing);
        cr.document(id.toString()).set(temp);
    }

    public void save(CollectionReference cr, Prods given, Object Ing){
        Map<String, Object> temp = new HashMap<>();
        temp.put("Name",given.getName());
        temp.put("Stock",Double.parseDouble(((given.getStock().toString()).split(" "))[0]));
        temp.put("Ingredients", Ing);
        cr.document(given.getId().toString()).set(temp);
        // cr.document("0").set("Name": *string, "Stock": Double")
    }
}
