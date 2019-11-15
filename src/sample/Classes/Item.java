package sample.Classes;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Item {
    private int idParent;
    private int idOwn;
    private String table;
    private StringProperty name = new SimpleStringProperty();

    public Item (int idParent, int idOwn, String table, String name){
        this.idParent = idParent;
        this.idOwn = idOwn;
        this.name.set(name);
        this.table = table;
    }
    //---------Setters-----------------

    public void setIdParent(int idParent) {
        this.idParent = idParent;
    }

    public void setIdOwn(int idOwn) {
        this.idOwn = idOwn;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public void setTable(String table) {
        this.table = table;
    }

    //--------Getters------------------

    public int getIdParent() {
        return idParent;
    }

    public int getIdOwn() {
        return idOwn;
    }

    public String getName() {
        return name.get();
    }

    public String getTable() {
        return table;
    }

    //---------ToString---------------

    @Override
    public String toString() {
        return getName();
    }
}
