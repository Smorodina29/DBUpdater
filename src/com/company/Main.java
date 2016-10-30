package com.company;

public class Main {

    public static void main(String[] args) {
        MSSQLServer connServer = new MSSQLServer();
//        connServer.dbConnect("jdbc:sqlserver:HeiDB_AON.spb.local;databaseName=Hei_DB_TEST", "DevLog", "123QAZwsx/*-");
        connServer.dbConnect("jdbc:sqlserver://ALEX-PC;instance=test_database;databaseName=test_database", "test", "test");
    }
}
