package sample.Classes;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.Optional;
import java.util.Properties;

public class Config {
    public static final File config = new File ("config.ini");
    public static Properties prop = new Properties();

    public static String configRead() throws IOException{
        if(!config.exists())
            config.createNewFile();
        prop.load(new FileInputStream(config));
        prop.getProperty("DBFileName");
        if(prop.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Внимание");
            alert.setHeaderText("Файл базы данных не найден. ");
            alert.setContentText("Вы можете создать новый файл или найти на диске сущетствующий");
            ButtonType btnNew = new ButtonType("Новый");
            ButtonType btnFind = new ButtonType("Найти");
            ButtonType btnCancel = new ButtonType("Отменить");
            alert.getButtonTypes().setAll(btnNew, btnFind, btnCancel);
            Optional<ButtonType> choise = alert.showAndWait();
            if (choise.get() == btnNew){
                writeProperty("DBFileName", "test.sqlite3");
                return "test.sqlite3";
            }
            if (choise.get()==btnFind){
                FileChooser chooser = new FileChooser();
                chooser.setTitle("Выберите файл базы данных");
                //chooser.setInitialDirectory(config);
                FileChooser.ExtensionFilter dbfilter = new FileChooser.ExtensionFilter("Файлы sqlite3","*.sqlite", "*.sqlite3","*.db");
                FileChooser.ExtensionFilter allfiles = new FileChooser.ExtensionFilter("Все файлы", "*.*");
                chooser.getExtensionFilters().setAll(dbfilter,allfiles);
                File dbfile = chooser.showOpenDialog(new Stage());
                if (dbfile != null){
                    writeProperty("DBFileName", dbfile.toString());
                }
            }
            if (choise.get()==btnCancel)
                alert.close();
        }
        return (prop.getProperty("DBFileName"));
    }

    public static void writeProperty (String key, String data)throws IOException{
        prop.setProperty(key,data);
        prop.store(new FileOutputStream(config),null);
    }
}
