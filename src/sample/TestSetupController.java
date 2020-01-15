package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sample.Classes.*;

import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ResourceBundle;

public class TestSetupController implements Initializable {
    @FXML
    MenuBar menuBar;
    @FXML
    Label lbStatus;
    @FXML
    TreeView<Item> treeView;
    @FXML
    TextField tfVars, tfQuestions, tfTasks;
    @FXML
    CheckBox cbIncludeTasks, cbHideVars;

    public void onExitHandle(ActionEvent actionEvent) {
        System.exit(0);
    }

    public void onEnterTests(ActionEvent actionEvent) throws IOException{
        startNewWindow("FXML/mainWindow.fxml");
    }

    public void onTasksHandle(ActionEvent actionEvent) throws IOException{
        startNewWindow("FXML/TaskFRM.fxml");
    }

    public void onExportHandle(ActionEvent actionEvent) throws IOException{
        startNewWindow("FXML/Export.fxml");
    }

    private void startNewWindow (String url) throws IOException{
        Stage stage = (Stage)menuBar.getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource(url));
        Scene scene = new Scene(root, 700, 500);
        stage.setScene(scene);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (DB.getConnection()!=null)
            lbStatus.setText("Соединение с базой данных установлено");
        buildTreeView();
        tfVars.setText("3");
        tfQuestions.setText("10");
        tfTasks.setText("3");
        tfTasks.setEditable(false);
        tfQuestions.setEditable(false);
        tfVars.setEditable(false);
        treeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    private void buildTreeView(){
        ObservableList<Item>subjects = FXCollections.observableArrayList();
        ObservableList<Item>topics = FXCollections.observableArrayList();
        TreeItem<Item> rootItem = new TreeItem<>();
        rootItem.setExpanded(true);
        treeView.setRoot(rootItem);
        treeView.setShowRoot(false);
        try{
            ResultSet rst = DB.Select("subjects", null);
            while(rst.next())
                subjects.add(new Item(0, rst.getInt("idSub"), "subjects", rst.getString("nameSub")));
            for (Item subject: subjects){
                TreeItem<Item> sub = new TreeItem<>(subject);
                rootItem.getChildren().add(sub);
                rst = DB.Select("topics", "idSub = "+subject.getIdOwn());
                while(rst.next())
                    topics.add(new Item(subject.getIdOwn(), rst.getInt("idTopic"), "topics", rst.getString("nameTopic")));
                for (Item topic: topics){
                    TreeItem<Item> top = new TreeItem<>(topic);
                    sub.getChildren().add(top);
                }
                topics.clear();
            }
        }catch(SQLException e){
            Alerts.Error(e.getMessage());
        }
    }

    public void onLessVar(ActionEvent actionEvent) {
        int count = Integer.parseInt(tfVars.getText());
        if (count > 1)
            count--;
        else
            Alerts.Warning("Предупреждение", "Количество вариантов теста не может быть меньше 1");
        tfVars.setText(String.valueOf(count));
    }

    public void onLessQuestions(ActionEvent actionEvent) {
        int count = Integer.parseInt(tfQuestions.getText());
        if (count > 10)
            count--;
        else
            Alerts.Warning("Предупреждение", "Количество вопросов теста не может быть меньше 10!");
        tfQuestions.setText(String.valueOf(count));
    }

    public void onLessTasks(ActionEvent actionEvent) {
        int counnt = Integer.parseInt(tfTasks.getText());
        if (counnt > 1)
            counnt--;
        else
            Alerts.Warning("Предупреждение", "Количество задач не может быть меньше 1!");
        tfTasks.setText(String.valueOf(counnt));
    }

    public void onMoreVars(ActionEvent actionEvent) {
        int count = Integer.parseInt(tfVars.getText());
        count++;
        tfVars.setText(String.valueOf(count));
    }

    public void onMoreQuestions(ActionEvent actionEvent) {
        int count = Integer.parseInt(tfQuestions.getText());
        count++;
        tfQuestions.setText(String.valueOf(count));
    }

    public void onMoreTasks(ActionEvent actionEvent) {
        int count = Integer.parseInt(tfTasks.getText());
        count++;
        tfTasks.setText(String.valueOf(count));
    }

