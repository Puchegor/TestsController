package sample.Classes;

public class Task {
    private int idTopic;
    private int idTask;
    private String nameTask;
    private String Answer;

    public Task (int idTopic, int idTask, String nameTask,String Answer){
        this.idTopic = idTopic;
        this.idTask = idTask;
        this.nameTask = nameTask;
        this.Answer = Answer;
    }

    public int getIdTopic() {
        return idTopic;
    }

    public int getIdTask() {
        return idTask;
    }

    public String getNameTask() {
        return nameTask;
    }

    public String getAnswer() {
        return Answer;
    }

    @Override
    public String toString() {
        return getNameTask();
    }
}
