package sample.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
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
    ListView listView;
    @FXML
    AnchorPane apRight;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ArrayList<Integer> topicIDs = TestData.getTopicIDs();
        ObservableList<Task> tasks = FXCollections.observableArrayList();
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

        //------------------------------
        for (int i = 0; i < numTasks; i++){
            ListView<Task> lv = new ListView<>();
            apRight.getChildren().add(lv);
        }
        //------------------------------
    }

    public void onCancelHandle(ActionEvent actionEvent) {
        Stage stage = (Stage)btnCancel.getScene().getWindow();
        stage.close();
    }

    public void onOkHandle(ActionEvent actionEvent) {
    }
}
