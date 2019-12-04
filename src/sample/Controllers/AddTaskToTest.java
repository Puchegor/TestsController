package sample.Controllers;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.fxml.Initializable;
import sample.Classes.DB;
import sample.Classes.Alerts;
import sample.Classes.Task;
import sample.Classes.TestData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.net.URL;


public class AddTaskToTest implements Initializable {
    @FXML
    ListView listView, lvToTest;
    @FXML
    Button btnOk, btnCancel;
    @FXML
    CheckBox cbRemoveTasks;
    @FXML
    AnchorPane apRight;
    @FXML
    Label labTaskNum;

    int taskNo = 1;

    ObservableList<Task> tasks = FXCollections.observableArrayList();
    ObservableList<Task> activeTasks = FXCollections.observableArrayList();
    ObservableList<ObservableList<Task>> toTest = FXCollections.observableArrayList();
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ArrayList<Integer> topicIDs = TestData.getTopicIDs();

        int numTasks = TestData.getNumTasks();
        for (int i = 0; i < numTasks; i++)
            toTest.add(i, null);
        ResultSet rst;
        for (int id: topicIDs){
            try {
                rst = DB.Select("tasks ", "idTopic = " + id);
                while (rst.next())
                    tasks.add(new Task(id, rst.getInt("idTask"), rst.getString("nameTask"),rst.getString("answer")));
            }catch (SQLException e){
                Alerts.Error(e.getMessage());
            }
        }
        listView.setItems(tasks);
        taskNo = 1;
        labTaskNum.setText("Задача № "+taskNo);
    }

    public void onCancelHandle(ActionEvent actionEvent) {
        Stage stage = (Stage)btnCancel.getScene().getWindow();
        stage.close();
    }

    public void onOkHandle(ActionEvent actionEvent) {
    }

    public void onToTestHandle(ActionEvent actionEvent) {
        MultipleSelectionModel<Task> selection = listView.getSelectionModel();
        selection.setSelectionMode(SelectionMode.SINGLE);
        activeTasks.add(selection.getSelectedItem());
        lvToTest.setItems(activeTasks);
        if (cbRemoveTasks.isSelected()){
            tasks.remove(selection.getSelectedItem());
        }
    }

    public void onOutTestHandle(ActionEvent actionEvent) {
        MultipleSelectionModel<Task> selection = lvToTest.getSelectionModel();
        selection.setSelectionMode(SelectionMode.SINGLE);
        activeTasks.remove(selection.getSelectedItem());
        if (cbRemoveTasks.isSelected())
            tasks.add(selection.getSelectedItem());
    }
    public void onNextHandle(ActionEvent actionEvent) {
        toTest.set(taskNo-1, activeTasks);
        if (taskNo < TestData.getNumTasks()){
            taskNo = ++taskNo;
            labTaskNum.setText("Задача № "+taskNo);
            activeTasks.clear();
        }
        else {
            taskNo = 1;
            labTaskNum.setText("Задача № "+taskNo);
            activeTasks.clear();
            if (toTest.get(taskNo-1)!=null)
                for (int i = 0; i < toTest.get(taskNo - 1).size(); i++)
                    activeTasks.add(toTest.get(taskNo-1).get(i));
        }
        lvToTest.setItems(activeTasks);
    }

    public void onPrevHandle(ActionEvent actionEvent) {
        /*if (taskNo > 1 && taskNo <=TestData.getNumTasks()){
            taskNo = --taskNo;
            labTaskNum.setText("Задача № "+taskNo);
        }else {
            taskNo = TestData.getNumTasks();
            labTaskNum.setText("Задача №"+taskNo);
        }*/
    }
}
