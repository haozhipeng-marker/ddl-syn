package com.firesoon.syn.bean;

import com.alibaba.fastjson.JSONObject;

public class DataSourceDetailDTO {
    private Object principal;
    private String note;
    private String database;
    private String password;
    private JSONObject other;
    private String port;
    private String name;
    private String host;
    private String type;
    private String userName;

    public DataSourceDetailDTO() {

    }

    public Object getPrincipal() {
        return principal;
    }

    public void setPrincipal(Object principal) {
        this.principal = principal;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public JSONObject getOther() {
        return other;
    }

    public void setOther(JSONObject other) {
        this.other = other;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public DataSourceDetailDTO(Object principal, String note, String database, String password, JSONObject other, String port, String name, String host, String type, String userName) {
        this.principal = principal;
        this.note = note;
        this.database = database;
        this.password = password;
        this.other = other;
        this.port = port;
        this.name = name;
        this.host = host;
        this.type = type;
        this.userName = userName;

    }


}
