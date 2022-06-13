package AdminView.Products;

import com.google.cloud.firestore.CollectionReference;

import java.util.HashMap;
import java.util.Map;

public class Prods {
    private Integer id;
    private String name;
    private Object stock;


    public Prods(Integer id, String pN, Object st){
        this.id = id;
        name = pN;
        this.stock = st;
    }

    public Prods(String pN, Object st){
        name = pN;
        this.stock = st;
    }

    public Prods(String pN){
        this.name = pN;
        this.stock = null;
    }

    public Integer getId(){
        return id;
    }
    public String getName(){
        return name;
    }
    public Object getStock(){
        return stock;
    }
    public void save(CollectionReference cr){
        Map<String, Object> temp = new HashMap<>();
        temp.put("Name",name);
        temp.put("Stock",Double.parseDouble((((String)stock).split("P"))[0]));
        temp.put("Ingredients", null);
        cr.document(id.toString()).set(temp);
    }
}
