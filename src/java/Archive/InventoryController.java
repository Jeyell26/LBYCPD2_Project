package Archive;

import Tools.Navigate;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Cell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.text.Font;

import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class InventoryController implements Initializable {

    @FXML
    private Button add;

    @FXML
    private TextField inName;

    @FXML
    private TextField inStock;

    @FXML
    private Button set;

    @FXML
    private Button back;

    @FXML
    private Button del;

    @FXML
    private ListView<String> inList;
    Integer id;
    HashMap<Integer, HashMap<String,Object>> inHash = new HashMap<>();
    String highlighted = null;
    Navigate x = new Navigate();

    // Database
    Firestore db = FirestoreClient.getFirestore();
    CollectionReference cr = db.collection("Admin Inventory");
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
    public void initialize(URL url, ResourceBundle resourceBundle){
        Iterable<DocumentReference> trial = cr.listDocuments();
        String name;
        Double stock;
        Platform.runLater(this::refresh);

        inList.setMaxWidth(1000);
        for (DocumentReference element : trial) {
            try {
                name = element.get().get().getString("Name");
                stock = Objects.requireNonNull(element.get().get().getDouble("Stock"));
                id = Integer.valueOf(element.getId());
                HashMap<String,Object> temp = new HashMap<>();
                temp.put("Name", name);
                temp.put("Stock", stock);
                inHash.put(id,temp);
                inList.getItems().add(pad(id,16) + pad(name,47) + stock);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        add.setOnAction(e -> addInv());
        set.setOnAction(e -> setInv());
        del.setOnAction(e -> delInv());
        inList.setOnMouseClicked(e -> highlight());
        set.setDisable(true);
        del.setDisable(true);
        back.setOnAction(e -> x.switchScene(e,"Administrator","Administrator Page"));
    }

    public void highlight(){
        if(inList.getSelectionModel().getSelectedItem() == null){
            return;
        }
        set.setDisable(false);
        del.setDisable(false);
        highlighted = inList.getSelectionModel().getSelectedItem();
        String[] temp = highlighted.split(" ");
        inStock.setText(String.valueOf(inHash.get(Integer.parseInt(temp[0])).get("Stock")));
    }

    public void delInv(){
        String[] temp = highlighted.split(" ");
        inHash.remove(Integer.parseInt(temp[0]));
        write();
        db.collection("Admin Inventory").document(temp[0]).delete();
    }

    public void setInv(){
        if(inStock.getText().isBlank()){
            return;
        }
        String[] temp = highlighted.split(" ");
        inHash.get(Integer.parseInt(temp[0])).replace("Stock", inStock.getText());
        write();
        db.collection("Admin Inventory").document(temp[0]).update("Stock",Double.parseDouble(inStock.getText()));
    }

    public void write(){
        List<String> selectedItemsCopy = new ArrayList<>(inList.getSelectionModel().getSelectedItems());
        inList.getItems().removeAll(selectedItemsCopy);
        inList.getItems().clear();
        for (Integer i: inHash.keySet()){
            inList.getItems().add(pad(i,16) + pad(inHash.get(i).get("Name"),47) + inHash.get(i).get("Stock"));
        }
        refresh();
    }

    public void addInv(){
        if(inName.getText().isBlank()){
            return;
        }
        else{
            id++;
            HashMap<String,Object> temp = new HashMap<>();
            temp.put("Name", inName.getText());
            temp.put("Stock", 0);
            inHash.put(id,temp);
            inList.getItems().add(pad(id,16) + pad(inName.getText(),47) + 0);
            saveToFirebase(id,temp);
        }
        inName.clear();
        refresh();
    }

    private void saveToFirebase(Integer data, HashMap<String,Object> temp){
        db.collection("Admin Inventory").document(data.toString()).set(temp);
    }


    public String pad(Object e, int len){
        String temp = e.toString();
        StringBuilder out = new StringBuilder();
        out.append(temp);
        for (int i = 0; i < len-temp.length(); i++) {
            out.append(" ");
        }
        return out.toString();
    }

    public void refresh(){
        if(!inList.getItems().isEmpty())
        {
            VirtualFlow ch= (VirtualFlow) inList.getChildrenUnmodifiable().get(0);
            Font anyfont =new Font("Consolas",12);
            for (int i = 0; i <= ch.getCellCount(); i++)
            {
                Cell cell = ch.getCell(i);
                cell.setFont(anyfont);
            }
        }
    }
}
