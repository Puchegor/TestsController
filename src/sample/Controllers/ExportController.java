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
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import sample.Classes.Alerts;
import sample.Classes.DB;
import sample.Classes.Item;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ExportController implements Initializable {
    @FXML
    MenuBar menuBar;
    @FXML
    TreeView<Item> treeView;
    @FXML
    Label lbStatus;
    @FXML
    Button btnExport;

    public void onQuestionsHandle(ActionEvent actionEvent) throws IOException {
        startNewWindow("../FXML/MainWindow.fxml", "DIZ Test Controller - Ввод тестов");
    }

    public void onTaskHandle(ActionEvent actionEvent) throws IOException{
        startNewWindow("../FXML/TaskFrm.fxml", "DIZ Test Controller - Ввод задач");
    }

    public void onExitHandle(ActionEvent actionEvent) {
        System.exit(0);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (DB.getConnection()!=null)
            lbStatus.setText("Соединение с базой данных установлено");
        buildTree();
    }
    private void startNewWindow(String fxmlResource, String title) throws IOException{
        Stage stage = (Stage)menuBar.getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource(fxmlResource));
        stage.setTitle(title);
        Scene scene = new Scene(root);
        stage.setScene(scene);
    }
    private void buildTree(){
        ObservableList<Item>subjects = FXCollections.observableArrayList();
        ObservableList<Item>topics = FXCollections.observableArrayList();
        TreeItem<Item> rootItem = new TreeItem<>();
        rootItem.setExpanded(true);
        treeView.setRoot(rootItem);
        treeView.setShowRoot(false);

        try{
            ResultSet rst = DB.Select("subjects", null);
            while(rst.next())
                subjects.add(new Item(0, rst.getInt("idSub"),"subjects", rst.getString("nameSub")));
            for (Item subject : subjects){
                TreeItem<Item>sub = new TreeItem<>(subject);
                rootItem.getChildren().add(sub);
                rst = DB.Select("topics", "idSub ="+subject.getIdOwn());
                topics.clear();
                while(rst.next())
                    topics.add(new Item(subject.getIdOwn(),rst.getInt("idTopic"),
                            "topics", rst.getString("nameTopic")));
                for (Item topic : topics){
                    TreeItem<Item> top = new TreeItem<>(topic);
                    sub.getChildren().add(top);
                }
            }
        }catch(SQLException e){
            Alerts.Error(e.getMessage());
        }
    }

    public void onExportClick(ActionEvent actionEvent) {
        MultipleSelectionModel<TreeItem<Item>>selection = treeView.getSelectionModel();
        selection.setSelectionMode(SelectionMode.SINGLE);
        if (selection.isEmpty()){
            Alerts.Warning("Не выбран элемент для экспорта",
                    "Для продолжения работы выберите элемент для экспорта");
            return;
        }
        FileChooser fc = new FileChooser();
        fc.setTitle("Выберите файл для экспорта");
        FileChooser.ExtensionFilter filter_db = new FileChooser.ExtensionFilter("Файл базы данных db","*.db");
        FileChooser.ExtensionFilter filter_sqlite = new FileChooser.ExtensionFilter("Файл sqlite", "*.sqlite");
        FileChooser.ExtensionFilter filter_all = new FileChooser.ExtensionFilter("Все файлы", "*.*");
        fc.getExtensionFilters().addAll(filter_db, filter_sqlite, filter_all);
        fc.setInitialDirectory(new File(System.getProperty("user.home")));
        File file = fc.showSaveDialog(btnExport.getScene().getWindow());
        if (file == null)
            return;
        exportDB(selection, file);
    }

    private void exportDB(MultipleSelectionModel<TreeItem<Item>> selection, File file){
        int subjectID = selection.getSelectedItem().getValue().getIdOwn();
        int level = treeView.getTreeItemLevel(selection.getSelectedItem());
        switch (level){
            case 1:
                DB.Query("ATTACH DATABASE \'"+file.getPath()+"\' AS otherdb; ");
                DB.Query("CREATE TABLE otherdb.subjects (_id INTEGER PRIMARY KEY AUTOINCREMENT, nameSub TEXT)");
                DB.Query("CREATE TABLE otherdb.topics (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "idSub INTEGER NOT NULL, nameTopic Text (50), " +
                        "FOREIGN KEY (idSub) REFERENCES subjects (_id))");
                DB.Query("CREATE TABLE otherdb.questions (_id INTEGER PRIMARY KEY AUTOINCREMENT, idTopic INTEGER NOT NULL, " +
                        "nameQuestion TEXT, correctAnswer TEXT, FOREIGN KEY (idTopic) REFERENCES topics(_id)); ");
                DB.Query("CREATE TABLE otherdb.answers (_id INTEGER PRIMARY KEY AUTOINCREMENT, idQuestion INTEGER NOT NULL, " +
                        "nameAnswer TEXT, isCorrect INTEGER, FOREIGN KEY (idQuestion) REFERENCES questions (_id)); ");
                DB.Query("CREATE TABLE otherdb.android_metadata (locale TEXT DEFAULT 'en_US')");
                DB.Query("INSERT INTO android_metadata (locale) VALUES ('en_US')");// NO COMPRENDO!!!!!!!!!!!
                DB.Query("INSERT INTO otherdb.subjects (_id, nameSub) SELECT idSub, nameSub FROM subjects WHERE idSub = \"" +
                        subjectID + "\"");
                DB.Query("INSERT INTO otherdb.topics (_id, idSub, nameTopic) SELECT idTopic, idSub, nameTopic" +
                        " FROM topics WHERE idSub = \"" +subjectID + "\"");
                DB.Query("INSERT INTO otherdb.questions (_id, idTopic, nameQuestion, correctAnswer) " +
                        "SELECT questions.idQuestion, questions.idTopic, questions.nameQuestion, questions.correctAnswer " +
                        "FROM questions, topics, subjects " +
                        "WHERE questions.idTopic=topics.idTopic " +
                        "AND topics.idSub = subjects.idSub " +
                        "AND subjects.idSub = \"" + subjectID + "\"");
                DB.Query("INSERT INTO otherdb.answers (_id, idQuestion, nameAnswer, isCorrect) " +
                        "SELECT answers.idAnswer, answers.idQuestion, answers.nameAnswer, answers.isCorrect " +
                        "FROM answers, questions, topics, subjects " +
                        "WHERE answers.idQuestion = questions.idQuestion " +
                        "AND questions.idTopic = topics.idTopic " +
                        "AND topics.idSub = subjects.idSub " +
                        "AND subjects.idSub = \"" + subjectID + "\"");
                //DB.Select("subjects", null);
                //DB.Query("DETACH DATABASE otherdb");
                break;
            case 2:
                int topicID = subjectID;
                DB.Query("ATTACH DATABASE \'"+file.getPath()+"\' AS otherdb; ");
                DB.Query("CREATE TABLE otherdb.subjects (idSub INTEGER PRIMARY KEY AUTOINCREMENT, nameSub TEXT); ");
                DB.Query("CREATE TABLE otherdb.topics (idTopic INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "idSub INTEGER NOT NULL, nameTopic TEXT(50), " +
                        "FOREIGN KEY (idSub) REFERENCES subjects (idSub)); ");
                DB.Query("CREATE TABLE otherdb.questions (idQuestion INTEGER PRIMARY KEY AUTOINCREMENT, idTopic INTEGER NOT NULL, " +
                        "nameQuestion TEXT, correctAnswer TEXT, FOREIGN KEY (idTopic) REFERENCES topics(idTopic)); ");
                DB.Query("CREATE TABLE otherdb.answers (idAnswer INTEGER PRIMARY KEY AUTOINCREMENT, idQuestion INTEGER NOT NULL, " +
                        "nameAnswer TEXT, isCorrect INTEGER, FOREIGN KEY (idQuestion) REFERENCES questions (idQuestion)); ");
                DB.Query("INSERT INTO otherdb.topics SELECT * FROM topics WHERE idTopic = \""+
                        topicID+"\"");
                DB.Query("INSERT INTO otherdb.questions " +
                        "SELECT questions.idQuestion, questions.idTopic, questions.nameQuestion, questions.correctAnswer " +
                        "FROM questions, topics WHERE questions.idTopic=topics.idTopic AND topics.idTopic = \""+topicID+"\"");
                DB.Query("INSERT INTO otherdb.answers "+
                        "SELECT answers.idAnswer, answers.idQuestion, answers.nameAnswer, answers.isCorrect " +
                        "FROM answers, questions, topics WHERE answers.idQuestion = questions.idQuestion " +
                        "AND questions.idTopic = topics.idTopic AND topics.idTopic = \""+topicID+"\"");
                //DB.Select("topics",null);
                //DB.Query("DETACH DATABASE otherdb");
                break;
        }
        Alerts.Succeses("База данных успешно экспортирована");
    }

    public void onTestSetupHandle(ActionEvent actionEvent) throws IOException{
        startNewWindow("../FXML/TestSetup.fxml", "DIZ Test Controller - Настройка теста");
    }
}
