package sample.Controllers;

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

    public TestViewController(){}

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
        String key = "";
        String [] asw = {"а) ", "б) ", "в) ", "г) ", "д) ", "е) "};
        printableTest = "<!doctype html><html lang-\"ru-BY\"><head><meta charset=\"UTF-8\">" +
                "<style> ol {list-style-type: none;}"+
                "li::before {margin-right: 5px;}"+
                "li:nth-child(1)::before { content: 'а)'; }"+
                "li:nth-child(2)::before { content: 'б)'; }"+
                "li:nth-child(3)::before { content: 'в)'; }"+
                "li:nth-child(4)::before { content: 'г)'; }"+
                "li:nth-child(5)::before { content: 'д)'; }"+
                "li:nth-child(6)::before { content: 'е)'; }"+
                "</style>" +
                "<style>.column{" +
                "column-count: 2; column-gap: 30px;}" +
                "</style><style media = \"print\">" +
                "@page{size: landscape;}.test{page-break-after: always}" +
                "</style></head><body><div class=\"column\">";
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
                printableTest += "<p><b>"+nq+". "+rawTest.get(k).getQuestion()+"</b></p>";
                int correct = -1;
                for (int a = 0; a < rawTest.get(k).getAnswers().size(); a++){
                    if (rawTest.get(k).getAnswers().get(a).isIsTrue())
                        correct = a;
                    printableTest += "<p>"+asw[a]+rawTest.get(k).getAnswers().get(a).getAnswer()+"</p>";
                }
                switch (correct){
                    case 0:
                        key += "a</p>";
                        break;
                    case 1:
                        key += "б</p>";
                        break;
                    case 2:
                        key += "в</p>";
                        break;
                    case 3:
                        key += "г</p>";
                        break;
                    case 4:
                        key += "д</p>";
                        break;
                    default:
                        key += "</p>";
                        break;
                }
            }
            //-----------Вставка задач-------------------------------------
            printableTest += "<br>";
            for (int j = 0; j < TestData.getNumTasks(); j++)
            {
                int taskNo = j+1;
                printableTest += "<p><b>Задача № "+taskNo+" </b>";
                Collections.shuffle(TestData.getTasks().get(j));
                printableTest += TestData.getTasks().get(j).get(0)+"</p>";
                key += "<p> Задача №"+taskNo+". "+TestData.getTasks().get(j).get(0).getAnswer()+"</p>";
                TestData.getTasks().get(j).remove(0);
            }
            printableTest+="</div>";
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
