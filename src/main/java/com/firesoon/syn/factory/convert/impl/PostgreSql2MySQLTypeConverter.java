package com.firesoon.syn.factory.convert.impl;

import com.firesoon.syn.bean.Column;
import com.firesoon.syn.factory.convert.BaseTypeConverter;
import java.util.Map;



/**
 *@ClassName PostgreSql2MySQLTypeConverter
 *@Description TODO
 *@author Roger
 *@Date 2019-12-29 20:58
 *@Version
 **/
public class PostgreSql2MySQLTypeConverter extends BaseTypeConverter {
    public PostgreSql2MySQLTypeConverter(Map<String, String> typeMapping,Map<String, String> typeProperties) {
        super(typeMapping,typeProperties);
    }

    @Override
    public String convert(Column column) {
        return typeMapping.get(column.getColumnType().toUpperCase());
    }


}
