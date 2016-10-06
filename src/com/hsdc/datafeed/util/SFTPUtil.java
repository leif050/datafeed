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
 * sftp���ߡ�ע�⣺���췽�����������ֱ��ǻ���������֤��������Կ��֤�� 
 *  
 * @see http://xliangwu.iteye.com/blog/1499764 
 * @author Somnus 
 */  
public class SFTPUtil {  
	private transient Logger log = LoggerFactory.getLogger(this.getClass());      
    private ChannelSftp sftp;  
      
    private Session session;  
    /** FTP ��¼�û���*/  
    private String username;   
    /** FTP ��¼����*/  
    private String password;  
    /** ˽Կ�ļ���·��*/  
    private String keyFilePath;  
    /** FTP ��������ַIP��ַ*/  
    private String host;  
    /** FTP �˿�*/  
    private int port;  
      
  
    /** 
     * �������������֤��sftp���� 
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
     * ���������Կ��֤��sftp���� 
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
     * ����sftp������ 
     *  
     * @throws Exception 
     */  
    public void login(){  
        try {  
            JSch jsch = new JSch();  
            if (keyFilePath != null) {  
                jsch.addIdentity(keyFilePath);// ����˽Կ  
                log.info("sftp connect,path of private key file��{}" , keyFilePath);  
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
     * �ر����� server 
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
     * ���������������ϴ���sftp��Ϊ�ļ� 
     *  
     * @param directory 
     *            �ϴ�����Ŀ¼ 
     * @param sftpFileName 
     *            sftp���ļ��� 
     * @param in 
     *            ������ 
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
     * �ϴ������ļ� 
     *  
     * @param directory 
     *            �ϴ���sftpĿ¼ 
     * @param uploadFile 
     *            Ҫ�ϴ����ļ�,����·�� 
     * @throws FileNotFoundException  
     * @throws Exception 
     */  
    public void upload(String directory, String uploadFile) throws FileNotFoundException{  
        File file = new File(uploadFile);  
        upload(directory, file.getName(), new FileInputStream(file));  
    }  
  
    /** 
     * ��byte[]�ϴ���sftp����Ϊ�ļ���ע��:��String����byte[]�ǣ�Ҫָ���ַ����� 
     *  
     * @param directory 
     *            �ϴ���sftpĿ¼ 
     * @param sftpFileName 
     *            �ļ���sftp�˵����� 
     * @param byteArr 
     *            Ҫ�ϴ����ֽ����� 
     * @throws Exception 
     */  
    public void upload(String directory, String sftpFileName, byte[] byteArr){  
        upload(directory, sftpFileName, new ByteArrayInputStream(byteArr));  
    }  
  
    /** 
     * ���ַ�������ָ�����ַ������ϴ���sftp 
     *  
     * @param directory 
     *            �ϴ���sftpĿ¼ 
     * @param sftpFileName 
     *            �ļ���sftp�˵����� 
     * @param dataStr 
     *            ���ϴ������� 
     * @param charsetName 
     *            sftp�ϵ��ļ��������ַ����뱣�� 
     * @throws UnsupportedEncodingException  
     * @throws Exception 
     */  
    public void upload(String directory, String sftpFileName, String dataStr, String charsetName) throws UnsupportedEncodingException{  
        upload(directory, sftpFileName, new ByteArrayInputStream(dataStr.getBytes(charsetName)));  
  
    }  
  
    /** 
     * �����ļ� 
     *  
     * @param directory 
     *            ����Ŀ¼ 
     * @param downloadFile 
     *            ���ص��ļ� 
     * @param saveFile 
     *            ���ڱ��ص�·�� 
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
     * �����ļ� 
     * @param directory ����Ŀ¼ 
     * @param downloadFile ���ص��ļ��� 
     * @return �ֽ����� 
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
     * ɾ���ļ� 
     *  
     * @param directory 
     *            Ҫɾ���ļ�����Ŀ¼ 
     * @param deleteFile 
     *            Ҫɾ�����ļ� 
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
     * �г�Ŀ¼�µ��ļ� 
     *  
     * @param directory 
     *            Ҫ�г���Ŀ¼ 
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
