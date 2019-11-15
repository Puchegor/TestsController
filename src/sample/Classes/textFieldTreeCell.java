package sample.Classes;

import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.input.KeyCode;

public class textFieldTreeCell extends TreeCell<Item> {
    private TextField textField;

    @Override
    public void startEdit(){
        super.startEdit();
        if (textField == null){
            createTextField();
        }
        setText(null);
        setGraphic(textField);
        textField.selectAll();
    }
    @Override
    public void cancelEdit(){
        super.cancelEdit();
        setText(getItem().getName());
        setGraphic(getTreeItem().getGraphic());
    }
    @Override
    public void updateItem(Item item, boolean empty){
        super.updateItem(item, empty);
        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()){
                if (textField!=null)
                    textField.setText(getItem().getName());
                setText(null);
                setGraphic(textField);
            } else {
                setText(getItem().getName());
                setGraphic(getTreeItem().getGraphic());
            }
        }
    }

    @Override
    public void commitEdit(Item item){
        super.commitEdit(item);
        String tableName = "", fieldName = "", idField = "";
        tableName = getItem().getTable();
        switch (tableName){
            case "subjects":
                fieldName = "nameSub";
                idField = "idSub";
                break;
            case "questions":
                fieldName = "nameQuestion";
                idField = "idQuestion";
                break;
            case "topics":
                fieldName = "nameTopic";
                idField = "idTopic";
                break;
            default:
                return;
        }
        DB.Update(tableName, fieldName+" = \""+textField.getText()+"\"",
                idField+ " = \""+getItem().getIdOwn()+"\"");
        getItem().setName(textField.getText());
    }

    private void createTextField(){
        textField = new TextField(getItem().getName());
        textField.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.ENTER)
                commitEdit(getItem());
            else if (event.getCode() == KeyCode.ESCAPE)
                cancelEdit();
        });
    }
}
