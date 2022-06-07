package AdminView.Products;

public class treeNode{
    private String productName;
    private Object stock;
    public treeNode(String pN, Object st){
        productName = pN;
        this.stock = st;
    }
    public treeNode(String pN){
        this.productName = pN;
        this.stock = null;
    }
    public String getProductName(){
        return productName;
    }
    public Object getStock(){
        return stock;
    }
}
