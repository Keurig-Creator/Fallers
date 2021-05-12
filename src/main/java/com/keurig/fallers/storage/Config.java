package com.keurig.fallers.storage;

import java.io.*;
import java.util.Properties;
import java.util.Set;

public class Config {

    private final Properties configProp = new Properties();

    private InputStream in;

    private Config() {
        //Private constructor to restrict new instances
        System.out.println("Reading all properties from the file");

        File file = new File("config.properties");

        if (!file.exists()) {
            in = getClass().getClassLoader().getResourceAsStream("config.properties");
        } else {
            try {
                in = new FileInputStream("config.properties");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        try {
            configProp.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Bill Pugh Solution for singleton pattern
    private static class LazyHolder {
        private static final Config INSTANCE = new Config();
    }

    public static Config getInstance() {
        return LazyHolder.INSTANCE;
    }

    public String getProperty(String key) {
        return configProp.getProperty(key);
    }

    public Set<String> getAllPropertyNames() {
        return configProp.stringPropertyNames();
    }

    public boolean containsKey(String key) {
        return configProp.containsKey(key);
    }
}
