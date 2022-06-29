package AdminView.GraphView;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

public class GraphController implements Initializable {

    @FXML
    private LineChart lineChart;

    @FXML
    private ComboBox<String> optionBox;

    @FXML
    private Button back;

    @FXML
    private CategoryAxis cAxis;

    @FXML
    private NumberAxis nAxis;

    // Database
    Firestore db = FirestoreClient.getFirestore();
    CollectionReference cr = db.collection("Cashier Product");
    DocumentReference docRef = cr.document("_");
    ApiFuture<DocumentSnapshot> future = docRef.get();
    DocumentSnapshot document;

    Map<String, Map<String,Object>> loaded = loadSales();

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
        ObservableList<String> items = FXCollections.observableArrayList();
        items.add("Sales");
        items.add("Used ingredients");
        items.add("Ordered Products");
        optionBox.setItems(items);
        optionBox.setOnAction(e -> option());
        loadSales();
    }

    public void option(){
        if(optionBox.getValue() == "Sales"){
            sales();
        }
        else if(optionBox.getValue() == "Used ingredients"){
            sales();
        }
        else{
            lineChart.getData().clear();
        }
    }

    public void sales(){
        lineChart.setAnimated(false);
        XYChart.Series series = new XYChart.Series();
        series.setName("Date");
        cAxis.setLabel("Date");
        nAxis.setLabel("Sales in pesos");

        for(String date: loaded.keySet()){
//            System.out.println(date + loaded.get(date).get("Total"));
            series.getData().add(new XYChart.Data(date, loaded.get(date).get("Total")));
        }

        lineChart.getData().add(series);
    }

    public Map<String, Map<String,Object>> loadSales(){
        Iterable<DocumentReference> trial = db.collection("Sale Tracker").listDocuments();
        Map<String, Map<String,Object>> out = new HashMap<>();
        // The output format is
        // Date: {Total: *total*; TotalIng: *hashmap of total ing*; TotalOrdered: *hashmap of ordered products that day}
        for (DocumentReference element : trial) {
            // Stores the current element in temp
            DocumentSnapshot temp = null;
            try {
                temp = element.get().get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

            Map<String, Object> totalIng = new HashMap<>();
            Map<String, Object> tempIng = (Map<String, Object>) temp.get("TotalIng");

            if(tempIng != null){
                for(String ing: tempIng.keySet()){
                    totalIng.put(ing,tempIng.get(ing));
                }
            }

            Map<String, Object> totalProd = new HashMap<>();
            Map<String, Object> tempProd = (Map<String, Object>) temp.get("TotalOrdered");

            if(tempProd != null){
                for(String prod: tempProd.keySet()){
                    totalProd.put(prod,tempProd.get(prod));
                }
            }
            Map<String, Object> save = new HashMap<>();
            save.put("Total", temp.getDouble("Total"));
            save.put("TotalIng", totalIng);
            save.put("TotalOrdered", totalProd);

            out.put(temp.getId(),save);
        }
//        System.out.println(out);
        return out;
    }


}
