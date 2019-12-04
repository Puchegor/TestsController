package sample.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import sample.Classes.Alerts;
import sample.Classes.DB;
import sample.Classes.Task;
import sample.Classes.TestData;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class AddTaskToTest implements Initializable {
    @FXML
    Button btnOk, btnCancel;
    @FXML
    ListView listView, lvToTest;
    @FXML
    AnchorPane apRight;
    @FXML
    Label labTaskNum;
    @FXML
            CheckBox cbRemoveTasks;

    int taskNo = 1;
    ObservableList<Task> tasks = FXCollections.observableArrayList();
    ObservableList<Task> activeTasks = FXCollections.observableArrayList();
    ArrayList<ObservableList<Task>> toTest = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ArrayList<Integer> topicIDs = TestData.getTopicIDs();

        int numTasks = TestData.getNumTasks();
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
        if (toTest.size()==0)
            toTest.add(taskNo, lvToTest.getItems());
        /*if (toTest.size() == 0 || toTest.get(taskNo)==null)
            toTest.add(taskNo, lvToTest.getItems());
        else
            toTest.set(taskNo, lvToTest.getItems());
        if (taskNo < TestData.getNumTasks()){
            taskNo = ++taskNo;
            labTaskNum.setText("Задача № "+taskNo);
        } else {
            taskNo = 1;
            labTaskNum.setText("Задача № "+taskNo);
        }
        if (toTest.get(taskNo)!=null)
            lvToTest.setItems(toTest.get(taskNo));*/
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
