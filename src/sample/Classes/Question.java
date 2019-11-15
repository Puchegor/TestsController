package sample.Classes;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Question {
    private int idQuestion;
    private int idTopic;
    private StringProperty Question = new SimpleStringProperty();
    private ObservableList<Answer> Answers = FXCollections.observableArrayList();

    //------Constructor--------------------
    public Question(int idQuestion, int idTopic, String Question, ObservableList<Answer> answers) {
        this.idQuestion = idQuestion;
        this.idTopic = idTopic;
        assert this.Question != null;
        this.Question.set(Question);
        this.Answers.setAll(answers);
    }

    //------Setters-------------------------


    public void setIdQuestion(int idQuestion) {
        this.idQuestion = idQuestion;
    }

    public void setIdTopic(int idTopic) {
        this.idTopic = idTopic;
    }

    public void setQuestion(String question) {
        this.Question.set(question);
    }

    public void setAnswers(ObservableList<Answer> answers) {
        Answers = answers;
    }
    //-------Getters-----------------

    public int getIdQuestion() {
        return idQuestion;
    }

    public int getIdTopic() {
        return idTopic;
    }

    public String getQuestion() {
        return Question.get();
    }

    public ObservableList<Answer> getAnswers() {
        return Answers;
    }
    //-----ToString----------------

    @Override
    public String toString() {
        return getQuestion();
    }
}