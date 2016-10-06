package com.hsdc.datafeed.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface RowMapper<T> {
	public abstract T mapRow(ResultSet rs) throws SQLException;
}
