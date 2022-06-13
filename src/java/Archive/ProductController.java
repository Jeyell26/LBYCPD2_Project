package Archive;

import AdminView.Products.treeNode;
import Tools.Navigate;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

public class ProductController implements Initializable {

    @FXML
    private TextField textPro;

    @FXML
    private Button addPro;

    @FXML
    private Button addIng;

    @FXML
    private ChoiceBox<String> ingChoice;

    @FXML
    private TextField textSto;

    @FXML
    private Button del;

    @FXML
    private Button back;

    @FXML
    private TreeTableView<treeNode> treeView;

    @FXML
    private TreeTableColumn<treeNode, String> productView;

    @FXML
    private TreeTableColumn<treeNode, Object> stockView;
    private treeNode hiddenRoot = new treeNode("hidden");
    private TreeItem<treeNode> hidden = new TreeItem<>(hiddenRoot);

    private ArrayList<String> products = new ArrayList<>();
    private Integer id;

    private TreeItem<treeNode> selectedItem;
    Navigate x = new Navigate();
    // Database
    Firestore db = FirestoreClient.getFirestore();
    CollectionReference cr = db.collection("Cashier Product");
    DocumentReference docRef = cr.document("_");
    ApiFuture<DocumentSnapshot> future = docRef.get();
    DocumentSnapshot document;

    // Run for loading
    {
        try {
            document = future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeTableView();
        initializeComboBox();
        addPro.setOnAction(e -> addNewPro());
        del.setOnAction(e -> delPro());
        addIng.setOnAction(e -> addNewIng());
        addIng.setDisable(true);
        del.setDisable(true);
        treeView.setOnMouseClicked(e -> highlight());
        back.setOnAction(e -> x.switchScene(e,"Administrator","Administrator Page"));
    }

    public void highlight(){
        selectedItem = treeView.getSelectionModel().getSelectedItem();
        if(selectedItem==null){
            return;
        }
        del.setDisable(false);
        addIng.setDisable(false);

    }

    public void addNewIng(){
        String[] temp = selectedItem.getValue().getProductName().split("\\.");
        HashMap<String, Double> ing;
        try {
            ing = (HashMap) db.collection("Cashier Product").document(temp[0]).get().get().get("Ingredients");
            if(ing==null){
                ing = new HashMap<>();
            }
            if(ing.containsKey(ingChoice.getValue())){
                selectedItem.getChildren().removeAll(selectedItem.getChildren());
            }
            ing.put(ingChoice.getValue(),Double.parseDouble(textSto.getText()));
            db.collection("Cashier Product").document(temp[0]).update("Ingredients",ing);
            treeNode item;
            TreeItem treeItem;
            for(String ingI: ing.keySet()){
                item = new treeNode(ingI,ing.get(ingI));
                treeItem = new TreeItem<treeNode>(item);
                selectedItem.getChildren().add(treeItem);
            }
//            treeNode treeTemp = new treeNode(ingChoice.getValue(),Double.parseDouble(textSto.getText()));
//            selectedItem.getChildren().add(new TreeItem(treeTemp));
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }



    }

    public void delPro(){
//        System.out.println(selectedItem.getValue().getProductName());
        String[] temp = selectedItem.getValue().getProductName().split("\\.");
        hidden.getChildren().remove(selectedItem);
//        System.out.println(temp[0]);
        db.collection("Cashier Product").document(temp[0]).delete();
        del.setDisable(true);
        addIng.setDisable(true);
    }

    public void addNewPro(){
        if(textPro.getText().isBlank()){
            System.out.println("here");
            return;
        }
        if(products.contains(textPro.getText())){
            return;
        }
        id++;
//        System.out.println(id + "  " + textPro.getText());
        HashMap<String, String> name = new HashMap<>();
        name.put("Name", textPro.getText());


        db.collection("Cashier Product").document(id.toString()).set(name);

        products.add(name.get("Name"));
        treeNode root = new treeNode(id + ". " + name.get("Name"));
        TreeItem<treeNode> treeRoot = new TreeItem<treeNode>(root);
        hidden.getChildren().add(treeRoot);

        textPro.clear();
    }

    public void initializeTableView(){
        Iterable<DocumentReference> trial = cr.listDocuments();
        String name;
        HashMap<String, Double> ing;
        treeNode root;
        treeNode item;
        TreeItem<treeNode> treeRoot, treeItem;
        products.clear();
        for (DocumentReference element : trial) {
            try {
                name = element.get().get().getString("Name");
                id = Integer.valueOf(element.getId());
                ing = (HashMap) element.get().get().get("Ingredients");
//                System.out.println(id + "  " + name + "  " + ing);
                products.add(name);
                root = new treeNode(id + ". " + name);
                treeRoot = new TreeItem<treeNode>(root);
                if(!(ing == null)){
                    for(String ingI: ing.keySet()){
//                    System.out.println(ingI + "  " + ing.get(ingI));
                        item = new treeNode(ingI,ing.get(ingI));
                        treeItem = new TreeItem<treeNode>(item);
                        treeRoot.getChildren().add(treeItem);
                    }
                }
                hidden.getChildren().add(treeRoot);

            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        treeView.setRoot(hidden);
        treeView.setShowRoot(false);

        //Data
//        treeNode root1 = new treeNode("Bacon Pandesal");
//        treeNode item1 = new treeNode("Bacon", 2.0);
//        treeNode item2 = new treeNode("Pandesal", 1.0);
//
//        TreeItem<treeNode> itemRoot = new TreeItem<treeNode>(root1);
//        TreeItem<treeNode> itemBac = new TreeItem<treeNode>(item1);
//        TreeItem<treeNode> itemPan = new TreeItem<treeNode>(item2);
//
//        itemRoot.getChildren().addAll(itemBac, itemPan);
//        treeView.setRoot(itemRoot);

        productView.setCellValueFactory(new TreeItemPropertyValueFactory<treeNode, String>("productName"));
        stockView.setCellValueFactory(new TreeItemPropertyValueFactory<treeNode, Object>("stock"));
        treeView.refresh();
    }

    public void initializeComboBox(){
        Iterable<DocumentReference> trial = db.collection("Admin Inventory").listDocuments();
        ArrayList<String> choices = new ArrayList<>();
        for (DocumentReference element : trial) {
            try {
                choices.add(element.get().get().getString("Name"));
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        ingChoice.setItems(FXCollections.observableArrayList(choices));
    }
}
