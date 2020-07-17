package com.firesoon.syn.factory.analyse.impl;

import com.firesoon.syn.bean.Column;
import com.firesoon.syn.bean.DataBaseDefine;
import com.firesoon.syn.bean.PrimaryKey;
import com.firesoon.syn.bean.Table;
import com.firesoon.syn.consts.DataBaseType;
import com.firesoon.syn.factory.analyse.Analyser;
import com.firesoon.syn.utils.StringUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SqlServerSqlAnalyser extends Analyser {

    /**
     * 构造方法
     *
     * @param connection 数据库连接
     */
    public SqlServerSqlAnalyser(Connection connection) {
        super(connection);
    }

    @Override
    protected List<PrimaryKey> getPrimaryKeyDefines(Connection connection, String catalog, String schema) {
        String sql = "SELECT a.name AS 'table_name',\n" +
                "  e.name AS 'column_name'\n" +
                " FROM sysobjects AS a         \n" +
                " LEFT JOIN sysobjects AS b\n" +
                "  ON a.id=b.parent_obj \n" +
                " LEFT JOIN sysindexes AS c\n" +
                "  ON a.id=c.id AND b.name=c.name\n" +
                " LEFT JOIN sysindexkeys AS d \n" +
                "  ON a.id=d.id AND c.indid=d.indid\n" +
                " LEFT JOIN syscolumns AS e \n" +
                "  ON a.id=e.id AND d.colid=e.colid\n" +
                "WHERE a.xtype='U' \n" +
                "  AND b.xtype='PK'";
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<PrimaryKey> primaryKeyList = new ArrayList<>();
        try {
            preparedStatement = connection.prepareStatement(sql);
            //preparedStatement.setString(1, CONSTRAINT_NAME_PRIMARY_KEY);
            resultSet = preparedStatement.executeQuery();

            PrimaryKey primaryKey = null;
            Map<String, PrimaryKey> primaryKeyMap = new HashMap<>();
            String tableName = null;
            while (resultSet.next()) {
                tableName = resultSet.getString("table_name").toLowerCase();
                primaryKey = primaryKeyMap.get(tableName);
                if (primaryKey == null) {
                    primaryKey = new PrimaryKey();
                    primaryKey.setTableName(tableName);

                    primaryKeyList.add(primaryKey);
                    primaryKeyMap.put(tableName, primaryKey);
                }
                primaryKey.addColumn(resultSet.getString("column_name").toLowerCase());
            }
        } catch (Throwable e) {
            throw new RuntimeException("获取Oracle表主键定义失败", e);
        } finally {
            this.releaseResources(preparedStatement, resultSet);
        }
        return primaryKeyList;
    }

    @Override
    protected List<Column> getColumnDefines(Connection connection, String catalog, String schema) {
        String sql = "SELECT table_name, column_name, cast(value as varchar(500)) AS column_comment ,ordinal_position, column_default, is_nullable, data_type, character_maximum_length, numeric_precision, numeric_scale FROM information_schema.columns A  JOIN sysobjects B ON A.TABLE_NAME = B.NAME LEFT JOIN sysproperties C ON C.id=B.id and C.smallid = A.ORDINAL_POSITION WHERE Xtype = 'u' and table_catalog = ? order by table_name, ordinal_position";

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<Column> columnList = new ArrayList<>();
        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, catalog);
            resultSet = preparedStatement.executeQuery();

            Column column = null;
            while (resultSet.next()) {
                column =this.recordColumn(resultSet);
                columnList.add(column);
            }
        } catch (Throwable e) {
            throw new RuntimeException("获取sqlserver表字段定义失败", e);
        } finally {
            this.releaseResources(preparedStatement, resultSet);
        }

        return columnList;
    }

    private Column recordColumn(ResultSet resultSet) throws SQLException {
        Column column = new Column();
        column.setDataBaseType(DataBaseType.SQLSERVER);
        column.setTableName(resultSet.getString("table_name").toLowerCase());
        column.setColumnName(resultSet.getString("column_name").toLowerCase());
        column.setColumnComment(resultSet.getString("column_comment"));
        column.setColumnOrder(resultSet.getInt("ordinal_position"));
        column.setDefaultDefine(resultSet.getString("column_default"));
        column.setNullAble(!"NO".equalsIgnoreCase(resultSet.getString("is_nullable")));
       // column.setColumnType(resultSet.getString("column_type"));
       // column.setColumnKey(resultSet.getString("column_key"));
        //column.setExtra(resultSet.getString("extra"));
        column.setDataType(resultSet.getString("data_type"));

        if (column.notTextType() && column.notBlobType() && column.notClobType()) {
            if (resultSet.getObject("numeric_precision") != null) {
                column.setPrecision(resultSet.getInt("numeric_precision"));
            } else if (resultSet.getObject("character_maximum_length") != null) {
                column.setStrLength(resultSet.getInt("character_maximum_length"));
            }

            if (resultSet.getObject("numeric_scale") != null) {
                column.setScale(resultSet.getInt("numeric_scale"));
            }
        }

        return column;
    }

    @Override
    protected DataBaseDefine getDataBaseDefines(Connection connection) {
        DataBaseDefine dataBaseDefine = new DataBaseDefine();
        try {
            dataBaseDefine.setCatalog(connection.getCatalog());

        } catch (Throwable e) {
            throw new RuntimeException("获取SQLSERVER库定义失败", e);
        } finally {

        }

        return dataBaseDefine;
    }

    @Override
    protected List<Table> getTableDefines(Connection connection, String catalog, String schema) {


        String sql = "SELECT name FROM sysobjects Where xtype='U' ORDER BY name";
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<Table> tableList = new ArrayList<>();
        try {
            preparedStatement = connection.prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Table table = new Table();
                table.setTableName(resultSet.getString("name").toLowerCase());
                //table.setTableComment(resultSet.getString("comments"));
                tableList.add(table);
            }
        } catch (Throwable e) {
            throw new RuntimeException("获取Oracle表定义失败", e);
        } finally {
            this.releaseResources(preparedStatement, resultSet);
        }

        return tableList;
    }
}
