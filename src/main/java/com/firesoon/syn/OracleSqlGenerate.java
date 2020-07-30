package com.firesoon.syn;

import com.firesoon.syn.bean.*;
import com.firesoon.syn.factory.analyse.Analyser;
import com.firesoon.syn.factory.analyse.AnalyserFactory;
import com.firesoon.syn.factory.convert.TypeConvertFactory;
import com.firesoon.syn.utils.DBUrlUtil;
import com.firesoon.syn.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class OracleSqlGenerate {
    private static Logger logger;
    static List keyWordsList = new ArrayList();
    static {
        logger = LoggerFactory.getLogger(OracleSqlGenerate.class);
        keyWordsList.add("size");
        keyWordsList.add("type");
    }

    public static List<String> getOracleSql(JDBCDetailDTO JDBCDetailDTO, List<String> tableNameList, String dataSourceType) throws ClassNotFoundException, SQLException,InstantiationException, IllegalAccessException {
        logger.info("开始连接至源数据库");

        try {
            Connection sourceConnection = getSourceConnection(JDBCDetailDTO);
            logger.info("成功连接至源数据库");
            Analyser analyser = AnalyserFactory.getInstance(sourceConnection);
            DataBaseDefine dataBaseDefine = analyser.getDataBaseDefine(sourceConnection,tableNameList);
            logger.info("源数据库指定表结构结构定义获取完毕");
            logger.info("开始转换为目标数据库类型");
            String sourceDataBaseType = sourceConnection.getMetaData().getDatabaseProductName().toUpperCase();
            String targetDataBaseType = dataSourceType.toUpperCase();
            String convertType = sourceDataBaseType + "2" + targetDataBaseType;
            if (!sourceDataBaseType.equals(targetDataBaseType)||convertType.equals("ORACLE2ORACLE")) {
                logger.info("开始转换为目标数据库类型");
                TypeConvertFactory.getInstance(convertType).convert(dataBaseDefine);
                logger.info("目标数据库指定表类型转换完成");
            }
            return getCreateTableSql(dataBaseDefine.getTablesMap().values());
        }catch(Throwable e){
            throw e;
        }


    }

    public static boolean isChange(JDBCDetailDTO jdbcSourceDetailDTO, List<String> sourceTableNameList, JDBCDetailDTO jdbcTargetDetailDTO) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {

            try{
                logger.info("连接源数据库，进行指定表结构的解析");
                Connection sourceConnection = getSourceConnection(jdbcSourceDetailDTO);
                Analyser analyserSource = AnalyserFactory.getInstance(sourceConnection);
                DataBaseDefine dataBaseDefineSource = analyserSource.getDataBaseDefine(sourceConnection,sourceTableNameList);
                logger.info("连接目标数据库，进行指定表结构的解析");
                Connection targetConnection = getSourceConnection(jdbcTargetDetailDTO);
                Analyser analyserTarget = AnalyserFactory.getInstance(targetConnection);
                DataBaseDefine dataBaseDefineTarget = analyserTarget.getDataBaseDefine(targetConnection);
                logger.info("源数据库和目标数据库指定表结构结构定义获取完毕");
                logger.info("将源数据库指定表进型数据类型的转化");
                String sourceDataBaseType = sourceConnection.getMetaData().getDatabaseProductName();
                String targetDataBaseType = targetConnection.getMetaData().getDatabaseProductName();
                String convertType = sourceDataBaseType.toUpperCase()+"2"+targetDataBaseType.toUpperCase();
                if(!sourceConnection.equals(targetConnection)||convertType.equalsIgnoreCase("oracle2oracle")){
                    TypeConvertFactory.getInstance(convertType).convert(dataBaseDefineSource);
                    logger.info("目标数据库指定表类型转换完成");
                }
                Set<String> targetTableNames =dataBaseDefineTarget.getTablesMap().keySet();
                Collection<Table> sourceTableDefine = dataBaseDefineSource.getTablesMap().values();
                if(sourceTableDefine!=null&&!sourceTableDefine.isEmpty()){
                    for(Table sourceTable :sourceTableDefine){
                        String tableName = sourceTable.getTableName();
                        if(!targetTableNames.contains(tableName)){
                            logger.info("元素数据库不存在当前的表结构");
                            return true;
                        }else{
                            return getChange(sourceTable,dataBaseDefineTarget.getTablesMap().get(tableName));
                        }
                    }
                }
                return true;

            }catch(Throwable e){
                throw e;

            }

    }

    public static List<String> getChangeTableddl(JDBCDetailDTO jdbcSourceDetailDTO, List<String> sourceTableNameList, JDBCDetailDTO jdbcTargetDetailDTO,int dataSourceId) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        List<String> tableDDLList = new ArrayList<>();
        logger.info("连接源数据库");
        Connection sourceConnection = getSourceConnection(jdbcSourceDetailDTO);
        Analyser analyserSource = AnalyserFactory.getInstance(sourceConnection);
        DataBaseDefine dataBaseDefineSource = analyserSource.getDataBaseDefine(sourceConnection,sourceTableNameList);
        logger.info("获取源数据库指定表结构成功");
        logger.info("连接目标数据库");
        Connection targetConnection = getSourceConnection(jdbcTargetDetailDTO);
        Analyser analyserTarget = AnalyserFactory.getInstance(targetConnection);
        DataBaseDefine dataBaseDefineTarget = analyserTarget.getDataBaseDefine(targetConnection);
        logger.info("源数据库和目标数据库指定表结构结构定义获取完毕");
        logger.info("将源数据库指定表进型数据类型的转化");
        String sourceDataBaseType = sourceConnection.getMetaData().getDatabaseProductName();
        String targetDataBaseType = targetConnection.getMetaData().getDatabaseProductName();
        String convertType = sourceDataBaseType.toUpperCase()+"2"+targetDataBaseType.toUpperCase();
        if(!sourceConnection.equals(targetConnection)||convertType.equalsIgnoreCase("oracle2oracle")){
            TypeConvertFactory.getInstance(convertType).convert(dataBaseDefineSource);
            logger.info("目标数据库指定表类型转换完成");
        }


        Set<String> targetTableNames =dataBaseDefineTarget.getTablesMap().keySet();
        Collection<Table> sourceTableDefine = dataBaseDefineSource.getTablesMap().values();
        if(sourceTableDefine!=null&&!sourceTableDefine.isEmpty()){
            String tableDDL = "";

            for(Table sourceTable :sourceTableDefine){
                String tableName = sourceTable.getTableName()+"_"+dataSourceId;
                if(!targetTableNames.contains(tableName)){
                    logger.info("元素数据库不存在当前的表结构");
                    tableDDL = getTableDDL(sourceTable);
                    String[] sqls = tableDDL.split(";");
                    for (String singleSql : sqls) {
                        tableDDLList.add(singleSql);

                    }
                    logger.info("生成指定表的建表语句", tableName);

                }else{

                    return getModifiedColumnDDL(sourceTable,dataBaseDefineTarget.getTablesMap().get(tableName),jdbcTargetDetailDTO);
                }
            }
        }
            return tableDDLList;
    }

    public static void executeChangeTable(JDBCDetailDTO jdbcSourceDetailDTO, List<String> sourceTableNameList, JDBCDetailDTO jdbcTargetDetailDTO,int dataSourceId) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {

        logger.info("连接源数据库");
        Connection sourceConnection = getSourceConnection(jdbcSourceDetailDTO);
        Analyser analyserSource = AnalyserFactory.getInstance(sourceConnection);
        DataBaseDefine dataBaseDefineSource = analyserSource.getDataBaseDefine(sourceConnection,sourceTableNameList);
        logger.info("获取源数据库指定表结构成功");
        logger.info("连接目标数据库");
        Connection targetConnection = getSourceConnection(jdbcTargetDetailDTO);
        Analyser analyserTarget = AnalyserFactory.getInstance(targetConnection);
        DataBaseDefine dataBaseDefineTarget = analyserTarget.getDataBaseDefine(targetConnection);
        logger.info("源数据库和目标数据库指定表结构结构定义获取完毕");
        logger.info("将源数据库指定表进型数据类型的转化");
        String sourceDataBaseType = sourceConnection.getMetaData().getDatabaseProductName();
        String targetDataBaseType = targetConnection.getMetaData().getDatabaseProductName();
        String convertType = sourceDataBaseType.toUpperCase()+"2"+targetDataBaseType.toUpperCase();
        if(!sourceConnection.equals(targetConnection)||convertType.equalsIgnoreCase("oracle2oracle")){
            TypeConvertFactory.getInstance(convertType).convert(dataBaseDefineSource);
            logger.info("目标数据库指定表类型转换完成");
        }
        Set<String> targetTableNames =dataBaseDefineTarget.getTablesMap().keySet();
        Collection<Table> sourceTableDefine = dataBaseDefineSource.getTablesMap().values();
        if(sourceTableDefine!=null&&!sourceTableDefine.isEmpty()){
            String tableDDL = "";
            List<String> tableDDLList = new ArrayList<>();
            try(Statement statement = targetConnection.createStatement();){
                for(Table sourceTable :sourceTableDefine){
                   // String tableName = sourceTable.getTableName();
                    String tableName = sourceTable.getTableName()+"_"+dataSourceId;
                    if(!targetTableNames.contains(tableName)){
                        logger.info("元素数据库不存在当前的表结构");
                        tableDDL = getTableDDL(sourceTable);
                        String[] sqls = tableDDL.split(";");
                        for (String singleSql : sqls) {
                            statement.execute(singleSql);
                        }
                        logger.info("生成指定表的建表语句", tableName);
                    }else{
                        tableDDLList = getModifiedColumnDDL(sourceTable,dataBaseDefineTarget.getTablesMap().get(tableName),jdbcTargetDetailDTO);
                        for(String modifiedColumnDDL:tableDDLList){
                            if(modifiedColumnDDL.contains("BLOB")||modifiedColumnDDL.contains("CLOB")){
                                continue;
                            }
                            statement.execute(modifiedColumnDDL);
                            //logger.info("\"表{}字段修改成功,执行DDL：{}\", tableName, modifiedColumnDDL");
                        }
                    }
                }
            }catch(Throwable e){
                logger.error(String.format("表创建或修改失败，DDL：%s", StringUtil.isBlank(tableDDL) ? tableDDLList.toString() : tableDDL), e);
            }

        }

    }

    private static List<String> getModifiedColumnDDL(Table sourceTableDefine, Table targetTableDefine, JDBCDetailDTO jdbcTargetDetailDTO){
        String userName = jdbcTargetDetailDTO.getDataSourceDetailDTO().getUserName();
        StringBuilder stringBuilder = null;
        List<String> resultList = new LinkedList<>();
        List<String> primaryKeys = new ArrayList<>();
        if(targetTableDefine.getPrimaryKey() != null) {
            primaryKeys = targetTableDefine.getPrimaryKey().getColumns();
        }
        for(Column sourceColumn :sourceTableDefine.getColumns()){
            stringBuilder = new StringBuilder("");
            String columnName = sourceColumn.getColumnName();
            if(keyWordsList.contains(columnName)){
                columnName = columnName+"ot";
            }
            Column targetColumn =targetTableDefine.getColumnsMap().get(columnName);
            if(targetColumn==null){
                stringBuilder.append("ALTER TABLE ").append(userName).append(".").append(sourceTableDefine.getTableName()).append(" ADD (").append(columnName).append(" ").append(sourceColumn.getFinalConvertDataType())
                        .append(" ");
                if (!sourceColumn.isNullAble()) {
                    stringBuilder.append("NOT NULL");
                } else {
                    stringBuilder.append("NULL");
                }
                stringBuilder.append(")");

                if (!StringUtil.isBlank(sourceColumn.getColumnComment())) {
                    stringBuilder.append("@COMMENT ON COLUMN ").append(sourceTableDefine.getTableName()).append(".").append(columnName).append(" IS '").append(sourceColumn.getColumnComment()).append("'");
                }
            }else{
                 if((sourceColumn.getStrLength()!=null&&sourceColumn.getStrLength().equals(targetColumn.getStrLength()))||(sourceColumn.getPrecision()!=null&&sourceColumn.getPrecision().equals(targetColumn.getPrecision()))){
                     continue;
                }else{
                    //如果字段的数据类型没有发生改变，只是其长度发生改变
                    if(!sourceColumn.getDataType().equals(targetColumn.getDataType()) && !primaryKeys.contains(targetColumn.getColumnName())) {
                        logger.info(sourceTableDefine.getTableName()+"的"+columnName+""+"字段数据类型发生了变化");
                    }else{
                        stringBuilder.append("ALTER TABLE ").append(userName).append(".").append(sourceTableDefine.getTableName()).append(" MODIFY (").append(columnName).append(" ").append(sourceColumn.getFinalConvertDataType())
                                .append(")");

                    }
                    //if (!StringUtil.isBlank(sourceColumn.getColumnComment()) && !sourceColumn.getColumnComment().equals(targetColumn.getColumnComment())) {
                    //    if(!StringUtil.isBlank(sourceTableDefine.getTableName())){
                    //        stringBuilder.append("@COMMENT ON COLUMN ").append(sourceTableDefine.getTableName()).append(".").append(columnName).append(" IS '").append(sourceColumn.getColumnComment()).append("'");
                    //    }
                    //}
                }
            }
            if(!StringUtil.isBlank(stringBuilder.toString())) {
                String  str = stringBuilder.toString();
                if(str.contains("@")){
                    String[] str1 = str.split("@");
                    for(String s :str1){

                        resultList.add(s);
                    }
                }else{
                    resultList.add(stringBuilder.toString());
                }
            }
        }
        return resultList;
    }

    private static boolean getChange(Table sourceTableDefine,Table targetTableDefine ){
        List<String> primaryKeys = new ArrayList<>();
        if(targetTableDefine.getPrimaryKey() != null) {
            primaryKeys = targetTableDefine.getPrimaryKey().getColumns();
        }
            for(Column sourceColumn : sourceTableDefine.getColumns()){
                String columnName = sourceColumn.getColumnName();
                //Oracle不支持size和type的字段
                if(keyWordsList.contains(columnName)){
                    columnName = columnName+"ot";
                }
                Column targetColumn = targetTableDefine.getColumnsMap().get(columnName);
                if(targetColumn==null){
                    logger.info("有新的数据字段的变动");
                    return true;
                }else{
                    if(sourceColumn.equals(targetColumn)){
                        logger.info("数据表结构没有发生变化");
                        return false;
                    }else{
                        if(!sourceColumn.getDataType().equals(targetColumn.getDataType()) && !primaryKeys.contains(targetColumn.getColumnName())){
                        return true;
                        }else if(!StringUtil.isBlank(sourceColumn.getColumnComment()) && !sourceColumn.getColumnComment().equals(targetColumn.getColumnComment())){
                            return true;
                        }
                    }
                }
            }
            return true;
        }


    private  static List<String> getCreateTableSql(Collection<Table> sourceTableDefines){
        List<String> columnDDLList = new LinkedList<>();
        String tableDDL = null;

        if(sourceTableDefines!=null&&!sourceTableDefines.isEmpty()){
            for(Table sourceTable :sourceTableDefines){
                tableDDL = getTableDDL(sourceTable);
                String[] sqls = tableDDL.split(";");
                for(String singleSql:sqls){
                    columnDDLList.add(singleSql);
                }
            }
        }
        return columnDDLList;
    }

    private static  String getTableDDL(Table tableDefine) {
        StringBuilder stringBuilder = new StringBuilder("create table ");
        stringBuilder.append(tableDefine.getTableName()).append(" (");

        List<Column> columnList = tableDefine.getColumns();
        for (Column column : columnList) {
            stringBuilder.append(getColumnDefineDDL(column));
            stringBuilder.append(",");
        }

        PrimaryKey primaryKey = tableDefine.getPrimaryKey();
        if (primaryKey != null) {
            stringBuilder.append(getPrimaryKeyDefineDDL(primaryKey)).append(",");
        }

        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        stringBuilder.append(");");
        stringBuilder.append(getCommentDefineDDL(tableDefine));

        return stringBuilder.toString().toLowerCase();
    }

    /**
     * 生成主键定义的DDL语句
     *
     * @param primaryKey 主键定义
     * @return DDL语句
     */
    private static String getPrimaryKeyDefineDDL(PrimaryKey primaryKey) {
        StringBuilder stringBuilder = new StringBuilder("");
        //fix bug for oracle table name length more than 30
        String pkname = primaryKey.getPkName();
        if (!StringUtil.isBlank(pkname)) {
            if(pkname.length()>30 ){
                stringBuilder.append("constraint ").append(pkname.substring(0,20)).append(" primary key(");
            }else{
                stringBuilder.append("constraint ").append(pkname).append(" primary key(");
            }
        } else {
            stringBuilder.append("primary key(");
        }
        List<String> columnNames = primaryKey.getColumns();
        for (String columnName : columnNames) {
            stringBuilder.append(columnName).append(",");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        stringBuilder.append(")");

        return stringBuilder.toString();
    }

    /**
     * 生成字段定义的DDL语句
     *
     * @param column 字段定义
     * @return DDL语句
     */
    private static String getColumnDefineDDL(Column column) {
        String columnName = column.getColumnName();
        if(keyWordsList.contains(columnName)){
            columnName = columnName+"ot";
        }
        StringBuilder stringBuilder = new StringBuilder(columnName);

        String type = column.getFinalConvertDataType();

        stringBuilder.append(" ");
        stringBuilder.append(type);
        if (!column.isNullAble()) {
            stringBuilder.append(" ").append("not null");
        }

        // 暂时注释掉默认值
        if (column.hasDefault() && column.getDefaultDefine().contains("now")) {
            stringBuilder.append(" default ").append("CURRENT_TIMESTAMP");
        }

        return stringBuilder.toString();
    }

    private static String getCommentDefineDDL(Table tableDefine) {
        StringBuilder stringBuilder = new StringBuilder("");
        List<Column> columns = tableDefine.getColumns();
        for (Column column : columns) {
            String columnName = column.getColumnName();
            if(keyWordsList.contains(columnName)){
                columnName = columnName+"ot";
            }
            if (!StringUtil.isBlank(column.getColumnComment())) {
                stringBuilder.append("COMMENT ON COLUMN ").append(tableDefine.getTableName()).append(".").append(columnName).append(" IS '").append(column.getColumnComment()).append("';");
//				stringBuilder.append("COMMENT ON COLUMN ").append(columnName).append(" IS '").append(column.getColumnComment()).append("';");
            }
        }
        if (!StringUtil.isBlank(tableDefine.getTableComment())) {
            stringBuilder.append("COMMENT ON TABLE ").append(tableDefine.getTableName()).append(" IS '").append(tableDefine.getTableComment()).append("'");
        }
        return stringBuilder.toString();
    }

    private static  Connection getSourceConnection(JDBCDetailDTO JDBCDetailDTO)throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException{
        String driverClass = JDBCDetailDTO.getDriverClass();
        String databaseName = JDBCDetailDTO.getDataSourceDetailDTO().getDatabase();
        String ip = JDBCDetailDTO.getDataSourceDetailDTO().getHost();
        String port = JDBCDetailDTO.getDataSourceDetailDTO().getPort();
        String databaseType = JDBCDetailDTO.getDataSourceDetailDTO().getType();
        String userName = JDBCDetailDTO.getDataSourceDetailDTO().getUserName();
        String password = JDBCDetailDTO.getDataSourceDetailDTO().getPassword();
        String url = JDBCDetailDTO.getUrl();
        if(StringUtils.isEmpty(url)){
            url = DBUrlUtil.generateDataBaseUrl(databaseName, ip, port, databaseType);
        }

        return getConnection(driverClass, url, userName, password);
    }

    private static Connection getConnection(String driverClass, String url, String userName, String password)
            throws ClassNotFoundException, IllegalAccessException, InstantiationException, SQLException {
        Class.forName(driverClass).newInstance();
        return DriverManager.getConnection(url, userName, password);
    }



}