    public void onReadyHandle(ActionEvent actionEvent) throws IOException{
        ObservableList<Question> questions = addQuestionsToTest();
        if (questions == null)
            return;
        boolean hidvar = false;
        if (cbHideVars.isSelected())
            hidvar = true;

        if (!cbIncludeTasks.isSelected()){
            try {
                startTestview(questions, hidvar);
            }catch (IOException e){
                Alerts.Error(e.getMessage());
            }
        }else{
            //-----ВСТАВКА ЗАДАЧ-------------------
            if (addTasksToTest() == null) {
                Alerts.Warning("Не добавлены задачи",
                        "Для продолжения работы добавьте задачи, или измените режим тестирования");
                return;
            } else {
                startTestview(questions, hidvar);
            }
        }
    }

    private void startTestview(ObservableList<Question> questions, boolean hiddenVariants) throws IOException{
        TestData.setQuestions(questions);
        TestData.setNumVariants(Integer.parseInt(tfVars.getText()));
        TestData.setNumQuestions(Integer.parseInt(tfQuestions.getText()));
        TestData.setHiddenVariants(hiddenVariants);
        TestData.setNumTasks(Integer.parseInt(tfTasks.getText()));

        Stage stage = new Stage();
        Parent root = FXMLLoader.load(getClass().getResource("FXML/TestView.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Просмотр теста");
        stage.setMaximized(true);
        stage.showAndWait();
    }

    private ObservableList<Question> addQuestionsToTest(){
        ObservableList<Question>questions = FXCollections.observableArrayList();
        MultipleSelectionModel<TreeItem<Item>> selection = treeView.getSelectionModel();
        selection.setSelectionMode(SelectionMode.MULTIPLE);
        if (selection.isEmpty()){
            Alerts.Warning("Предупреждение", "Не выбраны темы для тестирования");
            return null;
        }
        ResultSet rst;
        ArrayList<Integer> idQuestions = new ArrayList<>();
        ArrayList<Integer> topicIDs = new ArrayList<>();
        for (TreeItem<Item> item : selection.getSelectedItems()){
            try{
                if (item.getChildren().isEmpty()){
                    rst = DB.Select("questions", "idTopic = "+item.getValue().getIdOwn());
                    topicIDs.add(item.getValue().getIdOwn());
                    while (rst.next())
                        idQuestions.add(rst.getInt("idQuestion"));
                }else{
                    for(TreeItem<Item> topic : item.getChildren()){
                        rst = DB.Select("questions",
                                "idTopic = "+topic.getValue().getIdOwn());
                        topicIDs.add(topic.getValue().getIdOwn());
                        while(rst.next())
                            idQuestions.add(rst.getInt("idQuestion"));
                    }
                }
                ObservableList<Answer>answers = FXCollections.observableArrayList();
                for (int i : idQuestions){
                    rst = DB.Select("questions", "idQuestion = "+i);
                    String questionName = rst.getString("nameQuestion");
                    int idTopic = rst.getInt("idTopic");
                    rst = DB.Select("answers", "idQuestion = "+i);
                    while (rst.next()) {
                        boolean isCorect = false;
                        if (rst.getInt("isCorrect") != 0)
                            isCorect = true;
                        answers.add(new Answer(rst.getInt("idAnswer"), i, rst.getString("nameAnswer"),isCorect));
                        Collections.shuffle(answers);
                    }
                    questions.add(new Question(i, idTopic, questionName, answers));
                    answers.clear();
                }
                TestData.setTopicIDs(topicIDs);
            }catch(SQLException e){
                Alerts.Error(e.getMessage());
                return null;
            }
        }
        return questions;
    }

    private ObservableList<Task> addTasksToTest() throws IOException {
        ObservableList<Task> tasks = FXCollections.observableArrayList();
        TestData.setNumTasks(Integer.parseInt(tfTasks.getText()));

        showAddTask();
        return tasks;
    }

    private void showAddTask () throws IOException{
        Stage stage = new Stage();
        Parent root = FXMLLoader.load(getClass().getResource("FXML/AddTaskToTest.fxml"));
        Scene scene = new Scene(root);
        stage.setTitle("Добавить задачи");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.showAndWait();
    }
}
