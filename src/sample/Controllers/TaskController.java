package sample.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import sample.Classes.Alerts;
import sample.Classes.DB;
import sample.Classes.Item;
import sample.Classes.Task;

import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

public class TaskController implements Initializable {
    @FXML
    MenuBar menuBar;
    @FXML
    Label lbStatus;
    @FXML
            TreeView<Item> treeView;
    @FXML
            TextArea taTask, taAnswer;

    TreeItem<Item> rootItem = new TreeItem<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (DB.getConnection()!=null)
            lbStatus.setText("Соединение с базой данных установлено");
        buildTree();
    }

    public void onAddQuestions(ActionEvent actionEvent) throws IOException {
        Stage stage = (Stage)menuBar.getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource("../FXML/mainWindow.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
    }

    public void onExitHandle(ActionEvent actionEvent) {
        System.exit(0);
    }

    private void buildTree(){
        ObservableList<Item> subjects = FXCollections.observableArrayList();
        ObservableList<Item> topics = FXCollections.observableArrayList();
        ObservableList<Item> tasks = FXCollections.observableArrayList();

        rootItem.setExpanded(true);

        try{
            ResultSet rst =DB.Select("subjects", null);
            while(rst.next())
                subjects.add(new Item(0, rst.getInt("idSub"), "subjects", rst.getString("nameSub")));
            for (Item subject : subjects){
                TreeItem<Item> sub = new TreeItem<>(subject);
                rootItem.getChildren().add(sub);
                rst = DB.Select("topics", "idSub = "+sub.getValue().getIdOwn());
                topics.clear();
                while(rst.next())
                    topics.add(new Item(sub.getValue().getIdOwn(), rst.getInt("idTopic"),
                            "topics", rst.getString("nameTopic")));
                for (Item topic : topics){
                    TreeItem<Item> top = new TreeItem<>(topic);
                    sub.getChildren().add(top);
                    rst = DB.Select("tasks", "idTopic = "+top.getValue().getIdOwn());
                    tasks.clear();
                    while (rst.next())
                        tasks.add(new Item(top.getValue().getIdOwn(), rst.getInt("idTask"),
                                "tasks", rst.getString("nameTask")));
                    for (Item task : tasks){
                        TreeItem<Item> tsk = new TreeItem<>(task);
                        top.getChildren().add(tsk);
                    }
                }
            }
            treeView.setRoot(rootItem);
            treeView.setShowRoot(false);
        }catch (SQLException e){
            Alerts.Error(e.getMessage());
        }
    }

    public void onTreeViewClick(MouseEvent mouseEvent) {
        MultipleSelectionModel<TreeItem<Item>>selection = treeView.getSelectionModel();
        selection.setSelectionMode(SelectionMode.SINGLE);
        int level = treeView.getTreeItemLevel(selection.getSelectedItem());
        switch(level){
            case 3:
                ResultSet rst = DB.Select("tasks", "idTask = "+selection.getSelectedItem().getValue().getIdOwn());
                try {
                    taTask.setText(rst.getString("nameTask"));
                    taAnswer.setText(rst.getString("answer"));
                }catch (SQLException e){
                    Alerts.Error(e.getMessage());
                }
        }
    }

    public void onDelTask(ActionEvent actionEvent) {
        MultipleSelectionModel<TreeItem<Item>> selection = treeView.getSelectionModel();
        selection.setSelectionMode(SelectionMode.SINGLE);
        int level = treeView.getTreeItemLevel(selection.getSelectedItem());
        TreeItem<Item>item = selection.getSelectedItem();
        switch (level){
            case 3:
                Optional<ButtonType>choise = Alerts.Confirmation("Вы действительно хотите удалить задачу \""+
                                selection.getSelectedItem().getValue().getName()+"\"?",
                        "Пожалуйста, подтвердите выбор");
                if (choise.get() == ButtonType.OK){
                    DB.Delete("tasks", "idTask = "+selection.getSelectedItem().getValue().getIdOwn());
                    item.getParent().getChildren().remove(selection);//Почему-то не работает. Разобраться;
                    taTask.clear();
                    taAnswer.clear();
                }
        }
    }

    public void onSaveTask(ActionEvent actionEvent) {
        if (taTask.getText().isEmpty()){
            Alerts.Information("Не введено содержание задачи",
                    "Для продолжения работы введите задачу");
            return;
        }else{
            if (taAnswer.getText().isEmpty()){
                Alerts.Information("Не введен правильный ответ к задаче",
                        "Для продолжения работы введите правильный ответ");
                return;
            }
        }
        MultipleSelectionModel<TreeItem<Item>>selection = treeView.getSelectionModel();
        selection.setSelectionMode(SelectionMode.SINGLE);
        int level = treeView.getTreeItemLevel(selection.getSelectedItem());
        TreeItem<Item> parent = selection.getSelectedItem();
        switch (level){
            case 2:
                DB.Insert("tasks", "nameTask, answer, idTopic",
                        taTask.getText()+"\", \""+taAnswer+
                                "\", \""+selection.getSelectedItem().getValue().getIdOwn());
                ResultSet rst = DB.Select("tasks", "nameTask = \""+taTask.getText()+"\"");
                try{
                    TreeItem<Item> task = new TreeItem<>(new Item(parent.getValue().getIdOwn(),
                            rst.getInt("idTask"), "tasks", rst.getString("nameTask")));
                    parent.getChildren().add(task);
                }catch (SQLException e){
                    Alerts.Error(e.getMessage());
                }
                break;
            case 3:
                DB.Update("tasks", "nameTask =\""+taTask.getText()+"\", answer = \""+taAnswer.getText()+"\" ",
                        "idTask = \""+selection.getSelectedItem().getValue().getIdOwn()+"\"");
                ResultSet rst1 = DB.Select("tasks", "idTask = \""+parent.getValue().getIdOwn()+"\"");
                TreeItem<Item> superparent = parent.getParent();
                superparent.getChildren().remove(parent);
                try{
                    TreeItem<Item>updatedtask = new TreeItem<>(new Item(superparent.getValue().getIdOwn(),
                            rst1.getInt("idTask"), "tasks", rst1.getString("nameTask")));
                    superparent.getChildren().add(updatedtask);
                }catch (SQLException e){
                    Alerts.Error(e.getMessage());
                }
                break;
        }
    }

    public void onExportHandle(ActionEvent actionEvent) throws IOException{
        Stage stage = (Stage)menuBar.getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource("../FXML/Export.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
    }

    public void onTestHandle(ActionEvent actionEvent) throws IOException {
        Stage stage = (Stage)menuBar.getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource("../FXML/TestSetup.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
    }
}
