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
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class GraphController implements Initializable {

    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu-MM-dd");
    String currTime = LocalDate.now().format(dtf);

    @FXML
    private DatePicker startDate;

    @FXML
    private DatePicker endDate;


    @FXML
    private TreeTableView<Dates> mainView;

    @FXML
    private TreeTableColumn<Dates, String> dateCol;

    @FXML
    private TreeTableColumn<Dates, String> ingCol;

    @FXML
    private TreeTableColumn<Dates, String> ingTCol;

    @FXML
    private TreeTableColumn<Dates, String> prodCol;

    @FXML
    private TreeTableColumn<Dates, String> prodTCol;

    @FXML
    private TreeTableColumn<Dates, String> salesCol;

    @FXML
    private Button show;

    @FXML
    private Button back;

    @FXML
    private ComboBox<String> optionBox;

    TreeItem<Dates> hidden;

    Map<String, Map<String,Object>> sales;
    ArrayList<Date> dates = new ArrayList<>();
    ArrayList<Double> totSales = new ArrayList<>();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");




    // Database
    Firestore db = FirestoreClient.getFirestore();
    CollectionReference cr = db.collection("Admin Inventory");
    DocumentReference docRef = cr.document("_");
    ApiFuture<DocumentSnapshot> future = docRef.get();
    DocumentSnapshot document;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ObservableList<String> items = FXCollections.observableArrayList();
        items.add("Sales");
        items.add("Used ingredients");
        items.add("Ordered Products");
        optionBox.setItems(items);
        optionBox.setOnAction(e -> show.setDisable(false));
        initTableView();
        genDates();
        show.setOnAction(e -> genGraph());

        startDate.setValue(LocalDate.parse(sdf.format(dates.get(0))));
        endDate.setValue(LocalDate.parse(currTime));
    }

    public void genDates() {
        for(String curr: sales.keySet()){
            Date temp = null;
            try {
                temp = sdf.parse(curr);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            dates.add(temp);
            totSales.add((Double) sales.get(curr).get("Total"));
        }
        Collections.sort(dates);
        Collections.sort(totSales);
//        System.out.println(dates);
    }

    public void genDates(ArrayList<String> givenDates) {
        dates = new ArrayList<>();
        totSales = new ArrayList<>();
        for(String curr: givenDates){
            if(sales.containsKey(curr)){
                Date temp = null;
                try {
                    temp = sdf.parse(curr);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                dates.add(temp);
                totSales.add((Double) sales.get(curr).get("Total"));
            }
        }
        Collections.sort(dates);
        Collections.sort(totSales);
//        System.out.println(dates);
    }

    public void sales(){
        LocalDate start = startDate.getValue();
        LocalDate end = endDate.getValue();
        ArrayList<String> totalDates = new ArrayList<>();

        while (!start.isAfter(end)) {
            totalDates.add(String.valueOf(start));
            start = start.plusDays(1);
        }

        genDates(totalDates);

        CategoryAxis xAxis = new CategoryAxis(FXCollections.observableArrayList(totalDates));
        NumberAxis yAxis = new NumberAxis(0,totSales.get(totSales.size()-1)+300,totSales.size());
        XYChart.Series series = new XYChart.Series();
        LineChart lineChart = new LineChart(xAxis, yAxis);

        //Each item in withoutDuplicates is data point on graph. Array withDuplicates is for tracking repetitions of withoutDuplicates.
        for(String curr: totalDates){
            if (sales.containsKey(curr)){
                series.getData().add(new XYChart.Data(curr, sales.get(curr).get("Total")));
            }
        }

        //Initialize Scene + Stage
        xAxis.setLabel("Date");
        yAxis.setLabel("Sales");
        series.setName("Sales in given Date");
        lineChart.getData().add(series);
        Group root = new Group(lineChart);
        Scene scene = new Scene(root);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Sales Graph");
        stage.show();
    }

    public void ingredients(){
        LocalDate start = startDate.getValue();
        LocalDate end = endDate.getValue();
        ArrayList<String> totalDates = new ArrayList<>();


        while (!start.isAfter(end)) {
            totalDates.add(String.valueOf(start));
            start = start.plusDays(1);
        }

        Map<String, Object> tempIng = new HashMap<>();
        try {
            tempIng = loadItems();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        ArrayList<Double> ingTracker = new ArrayList();
        for(String ing: tempIng.keySet()){
            Boolean isPart = false;
            Double totIng = 0.0;

            for(String curr: totalDates){
                if (sales.containsKey(curr)){
                    Map<String, Object> dbIng = (Map<String, Object>) sales.get(curr).get("TotalIng");
                    if(dbIng.containsKey(ing)){
                        isPart = true;
                        if(totIng<Double.parseDouble(String.valueOf(dbIng.get(ing)))){
                            totIng = Double.parseDouble(String.valueOf(dbIng.get(ing)));
                        }
                    }
                }
            }

            if(isPart){
                // save all ing that have been used in that range
                ingTracker.add(totIng);
            }
        }
        Collections.sort(ingTracker);

        if(ingTracker.size() <= 0){
            return;
        }

        CategoryAxis xAxis = new CategoryAxis(FXCollections.observableArrayList(totalDates));
        NumberAxis yAxis = new NumberAxis(0,ingTracker.get(ingTracker.size()-1)+5,ingTracker.size());
        BarChart lineChart = new BarChart(xAxis, yAxis);


        for(String ing: tempIng.keySet()){
            XYChart.Series series = new XYChart.Series();
            Boolean isPart = false;
            series.setName(ing);

            for(String curr: totalDates){
                if (sales.containsKey(curr)){
                    Map<String, Object> dbIng = (Map<String, Object>) sales.get(curr).get("TotalIng");
                    if(dbIng.containsKey(ing)){
                        series.getData().add(new XYChart.Data(curr, dbIng.get(ing)));
                        isPart = true;
                    }
                }
            }

            if(isPart){
                lineChart.getData().add(series);
            }
        }



        //Initialize Scene + Stage
        xAxis.setLabel("Date");
        yAxis.setLabel("Ingredients Used");

        Group root = new Group(lineChart);
        Scene scene = new Scene(root);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Ingredients Graph");
        stage.show();
    }

    public void products(){
        LocalDate start = startDate.getValue();
        LocalDate end = endDate.getValue();
        ArrayList<String> totalDates = new ArrayList<>();


        while (!start.isAfter(end)) {
            totalDates.add(String.valueOf(start));
            start = start.plusDays(1);
        }

        Map<String, Object> tempProd = new HashMap<>();
        try {
            tempProd = loadProduct();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        ArrayList<Double> prodTracker = new ArrayList();
        for(String product: tempProd.keySet()){
            Boolean isPart = false;
            Double totProd = 0.0;

            for(String curr: totalDates){
                if (sales.containsKey(curr)){
                    Map<String, Object> dbIng = (Map<String, Object>) sales.get(curr).get("TotalOrdered");
                    if(dbIng.containsKey(product)){
                        isPart = true;
                        if(totProd<Double.parseDouble(String.valueOf(dbIng.get(product)))){
                            totProd = Double.parseDouble(String.valueOf(dbIng.get(product)));
                        }
                    }
                }
            }

            if(isPart){
                prodTracker.add(totProd);
            }
        }
        Collections.sort(prodTracker);

        if(prodTracker.size() <= 0){
            return;
        }

        CategoryAxis xAxis = new CategoryAxis(FXCollections.observableArrayList(totalDates));
        NumberAxis yAxis = new NumberAxis(0,prodTracker.get(prodTracker.size()-1)+5,prodTracker.size());
        BarChart lineChart = new BarChart(xAxis, yAxis);


        for(String product: tempProd.keySet()){
            XYChart.Series series = new XYChart.Series();
            Boolean isPart = false;
            series.setName(product);

            for(String curr: totalDates){
                if (sales.containsKey(curr)){
                    Map<String, Object> dbIng = (Map<String, Object>) sales.get(curr).get("TotalOrdered");
                    if(dbIng.containsKey(product)){
                        series.getData().add(new XYChart.Data(curr, dbIng.get(product)));
                        isPart = true;
                    }
                }
            }

            if(isPart){
                lineChart.getData().add(series);
            }
        }



        //Initialize Scene + Stage
        xAxis.setLabel("Date");
        yAxis.setLabel("Products Ordered");

        Group root = new Group(lineChart);
        Scene scene = new Scene(root);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Products Graph");
        stage.show();
    }

    public void genGraph(){
        if(optionBox.getValue() == "Sales"){
            sales();
        }
        else if(optionBox.getValue() == "Used ingredients"){
            ingredients();
        }
        else{
            products();
        }
    }

    public void initTableView(){
        // load the data and store it in the table
        sales = loadSales();

        hidden = new TreeItem<>();

        dateCol.setCellValueFactory(new TreeItemPropertyValueFactory<Dates, String>("date"));
        ingCol.setCellValueFactory(new TreeItemPropertyValueFactory<Dates, String>("ing"));
        ingTCol.setCellValueFactory(new TreeItemPropertyValueFactory<Dates, String>("totIng"));
        prodCol.setCellValueFactory(new TreeItemPropertyValueFactory<Dates, String>("prod"));
        prodTCol.setCellValueFactory(new TreeItemPropertyValueFactory<Dates, String>("totProd"));
        salesCol.setCellValueFactory(new TreeItemPropertyValueFactory<Dates, String>("total"));

        dateCol.setReorderable(false);
        ingCol.setReorderable(false);
        ingTCol.setReorderable(false);
        prodCol.setReorderable(false);
        prodTCol.setReorderable(false);
        salesCol.setReorderable(false);



        for (String sale : sales.keySet()){
            Map<String, Integer> totOrdered = (Map<String, Integer>) sales.get(sale).get("TotalOrdered");
            Map<String, Integer> totIng = (Map<String, Integer>) sales.get(sale).get("TotalIng");

            TreeItem<Dates> date = new TreeItem<>(new Dates(sale,Double.parseDouble(String.valueOf(sales.get(sale).get("Total")))));

            ArrayList<String> ordList = new ArrayList<>(totOrdered.keySet());
            ArrayList<String> ingList = new ArrayList<>(totIng.keySet());
//            System.out.println(ordList);
//            System.out.println(ingList);

            int orderedTrack = totOrdered.size();
            int ingTrack = totIng.size();
//            System.out.println(sales.get(sale));
//            System.out.println(orderedTrack);
//            System.out.println(ingTrack);
            int min = 0;
            if (ingTrack>orderedTrack){
                min = orderedTrack;
            }
            else{
                min = ingTrack;
            }
            // when prod and ing should be in the same row
            for(int i = 0; i<min;i++){
                String ing = String.valueOf(totIng.get(ingList.get(i)));
                String prod = String.valueOf(totOrdered.get(ordList.get(i)));
                TreeItem<Dates> temp = new TreeItem<>(new Dates(ingList.get(i),ing + " items",ordList.get(i),prod + " orders"));
                date.getChildren().add(temp);
            }

            // if ing has more
            if (ingTrack>orderedTrack){
                for(int i = min; i<ingTrack;i++){
                    String ing = String.valueOf(totIng.get(ingList.get(i)));
                    TreeItem<Dates> temp = new TreeItem<>(new Dates(ingList.get(i),ing + " items"));
                    date.getChildren().add(temp);
                }
            }
            // if prod has more
            else{
                for(int i = min; i<orderedTrack;i++){
                    String prod = String.valueOf(totOrdered.get(ordList.get(i)));
                    TreeItem<Dates> temp = new TreeItem<>(new Dates(ordList.get(i),prod + " orders",1));
                    date.getChildren().add(temp);
                }
            }

            hidden.getChildren().add(date);
        }

        // Variable for tree itself, hide root.
        hidden.setExpanded(true);
        mainView.setRoot(hidden);
        mainView.setShowRoot(false);


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

    // Loads the items from database and returns a Map within a Map
    private Map<String, Object> loadItems() throws ExecutionException, InterruptedException {
        Iterable<DocumentReference> trial = cr.listDocuments();
        Map<String, Object> out = new HashMap<>();
        // The output format is
        // ID: {Name: *name*; Stock: *stock*}
        for (DocumentReference element : trial) {
            // Stores the current element in temp
            DocumentSnapshot temp = element.get().get();
            String name = temp.getString("Name");
            Double stock = temp.getDouble("Stock");

            // Store sub values inside hashmap
            Map<String, Object> mapTemp = new HashMap<>();
            mapTemp.put("Name",name);
            mapTemp.put("Stock",stock);

            out.put(name,stock);
        }
        return out;
    }

    // Loads the items from database and returns a Map within a Map
    private Map<String, Object> loadProduct() throws ExecutionException, InterruptedException {
        Iterable<DocumentReference> trial = db.collection("Cashier Product").listDocuments();
        Map<String, Object> out = new HashMap<>();
        // The output format is
        // ID: {Name: *name*; Stock: *stock*}
        for (DocumentReference element : trial) {
            // Stores the current element in temp
            DocumentSnapshot temp = element.get().get();
            String name = temp.getString("Name");
            Double stock = temp.getDouble("Stock");

            out.put(name,stock);
        }
        return out;
    }


}
