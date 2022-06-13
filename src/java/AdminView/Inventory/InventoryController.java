package AdminView.Inventory;

import Tools.Navigate;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;

import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class InventoryController implements Initializable {


    @FXML
    private Button back;

    @FXML
    private TreeTableView<Stock> mainTree;

    @FXML
    private TreeTableColumn<Stock, String> idCol;

    @FXML
    private TreeTableColumn<Stock, String> nameCol;

    @FXML
    private TreeTableColumn<Stock, String> stockCol;

    @FXML
    private TreeTableColumn<Stock, Object> conCol;

    @FXML
    private Button add;

    @FXML
    private Button del;

    @FXML
    private TextField titleTF;

    @FXML
    private TextField searchTF;

    // Database
    Firestore db = FirestoreClient.getFirestore();
    CollectionReference cr = db.collection("Admin Inventory");
    DocumentReference docRef = cr.document("_");
    ApiFuture<DocumentSnapshot> future = docRef.get();
    DocumentSnapshot document;

    TreeItem<Stock> hidden;

    private TreeItem<Stock> selectedItem;
    Navigate x = new Navigate();
    private int id = 0;
    private ArrayList<Integer> idTrack = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle){
        //

        try {
            initTableView();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        editableCells();
        add.setOnAction(e -> addNew());
        del.setOnAction(e -> delItem());
        del.setDisable(true);
        mainTree.setOnMouseClicked(e -> highlight());


        searchTF.textProperty().addListener(new ChangeListener<String>(){
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                TreeItem<Stock> filter = new TreeItem<>();
                // item loader
                Map<Integer,Map<String, Object>> items = null;
                try {
                    items = loadItems();
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
                for(Integer i: items.keySet()){
                    Map <String, Object> temp = items.get(i);
                    // The output format is
                    // ID: {Name: *name*; Stock: *stock*}
                    if(!idTrack.contains(i)){
                        idTrack.add(i);
                    }
                    String idName = (String) temp.get("Name");
                    if ((idName.toLowerCase().contains(searchTF.getText().toLowerCase()))){
                        TreeItem<Stock> itemAdder = new TreeItem<>(new Stock(i, idName, temp.get("Stock").toString()));
                        filter.getChildren().add(itemAdder);
                    }
                }
                mainTree.setRoot(filter);
                if(searchTF.getText().isBlank()){
                    try {
                        initTableView();
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
        });

        back.setOnAction(e -> x.switchScene(e,"Administrator","Administrator"));
    }

    // Del item based on selection ID
    public void delItem(){
        //        System.out.println(selectedItem.getValue().getProductName());
        Integer idTemp = selectedItem.getValue().getId();
        hidden.getChildren().remove(selectedItem);
        //        System.out.println(temp[0]);
        db.collection("Admin Inventory").document(String.valueOf(idTemp)).delete();
        del.setDisable(true);
        idTrack.remove(idTemp);
    }

    public void highlight(){
        selectedItem = mainTree.getSelectionModel().getSelectedItem();
        if(selectedItem==null){
            return;
        }
        del.setDisable(false);
    }

    // Add new item based on titleTF
    public void addNew(){
        if(!titleTF.getText().isBlank()){
            while(idTrack.contains(id)){

                id++;
            }
            idTrack.add(id);
            Stock newStock = new Stock(id,titleTF.getText(),"0");
            TreeItem<Stock> itemAdder = new TreeItem<>(newStock);
            hidden.getChildren().add(itemAdder);
            titleTF.clear();
            newStock.save(cr);
        }
        else{
            titleTF.setPromptText("Insert Text Here");
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
    public void editableCells(){
        // Making Editable
        nameCol.setEditable(true);
        nameCol.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn());
        nameCol.setOnEditCommit(new EventHandler<TreeTableColumn.CellEditEvent<Stock,String>>() {
            @Override
            public void handle(TreeTableColumn.CellEditEvent<Stock, String> event){
                TreeItem<Stock> currentEditing = mainTree.getTreeItem(event.getTreeTablePosition().getRow());
                currentEditing.getValue().setName(event.getNewValue());
                currentEditing.getValue().save(cr);
            }
        });

        stockCol.setEditable(true);
        stockCol.setCellFactory(TextFieldTreeTableCell.<Stock>forTreeTableColumn());
        stockCol.setOnEditCommit(new EventHandler<TreeTableColumn.CellEditEvent<Stock, String>>() {

            @Override
            public void handle(TreeTableColumn.CellEditEvent<Stock, String> event) {
                if(isNumeric(event.getNewValue())) {
                    TreeItem<Stock> currentEditing = mainTree.getTreeItem(event.getTreeTablePosition().getRow());
                    currentEditing.getValue().setStock(Double.valueOf(event.getNewValue()));
                    currentEditing.getValue().save(cr);
                }
                else{
                    System.out.println("ERROR: ");
                }
            }

        });
    }

    // Initializes first run table view
    public void initTableView() throws ExecutionException, InterruptedException {


        // Col settings.
        conCol.setSortable(false);
        conCol.setReorderable(false);
        idCol.setReorderable(false);
        nameCol.setReorderable(false);
        stockCol.setReorderable(false);

        // Variable for hidden root
        hidden = new TreeItem();

        // Setting Col variables
        idCol.setCellValueFactory(new TreeItemPropertyValueFactory<Stock, String>("id"));
        nameCol.setCellValueFactory(new TreeItemPropertyValueFactory<Stock, String>("name"));
        stockCol.setCellValueFactory(new TreeItemPropertyValueFactory<Stock, String>("stock"));



        // item loader
        Map<Integer,Map<String, Object>> items = loadItems();
        for(Integer i: items.keySet()){
            Map <String, Object> temp = items.get(i);
            // The output format is
            // ID: {Name: *name*; Stock: *stock*}
            if(!idTrack.contains(i)){
                idTrack.add(i);
            }
            TreeItem<Stock> itemAdder = new TreeItem<>(new Stock(i, (String) temp.get("Name"), temp.get("Stock").toString()));
            hidden.getChildren().add(itemAdder);
        }

        // Variable for tree itself, hide root.
        mainTree.setRoot(hidden);
        mainTree.setShowRoot(false);
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

            // Store sub values inside hashmap
            Map<String, Object> mapTemp = new HashMap<>();
            mapTemp.put("Name",name);
            mapTemp.put("Stock",stock);

            out.put(id,mapTemp);
        }
        return out;
    }

}
