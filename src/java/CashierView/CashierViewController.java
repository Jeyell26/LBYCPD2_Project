package CashierView;

import Tools.Navigate;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.database.OnDisconnect;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.text.Text;

import java.net.URL;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class CashierViewController implements Initializable {

    @FXML
    private TreeTableView<Order> mainView;

    @FXML
    private TreeTableColumn<Order, String> orderCol;

    @FXML
    private TreeTableColumn<Order, String> quanCol;

    @FXML
    private TreeTableColumn<Order, String> priceCol;

    @FXML
    private TreeTableColumn<Order, String> totCol;

    @FXML
    private ComboBox<String> addCB;

    @FXML
    private Button add;

    @FXML
    private Button del;

    @FXML
    private Text priceText;

    @FXML
    private Button confirm;

    @FXML
    private Button back;


    // Database
    Firestore db = FirestoreClient.getFirestore();
    CollectionReference cr = db.collection("Cashier Product");
    DocumentReference docRef = cr.document("_");
    ApiFuture<DocumentSnapshot> future = docRef.get();
    DocumentSnapshot document;

    Map<String, Map<String,Object>> ingMap;
    Map<String, Map<String,Object>> currCar = new HashMap<>();
    TreeItem<Order> hidden;
    TreeItem<Order> selectedItem;
    Navigate x = new Navigate();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        addCB.setPromptText("Select product here");
        try {
            ObservableList<String> out = FXCollections.observableArrayList();
            ingMap = loadProd();
            for(String elem:ingMap.keySet()){
                out.add(elem);
            }
            addCB.setItems(out);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        add.setOnAction(e -> addProd());
        del.setOnAction(e -> delProd());
        del.setDisable(true);
        mainView.setOnMouseClicked(e -> highlight());
        // editable
        editableCells();
        
        confirm.setOnAction(e -> showConfirmation());
        back.setOnAction(e -> x.switchScene(e,"login","Login Page"));

    }

    public void showConfirmation(){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Confirm Orders?");
        alert.setContentText("Transact current orders?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            try {
                transact();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        else {
            alert.close();
        }
    }

    // Remove ingredients based on order (database)
    // Save on database the total price and date (date: totPrice: ***) (database)
    public void transact() throws ExecutionException, InterruptedException {
        // if database on that date exists, update total price, update ingredients deducted
        // if not, create a new one. put the current total price, update ingredients deducted
//        Date: totPrice: **** : IngUsed: : ****: ProdOrdered:*****
        LocalDate setDate = LocalDate.now();
        DocumentSnapshot currColl = null;
        try {
            currColl = db.collection("Sale Tracker").document(String.valueOf(setDate)).get().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        // If first order of the day
        if (currColl.get("Total") == null){
            Map<String,Map<String,Object>> save = new HashMap<>();
            Map<String,Object> temp = new HashMap<>();
            temp.put("Total", Double.parseDouble(priceText.getText().split(" ")[0]));
            Map<String, Double> ingUsed = new HashMap<>();
            Map<String, Double> prodOrdered = new HashMap<>();

            // Store product
            for(String prod : currCar.keySet()){
                if(prodOrdered.containsKey(prod)){
                    prodOrdered.put(prod,prodOrdered.get(prod) + Double.parseDouble(String.valueOf(currCar.get(prod).get("Quantity"))));
                }
                else{
                    prodOrdered.put(prod,Double.parseDouble(String.valueOf(currCar.get(prod).get("Quantity"))));
                }
            }

            // Store current totalIng in ingUsed
            for(String prod: currCar.keySet()){
                Map<String, Double> ingTemp = (Map<String, Double>) currCar.get(prod).get("Ingredients");
                Integer prodQuan = Integer.parseInt(String.valueOf(currCar.get(prod).get("Quantity")));
                if(currCar.get(prod).get("Ingredients") != null){
                    for(String ing: ingTemp.keySet()){
                        if(ingUsed.containsKey(ing)){
                            ingUsed.put(ing,ingUsed.get(ing) + (ingTemp.get(ing)*prodQuan));
                        }
                        else{
                            ingUsed.put(ing,ingTemp.get(ing)*prodQuan);
                        }
                    }
                }
            }
            temp.put("TotalIng", ingUsed);
            temp.put("TotalOrdered", prodOrdered);

            // Saves to database
            db.collection("Sale Tracker").document(String.valueOf(setDate)).set(temp);
        }
        // If date already exists
        else{
            Map<String,Map<String,Object>> save = new HashMap<>();
            Map<String,Object> temp = new HashMap<>();
            temp.put("Total", Double.parseDouble(priceText.getText().split(" ")[0]) + currColl.getDouble("Total"));
            Map<String, Double> ingUsed = (Map<String, Double>) currColl.get("TotalIng");
            Map<String, Double> prodOrdered = (Map<String, Double>) currColl.get("TotalOrdered");

            // Store product
            for(String prod : currCar.keySet()){
                if(prodOrdered.containsKey(prod)){
                    prodOrdered.put(prod,prodOrdered.get(prod) + Double.parseDouble(String.valueOf(currCar.get(prod).get("Quantity"))));
                }
                else{
                    prodOrdered.put(prod,Double.parseDouble(String.valueOf(currCar.get(prod).get("Quantity"))));
                }
            }

            // Store current totalIng in ingUsed
            for(String prod: currCar.keySet()){
                Map<String, Double> ingTemp = (Map<String, Double>) currCar.get(prod).get("Ingredients");
                Integer prodQuan = Integer.parseInt(String.valueOf(currCar.get(prod).get("Quantity")));
                if(currCar.get(prod).get("Ingredients") != null){
                    for(String ing: ingTemp.keySet()){
                        if(ingUsed.containsKey(ing)){
                            ingUsed.put(ing,ingUsed.get(ing) + (ingTemp.get(ing)*prodQuan));
                        }
                        else{
                            ingUsed.put(ing,ingTemp.get(ing)*prodQuan);
                        }
                    }
                }
            }
            temp.put("TotalIng", ingUsed);
            temp.put("TotalOrdered", prodOrdered);

            // Saves to database
            db.collection("Sale Tracker").document(String.valueOf(setDate)).set(temp);
        }

        // Deduct Current ingredients from inventory
        // Store current totalIng in ingUsed
        Map<String, Double> deductibles = new HashMap<>();
        for(String prod: currCar.keySet()){
            Map<String, Double> ingTemp = (Map<String, Double>) currCar.get(prod).get("Ingredients");
            Integer prodQuan = Integer.parseInt(String.valueOf(currCar.get(prod).get("Quantity")));
            if(currCar.get(prod).get("Ingredients") != null){
                for(String ing: ingTemp.keySet()){
                    if(deductibles.containsKey(ing)){
                        deductibles.put(ing,deductibles.get(ing) + (ingTemp.get(ing)*prodQuan));
                    }
                    else{
                        deductibles.put(ing,ingTemp.get(ing)*prodQuan);
                    }
                }
            }
        }



        // Load all items
        Map<Integer, Map<String, Object>> allIng = loadItems();
//        System.out.println(allIng);
//        System.out.println(deductibles);


        // Filter only those affected
        // Deduct immediately
        for(Integer id: allIng.keySet()){
            List<String> listIng = new ArrayList<>();
            listIng.addAll(allIng.get(id).keySet());
            String currIng = listIng.get(0);
            // If current id is in deductibles
            if(deductibles.containsKey(currIng)){
                Map<String, Object> save = new HashMap<>();
                Double currQuan = (Double) allIng.get(id).get(currIng);
                save.put("Name", currIng);
                save.put("Stock", currQuan - deductibles.get(currIng));
                db.collection("Admin Inventory").document(String.valueOf(id)).set(save);
            }
        }

        // Clear table, update price
        hidden = new TreeItem();
        currCar = new HashMap<>();

        mainView.setRoot(hidden);
        updatePrice();

    }

    public void updatePrice(){
        Double tot = 0.0;
        if(hidden != null){
            for(TreeItem<Order> elem: hidden.getChildren()){
                tot += elem.getValue().getTotal();
            }
        }
        priceText.setText(String.valueOf(tot) + " P");


    }

    public void delProd(){
        del.setDisable(true);
        hidden.getChildren().remove(selectedItem);
        currCar.remove(selectedItem.getValue().getName());
        updatePrice();
    }

    public void highlight(){
        selectedItem = mainView.getSelectionModel().getSelectedItem();
        if(selectedItem==null){
            return;
        }
        del.setDisable(false);
    }

    public boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    // Allow editing of cells
    public void editableCells(){
        mainView.setEditable(true);

        quanCol.setEditable(true);
        quanCol.setCellFactory(TextFieldTreeTableCell.<Order>forTreeTableColumn());
        quanCol.setOnEditCommit(new EventHandler<TreeTableColumn.CellEditEvent<Order, String>>() {

            @Override
            public void handle(TreeTableColumn.CellEditEvent<Order, String> event) {
                if(isNumeric(event.getNewValue())) {
                    TreeItem<Order> currentEditing = mainView.getTreeItem(event.getTreeTablePosition().getRow());
                    currentEditing.getValue().setQuan(Integer.valueOf(event.getNewValue()));
                    currCar.get(currentEditing.getValue().getName()).put("Quantity",Integer.valueOf(event.getNewValue()));
                }
                processTable();
                updatePrice();

            }

        });
    }

    public void addProd(){
        if(addCB.getValue() != null){
            Map<String, Object> temp = new HashMap<>();
            temp.put("Price", ingMap.get(addCB.getValue()).get("Stock"));
            Map<String,Object> ing = (Map<String, Object>) ingMap.get(addCB.getValue()).get("ingredients");
            Map<String, Object> ingTemp = new HashMap<>();

            if(!(ing == null)){
                for(String x: ing.keySet()){
                    ingTemp.put(x,ing.get(x));
                }
            }
            temp.put("Ingredients", ingTemp);
            temp.put("Quantity", 1);
            currCar.put(addCB.getValue(),temp);
            processTable();
            updatePrice();
        }
    }

    public void processTable(){

        // Col settings.
        orderCol.setReorderable(false);
        quanCol.setReorderable(false);
        priceCol.setReorderable(false);
        totCol.setReorderable(false);

        // Add items


        // Variable for hidden root
        hidden = new TreeItem();
        for(String elem: currCar.keySet()){
            String name = elem;
            Map<String,Double> ingTemp = (Map<String, Double>) currCar.get(elem).get("Ingredients");
            // String name, String price, Integer quan, Map<String, Double> ing

            String price = currCar.get(elem).get("Price").toString() + " P";
            Integer quan = (Integer) currCar.get(elem).get("Quantity");

            TreeItem<Order> newEntry = new TreeItem<>(new Order(name,price,quan,ingTemp));
            hidden.getChildren().add(newEntry);
        }
        mainView.setRoot(hidden);
        mainView.setShowRoot(false);


        // Setting Col variables
        orderCol.setCellValueFactory(new TreeItemPropertyValueFactory<Order, String>("name"));
        quanCol.setCellValueFactory(new TreeItemPropertyValueFactory<Order, String>("quan"));
        priceCol.setCellValueFactory(new TreeItemPropertyValueFactory<Order, String>("price"));
        totCol.setCellValueFactory(new TreeItemPropertyValueFactory<Order, String>("total"));


    }

    public Map<String, Map<String,Object>> loadProd() throws ExecutionException, InterruptedException {
        Iterable<DocumentReference> trial = cr.listDocuments();
        Map<String,Map<String, Object>> out = new HashMap<>();
        // The output format is
        // Name: {Stock: *stock*; Ingredients: Map<Name,Req>}
        for (DocumentReference element : trial) {
            // Stores the current element in temp
            DocumentSnapshot temp = element.get().get();
            String name = temp.getString("Name");
            Double stock = temp.getDouble("Stock");
            Map<String,Object> ing = (Map<String, Object>) temp.get("Ingredients");

            // Store sub values inside hashmap
            Map<String, Object> mapTemp = new HashMap<>();
            mapTemp.put("Stock",stock);

            Map<String, Object> ingTemp = new HashMap<>();

            if(!(ing == null)){
                for(String x: ing.keySet()){
                    ingTemp.put(x,ing.get(x));
                }
            }


            mapTemp.put("ingredients", ingTemp);

            out.put(name,mapTemp);
        }
        return out;
    }

    // Loads the items from database and returns a Map within a Map
    private Map<Integer, Map<String, Object>> loadItems() throws ExecutionException, InterruptedException {
        Iterable<DocumentReference> trial = db.collection("Admin Inventory").listDocuments();
        Map<Integer, Map<String,Object>> out = new HashMap<>();
        // The output format is
        // ID: {Name: *name*; Stock: *stock*}
        for (DocumentReference element : trial) {
            Map<String, Object> outTemp = new HashMap<>();
            // Stores the current element in temp
            DocumentSnapshot temp = element.get().get();
            String name = temp.getString("Name");
            Double stock = temp.getDouble("Stock");

            outTemp.put(name,stock);
            out.put(Integer.valueOf(temp.getId()),outTemp);
        }
        return out;
    }

}
