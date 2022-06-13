package AdminView.Inventory;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Stock {
    private Integer id;
    private Double stock;
    private String name;

    Stock(int id, String name, String stock){
        this.id = id;
        this.name = name;
        this.stock = Double.valueOf(stock);
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public int getId(){
        return id;
    }

    public String getStock(){
        return stock.toString();
    }

    public void setStock(Double newValue){
        this.stock = newValue;
    }

    public void save(CollectionReference cr){
        Map<String, Object> temp = new HashMap<>();
        temp.put("Name",name);
        temp.put("Stock",stock);
        cr.document(id.toString()).set(temp);
    }

}
