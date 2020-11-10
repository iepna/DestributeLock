package com.tp.utils;

import org.apache.commons.net.ftp.*;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.regex.Matcher;

/*
 * 类名：MyFtpClient.java
 * 版本：1.0
 * 日期：
 * 描述：ftp客户端
 * 作者：章军
 * 修改历史：
 * 2015-06-12:陈赞锦进行代码重构修改。
 */

public class MyFtpClient {

    public static void main(String[] args) {
        String ip = "10.2.38.2";
        int port = 2111;
        String username = "ftpuser01";
        String password = "oSQzAkVOLAx0T3zo";
        String file = "/incoming/Consistency";
        String serverfile = "D:\\usr\\local\\ai_javasdk_1.3.1.jar";
        MyFtpClient myFtpClient = new MyFtpClient();
        myFtpClient.uploadFile(ip,port,username,password,file,serverfile);

    }
	
	 public boolean uploadFile(String ip, int port, String username,  
			   String password, String serverpath, String file) {
		 // 初始表示上传失败  
		 boolean success = false;  
		 // 创建FTPClient对象  
		 FTPClient ftp = new FTPClient();  
		 ftp.setControlEncoding("UTF-8");
		 ftp.setConnectTimeout(20000);
		 ftp.setDataTimeout(600000);
		 ftp.enterLocalPassiveMode();
		 ftp.setActivePortRange(4000, 4100);
		 try {  
			 int reply=0;  
			 // 连接FTP服务器  
			 // 如果采用默认端口，可以使用ftp.connect(ip)的方式直接连接FTP服务器  
			 ftp.connect(ip, port);
			 //ftp.connect("192.168.20.221", 21);  
			 // 登录ftp  
			 ftp.login(username, password);  
			 // 看返回的值是不是reply>=200&&reply<300 如果是，表示登陆成功  
			 reply = ftp.getReplyCode();  
			 // 以2开头的返回值就会为真  
			 if (!FTPReply.isPositiveCompletion(reply)) {  
				 ftp.disconnect();  
				 return success;  
			 }  
			   
			 ftp.setActivePortRange(40000, 41000);
			 
			 checkPathExist(ftp,iso8859ToGbk(serverpath));  
     
			 //输入流  
			 InputStream input=null;  
			 try {  
				 file=gbkToIso8859(file);  
				 input = new FileInputStream(iso8859ToGbk(file));  
			 } catch (FileNotFoundException e) { 
				 LoggerFactory.getLogger(this.getClass()).info(e.toString());
				 e.printStackTrace();  
			 }  
			 // 将上传文件存储到指定目录  
			 file=iso8859ToGbk(file);
			 
			 String fileName =  getFilename(file);//8859
			 
			 int index = fileName.lastIndexOf(".");
			 String tmpFileName = fileName.substring(0,index)+".tmp";
			 
			 ftp.deleteFile(iso8859ToGbk(fileName));
			 
			 ftp.setFileType(FTPClient.BINARY_FILE_TYPE);

			 String path = iso8859ToGbk(serverpath)+"/"+iso8859ToGbk(tmpFileName);
			 
			 boolean flag = ftp.storeFile(path, input);
			 // 关闭输入流  
			 input.close();
			 
			 if(flag){
				 ftp.rename(iso8859ToGbk(serverpath)+"/"+iso8859ToGbk(tmpFileName),
						 iso8859ToGbk(serverpath)+"/"+iso8859ToGbk(fileName));
				 success = true;
			 }
			 // 退出ftp  
			 ftp.logout();  
		 } catch (IOException e) {  
			 success = false;
			 LoggerFactory.getLogger(this.getClass()).error("上传数据到ftp出错",e);
		 } finally {  
			 if (ftp.isConnected()) {  
				 try {  
					 ftp.disconnect();  
				 } catch (IOException ioe) {  
					 LoggerFactory.getLogger(this.getClass()).info(ioe.toString());
				 }  
			 }  
		 }  
		 return success;  
	 }  
	
