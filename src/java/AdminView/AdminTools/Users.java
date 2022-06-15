package AdminView.AdminTools;

import com.google.cloud.firestore.CollectionReference;

import java.util.HashMap;
import java.util.Map;

public class Users {
    private String user,pass,type;

    Users(String user, String pass, String type){
        this.user = user;
        this.pass = pass;
        this.type = type;
    }

    Users(String user){
        this.pass = "123";
        this.user = user;
        this.type = "Cashier";
    }

    public String getUser(){
        return user;
    }

    public String getPass(){
        return pass;
    }

    public String getType(){
        return type;
    }

    public void setUser(String user){
        this.user = user;
    }

    public void setPass(String pass){
        this.pass = pass;
    }

    public void setType(String type){
        this.type = type;
    }

    public void save(CollectionReference cr){
        Map<String, Object> temp = new HashMap<>();
        temp.put("Pass",pass);
        temp.put("Type",type);
        cr.document(user).set(temp);
    }
}
