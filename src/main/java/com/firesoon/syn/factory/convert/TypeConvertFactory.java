package com.firesoon.syn.factory.convert;

import com.firesoon.syn.consts.ConvertType;
import com.firesoon.syn.consts.DataBaseTypeProperties;
import com.firesoon.syn.factory.convert.impl.MySQL2PostgreSQLTypeConverter;
import com.firesoon.syn.factory.convert.impl.PostgreSql2MySQLTypeConverter;
import com.firesoon.syn.factory.convert.impl.SqlServer2OracleSQLTypeConverter;
import com.firesoon.syn.utils.JsonUtil;
import com.firesoon.syn.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.firesoon.syn.factory.convert.impl.Oracle2PostgreSQLTypeConverter;

import java.awt.*;
import java.util.Map;

/**
 * 转换工厂类，用于转化数据类型，方法等
 *
 * @author Roger
 */
public final class TypeConvertFactory {

	private static Logger logger = LoggerFactory.getLogger(TypeConvertFactory.class);

	/**
	 * 无需实例化
	 */
	private TypeConvertFactory() {
	}

	public static BaseTypeConverter getInstance(String convertType) {
		BaseTypeConverter typeConverter = null;
		Map<String, String> mapping = null;
		Map<String, String> typeProperties = null;
		try {
			Map<String, Map<String, String>> typeMapping = JsonUtil.readJsonData("TypeMapping.json");
			if (ConvertType.MYSQL2POSTGRESQL.equals(convertType)) {
				mapping = typeMapping.get("mysql2pg");
				typeProperties = StringUtil.str2Map(DataBaseTypeProperties.POSTGRE_TYPE_SCALA);
				typeConverter = new MySQL2PostgreSQLTypeConverter(mapping, typeProperties);
			} else if (ConvertType.POSTGRESQL2MYSQL.equals(convertType)) {
				mapping = typeMapping.get("pg2mysql");
				typeProperties = StringUtil.str2Map(DataBaseTypeProperties.MYSQL_TYPE_SCALA);
				typeConverter = new PostgreSql2MySQLTypeConverter(mapping, typeProperties);
			} else if (ConvertType.ORACLE2POSTGRESQL.equals(convertType)) {
				mapping = typeMapping.get("oracle2pg");
				typeProperties = StringUtil.str2Map(DataBaseTypeProperties.POSTGRE_TYPE_SCALA);
				typeConverter = new Oracle2PostgreSQLTypeConverter(mapping, typeProperties);
			} else if(ConvertType.POSTGRESQL2ORACLE.equals(convertType)) {
				mapping = typeMapping.get("pg2oracle");
				typeProperties = StringUtil.str2Map(DataBaseTypeProperties.ORACLE_TYPE_SCALA);
				typeConverter = new Oracle2PostgreSQLTypeConverter(mapping, typeProperties);
			} else if(ConvertType.MYSQL2ORACLE.equals(convertType)) {
				mapping = typeMapping.get("mysql2oracle");
				typeProperties = StringUtil.str2Map(DataBaseTypeProperties.ORACLE_TYPE_SCALA);
				typeConverter = new Oracle2PostgreSQLTypeConverter(mapping, typeProperties);
			}else if(ConvertType.SQLSERVER2ORACLE.equals(convertType)){
				mapping = typeMapping.get("sqlserver2oracle");
				typeProperties = StringUtil.str2Map(DataBaseTypeProperties.ORACLE_TYPE_SCALA);
				typeConverter = new SqlServer2OracleSQLTypeConverter(mapping, typeProperties);
			}
			else {
				throw new IllegalArgumentException(String.format("无法识别的数据库类型：%s", convertType));
			}
		} catch (Exception e) {
			logger.error("读取TypeMapping.json文件并初始化转换器出现异常");
		}
		return typeConverter;
	}
}