	public boolean downFile(String ip, int port, String username,  
			   String password, String serverpath, String fileName,  
			   String localPath) {  

	// 初始表示下载失败  
	boolean success = false;  
	// 创建FTPClient对象  
	FTPClient ftp = new FTPClient();
	ftp.setConnectTimeout(20000);
	ftp.setDataTimeout(600000);
	ftp.setControlEncoding("UTF-8");   
	ftp.enterLocalPassiveMode();

	try { 
		int reply;  
	// 连接FTP服务器  
	// 如果采用默认端口，可以使用ftp.connect(ip)的方式直接连接FTP服务器  
		ftp.connect(ip, port);  
	// 登录ftp  
		ftp.login(username, password);  
		reply = ftp.getReplyCode();  
		if (!FTPReply.isPositiveCompletion(reply)) {  
			ftp.disconnect();  
			return success;  
		}   
		ftp.setActivePortRange(40000, 41000);
	// 转到指定下载目录  
		serverpath=gbkToIso8859(serverpath);  
		ftp.changeWorkingDirectory(this.iso8859ToGbk(serverpath));  
	// 列出该目录下所有文件  
		FTPFile[] fs = ftp.listFiles();  
		fileName=this.gbkToIso8859(fileName);  
		localPath=this.gbkToIso8859(localPath);  
     
	// 遍历所有文件，找到指定的文件  
		for (FTPFile f : fs) {  
			if (f.getName().equals(iso8859ToGbk(fileName))) {  
	// 根据绝对路径初始化文件  
				File localFile = new File(iso8859ToGbk(localPath) + "/" + f.getName());  
				File localFileDir = new File(iso8859ToGbk(localPath));  
	//保存路径不存在时创建  
				if(!localFileDir.exists()){  
					localFileDir.mkdirs();  
				}  
	// 输出流  
				OutputStream is = new FileOutputStream(localFile);  
	// 下载文件  
				ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
				boolean flag = ftp.retrieveFile(f.getName(), is);  
				is.close();  
				success = flag;

			}else{
//				System.out.println(f.getName()+"/"+iso8859ToGbk(fileName));
			}
		}  
	// 退出ftp  
		ftp.logout();  
	}catch (IOException e) {  
		LoggerFactory.getLogger(this.getClass()).info(e.toString());
		e.printStackTrace();  
	}finally {  
		if (ftp.isConnected()) {  
			try {  
				ftp.disconnect();  
			} catch (IOException ioe) {  
				LoggerFactory.getLogger(this.getClass()).info(ioe.toString());
			}  
		}  
	}
   
	return success;  
	}
	
