package server;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

//для получения конфигурации сервера
public class Configuration {
    //конфигурационный файл по умолчанию
    private String confFilePath = "server/src/main/resources/server.properties";
    private FileInputStream fis;
    private Properties prop;

    public Configuration() {
        this.prop = new Properties();
        try {
            fis = new FileInputStream(this.confFilePath);
            prop.load(fis);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Configuration(String confFilePath) {
        this.confFilePath = confFilePath;
        try {
            fis = new FileInputStream(this.confFilePath);
            prop.load(fis);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getConfFilePath() {
        return confFilePath;
    }

    public void setConfFilePath(String confFilePath) {
        this.confFilePath = confFilePath;
    }

    public String getProperty(String name) {
        return this.prop.getProperty(name);
    }
}
