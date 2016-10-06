package com.hsdc.datafeed.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Properties;
import java.util.Vector;

import org.apache.commons.io.IOUtils;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import org.slf4j.Logger;  
import org.slf4j.LoggerFactory;  
  
/** 
 * sftp工具。注意：构造方法有两个：分别是基于密码认证、基于秘钥认证。 
 *  
 * @see http://xliangwu.iteye.com/blog/1499764 
 * @author Somnus 
 */  
public class SFTPUtil {  
	private transient Logger log = LoggerFactory.getLogger(this.getClass());      
    private ChannelSftp sftp;  
      
    private Session session;  
    /** FTP 登录用户名*/  
    private String username;   
    /** FTP 登录密码*/  
    private String password;  
    /** 私钥文件的路径*/  
    private String keyFilePath;  
    /** FTP 服务器地址IP地址*/  
    private String host;  
    /** FTP 端口*/  
    private int port;  
      
  
    /** 
     * 构造基于密码认证的sftp对象 
     * @param userName 
     * @param password 
     * @param host 
     * @param port 
     */  
    public SFTPUtil(String username, String password, String host, int port) {  
        this.username = username;  
        this.password = password;  
        this.host = host;  
        this.port = port;  
    }  
  
    /** 
     * 构造基于秘钥认证的sftp对象 
     * @param userName 
     * @param host 
     * @param port 
     * @param keyFilePath 
     */  
    public SFTPUtil(String username, String host, int port, String keyFilePath) {  
        this.username = username;  
        this.host = host;  
        this.port = port;  
        this.keyFilePath = keyFilePath;  
    }  
  
    public SFTPUtil(){}  
  
    /** 
     * 连接sftp服务器 
     *  
     * @throws Exception 
     */  
    public void login(){  
        try {  
            JSch jsch = new JSch();  
            if (keyFilePath != null) {  
                jsch.addIdentity(keyFilePath);// 设置私钥  
                log.info("sftp connect,path of private key file：{}" , keyFilePath);  
            }  
            log.info("sftp connect by host:{} username:{}",host,username);  
  
            session = jsch.getSession(username, host, port);  
            log.info("Session is build");  
            if (password != null) {  
                session.setPassword(password);  
            }  
            Properties config = new Properties();  
            config.put("StrictHostKeyChecking", "no");  
              
            session.setConfig(config);  
            session.connect();  
            log.info("Session is connected");  
              
            Channel channel = session.openChannel("sftp");  
            channel.connect();  
            log.info("channel is connected");  
  
            sftp = (ChannelSftp) channel;  
            log.info(String.format("sftp server host:[%s] port:[%s] is connect successfull", host, port));  
        } catch (JSchException e) {  
            log.error("Cannot connect to specified sftp server : {}:{} \n Exception message is: {}", new Object[]{host, port, e.getMessage()});  
            throw new RuntimeException(e);  
        }  
    }  
  
    /** 
     * 关闭连接 server 
     */  
    public void logout(){  
        if (sftp != null) {  
            if (sftp.isConnected()) {  
                sftp.disconnect();  
                log.info("sftp is closed already");  
            }  
        }  
        if (session != null) {  
            if (session.isConnected()) {  
                session.disconnect();  
                log.info("sshSession is closed already");  
            }  
        }  
    }  
  
    /** 
     * 将输入流的数据上传到sftp作为文件 
     *  
     * @param directory 
     *            上传到该目录 
     * @param sftpFileName 
     *            sftp端文件名 
     * @param in 
     *            输入流 
     * @throws Exception 
     */  
    public void upload(String directory, String sftpFileName, InputStream input){  
        try {  
            sftp.cd(directory);  
        } catch (SftpException e) {  
            log.error(e.getMessage(), e);  
            log.warn("directory is not exist");  
            try {  
                sftp.mkdir(directory);  
                sftp.cd(directory);  
            } catch (SftpException e2) {  
                log.error(e2.getMessage(), e2);  
                throw new RuntimeException(e2);  
            }  
        }  
        try {  
            sftp.put(input, sftpFileName);  
            log.info("file:{} is upload successful" , sftpFileName);  
        } catch (SftpException e) {  
            log.error(e.getMessage(), e);  
            throw new RuntimeException(e);  
        }  
    }  
  