	public String downExtFile(String ip, int port, String username,  
			   String password, String serverpath, String transIdo,String fileType,
			   String localPath,int index) {  


	
	// 初始表示下载失败  
	boolean success = false;
	String fileName = null;
	// 创建FTPClient对象  
	FTPClient ftp = new FTPClient();  
	ftp.setControlEncoding("UTF-8");
	ftp.setConnectTimeout(20000);
	ftp.setDataTimeout(600000);
	ftp.enterLocalPassiveMode();	
	ftp.setActivePortRange(4000, 4100);
	try {  
		int reply;  
	// 连接FTP服务器  
	// 如果采用默认端口，可以使用ftp.connect(ip)的方式直接连接FTP服务器  
		ftp.connect(ip, port);  
	// 登录ftp  

		ftp.login(username, password);  
		reply = ftp.getReplyCode();  
		if (!FTPReply.isPositiveCompletion(reply)) {  
			ftp.disconnect();  
			return fileName;  
		}   
		ftp.setActivePortRange(40000, 41000);
	// 转到指定下载目录  
		serverpath=gbkToIso8859(serverpath);  
		ftp.changeWorkingDirectory(this.iso8859ToGbk(serverpath));  
	// 列出该目录下所有文件  
		FTPFile[] fs = ftp.listFiles();  
		transIdo=this.gbkToIso8859(transIdo);  
		localPath=this.gbkToIso8859(localPath);  
  
	// 遍历所有文件，找到指定的文件  
		for (FTPFile f : fs) { 
			String[] fields = f.getName().split("_");
			String extName = f.getName().substring(f.getName().lastIndexOf(".")+1);
			
			if(extName.equalsIgnoreCase("gz")){
				String temp = f.getName().substring(0,f.getName().lastIndexOf("."));
				extName = temp.substring(temp.lastIndexOf(".")+1);
		
			}
			if(fields.length<=7)
				continue;
			
			System.out.printf("file (%s) %s (%d)-%d\n",fields[7],transIdo,Integer.valueOf(fields[6]).intValue(),index);
			if (fields[7].equals(iso8859ToGbk(transIdo)) && 
					extName.equalsIgnoreCase(fileType) && 
				Integer.valueOf(fields[6]).intValue() == index ) {  
	// 根据绝对路径初始化文件  
				File localFile = new File(iso8859ToGbk(localPath) + "/" + f.getName());  
				File localFileDir = new File(iso8859ToGbk(localPath));  
	//保存路径不存在时创建  
				if(!localFileDir.exists()){  
					localFileDir.mkdirs();  
				}  
	// 输出流  
				OutputStream is = new FileOutputStream(localFile);  
	// 下载文件  
				ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
				boolean flag = ftp.retrieveFile(f.getName(), is);  
				is.close();  
				success = flag;
				
				if(success)
					fileName = f.getName();
			}  
		}  
	// 退出ftp  
		ftp.logout();  
	}catch (IOException e) {  
		LoggerFactory.getLogger(this.getClass()).info(e.toString());
		e.printStackTrace();  
	}finally {  
		if (ftp.isConnected()) {  
			try {  
				ftp.disconnect();  
			} catch (IOException ioe) {  
				LoggerFactory.getLogger(this.getClass()).info(ioe.toString());
			}  
		}  
	}

	return fileName;  
	}
	public String downExtFile(String ip, int port, String username,  
			   String password, String serverpath, String transIdo,String fileType,
			   String localPath) {  


	
	// 初始表示下载失败  
	boolean success = false;
	String fileName = null;
	// 创建FTPClient对象  
	FTPClient ftp = new FTPClient();  
	ftp.setConnectTimeout(20000);
	ftp.setDataTimeout(600000);
	ftp.setControlEncoding("UTF-8");
	ftp.enterLocalPassiveMode();	
	ftp.setActivePortRange(4000, 4100);
	try {  
		int reply;  
	// 连接FTP服务器  
	// 如果采用默认端口，可以使用ftp.connect(ip)的方式直接连接FTP服务器  
		ftp.connect(ip, port);  
	// 登录ftp  

		ftp.login(username, password);  
		reply = ftp.getReplyCode();  
		if (!FTPReply.isPositiveCompletion(reply)) {  
			ftp.disconnect();  
			return fileName;  
		}   
		ftp.setActivePortRange(40000, 41000);
	// 转到指定下载目录  
		serverpath=gbkToIso8859(serverpath);  
		ftp.changeWorkingDirectory(this.iso8859ToGbk(serverpath));  
	// 列出该目录下所有文件  
		FTPFile[] fs = ftp.listFiles();  
		transIdo=this.gbkToIso8859(transIdo);  
		localPath=this.gbkToIso8859(localPath);  

	// 遍历所有文件，找到指定的文件  
		for (FTPFile f : fs) { 
			String[] fields = f.getName().split("_");
			String extName = f.getName().substring(f.getName().lastIndexOf(".")+1);
			
			if(extName.equalsIgnoreCase("gz")){
				String temp = f.getName().substring(0,f.getName().lastIndexOf("."));
				extName = temp.substring(temp.lastIndexOf(".")+1);
		
			}
			
			if(fields.length<=6)
				continue;
			System.out.printf("file (%s) %s\n",fields[6],extName);
			if (fields[6].equals(iso8859ToGbk(transIdo)) && 
					extName.equalsIgnoreCase(fileType)) {  
	// 根据绝对路径初始化文件  
				File localFile = new File(iso8859ToGbk(localPath) + "/" + f.getName());  
				File localFileDir = new File(iso8859ToGbk(localPath));  
	//保存路径不存在时创建  
				if(!localFileDir.exists()){  
					localFileDir.mkdirs();  
				}  
	// 输出流  
				OutputStream is = new FileOutputStream(localFile);  
	// 下载文件  
				ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
				boolean flag = ftp.retrieveFile(f.getName(), is);  
				is.close();  
				success = flag;
				
				if(success)
					fileName = f.getName();
			}  
		}  
	// 退出ftp  
		ftp.logout();  
	}catch (IOException e) {  
		LoggerFactory.getLogger(this.getClass()).info(e.toString());
		e.printStackTrace();  
	}finally {  
		if (ftp.isConnected()) {  
			try {  
				ftp.disconnect();  
			} catch (IOException ioe) {  
				LoggerFactory.getLogger(this.getClass()).info(ioe.toString());
			}  
		}  
	}

	return fileName;  
	}

