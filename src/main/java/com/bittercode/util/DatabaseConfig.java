package com.bittercode.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

class DatabaseConfig {

    static Properties prop = new Properties();
    static {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream input = classLoader.getResourceAsStream("application.properties");

        try {
            prop.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Allow properties to be overriden with global properties passed via JAVA_OPTS or similar
        Set<String> sysProperties = System.getProperties().stringPropertyNames();
        for (String key: sysProperties) {
            if (key.startsWith("db.")) {
                prop.setProperty(key, System.getProperty(key));
            }
        }
    }

    public final static String DRIVER_NAME = prop.getProperty("db.driver");
    public final static String DB_HOST = prop.getProperty("db.host");
    public final static String DB_PORT = prop.getProperty("db.port");
    public final static String DB_NAME = prop.getProperty("db.name");
    public final static String DB_USER_NAME = prop.getProperty("db.username");
    public final static String DB_PASSWORD = prop.getProperty("db.password");

    // Allow overriding the CONNECTION_STRING using the environment variable DB_URL
    public final static String CONNECTION_STRING = 
        System.getenv().containsKey("DB_URL") ? System.getenv("DB_URL") : (DB_HOST + ":" + DB_PORT + "/" + DB_NAME);

}
