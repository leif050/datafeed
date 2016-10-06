package com.hsdc.datafeed;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.log4j.Logger;

import com.hsdc.datafeed.exception.ConfigurationException;
import com.hsdc.datafeed.jdbc.JdbcUtil;
import com.hsdc.datafeed.jdbc.RowMapper;
import com.hsdc.datafeed.jdbc.SimpleJdbc;
import com.hsdc.datafeed.util.TarUtils;

public class AppMain {
	private static Logger logger = Logger.getLogger(AppMain.class);
	static String csvOutPutDir;
	static String tarOutPutDir;
	static String fileEncode;
	static Properties prop = new Properties();

	// public static void main(String[] args) {
	// //connect db
	// //exec sql1
	// //thread1 write csv
	// //exec sql2
	// //thread2 write csv
	// //exec sql3
	// //thread3 write csv
	//
	// //
	//
	//
	//
	//
	// }

	public static void main(String[] args) throws IOException,
			ConfigurationException {
		// 判断args参数个数
		if (args == null || args.length != 1) {
			throw new IllegalArgumentException(
					"please add arg file:config.properties path.");
		}
		String filePath = args[0];

		File cfgFile = new File(filePath);

		if (!cfgFile.isFile()) {
			throw new ConfigurationException("filePath:" + filePath
					+ " is not exists.");
		}

		JdbcUtil jdbcUtil = JdbcUtil.getInstance();
		// 指定2个配置文件为参数
		try {
			// 利用类加载器读取配置文件
			InputStream is = new FileInputStream(filePath);
			prop.load(is);
			JdbcUtil.setUrl(prop.getProperty("url"));
			JdbcUtil.setUser(prop.getProperty("user"));
			JdbcUtil.setPassword(prop.getProperty("password"));
			JdbcUtil.setDriver(prop.getProperty("driver"));

			csvOutPutDir = prop.getProperty("csvOutPutDir");
			tarOutPutDir = prop.getProperty("tarOutPutDir");
			fileEncode = prop.getProperty("fileEncode");
		} catch (IOException e) {
			e.printStackTrace();
		}

		// check and create dir
		File outPutDir = new File(csvOutPutDir);
		if (!outPutDir.exists()) {
			System.out.println("dir not exists create dir...");
			outPutDir.mkdir();
		} else {
			System.out.println("dir exists");
		}

		SimpleJdbc jdbc = new SimpleJdbc();
		List<User> list = null;
		try {
			list = (List<User>) jdbc.queryForBean("select * from user",
					new RowMapper<User>() {
						User user = null;

						@Override
						public User mapRow(ResultSet rs) throws SQLException {
							user = new User();
							user.setId(rs.getInt("id"));
							user.setName(rs.getString("name"));
							return user;
						}
					});
		} catch (SQLException e1) {
			logger.error("query error." + e1);
		}
		for (User user : list) {
			System.out.println(user.getId() + "---" + user.getName());
		}

		// output to csv
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		String dateStr = dateFormat.format(new Date());

		File accountFile = new File(
				csvOutPutDir +File.separator+ "account" + dateStr + ".csv");
		if (accountFile.exists()) {
			System.out.println("delete accountFile.");
			accountFile.delete();
		}
		FileOutputStream fos = null;
		OutputStreamWriter osw = null;
		try {
			fos = new FileOutputStream(accountFile);
			osw = new OutputStreamWriter(fos, fileEncode);
			osw.write("header...\n");
			for (User user : list) {
				String line = user.getId() + "," + user.getName() + "\n";
				osw.write(line);
			}
			osw.write("tailer..." + "," + list.size()+"\n");
			osw.flush();

		} catch(IOException ioe){
			logger.error("write file error.", ioe);
			
		}finally{
			osw.close();
			fos.close();
		}
		
		File entilementFile = new File(
				csvOutPutDir +File.separator+ "entilement" + dateStr + ".csv");
		if (entilementFile.exists()) {
			System.out.println("delete entilementFile.");
			entilementFile.delete();
		}
//		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(entilementFile);
			osw = new OutputStreamWriter(fos, fileEncode);
			osw.write("header...\n");
			for (User user : list) {
				String line = user.getId() + "," + user.getName() + "\n";
				osw.write(line);
			}
			osw.write("tailer..." + "," + list.size()+"\n");
			osw.flush();

		} catch(IOException ioe){
			logger.error("write file error.", ioe);
		}finally{
			osw.close();
			fos.close();
		}
		
		//tar
		try {
			TarArchiveOutputStream taos = new TarArchiveOutputStream(
					new FileOutputStream(new File(tarOutPutDir+"/"+dateStr+".tar")));
			
			TarUtils.archiveFile(accountFile, taos, "");
			taos.flush();
			
			TarUtils.archiveFile(entilementFile, taos, "");
			taos.flush();
			
			taos.close();

		} catch (Exception e) {
			logger.error("archive failed.", e);
			//send mail?
		}
		
		//send via sftp
		
	}

}
