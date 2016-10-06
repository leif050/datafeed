package com.hsdc.datafeed.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

//import javax.sql.DataSource;

public interface JdbcOperation {
	public abstract int execute(String sql, Object[] params) throws SQLException;
	public abstract int execute(String sql) throws SQLException;  
	public abstract int executeBatch(String sql, List<Object[]> params) throws SQLException;
	public abstract int executeBatch(String sql) throws SQLException;
	public abstract ResultSet queryForResultSet(String sql, Object[] params) throws SQLException;
	public abstract ResultSet queryForResultSet(String sql) throws SQLException; 
	public abstract List<?> queryForBean(String sql, Object[] params, RowMapper<?> mapper) throws SQLException;  
	public abstract List<?> queryForBean(String sql, RowMapper<?> mapper) throws SQLException;  
	public abstract List<Map<String, Object>> queryForMap(String sql, Object[] params) throws SQLException;  
	public abstract List<Map<String, Object>> queryForMap(String sql) throws SQLException;  
	public abstract int queryForInt(String sql, Object[] params) throws SQLException;
	public abstract int queryForInt(String sql) throws SQLException;
	public abstract void free(Connection x); 
	public abstract void free(Statement x);  
	public abstract void free(PreparedStatement x);  
	public abstract void free(ResultSet x);  
//	public abstract void setDataSource(DataSource dataSource);  
	public abstract Connection getConnection();  
	public Connection getConnection(boolean autoCommit);  
}
