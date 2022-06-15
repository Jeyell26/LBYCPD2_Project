package AdminView.AdminTools;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import javafx.application.Platform;
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
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

public class AdminToolsController implements Initializable {

    @FXML
    private TreeTableView<Users> mainView;

    @FXML
    private TreeTableColumn<Users, String> userCol;

    @FXML
    private TreeTableColumn<Users, String> passCol;

    @FXML
    private TreeTableColumn<Users, String> typeCol;

    @FXML
    private Button back;

    @FXML
    private Button add;

    @FXML
    private Button del;

    @FXML
    private TextField userTF;

    @FXML
    private TextField searchTF;

    TreeItem<Users> hidden;

    // Database
    Firestore db = FirestoreClient.getFirestore();
    CollectionReference cr = db.collection("User Details");
    DocumentReference docRef = cr.document("_");
    ApiFuture<DocumentSnapshot> future = docRef.get();
    DocumentSnapshot document;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            initTableView();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        editableCells();
    }

    // Allow editing of cells
    public void editableCells(){
        mainView.setEditable(true);

        // Making Editable
        userCol.setEditable(true);
        userCol.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn());
        userCol.setOnEditCommit(new EventHandler<TreeTableColumn.CellEditEvent<Users,String>>() {
            @Override
            public void handle(TreeTableColumn.CellEditEvent<Users, String> event){
                cr.document(event.getOldValue()).delete();
                TreeItem<Users> currentEditing = mainView.getTreeItem(event.getTreeTablePosition().getRow());
                currentEditing.getValue().setUser(event.getNewValue());
                currentEditing.getValue().save(cr);
//                try {
//                    initTableView();
//                } catch (ExecutionException | InterruptedException e) {
//                    e.printStackTrace();
//                }
            }
        });

        passCol.setEditable(true);
        passCol.setCellFactory(TextFieldTreeTableCell.<Users>forTreeTableColumn());
        passCol.setOnEditCommit(new EventHandler<TreeTableColumn.CellEditEvent<Users, String>>() {

            @Override
            public void handle(TreeTableColumn.CellEditEvent<Users, String> event) {
                TreeItem<Users> currentEditing = mainView.getTreeItem(event.getTreeTablePosition().getRow());
                currentEditing.getValue().setPass(event.getNewValue());
                currentEditing.getValue().save(cr);
                try {
                    initTableView();
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }

        });

        passCol.setEditable(true);
        passCol.setCellFactory(TextFieldTreeTableCell.<Users>forTreeTableColumn());
        passCol.setOnEditCommit(new EventHandler<TreeTableColumn.CellEditEvent<Users, String>>() {

            @Override
            public void handle(TreeTableColumn.CellEditEvent<Users, String> event) {
                TreeItem<Users> currentEditing = mainView.getTreeItem(event.getTreeTablePosition().getRow());
                currentEditing.getValue().setPass(event.getNewValue());
                currentEditing.getValue().save(cr);
                try {
                    initTableView();
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }

        });

        ObservableList<String> list = FXCollections.observableArrayList();
        list.add("Cashier");
        list.add("Administrator");

        typeCol.setEditable(true);
        typeCol.setCellFactory(ComboBoxTreeTableCell.forTreeTableColumn(list));
        typeCol.setOnEditCommit(new EventHandler<TreeTableColumn.CellEditEvent<Users,String>>() {
            @Override
            public void handle(TreeTableColumn.CellEditEvent<Users, String> event){
                TreeItem<Users> currentEditing = mainView.getTreeItem(event.getTreeTablePosition().getRow());
                currentEditing.getValue().setType(event.getNewValue());
                Map<String, Double> ingStore = new HashMap<>();
                currentEditing.getValue().save(cr);
                try {
                    initTableView();
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // Initializes first run table view
    public void initTableView() throws ExecutionException, InterruptedException {
        // Col settings.

        userCol.setReorderable(false);
        passCol.setReorderable(false);
        typeCol.setReorderable(false);

        // Variable for hidden root
        hidden = new TreeItem();

        // Setting Col variables
        userCol.setCellValueFactory(new TreeItemPropertyValueFactory<Users, String>("user"));
        passCol.setCellValueFactory(new TreeItemPropertyValueFactory<Users, String>("pass"));
        typeCol.setCellValueFactory(new TreeItemPropertyValueFactory<Users, String>("type"));



        // item loader
        Map<String, Map<String, String>> items = loadItems();
        for(String  i: items.keySet()){
            Map <String, String> temp = items.get(i);
            // The output format is
            // User: {Pass: *pass*; Type: *type*}
            TreeItem<Users> itemAdder = new TreeItem<>(new Users(i,temp.get("Pass"),temp.get("Type")));
            hidden.getChildren().add(itemAdder);
        }

        // Variable for tree itself, hide root.
        mainView.setRoot(hidden);
        mainView.setShowRoot(false);
    }

    // Loads the items from database and returns a Map within a Map
    private Map<String,Map<String, String>> loadItems() throws ExecutionException, InterruptedException {
        Iterable<DocumentReference> trial = cr.listDocuments();
        Map<String,Map<String, String>> out = new HashMap<>();
        // The output format is
        // Username: {Pass: *password*; Type: *type*}
        for (DocumentReference element : trial) {
            // Stores the current element in temp
            DocumentSnapshot temp = element.get().get();
            String user = element.getId();
            String pass = temp.getString("Pass");
            String type = temp.getString("Type");

            // Store sub values inside hashmap
            Map<String, String> mapTemp = new HashMap<>();
            mapTemp.put("Pass",pass);
            mapTemp.put("Type",type);

            out.put(user,mapTemp);
        }
        return out;
    }
}
