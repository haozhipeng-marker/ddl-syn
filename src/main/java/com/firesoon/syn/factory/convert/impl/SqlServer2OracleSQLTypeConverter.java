package com.firesoon.syn.factory.convert.impl;

import com.firesoon.syn.bean.Column;
import com.firesoon.syn.factory.convert.BaseTypeConverter;

import java.util.Map;

public class SqlServer2OracleSQLTypeConverter extends BaseTypeConverter {

    public SqlServer2OracleSQLTypeConverter(Map<String, String> typeMapping, Map<String, String> typeProperties) {
        super(typeMapping,typeProperties);
    }



    @Override
    public String convert(Column column) {
        return typeMapping.get(column.getColumnType().toUpperCase());
    }
}
