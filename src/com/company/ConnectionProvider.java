package com.company;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Created by Александр on 17.07.2016.
 */
public class ConnectionProvider {

    public static final String APP_CONF_PATH = System.getProperty("user.dir") + File.separator + "configuration" + File.separator + "application.conf";
    private static ConnectionProvider provider = new ConnectionProvider();
    private AppConfig appConfig;

    private ConnectionProvider() {
    }

    private AppConfig loadCfg() {
        Properties defaults = new Properties();
        defaults.put(AppConfig.DB_URL_Key, "jdbc:sqlserver://ALEX-PC;instance=test_database;databaseName=test_database");
        defaults.put(AppConfig.DB_USER_KEY, "test");
        defaults.put(AppConfig.DB_USER_PWD_KEY, "test");

        Properties props = new Properties(defaults);
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(APP_CONF_PATH);
            props.load(fileInputStream);
//            Path path = Paths.get("/configuration/application.conf");
            System.out.println("Loaded app config from \'" + APP_CONF_PATH + "\'");
        } catch (Throwable e ) {
            System.out.println("Failed to load config:" + e.getMessage() + ". Will use defaults.");
        } finally {
            Utils.closeQuietly(fileInputStream);
        }

        return new AppConfig(props.getProperty(AppConfig.DB_URL_Key),
                props.getProperty(AppConfig.DB_USER_KEY),
                props.getProperty(AppConfig.DB_USER_PWD_KEY));
    }

    public Connection getConnection() {
        if (appConfig == null) {
            appConfig = loadCfg();
        }

        Connection connection = null;
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            connection = DriverManager.getConnection(appConfig.getDbConnectString(), appConfig.getDbUserid(), appConfig.getDbPassword());
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

        return connection;
    }

    public boolean updateConfig(AppConfig updated) throws IOException {
        boolean savedSuccessfully = save(updated);
        appConfig = updated;
        System.out.println("Start using updated config:" + appConfig);
        return savedSuccessfully;
    }

    public boolean save(AppConfig config) throws IOException {
        FileOutputStream out = null;
        try {
            Path cfgFile = Paths.get(APP_CONF_PATH);
            if (!Files.exists(cfgFile)) {
                Files.createDirectories(cfgFile.getParent());
                Files.createFile(cfgFile);
            }
            out = new FileOutputStream(APP_CONF_PATH);
            config.toProperies().store(out, null);
            System.out.println("Saved app config to \'" + APP_CONF_PATH + "\'");
        } finally {
            Utils.closeQuietly(out);
        }
        return true;
    }

    public AppConfig getAppConfig() {
        if (appConfig == null) {
            appConfig = loadCfg();
        }
        return appConfig.copy();
    }

    public static ConnectionProvider get() {
        return provider;
    }

    public boolean check(AppConfig cfg) throws Throwable {
        if (Utils.isEmpty(cfg.getDbConnectString()) || Utils.isEmpty(cfg.getDbUserid())){
            String msg;
            if (Utils.isEmpty(cfg.getDbConnectString())) {
                msg = "Адрес БД не заполнен.";
            } else {
                msg = "Имя пользователя не заполнено.";
            }
            throw new RuntimeException(msg);
        }

        Connection connection = null;
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            connection = DriverManager.getConnection(cfg.getDbConnectString(), cfg.getDbUserid(), cfg.getDbPassword());
            connection.prepareStatement("select * from for_update").executeQuery();
        } finally {
            Utils.closeQuietly(connection);
        }
        return true;
    }
}
