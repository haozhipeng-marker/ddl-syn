package com.firesoon.syn.factory.convert.impl;
import com.firesoon.syn.bean.Column;
import com.firesoon.syn.factory.convert.BaseTypeConverter;
import java.util.Map;


/**
 * Mysql至PostgreSQL的数据类型转换
 *
 * @author Roger
 */
public class MySQL2PostgreSQLTypeConverter extends BaseTypeConverter {

    public MySQL2PostgreSQLTypeConverter(Map<String, String> typeMapping,Map<String, String> typeProperties) {
        super(typeMapping,typeProperties);
    }



    @Override
    public String convert(Column column) {
        return typeMapping.get(column.getColumnType().toUpperCase());
    }




}
