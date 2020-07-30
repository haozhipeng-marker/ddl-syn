package com.firesoon.syn.bean;

public class JDBCDetailDTO {
    DataSourceDetailDTO dataSourceDetailDTO;
    String driverClass;
    String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public DataSourceDetailDTO getDataSourceDetailDTO() {
        return dataSourceDetailDTO;
    }

    public void setDataSourceDetailDTO(DataSourceDetailDTO dataSourceDetailDTO) {
        this.dataSourceDetailDTO = dataSourceDetailDTO;
    }

    public String getDriverClass() {
        return driverClass;
    }

    public void setDriverClass(String driverClass) {
        this.driverClass = driverClass;
    }
}
