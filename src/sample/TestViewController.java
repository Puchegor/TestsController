package sample;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import sample.Classes.Alerts;
import sample.Classes.Question;
import sample.Classes.Task;
import sample.Classes.TestData;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.ResourceBundle;

public class TestViewController implements Initializable {
    @FXML
    Button btnClose, btnSave;
    @FXML
    WebView webView;

    String printableTest;

    //public TestViewController(){}

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        WebEngine engine = webView.getEngine();
        engine.loadContent(makeTest(TestData.getQuestions()));
    }

    public void onCloseHandle(ActionEvent actionEvent) {
        Stage stage = (Stage)btnClose.getScene().getWindow();
        stage.close();
    }

    private String makeTest(ObservableList<Question> rawTest){
        boolean delquestion = true;
        if (TestData.getQuestions().size() < TestData.getNumVariants()*TestData.getNumQuestions()){
            Alerts.Warning("Вопросов в базе данных меньше, чем требуется для тестов. Возможны повторы вопросов.",
                    "Введите дополнительные вопросы в базу или подключите дополнительные темы, если необходимо избегать повторов.");
            delquestion = false;
        }
        String key = "";
        String [] asw = {"а) ", "б) ", "в) ", "г) ", "д) ", "е) "};
        printableTest = "<!doctype html><html lang-\"ru-BY\"><head><meta charset=\"UTF-8\">" +
                "<style>.column{" +
                "column-count: 2; column-gap: 30px;}" +
                "</style><style media = \"print\">" +
                "@page{size: landscape;}.test{page-break-after: always}" +
                "</style></head><body><div class=\"column\">";
        for (int u = 0; u < 4; u++)
            TestData.Shuffle();
        for (int i = 0; i < TestData.getNumVariants(); i++){
            int v = i+1;
            String varNum;
            if (TestData.isHiddenVariants())
                varNum = "__";
            else
                varNum = String.valueOf(v);
            printableTest += "<div class=\"test\"><p>Вариант № "+ varNum +"</p>";
            key += "<p>Вариант № "+v+"</p>";
            for (int k = 0; k < TestData.getNumQuestions(); k++){
                int nq = k+1;
                key +="<p>"+nq;
                printableTest += "<p><b>"+nq+". "+rawTest.get(0).getQuestion()+"</b></p>";
                int correct = -1;
                for (int a = 0; a < rawTest.get(0).getAnswers().size(); a++){
                    if (rawTest.get(0).getAnswers().get(a).isIsTrue())
                        correct = a;
                    printableTest += "<p>"+asw[a]+rawTest.get(0).getAnswers().get(a).getAnswer()+"</p>";
                }
                switch (correct){
                    case 0:
                        key += " a</p>";
                        break;
                    case 1:
                        key += " б</p>";
                        break;
                    case 2:
                        key += " в</p>";
                        break;
                    case 3:
                        key += " г</p>";
                        break;
                    case 4:
                        key += " д</p>";
                        break;
                    default:
                        key += "</p>";
                        break;
                }
                if (delquestion)
                    rawTest.remove(0);
                TestData.Shuffle();
            }
            //-----------Вставка задач-------------------------------------
            printableTest += "<br>";
            if (TestData.getTasks().size()!=0){
                int num = TestData.getNumTasks();
                for (int j = 0; j < num; j++)
                {
                    int taskNo = j+1;
                    printableTest += "<p><b>Задача № "+taskNo+" </b>";
                    ObservableList<Task> currTasks = TestData.getTasks().get(j);
                    Collections.shuffle(currTasks);
                    printableTest += TestData.getTasks().get(j).get(0)+"</p>";
                    key += "<p> Задача №"+taskNo+". "+TestData.getTasks().get(j).get(0).getAnswer()+"</p>";
                    TestData.getTasks().get(j).remove(0);
                }
            }
            printableTest+="</div>";
            for (int u = 0; u < 4; u++)
                TestData.Shuffle();
        }
        printableTest = printableTest + "<p>Ответы</p>" + key;
        printableTest += "</div></body></html>";

        return printableTest;
    }

    public void onSaveHandle(ActionEvent actionEvent) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Выберите имя файла");
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Файлы HTML", "*.html");
        chooser.getExtensionFilters().add(filter);
        chooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File testFile = chooser.showSaveDialog(btnSave.getScene().getWindow());
        if (testFile != null){
            try (FileWriter writer = new FileWriter(testFile, false)){
                writer.write(printableTest);
                writer.flush();
            }catch (IOException e){
                Alerts.Error(e.getMessage());
            }
        }
    }
}
