package sample.Classes;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Answer {

    private int idAnswer;
    private int idQuestion;
    private StringProperty Answer = new SimpleStringProperty();
    private BooleanProperty isTrue = new SimpleBooleanProperty();
    //---------------Constructor----------------------
    public Answer(int idAnswer, int idQuestion, String Answer, Boolean isTrue){
         this.idAnswer =idAnswer;
         this.idQuestion = idQuestion;
         this.Answer.setValue(Answer);
         this.isTrue.setValue(isTrue);
    }
    //---------------Setters-------------------------

    public void setIdAnswer(int idAnswer) {
        this.idAnswer = idAnswer;
    }

    public void setIdQuestion(int idQuestion) {
        this.idQuestion = idQuestion;
    }

    public void setIsTrue(boolean isTrue) {
        this.isTrue.set(isTrue);
    }

    public void setAnswer(String answer) {
        this.Answer.set(answer);
    }
    //--------------Getters-------------------------

    public int getIdAnswer() {
        return idAnswer;
    }

    public int getIdQuestion() {
        return idQuestion;
    }

    @SuppressWarnings("WeakerAccess")
    public String getAnswer() {
        return Answer.get();
    }

    public boolean isIsTrue() {
        return isTrue.get();
    }
    //---------------ToString-------------------------

    @Override
    public String toString() {
        return getAnswer();
    }
}
