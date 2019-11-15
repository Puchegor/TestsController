package sample.Classes;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.Collections;

public class TestData {
    private static ObservableList<Question>questions = FXCollections.observableArrayList();
    private static ObservableList<Task>tasks = FXCollections.observableArrayList();
    private static boolean hiddenVariants;
    private static int numVariants;
    private static int numQuestions;
    private static int numTasks;
    private static ArrayList<Integer> topicIDs = new ArrayList<>();

    public TestData(ObservableList<Question>questions, boolean hiddenVariants, int numVariants, int numQuestions){
        TestData.questions = questions;
        TestData.hiddenVariants = hiddenVariants;
        TestData.numVariants = numVariants;
        TestData.numQuestions = numQuestions;
    }

    public static void setQuestions(ObservableList<Question> questions) {
        TestData.questions = questions;
    }

    public static void setTasks(ObservableList<Task> tasks) {
        TestData.tasks = tasks;
    }

    public static void setHiddenVariants(boolean hiddenVariants) {
        TestData.hiddenVariants = hiddenVariants;
    }

    public static void setNumVariants(int numVariants) {
        TestData.numVariants = numVariants;
    }

    public static void setNumQuestions(int numQuestions) {
        TestData.numQuestions = numQuestions;
    }

    public static void setNumTasks(int numTasks) {
        TestData.numTasks = numTasks;
    }

    public static ObservableList<Question> getQuestions() {
        return questions;
    }

    public static ObservableList<Task> getTasks() {
        return tasks;
    }

    public static boolean isHiddenVariants() {
        return hiddenVariants;
    }

    public static int getNumVariants() {
        return numVariants;
    }

    public static int getNumQuestions() {
        return numQuestions;
    }

    public static int getNumTasks() {
        return numTasks;
    }

    public static void setTopicIDs(ArrayList<Integer> topicIDs) {
        TestData.topicIDs = topicIDs;
    }

    public static ArrayList<Integer> getTopicIDs() {
        return topicIDs;
    }

    public static void Shuffle(){
        Collections.shuffle(questions);
    }
}