	public boolean deleteFile(String ip, int port, String username,  
			   String password, String serverpath, String fileName) {  

	// 初始表示下载失败  
	boolean success = false;  
	// 创建FTPClient对象  
	FTPClient ftp = new FTPClient();  
	ftp.setConnectTimeout(20000);
	ftp.setDataTimeout(600000);
	ftp.setControlEncoding("UTF-8");
	ftp.enterLocalPassiveMode();	
	ftp.setActivePortRange(4000, 4100);
	try {  
		int reply;  
	// 连接FTP服务器  
	// 如果采用默认端口，可以使用ftp.connect(ip)的方式直接连接FTP服务器  
		ftp.connect(ip, port);  
	// 登录ftp  
		ftp.login(username, password);  
		reply = ftp.getReplyCode();  
		if (!FTPReply.isPositiveCompletion(reply)) {  
			ftp.disconnect();  
			return success;  
		}   
		ftp.setActivePortRange(40000, 41000);
	// 转到指定下载目录  
		serverpath=gbkToIso8859(serverpath);  
		ftp.changeWorkingDirectory(this.iso8859ToGbk(serverpath));  
  
		success = ftp.deleteFile(this.iso8859ToGbk(fileName));

		// 退出ftp  
		ftp.logout();
		
	}catch (Exception e) {  
		LoggerFactory.getLogger(this.getClass()).info(e.toString());
		e.printStackTrace();  
	}finally {  
		if (ftp.isConnected()) {  
			try {  
				ftp.disconnect();  
			} catch (IOException ioe) {  
				LoggerFactory.getLogger(this.getClass()).info(ioe.toString());
			}  
		}  
	}
	return success;  
	}
	
	private  boolean checkPathExist(FTPClient ftpClient, String filePath)  
		throws IOException {  
		boolean existFlag = false;  
		try {  
			if (filePath != null && !filePath.equals("")) {  
				if (filePath.indexOf("/") != -1) {  
					int index = 0;  
					while ((index = filePath.indexOf("/")) != -1) { 
						if(index == 0){
							ftpClient.changeWorkingDirectory("/");
						}else{
							if (!ftpClient.changeWorkingDirectory(filePath.substring(0,index))) { 
//								MyLog.loginfo("make dir 1 "+filePath.substring(0,index));
								ftpClient.makeDirectory(filePath.substring(0,index));
								ftpClient.changeWorkingDirectory(filePath.substring(0,index));
							}
  						}
						filePath = filePath.substring(index + 1, filePath.length());
//						MyLog.loginfo(filePath);
					}  
					if (!filePath.equals("")) {  
						if (!ftpClient.changeWorkingDirectory(filePath)) {  
							ftpClient.makeDirectory(filePath);  
//							MyLog.loginfo("make dir 2 "+filePath.substring(0,index));
						}  
					}  
				}   
				existFlag = true;  
			}  
		} catch (Exception e) {  
			LoggerFactory.getLogger(this.getClass()).info(e.toString());
		}  
		return existFlag;  
	 }  
	   
	   
	private String getFilename(String file){  
		//文件名  
		String filename="";  
		if(file!=null&&!file.equals("")){  
			file=file.replaceAll(Matcher.quoteReplacement("//"), "/");  
			String[] strs=file.split("/");  
			filename=strs[strs.length-1];  
		}  
		filename=gbkToIso8859(filename);//转码  
		return filename;  
	}
	
	private  String iso8859ToGbk(Object obj) {  
		try {  
			if (obj == null)  
				return "";  
			else  
				return new String(obj.toString().getBytes("iso-8859-1"), "GBK");  
		} catch (Exception e) {  
			LoggerFactory.getLogger(this.getClass()).info(e.toString());
			return "";  
		}  
	}  
	  
	 /**  
	  * 转码[GBK ->  ISO-8859-1]  
	  * 不同的平台需要不同的转码  
	  * @param obj  
	  * @return  
	  */  
	private  String gbkToIso8859(Object obj) {  
		try {  
			if (obj == null)  
				return "";  
			else
				return new String(obj.toString().getBytes("GBK"), "iso-8859-1");  
		} catch (Exception e) {  
			LoggerFactory.getLogger(this.getClass()).info(e.toString());
			return "";  
		}  
	}
	
	
}

