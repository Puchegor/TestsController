package sample;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import sample.Classes.*;

import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainWindow implements Initializable {
    @FXML
    Button btnExit, btnAddItem, btnDelItem, btnSaveAnswer, btnDelAnswer;
    @FXML
    TreeView treeView;
    @FXML
    Label lbConnection;
    @FXML
    TextArea taAddTreeItem, taCorrectAnswer, taAnswer;
    @FXML
    TableView<Answer> tableView;
    @FXML
            TableColumn<Answer, String> tcAnswer;
    @FXML
            TableColumn<Answer, Boolean> tcIsTrue;
    @FXML
            MenuItem mniAddTask;
    @FXML
            MenuBar menuBar;

    ObservableList<Item> subjects = FXCollections.observableArrayList();
    ObservableList<Item> topics = FXCollections.observableArrayList();
    ObservableList<Item> questions = FXCollections.observableArrayList();
    ObservableList<Answer> answers = FXCollections.observableArrayList();

    TreeItem<Item> rootItem = new TreeItem<Item>();

    public void exitHandle(ActionEvent actionEvent) {
        System.exit(0);
    }

    public void delItemHandle(ActionEvent actionEvent) {
        MultipleSelectionModel<TreeItem<Item>> selection = treeView.getSelectionModel();
        selection.setSelectionMode(SelectionMode.SINGLE);
        TreeItem<Item>item = selection.getSelectedItem();
        if(item == null){
            Alerts.Information("Не выделен пункт для удаления",
                    "Для продолжения работы выделите пункт для удаления");
            return;
        }else {
            if (!item.isLeaf()){
                Alerts.Information("Выбранный пункт имеет вложения",
                        "Для продолжения работы удалите все вложения");
                return;
            }else{
                String table = item.getValue().getTable();
                String id = "";
                switch (table) {
                    case "subjects":
                        id = "idSub";
                        break;
                    case "topics":
                        id = "idTopic";
                        break;
                    case "questions":
                        id = "idQuestion";
                        break;
                }
                Optional<ButtonType> choise = Alerts.Confirmation("Вы действительно хотите удалить \""+
                                item.getValue().getName()+"\"?",
                        "Пожалуйста, подтвердите");
                if (choise.get()==ButtonType.OK) {
                    DB.Delete(table, id+" = \""+item.getValue().getIdOwn()+"\"");
                    item.getParent().getChildren().remove(item);
                }
            }
        }
    }

    public void addItemHandle(ActionEvent actionEvent) {
        if(taAddTreeItem.getText().isEmpty()|taAddTreeItem.getText().trim()==""){
            Alerts.Information("Не заполнено поле нового пункта",
                    "Чтобы продолжить, необходимо ввести текст");
            return;
        }
        MultipleSelectionModel<TreeItem<Item>> selection = treeView.getSelectionModel();
        selection.setSelectionMode(SelectionMode.SINGLE);
        TreeItem<Item> parent = selection.getSelectedItem();
        try {
            if (parent == null) {
                String nameSub = taAddTreeItem.getText();
                DB.Insert("subjects", "nameSub", nameSub);
                ResultSet rst = DB.Select("subjects", " nameSub = \"" + nameSub + "\"");
                TreeItem<Item> sub = new TreeItem<>(new Item(0, rst.getInt("idSub"), "subjects", nameSub));
                rootItem.getChildren().add(sub);
                taAddTreeItem.clear();
            } else{
                if (parent.getParent().getParent()==null){
                    taAddTreeItem.setDisable(false);
                    String nameTopic = taAddTreeItem.getText();
                    int idSub = parent.getValue().getIdOwn();
                    DB.Insert("topics", "nameTopic, idSub",
                            nameTopic+"\", \""+idSub);
                    ResultSet rst = DB.Select("topics",
                            "nameTopic = \""+nameTopic+"\" AND idSub = \""+idSub+"\"");
                    TreeItem<Item> topic = new TreeItem<>(new Item(idSub, rst.getInt("idTopic"), "topics", nameTopic));
                    parent.getChildren().add(topic);
                    taAddTreeItem.clear();
                }else {
                    if(parent.getParent().getParent().getParent()==null){
                        taAddTreeItem.setDisable(false);
                        String nameQuestion = taAddTreeItem.getText();
                        int idTopic = parent.getValue().getIdOwn();
                        DB.Insert("questions", "nameQuestion, idTopic",
                                nameQuestion+"\", \""+idTopic);
                        ResultSet rst = DB.Select("questions",
                                "nameQuestion = \""+nameQuestion+"\" AND idTopic = \""+idTopic+"\"");
                        TreeItem<Item> question = new TreeItem<>(new Item (idTopic, rst.getInt("idQuestion"),
                                "questions", nameQuestion));
                        parent.getChildren().add(question);
                        taAddTreeItem.clear();
                    }
                }
            }
        }catch (SQLException e){
            Alerts.Error(e.getMessage());
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (DB.getConnection()!= null){
            lbConnection.setText("Соединение с базой данных установлено");
        }
        buildTree();
    }
    private void buildTree(){
        try {
            ResultSet rst = DB.Select("subjects", null);
            rootItem.setExpanded(true);
            assert rst != null;
            while (rst.next()){
                setSubjects(new Item(0, rst.getInt("idSub"), "subjects",
                        rst.getString("nameSub")));
            }
            for (Item subject : subjects) {
                TreeItem<Item> sub = new TreeItem<>(subject);
                rootItem.getChildren().add(sub);
                rst = DB.Select("topics", "idSub = " + sub.getValue().getIdOwn());//ERROR!!!!!
                topics.clear();
                while (rst.next()) {
                    setThemes(new Item(rst.getInt("idSub"),
                            rst.getInt("idTopic"), "topics", rst.getString("nameTopic")));
                }
                for (Item item : topics) {
                    TreeItem<Item> theme = new TreeItem<>(item);
                    sub.getChildren().add(theme);
                    rst = DB.Select("questions", "idTopic = " + theme.getValue().getIdOwn());
                    questions.clear();
                    while (rst.next()) {
                        setQuestions(new Item(rst.getInt("idTopic"),
                                rst.getInt("idQuestion"), "questions",
                                rst.getString("nameQuestion")));
                    }
                    for (Item question : questions) {
                        TreeItem<Item> quest = new TreeItem<>(question);
                        theme.getChildren().add(quest);
                    }
                }
            }
            treeView.setRoot(rootItem);
            treeView.setShowRoot(false);
            treeView.setEditable(true);
            treeView.setCellFactory((Callback<TreeView<Item>, TreeCell<Item>>) param -> new textFieldTreeCell());
        }
        catch (SQLException e){
            Alerts.Error(e.getMessage());
            return;
        }
    }
    private void setSubjects(Item item){
        subjects.add(item);
    }
    private void setThemes(Item item){
        topics.add(item);
    }
    private void setQuestions(Item item){
        questions.add(item);
    }

    public void onDelAnswerHandle(ActionEvent actionEvent) {
        MultipleSelectionModel<Answer> selection = tableView.getSelectionModel();
        DB.Delete("answers", "idAnswer = \""+selection.getSelectedItem().getIdAnswer()+"\"");
    }

    public void onSaveAnswerHandle(ActionEvent actionEvent) {
        if (taAnswer.getText().isEmpty() | taAnswer.getText().trim().equals("")){
            Alerts.Information("Не введен вариант овета",
                    "Для продолжения работы необходимо ввести вариант ответа");
            return;
        }else{
            MultipleSelectionModel<TreeItem<Item>> selection = treeView.getSelectionModel();
            selection.setSelectionMode(SelectionMode.SINGLE);
            int idQuestion = selection.getSelectedItem().getValue().getIdOwn();
            int isTrue = 0;
            DB.Insert("answers", "nameAnswer, idQuestion, isCorrect",
                    taAnswer.getText()+"\", \""+idQuestion+"\", \""+isTrue);
            ResultSet rst = DB.SelectMax("idAnswer", "answers");
            try {
                answers.add(new Answer(rst.getInt(1), idQuestion, taAnswer.getText(), false));
            }catch (SQLException e){
                Alerts.Error(e.getMessage());
            }
            taAnswer.clear();
        }
    }

    public void onTreeViewEntered(MouseEvent mouseEvent) {
        answers.clear();
        MultipleSelectionModel<TreeItem<Item>> selection = treeView.getSelectionModel();
        selection.setSelectionMode(SelectionMode.SINGLE);
        int level = treeView.getTreeItemLevel(selection.getSelectedItem());
        switch (level){
            case 3:
                int idQuestion = selection.getSelectedItem().getValue().getIdOwn();
                try{
                    ResultSet rst = DB.Select("questions", "idQuestion = \""+idQuestion+"\"");
                    taCorrectAnswer.setText(rst.getString("correctAnswer"));
                    rst = DB.Select("answers", "idQuestion = \""+idQuestion+"\"");
                    while (rst.next()) {
                        int idAnswer = rst.getInt("idAnswer");
                        String answerText = rst.getString("nameAnswer");
                        Boolean isTrue = false;
                        if (rst.getInt("isCorrect") != 0)
                            isTrue = true;
                        answers.add(new Answer(idAnswer, idQuestion, answerText, isTrue));
                    }
                }catch (SQLException e){
                    Alerts.Error(e.getMessage());
                }
                tableView.setItems(answers);
                tableView.setEditable(true);
                //------Редактирование строк в таблице----------------
                tcAnswer.setCellValueFactory(new PropertyValueFactory<>("Answer"));
                tcAnswer.setCellFactory(TextFieldTableCell.forTableColumn());
                tcAnswer.setOnEditCommit(event -> {
                    event.getTableView().getItems().get(event.getTablePosition().getRow()).setAnswer(event.getNewValue());
                    DB.Update("answers",
                            "nameAnswer = \""+event.getNewValue()+"\"",
                            "idAnswer = "+ event.getTableView().getItems().get(event.getTablePosition(
                            ).getRow()).getIdAnswer());
                });

                tcIsTrue.setCellValueFactory(param -> {
                    Answer answer = param.getValue();
                    SimpleBooleanProperty boolProp = new SimpleBooleanProperty(answer.isIsTrue());
                    boolProp.addListener((observable, oldValue, newValue) -> {
                        answer.setIsTrue(newValue);
                        int isTrue;
                        if (newValue) isTrue = 1;
                        else isTrue = 0;
                        DB.Update("answers", "isCorrect = \""+isTrue+"\"",
                                "idAnswer = \""+answer.getIdAnswer()+"\"");
                    });
                    return boolProp;
                });
                tcIsTrue.setCellFactory(param -> {
                    CheckBoxTableCell<Answer, Boolean> cell = new CheckBoxTableCell<>();
                    cell.setAlignment(Pos.CENTER);
                    return cell;
                });
                break;
        }
    }

    public void onSaveCorrectHandle(ActionEvent actionEvent) {
        MultipleSelectionModel<TreeItem<Item>> selection = treeView.getSelectionModel();
        selection.setSelectionMode(SelectionMode.SINGLE);
        if (selection.getSelectedItem() == null){
            Alerts.Information("Не выбран вопрос", "Для продолжения выберите вопрос");
            return;
        }
        if (taCorrectAnswer.getText().isEmpty() || taCorrectAnswer.getText().trim().equals("")){//Почему-то сыплется!
            Alerts.Information("Не введено пояснение к ответу",
                    "Для продолжения работы введите пояснение к ответу");
            return;
        }
        int idQuestion = selection.getSelectedItem().getValue().getIdOwn();
        DB.Update("questions", "correctAnswer =\""+taCorrectAnswer.getText()+"\"", "idQuestion = \""+idQuestion+"\"");
    }

    public void onAddTask(ActionEvent actionEvent) throws IOException {
        startNewWindow("FXML/TaskFrm.fxml", "Ввод задач");
    }

    public void onExportHandle(ActionEvent actionEvent) throws IOException{
        startNewWindow("FXML/Export.fxml", "Экспорт данных");
    }
    private void startNewWindow(String url, String titlt){
        Stage stage = (Stage)menuBar.getScene().getWindow();
        try {
            AnchorPane root = FXMLLoader.load(getClass().getResource(url));
            stage.setTitle(titlt);
            Scene scene = new Scene(root, 700, 500);
            stage.setScene(scene);
        } catch (IOException e){
            Alerts.Error(e.getMessage());
        }

    }

    public void onTestSetupHandle(ActionEvent actionEvent) throws IOException{
        startNewWindow("FXML/TestSetup.fxml", "Настройка теста");
    }

    public void onCopyHandle(ActionEvent actionEvent) {
        MultipleSelectionModel<TreeItem<Item>> selection = treeView.getSelectionModel();
        selection.setSelectionMode(SelectionMode.SINGLE);
        int level = treeView.getTreeItemLevel(selection.getSelectedItem());
        int idParent = selection.getSelectedItem().getValue().getIdParent();
        int idOwn = selection.getSelectedItem().getValue().getIdOwn();
        String itemName = selection.getSelectedItem().getValue().getName();
        switch (level){
            case 1:
                Alerts.Warning("Предмет не может быть скопирован", "Скопировать можно только тему или вопрос");
                break;
            case 2:
                treeView.setOnMousePressed(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        int targetLevel = treeView.getTreeItemLevel(selection.getSelectedItem());
                        switch(targetLevel){
                            case 1:
                                if (idParent == selection.getSelectedItem().getValue().getIdOwn())
                                    Alerts.Warning("Нельзя скопировать выбранную тему в предмет, которому она принадлежит",
                                            "Выберите другой предмет");
                                else {
                                    Optional<ButtonType> choise = Alerts.Confirmation("Вы хотите перенести тему \"" + itemName + "\" в предмет \"" +
                                            selection.getSelectedItem().getValue().getName() + "\"", "Пожалуйста, подтвердите");
                                    if (choise.get() == ButtonType.CANCEL){
                                        treeView.setOnMousePressed(MainWindow.this::onTreeViewEntered);
                                        break;
                                    }else {
                                        DB.Insert("topics",
                                                "idSub, nameTopic",
                                                selection.getSelectedItem().getValue().getIdOwn() + "\" ,\"" +
                                                        itemName);
                                        ResultSet rst = DB.Select("topics",
                                                "nameTopic = \"" + itemName + "\" AND idSub = \"" +
                                                        selection.getSelectedItem().getValue().getIdOwn() + "\"");
                                        try {
                                            selection.getSelectedItem().getChildren().add(new TreeItem<>(new Item(selection.getSelectedItem().getValue().getIdOwn(),
                                                    rst.getInt("idTopic"), "topics", itemName)));
                                        } catch (SQLException e) {
                                            Alerts.Error(e.getMessage());
                                        }
                                        treeView.setOnMousePressed(MainWindow.this::onTreeViewEntered);
                                    }
                                }
                                break;
                            case 2:
                                Alerts.Warning("Нельзя скопировать тему в другую тему!",
                                        "Выберите предмет для продолжения работы");
                                break;
                            case 3:
                                Alerts.Warning("Нельзя скопировать тему в вопрос!",
                                        "Выберите предмет для продолжения работы");
                                break;
                        }

                    }
                });
                break;
            case 3:
                treeView.setOnMousePressed(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        int targetLevel = treeView.getTreeItemLevel(selection.getSelectedItem());
                        switch (targetLevel){
                            case 1:
                                break;
                            case 2:
                                if (idParent == selection.getSelectedItem().getValue().getIdOwn()) {
                                    Alerts.Warning("Нельзя скопировать вопрос в тему, которой он принадлежит", "Выберите другую тему");
                                    break;
                                }else{
                                    Optional<ButtonType> choise = Alerts.Confirmation("Вы хотите скопировать вопрос \""+itemName+"\"\n в тему \""+
                                            selection.getSelectedItem().getValue().getName()+"\"?", "Пожалуйста, подтвердите");
                                    if (choise.get() == ButtonType.CANCEL) {
                                        treeView.setOnMousePressed(MainWindow.this::onTreeViewEntered);
                                        break;
                                    }else{
                                        try {
                                            ResultSet rst = DB.Select("questions", "idQuestion = \""+idOwn+"\"");
                                            DB.Insert("questions","idTopic, nameQuestion, correctAnswer",
                                                selection.getSelectedItem().getValue().getIdOwn()+"\", \""+itemName+"\", \""+
                                                        rst.getString("correctAnswer"));
                                            rst = DB.Select("questions", "nameQuestion = \""+itemName+"\" AND idTopic = \""+
                                                selection.getSelectedItem().getValue().getIdOwn()+"\"");

                                            int newQuestionId = rst.getInt("idQuestion");
                                            selection.getSelectedItem().getChildren().add(new TreeItem<>(new Item(selection.getSelectedItem().getValue().getIdOwn(),
                                                    newQuestionId, "questions", itemName)));
                                            rst = DB.Select("answers", "idQuestion = \""+idOwn+"\"");
                                            while (rst.next()){
                                                DB.Insert("answers", "idQuestion, nameAnswer, isCorrect",
                                                        newQuestionId+"\", \""+rst.getString("nameAnswer")+"\", \""+
                                                        rst.getInt("isCorrect"));
                                            }
                                        }catch (SQLException e){
                                            Alerts.Error(e.getMessage());
                                        }

                                    }
                                }
                                treeView.setOnMousePressed(MainWindow.this::onTreeViewEntered);
                                break;
                            case 3:
                                Alerts.Warning("Нельзя скопировать вопрос в вопрос", "Выберите тему для копирования");
                                break;
                                //----ПОДУМАТЬ НАД Alerts.Warning with Confirmation
                        }
                    }
                });
        }
    }

    public void onTransferHandle(ActionEvent actionEvent) {
        MultipleSelectionModel<TreeItem<Item>> selection = treeView.getSelectionModel();
        selection.setSelectionMode(SelectionMode.SINGLE);
        int level = treeView.getTreeItemLevel(selection.getSelectedItem());
        int itemId = selection.getSelectedItem().getValue().getIdOwn();
        int parentId = selection.getSelectedItem().getValue().getIdParent();
        String nameItem = selection.getSelectedItem().getValue().getName();
        switch (level){
            case 1:
                Alerts.Warning("Нельзя переместить предмет", "Для перемещения выберите тему или вопрос");
                break;
            case 2://-----УБРАТЬ - ДОБАВИТЬ в дерево
                treeView.setOnMousePressed(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        int newLevel = treeView.getTreeItemLevel(selection.getSelectedItem());
                        switch (newLevel){
                            case 1:
                                if (parentId == selection.getSelectedItem().getValue().getIdOwn()){
                                    Alerts.Warning("Нельзя перенести тему в предмет, частью которого она является",
                                            "Выберите другой предмет");
                                    break;
                                }else{
                                    Optional<ButtonType> choise = Alerts.Confirmation("Вы хотите перенести тему \""+nameItem+
                                            "\" в \n \""+selection.getSelectedItem().getValue().getName()+"\"", "Пожалуйста подтвердите");
                                    if (choise.get() == ButtonType.CANCEL){
                                        treeView.setOnMousePressed(MainWindow.this::onTreeViewEntered);
                                        break;
                                    }else{
                                        DB.Update("topics",
                                                "idSub = \""+selection.getSelectedItem().getValue().getIdOwn()+"\"",
                                                "idTopic = \""+itemId+"\"");
                                        treeView.setOnMousePressed(MainWindow.this::onTreeViewEntered);
                                    }
                                }
                                break;
                            case 2:
                                Alerts.Warning("Нельзя переместить тему в другую тему","Для перемещения темы выберите  предмет");
                                break;
                            case 3:
                                Alerts.Warning("Нельзя переместить тему в вопрос", "Для перемещения темы выберите предмет");
                                break;
                        }
                    }
                });
                break;
            case 3:
                int newQuestionLevel = treeView.getTreeItemLevel(selection.getSelectedItem());
                switch (newQuestionLevel){
                    case 1:
                        break;
                    case 2:
                        if (parentId == selection.getSelectedItem().getValue().getIdOwn() ){
                            Alerts.Warning("Нельзя переместить вопрос в тему, частью которой он является", "Выберите другую тему");
                        }else {
                            Optional<ButtonType> choise = Alerts.Confirmation("Вы хотите перенести вопрос \"" + nameItem +
                                    "\"\n в " + selection.getSelectedItem().getValue().getName() + "\"?", "Пожалуйста, подтвердите");
                            if (choise.get() == ButtonType.CANCEL){
                                treeView.setOnMousePressed(this::onTreeViewEntered);
                            }else{
                                DB.Update("questions", "idTopic = \""+selection.getSelectedItem().getValue().getIdOwn()+"\"",
                                        "idQuestion = \""+itemId+"\"");
                                selection.getSelectedItem().getChildren().add(new TreeItem<>(new Item(selection.getSelectedItem().getValue().getIdOwn(),
                                        itemId, "questions", nameItem)));
                                Alerts.Succeses("Вопрос успешно перенесен");
                                treeView.setOnMousePressed(this::onTreeViewEntered);
                            }
                        }
                        break;
                    case 3:
                        Alerts.Warning("Нельзя перенести вопрос в другой вопрос", "Для переноса выберите тему");
                        break;
                }
                break;
        }
    }
}
