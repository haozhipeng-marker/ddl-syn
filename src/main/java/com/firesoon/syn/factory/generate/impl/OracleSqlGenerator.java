package com.firesoon.syn.factory.generate.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.firesoon.syn.bean.Column;
import com.firesoon.syn.bean.DBSettings;
import com.firesoon.syn.bean.DataBaseDefine;
import com.firesoon.syn.bean.PrimaryKey;
import com.firesoon.syn.bean.Table;
import com.firesoon.syn.factory.generate.Generator;
import com.firesoon.syn.utils.StringUtil;
import com.mysql.cj.util.StringUtils;

public class OracleSqlGenerator extends Generator {
	static List keyWordsList = new ArrayList();

	//临时测试使用，mysql表列名可以使用 size type ，但是在oracle里面是关键字
	static {
		keyWordsList.add("size");
		keyWordsList.add("type");
	}
	public OracleSqlGenerator(Connection connection, DataBaseDefine sourceDataBaseDefine, DBSettings targetDBSettings) {
		super(connection, sourceDataBaseDefine, targetDBSettings);
	}

	@Override
	protected String getDataBaseDDL(DataBaseDefine dataBaseDefine) {
		String sql = "select * from all_users where username = ?";
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		boolean flag = false;
		try {
			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, dataBaseDefine.getCatalog().toUpperCase());
			resultSet = preparedStatement.executeQuery();
			if(resultSet.next()) {
				flag = true;
			}
		} catch (Throwable e) {
			throw new RuntimeException("获取Oracle表定义失败", e);
		} finally {
			this.releaseResources(preparedStatement, resultSet);
		}
		StringBuilder stringBuilder = new StringBuilder("");
		if (!flag) {
			// Oracle创建账号、密码相同的账号
//			stringBuilder.append("CREATE USER ").append(dataBaseDefine.getCatalog()).append(" IDENTIFIED BY " + dataBaseDefine.getCatalog() + ";");
		}

		return stringBuilder.toString();
	}

	@Override
	protected String getTableDDL(Table tableDefine) {
		StringBuilder stringBuilder = new StringBuilder("create table ");
		stringBuilder.append(tableDefine.getTableName()).append(" (");

		List<Column> columnList = tableDefine.getColumns();
		for (Column column : columnList) {
			stringBuilder.append(this.getColumnDefineDDL(column));
			stringBuilder.append(",");
		}

		PrimaryKey primaryKey = tableDefine.getPrimaryKey();
		if (primaryKey != null) {
			stringBuilder.append(this.getPrimaryKeyDefineDDL(primaryKey)).append(",");
		}

		stringBuilder.deleteCharAt(stringBuilder.length() - 1);
		stringBuilder.append(");");
		stringBuilder.append(this.getCommentDefineDDL(tableDefine));

		return stringBuilder.toString().toLowerCase();
	}
	
	/**
	 * 生成主键定义的DDL语句
	 * 
	 * @param primaryKey 主键定义
	 * @return DDL语句
	 */
	private String getPrimaryKeyDefineDDL(PrimaryKey primaryKey) {
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
	private String getColumnDefineDDL(Column column) {
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
	
	private String getCommentDefineDDL(Table tableDefine) {
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

	@Override
	//增加了targetDBSettings 用来在新增字段的时候shame的选择，
	protected List<String>  getModifiedColumnDDL(Table sourceTableDefine, Table targetTableDefine,DBSettings targetDBSettings) {
		StringBuilder stringBuilder = null;
		List<String> resultList = new LinkedList<>();
		List<String> primaryKeys = new ArrayList<>();
		if(targetTableDefine.getPrimaryKey() != null) {
			primaryKeys = targetTableDefine.getPrimaryKey().getColumns();
		}
		for (Column sourceColumn : sourceTableDefine.getColumns()) {
			stringBuilder = new StringBuilder("");
			String columnName = sourceColumn.getColumnName();
			//Oracle不支持size和type的字段
			if(keyWordsList.contains(columnName)){
				columnName = columnName+"ot";
			}
			Column targetColumn = targetTableDefine.getColumnsMap().get(columnName);
			if (targetColumn == null) {
				// 字段不存在直接添加，在表结构的前面增加了shame，并在sql语句的最后去掉了";"

				stringBuilder.append("ALTER TABLE ").append(targetDBSettings.getUserName()).append(".").append(sourceTableDefine.getTableName()).append(" ADD (").append(columnName).append(" ").append(sourceColumn.getFinalConvertDataType())
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
			} else {
				if (sourceColumn.equals(targetColumn)) {
					continue;
				} else {
					// 由于不同数据库类型转换后与实际查询的类型存在不一致，导致不应该修改类型的字段也会再次执行类型修改操作，表数据量大时影响性能，暂关闭类型修改功能
					if(!sourceColumn.getDataType().equals(targetColumn.getDataType()) && !primaryKeys.contains(targetColumn.getColumnName())) {
						stringBuilder.append("ALTER TABLE ").append(targetDBSettings.getUserName()).append(".").append(sourceTableDefine.getTableName()).append(" MODIFY (").append(columnName).append(" ").append(sourceColumn.getFinalConvertDataType())
						.append(")");
					}

					if (!StringUtil.isBlank(sourceColumn.getColumnComment()) && !sourceColumn.getColumnComment().equals(targetColumn.getColumnComment())) {
						if(!StringUtil.isBlank(sourceDataBaseDefine.getTableName())){
							stringBuilder.append("@COMMENT ON COLUMN ").append(sourceDataBaseDefine.getTableName()).append(".").append(columnName).append(" IS '").append(sourceColumn.getColumnComment()).append("'");
						}

					}

					/*if(!sourceColumn.isNullAble() == targetColumn.isNullAble() && !targetColumn.isNullAble()) {
						// 删除非空校验
						stringBuilder.append("ALTER TABLE ").append(sourceTableDefine.getTableName()).append(" MODIFY ").append(columnName).append(" null;");
					}*/
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

}
