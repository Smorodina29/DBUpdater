package com.company;

import java.util.Properties;

/**
 * Created by Александр on 02.05.2017.
 */
public class AppConfig {

    public static final String DB_URL_Key = "dburl";
    public static final String DB_USER_KEY = "user";
    public static final String DB_USER_PWD_KEY = "pwd";


    private String dbUrl;
    private String dbUser;
    private String dbUserPwd;

    public AppConfig(String dbUrl, String dbUser, String dbUserPwd) {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbUserPwd = dbUserPwd;
    }

    public String getDbUserid() {
        return dbUser;
    }

    public String getDbPassword() {
        return dbUserPwd;
    }

    public String getDbConnectString() {
        return dbUrl;
    }

    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    public void setDbUserPwd(String dbUserPwd) {
        this.dbUserPwd = dbUserPwd;
    }

    public Properties toProperies() {
        Properties props = new Properties();
        props.put(DB_URL_Key, getDbConnectString());
        props.put(DB_USER_KEY, getDbUserid());
        props.put(DB_USER_PWD_KEY, getDbPassword());
        return props;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AppConfig appConfig = (AppConfig) o;

        if (dbUrl != null ? !dbUrl.equals(appConfig.dbUrl) : appConfig.dbUrl != null) return false;
        if (dbUser != null ? !dbUser.equals(appConfig.dbUser) : appConfig.dbUser != null) return false;
        return dbUserPwd != null ? dbUserPwd.equals(appConfig.dbUserPwd) : appConfig.dbUserPwd == null;

    }

    @Override
    public int hashCode() {
        int result = dbUrl != null ? dbUrl.hashCode() : 0;
        result = 31 * result + (dbUser != null ? dbUser.hashCode() : 0);
        result = 31 * result + (dbUserPwd != null ? dbUserPwd.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AppConfig{" +
                "dbUrl='" + dbUrl + '\'' +
                ", dbUser='" + dbUser + '\'' +
                ", dbUserPwd='" + dbUserPwd + '\'' +
                '}';
    }

    public AppConfig copy() {
        return new AppConfig(getDbConnectString(), getDbUserid(), getDbPassword());
    }
}
