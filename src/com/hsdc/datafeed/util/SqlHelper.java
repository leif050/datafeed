package com.hsdc.datafeed.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

import com.hsdc.datafeed.jdbc.JdbcUtil;

public final class SqlHelper {

	// ������Ҫ�ı���
	private Connection conn = null;
	private PreparedStatement ps = null;
	private ResultSet rs = null;

	static JdbcUtil jdbcUtil;
	
	static{
		jdbcUtil = JdbcUtil.getInstance();
	}

	// �÷���ִ��һ��update/delete/insert���
	// sql����Ǵ��ʺŵĸ�ʽ���磺update table_name set column_name = ? where ...
	// parameters = {"...", "..."...}��
	public void executeUpdate(String sql, String[] parameters) {

		try {
			conn = JdbcUtil.getConnection();
			ps = conn.prepareStatement(sql);
			// ������ֵ
			if (parameters != null) {
				for (int i = 0; i < parameters.length; i++) {
					ps.setString(i + 1, parameters[i]);
				}
			}
			// ִ�����
			ps.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		} finally {
			// �ر���Դ
			JdbcUtil.free(conn, ps, rs);
		}
	}

	// ����ִ�ж��update��delete��insert��䣨��������
	public void executeUpdate(String[] sqls, String[][] parameters) {
		try {
			// �õ�����
			conn = JdbcUtil.getConnection();
			// ���sql��䣬��������
			conn.setAutoCommit(false);

			for (int i = 0; i < sqls.length; i++) {
				if (parameters[i] != null) {
					ps = conn.prepareStatement(sqls[i]);

					for (int j = 0; j < parameters[i].length; j++) {
						ps.setString(j + 1, parameters[i][j]);
					}

					ps.executeUpdate();
				}

			}

			conn.commit();
		} catch (SQLException e) {
			// �ع�
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		} finally {
			JdbcUtil.free(conn, ps, rs);
		}
	}

	// ͳһ��select��䣬Ϊ���ܹ����ʽ�����������������ArrayList����������ֱ�ӹر���Դ
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ArrayList executeQuery(String sql, String[] parameters) {
		ArrayList results = new ArrayList();

		try {
			conn = JdbcUtil.getConnection();
			ps = conn.prepareStatement(sql);

			if (parameters != null) {
				for (int i = 0; i < parameters.length; i++) {
					ps.setString(i + 1, parameters[i]);
				}
			}

			rs = ps.executeQuery();

			ResultSetMetaData rsmd = rs.getMetaData();
			int column = rsmd.getColumnCount();

			while (rs.next()) {
				Object[] objects = new Object[column];

				for (int i = 1; i <= column; i++) {
					objects[i - 1] = rs.getObject(i);
				}

				results.add(objects);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		} finally {
			JdbcUtil.free(conn, ps, rs);
		}
		return results;
	}

}
