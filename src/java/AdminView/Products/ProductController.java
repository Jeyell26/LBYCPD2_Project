package AdminView.Products;

import AdminView.Inventory.Stock;
import Tools.Navigate;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTreeTableCell;
import javafx.scene.control.cell.TextFieldTreeTableCell;
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
    private TreeTableColumn<Prods, String> prodCol;

    @FXML
    private TreeTableColumn<Prods, String> ingCol;

    @FXML
    private TreeTableColumn<Prods, String> stockCol;

    @FXML
    private TreeTableColumn<Prods, Integer> idCol;

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

    @FXML
    private ComboBox<String> ingBox;

    Navigate x = new Navigate();

    // Database
    Firestore db = FirestoreClient.getFirestore();
    CollectionReference cr = db.collection("Cashier Product");
    DocumentReference docRef = cr.document("_");
    ApiFuture<DocumentSnapshot> future = docRef.get();
    DocumentSnapshot document;

    TreeItem<Prods> hidden;
    private TreeItem<Prods> selectedItem;
    ArrayList<Integer> idTrack = new ArrayList<>();


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            initTableView();
            editableCells();
            ingBox.setItems(loadIng());
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        mainView.setOnMouseClicked(e -> highlight());
        addProd.setOnAction(e -> addProduct());
        addIng.setOnAction(e -> addIngredient());
        del.setOnAction(e -> delete());
        addIng.setDisable(true);
        del.setDisable(true);

        searchTF.textProperty().addListener(new ChangeListener<String>(){
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                TreeItem<Prods> filter = new TreeItem<>(new Prods());
                Map<Integer, Map<String, Object>> items = null;

                try {
                    items = loadItems();
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
                Boolean flag;
                for(Integer i: items.keySet()){
                    flag = false;
                    Map <String, Object> temp = items.get(i);
                    if(temp.get("Stock") == null){
                        continue;
                    }
                    // The output format is
                    // ID: {Name: *name*; Stock: *price*; Ing: HashMap<Ing, Stock>}
                    TreeItem<Prods> itemAdder = new TreeItem<>(new Prods(i, (String) temp.get("Name"), temp.get("Stock").toString() + " Php"));
                    if(((String) temp.get("Name")).toLowerCase().contains(searchTF.getText().toLowerCase())){
                        flag = true;
                    }

                    Map <String, Object> ingTemp = (Map<String, Object>) temp.get("ingredients");
                    if (!(ingTemp == null)) {
                        for(String x: ingTemp.keySet()){
                            TreeItem<Prods> ingAdder = new TreeItem<>(new Prods(x, ingTemp.get(x) + " items"));
                            itemAdder.getChildren().add(ingAdder);
                            if((x.toLowerCase().contains(searchTF.getText().toLowerCase()))){
                                flag = true;
                            }
                        }
                    }
                    itemAdder.setExpanded(true);
                    if(flag){
                        filter.getChildren().add(itemAdder);
                    }
                }
                mainView.setRoot(filter);
                hidden = filter;
            };
        });

        back.setOnAction(e -> x.switchScene(e,"Administrator","Administrator"));
    }


    public void delete(){
        // if it is a product
        if(selectedItem.getParent() == hidden){
            cr.document(String.valueOf(selectedItem.getValue().getId())).delete();
        }
        // if it is an ingredient
        else{
            Map<String, Double> ingStore = new HashMap<>();
            for(TreeItem<Prods> element:selectedItem.getParent().getChildren()){
                if(selectedItem.getValue()!=element.getValue()){
                    ingStore.put(element.getValue().getIng(), Double.parseDouble(((String) element.getValue().getStock()).split(" ")[0]));
                }

            }
            selectedItem.getValue().save(cr,selectedItem.getParent().getValue(),ingStore);
        }
        try {
            initTableView();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void addIngredient(){
        if(ingBox.getValue() != null && selectedItem.getParent() == hidden){
            TreeItem<Prods> newIng = new TreeItem<>(new Prods(ingBox.getValue(),"0 items"));
            selectedItem.getChildren().add(newIng);
            Map<String, Double> ingStore = new HashMap<>();
            for(TreeItem<Prods> element:selectedItem.getChildren()){
                ingStore.put(element.getValue().getIng(), Double.parseDouble(((String) element.getValue().getStock()).split(" ")[0]));
            }
            newIng.getValue().save(cr,selectedItem.getValue(),ingStore);
            addIng.setDisable(true);
            del.setDisable(true);
            try {
                initTableView();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }


    }

    public void highlight(){
        selectedItem = mainView.getSelectionModel().getSelectedItem();
        if(selectedItem==null){
            return;
        }
        addIng.setDisable(false);
        del.setDisable(false);
    }

    public void addProduct(){
        if(!prodTF.getText().isBlank()){
            Integer id = 0;
            while(idTrack.contains(id)){
                id++;
            }
            idTrack.add(id);
            Prods newProd = new Prods(id,prodTF.getText(),0 + " Php");
            TreeItem<Prods> itemAdder = new TreeItem<>(newProd);
            hidden.getChildren().add(itemAdder);
            prodTF.clear();
            newProd.save(cr, null);
        }
        else{
            prodTF.setPromptText("Insert Text Here");
        }
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
    public void editableCells() throws ExecutionException, InterruptedException {
        // Making Editable
        ObservableList<String> list = (ObservableList<String>) loadIng();

        ingCol.setEditable(true);
        ingCol.setCellFactory(ComboBoxTreeTableCell.forTreeTableColumn(list));
        ingCol.setOnEditCommit(new EventHandler<TreeTableColumn.CellEditEvent<Prods,String>>() {
            @Override
            public void handle(TreeTableColumn.CellEditEvent<Prods, String> event){
                TreeItem<Prods> currentEditing = mainView.getTreeItem(event.getTreeTablePosition().getRow());
                if(currentEditing.getParent() != hidden){
                    currentEditing.getValue().setIng(event.getNewValue());
                    Map<String, Double> ingStore = new HashMap<>();
                    for(TreeItem<Prods> element:currentEditing.getParent().getChildren()){
                        ingStore.put(element.getValue().getIng(), Double.parseDouble(((String) element.getValue().getStock()).split(" ")[0]));
                    }

                    currentEditing.getValue().save(cr,currentEditing.getParent().getValue(),ingStore);
                }
                try {
                    initTableView();
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        prodCol.setEditable(true);
        prodCol.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn());
        prodCol.setOnEditCommit(new EventHandler<TreeTableColumn.CellEditEvent<Prods,String>>() {
            @Override
            public void handle(TreeTableColumn.CellEditEvent<Prods, String> event){
                TreeItem<Prods> currentEditing = mainView.getTreeItem(event.getTreeTablePosition().getRow());
                if(currentEditing.getParent() == hidden){
                    currentEditing.getValue().setProd(event.getNewValue());
                    Map<String, Double> ingStore = new HashMap<>();
                    for(TreeItem<Prods> element:currentEditing.getChildren()){
                        ingStore.put(element.getValue().getIng(), Double.parseDouble(((String) element.getValue().getStock()).split(" ")[0]));
                    }
                    currentEditing.getValue().save(cr,ingStore);
                }
                else{
                    try {
                        initTableView();
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        stockCol.setEditable(true);
        stockCol.setCellFactory(TextFieldTreeTableCell.<Prods>forTreeTableColumn());
        stockCol.setOnEditCommit(new EventHandler<TreeTableColumn.CellEditEvent<Prods, String>>() {

            @Override
            public void handle(TreeTableColumn.CellEditEvent<Prods, String> event) {
                if(isNumeric(event.getNewValue())) {
                    TreeItem<Prods> currentEditing = mainView.getTreeItem(event.getTreeTablePosition().getRow());
                    if(currentEditing.getParent() == hidden){
                        currentEditing.getValue().setStock(String.valueOf(Double.valueOf(event.getNewValue()))+ " P");
                        Map<String, Double> ingStore = new HashMap<>();
                        for(TreeItem<Prods> element:currentEditing.getChildren()){
                            ingStore.put(element.getValue().getIng(), Double.parseDouble(((String) element.getValue().getStock()).split(" ")[0]));
                        }
                        currentEditing.getValue().save(cr,ingStore);
                    }
                    else{
                        currentEditing.getValue().setStock(String.valueOf(Double.valueOf(event.getNewValue()))+ " items");
                        Map<String, Double> ingStore = new HashMap<>();
                        for(TreeItem<Prods> element:currentEditing.getParent().getChildren()){
                            ingStore.put(element.getValue().getIng(), Double.parseDouble(((String) element.getValue().getStock()).split(" ")[0]));
                        }

                        currentEditing.getValue().save(cr,currentEditing.getParent().getValue(),ingStore);
                    }


                }
                try {
                    initTableView();
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }

        });
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

    private ObservableList<String> loadIng() throws ExecutionException, InterruptedException {

        Iterable<DocumentReference> trial = db.collection("Admin Inventory").listDocuments();
        ObservableList<String> out = FXCollections.observableArrayList();
        // The output format is
        // ID: {Name: *name*; Stock: *stock*}
        for (DocumentReference element : trial) {
            // Stores the current element in temp
            DocumentSnapshot temp = element.get().get();
            String name = temp.getString("Name");

            out.add(name);
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
        idCol.setCellValueFactory(new TreeItemPropertyValueFactory<Prods, Integer>("id"));
        prodCol.setCellValueFactory(new TreeItemPropertyValueFactory<Prods, String>("name"));
        ingCol.setCellValueFactory(new TreeItemPropertyValueFactory<Prods, String>("ing"));
        stockCol.setCellValueFactory(new TreeItemPropertyValueFactory<Prods, String>("stock"));



        // item loader
        Map<Integer, Map<String, Object>> items = loadItems();

        for(Integer i: items.keySet()){
            Map <String, Object> temp = items.get(i);
            if(temp.get("Stock") == null){
                continue;
            }
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
            itemAdder.setExpanded(true);
            hidden.getChildren().add(itemAdder);
        }

        // Variable for tree itself, hide root.
        hidden.setExpanded(true);
        mainView.setRoot(hidden);
        mainView.setShowRoot(false);
        searchTF.clear();

    }
}