    /** 
     * 上传单个文件 
     *  
     * @param directory 
     *            上传到sftp目录 
     * @param uploadFile 
     *            要上传的文件,包括路径 
     * @throws FileNotFoundException  
     * @throws Exception 
     */  
    public void upload(String directory, String uploadFile) throws FileNotFoundException{  
        File file = new File(uploadFile);  
        upload(directory, file.getName(), new FileInputStream(file));  
    }  
  
    /** 
     * 将byte[]上传到sftp，作为文件。注意:从String生成byte[]是，要指定字符集。 
     *  
     * @param directory 
     *            上传到sftp目录 
     * @param sftpFileName 
     *            文件在sftp端的命名 
     * @param byteArr 
     *            要上传的字节数组 
     * @throws Exception 
     */  
    public void upload(String directory, String sftpFileName, byte[] byteArr){  
        upload(directory, sftpFileName, new ByteArrayInputStream(byteArr));  
    }  
  
    /** 
     * 将字符串按照指定的字符编码上传到sftp 
     *  
     * @param directory 
     *            上传到sftp目录 
     * @param sftpFileName 
     *            文件在sftp端的命名 
     * @param dataStr 
     *            待上传的数据 
     * @param charsetName 
     *            sftp上的文件，按该字符编码保存 
     * @throws UnsupportedEncodingException  
     * @throws Exception 
     */  
    public void upload(String directory, String sftpFileName, String dataStr, String charsetName) throws UnsupportedEncodingException{  
        upload(directory, sftpFileName, new ByteArrayInputStream(dataStr.getBytes(charsetName)));  
  
    }  
  
    /** 
     * 下载文件 
     *  
     * @param directory 
     *            下载目录 
     * @param downloadFile 
     *            下载的文件 
     * @param saveFile 
     *            存在本地的路径 
     * @throws Exception 
     */  
    public void download(String directory, String downloadFile, String saveFile){  
        try {  
            if (directory != null && !"".equals(directory)) {  
                sftp.cd(directory);  
            }  
            File file = new File(saveFile);  
            sftp.get(downloadFile, new FileOutputStream(file));  
            log.info("file:{} is download successful" , downloadFile);  
        } catch (FileNotFoundException e) {  
            log.error(e.getMessage(), e);  
            throw new RuntimeException(e);  
        } catch (SftpException e) {  
            log.error(e.getMessage(), e);  
            throw new RuntimeException(e);  
        }  
    }  
    /** 
     * 下载文件 
     * @param directory 下载目录 
     * @param downloadFile 下载的文件名 
     * @return 字节数组 
     * @throws Exception 
     */  
    public byte[] download(String directory, String downloadFile){  
        byte[] fileData = null;  
        try {  
            if (directory != null && !"".equals(directory)) {  
                sftp.cd(directory);  
            }  
            InputStream is = sftp.get(downloadFile);  
              
            fileData = IOUtils.toByteArray(is);  
              
            log.info("file:{} is download successful" , downloadFile);  
        } catch (SftpException e) {  
            log.error(e.getMessage(), e);  
            throw new RuntimeException(e);  
        } catch (IOException e) {  
            log.error(e.getMessage(), e);  
            throw new RuntimeException(e);  
        }  
        return fileData;  
    }  
  
    /** 
     * 删除文件 
     *  
     * @param directory 
     *            要删除文件所在目录 
     * @param deleteFile 
     *            要删除的文件 
     * @throws Exception 
     */  
    public void delete(String directory, String deleteFile){  
        try {  
            sftp.cd(directory);  
            sftp.rm(deleteFile);  
        } catch (SftpException e) {  
            log.error(e.getMessage(), e);  
            throw new RuntimeException(e);  
        }  
    }  
  
    /** 
     * 列出目录下的文件 
     *  
     * @param directory 
     *            要列出的目录 
     * @param sftp 
     * @return 
     * @throws SftpException 
     */  
    public Vector<?> listFiles(String directory) throws SftpException {  
        return sftp.ls(directory);  
    }  
      
    public static void main(String[] args) {  
        SFTPUtil sftp = new SFTPUtil("177779259", "123456", "101.231.206.140", 21121);  
        sftp.login();  
        byte[] buff = sftp.download("./download", "abc.jar");  
        System.out.println(Arrays.toString(buff));  
        sftp.logout();  
    }  
      
}  
