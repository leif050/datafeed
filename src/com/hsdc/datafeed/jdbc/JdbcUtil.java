package com.hsdc.datafeed.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public final class JdbcUtil {

	// 连接数据库的参数
	private static String url = null;
	private static String user = null;
	private static String driver = null;
	private static String password = null;

	private JdbcUtil() {

	}

	private static JdbcUtil instance = null;

	public static JdbcUtil getInstance() {
		if (instance == null) {
			synchronized (JdbcUtil.class) {
				if (instance == null) {
					instance = new JdbcUtil();
				}

			}
		}

		return instance;
	}

	// 配置文件
//	private static Properties prop = new Properties();

	// 注册驱动
//	static {
//		try {
//			// 利用类加载器读取配置文件
//			InputStream is = JdbcUtil.class.getClassLoader()
//					.getResourceAsStream("db.properties");
//			prop.load(is);
//			url = prop.getProperty("url");
//			user = prop.getProperty("user");
//			driver = prop.getProperty("driver");
//			password = prop.getProperty("password");
//
//			Class.forName(driver);
//
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

	// 该方法获得连接
	public Connection getConnection() throws SQLException {
		return DriverManager.getConnection(url, user, password);
	}

	// 释放资源
	public void free(Connection conn, Statement st, ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {

				e.printStackTrace();
			} finally {
				if (st != null) {
					try {
						st.close();
					} catch (SQLException e) {

						e.printStackTrace();
					} finally {
						if (conn != null) {
							try {
								conn.close();
							} catch (SQLException e) {

								e.printStackTrace();
							}
						}
					}
				}
			}
		}
	}

	public static String getUrl() {
		return url;
	}

	public static void setUrl(String url) {
		JdbcUtil.url = url;
	}

	public static String getUser() {
		return user;
	}

	public static void setUser(String user) {
		JdbcUtil.user = user;
	}

	public static String getDriver() {
		return driver;
	}

	public static void setDriver(String driver) {
		JdbcUtil.driver = driver;
	}

	public static String getPassword() {
		return password;
	}

	public static void setPassword(String password) {
		JdbcUtil.password = password;
	}

}
