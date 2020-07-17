package com.firesoon.syn.factory.convert.impl;

import java.util.Map;

import com.firesoon.syn.bean.Column;
import com.firesoon.syn.factory.convert.BaseTypeConverter;

/**
 * PostgreSQL至Oracle的数据类型转换
 *
 * @author Roger
 */
public class PostgreSQL2OracleTypeConverter extends BaseTypeConverter {

	public PostgreSQL2OracleTypeConverter(Map<String, String> typeMapping, Map<String, String> typeProperties) {
		super(typeMapping, typeProperties);
	}

	@Override
	public String convert(Column column) {
		return typeMapping.get(column.getColumnType().toUpperCase());
	}

}
