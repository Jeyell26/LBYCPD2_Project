package AdminView.Products;

import AdminView.Inventory.Stock;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

public class ProductController implements Initializable {

    @FXML
    private Button back;

    @FXML
    private TreeTableView<Prods> mainView;

    @FXML
    private TreeTableColumn<Stock, String> prodCol;

    @FXML
    private TreeTableColumn<Stock, String> stockCol;

    @FXML
    private TreeTableColumn<Stock, Integer> idCol;

    @FXML
    private Button addIng;

    @FXML
    private Button del;

    @FXML
    private Button addProd;

    @FXML
    private TextField searchTF;

    @FXML
    private TextField prodTF;

    // Database
    Firestore db = FirestoreClient.getFirestore();
    CollectionReference cr = db.collection("Cashier Product");
    DocumentReference docRef = cr.document("_");
    ApiFuture<DocumentSnapshot> future = docRef.get();
    DocumentSnapshot document;

    TreeItem<Prods> hidden;
    ArrayList<Integer> idTrack = new ArrayList<>();


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            initTableView();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        addProd.setOnAction(e -> addProduct());
    }

    public void addProduct(){
        if(!prodTF.getText().isBlank()){
            Integer id = 0;
            while(idTrack.contains(id)){
                id++;
            }
            idTrack.add(id);
            Prods newProd = new Prods(id,prodTF.getText(),0 + " P");
            TreeItem<Prods> itemAdder = new TreeItem<>(newProd);
            hidden.getChildren().add(itemAdder);
            prodTF.clear();
            newProd.save(cr);
        }
        else{
            prodTF.setPromptText("Insert Text Here");
        }
    }

    // Loads the items from database and returns a Map within a Map
    private Map<Integer,Map<String, Object>> loadItems() throws ExecutionException, InterruptedException {
        Iterable<DocumentReference> trial = cr.listDocuments();
        Map<Integer,Map<String, Object>> out = new HashMap<>();
        // The output format is
        // ID: {Name: *name*; Stock: *stock*}
        for (DocumentReference element : trial) {
            // Stores the current element in temp
            DocumentSnapshot temp = element.get().get();
            Integer id = Integer.valueOf(element.getId());
            String name = temp.getString("Name");
            Double stock = temp.getDouble("Stock");
            Map<String,Object> ing = (Map<String, Object>) temp.get("Ingredients");

            if(!idTrack.contains(id)){
                idTrack.add(id);
            }

            // Store sub values inside hashmap
            Map<String, Object> mapTemp = new HashMap<>();
            mapTemp.put("Name",name);
            mapTemp.put("Stock",stock);

            Map<String, Object> ingTemp = new HashMap<>();

            if(!(ing == null)){
                for(String x: ing.keySet()){
                    ingTemp.put(x,ing.get(x));
                }
            }


            mapTemp.put("ingredients", ingTemp);

            out.put(id,mapTemp);
        }
        return out;
    }

    // Initializes first run table view
    public void initTableView() throws ExecutionException, InterruptedException {

        // Col settings.
        idCol.setReorderable(false);
        prodCol.setReorderable(false);
        stockCol.setReorderable(false);

        // Variable for hidden root
        hidden = new TreeItem();

        // Setting Col variables
        idCol.setCellValueFactory(new TreeItemPropertyValueFactory<Stock, Integer>("id"));
        prodCol.setCellValueFactory(new TreeItemPropertyValueFactory<Stock, String>("name"));
        stockCol.setCellValueFactory(new TreeItemPropertyValueFactory<Stock, String>("stock"));



        // item loader
        Map<Integer, Map<String, Object>> items = loadItems();
        System.out.println(items);
        for(Integer i: items.keySet()){
            Map <String, Object> temp = items.get(i);
            // The output format is
            // ID: {Name: *name*; Stock: *price*; Ing: HashMap<Ing, Stock>}
            TreeItem<Prods> itemAdder = new TreeItem<>(new Prods(i, (String) temp.get("Name"), temp.get("Stock").toString() + " P"));

            Map <String, Object> ingTemp = (Map<String, Object>) temp.get("ingredients");
            if (!(ingTemp == null)) {
                for(String x: ingTemp.keySet()){
                    TreeItem<Prods> ingAdder = new TreeItem<>(new Prods(x, ingTemp.get(x) + " items"));
                    itemAdder.getChildren().add(ingAdder);
                }
            }

            hidden.getChildren().add(itemAdder);
        }

        // Variable for tree itself, hide root.
        mainView.setRoot(hidden);
        mainView.setShowRoot(false);
    }
}
