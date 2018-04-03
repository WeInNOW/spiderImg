package com.todaysoft.image;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

/*
 * 负责把阻塞队列中的数据保存到本地文件
 * 抓取线程和保存线程目前没有自动结束的标志，需手工关闭，未来完善
 * 
 */
public class ConsumerNIO implements Runnable {
	
	private LinkedBlockingQueue<byte[]> contentQueue;    //共享内容存储区
	private ThreadLocal<Long> threadTime=new ThreadLocal<Long>();
	public ConsumerNIO(LinkedBlockingQueue<byte[]> contentQueue) {
		this.contentQueue = contentQueue;
	}

	@Override
	public void run() {
		int count = 0;
		while (true) {
			//保存图片
			threadTime.set(System.currentTimeMillis());
			BufferedOutputStream bos = null;
			FileOutputStream fos = null;
			//BufferedWriter bw = null;
			File file=null;
			try {
				count++;
				System.out.println(this.contentQueue.size()+"consumers");
				byte[] img = this.contentQueue.take(); // 内容队列取出来后 还要放到对应的文件夹中；
				
				int len= img[0]; // 长度； 表示后面有多少个字符表示 目录；
				byte[] tmps=Arrays.copyOfRange(img, 1, len+1);
				/*
				 * 转换成 String类型； dcw-2017-09XXX之类的名称；
				 */
				img= Arrays.copyOfRange(img, len+1, img.length);
				String dir="";
				for(byte tmp: tmps){
					dir+= (char)tmp;
				}
				String dirs=Config.filePath+dir;
				file = new File(dirs);
				if(!file.exists())  //不存在的话就创建目录
					file.mkdir();
				file = new File(dirs+"/"+count+".ipeg");
				fos = new FileOutputStream(file);
				/*
				 * 1、用缓冲输出流来进行； 
				 */
//				bos = new BufferedOutputStream(fos);
				//bos.write(img);
				/*
				 * 2、用文件通道来写；
				 */
				FileChannel fc=fos.getChannel();
				ByteBuffer buf=ByteBuffer.allocate(img.length);
				buf.put(img);
				buf.flip();  //把游标置0；
				fc.write(buf);
				//MappedByteBuffer mbb=channel.map(FileChannel.MapMode.READ_ONLY, 0,channel.size());
				//System.out.println(fc.size());
				buf.clear();
				fc.close();
				System.out.println(System.currentTimeMillis()-threadTime.get());
				if (count % 100 == 0) {
					System.out.println("===== stored ===== " + count + " =====");
				}
				
			} catch (Exception e) {
				//Auto-generated catch block
				e.printStackTrace();
			} finally {
				//close
//				try {
//					if (bw != null) bw.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
				try {
					if (bos != null) fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					if (fos != null) fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
