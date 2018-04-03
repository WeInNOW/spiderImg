package com.todaysoft.image;

import java.io.*;
import java.nio.Buffer;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;


public class Producer implements Runnable {

	private static AtomicInteger count = new AtomicInteger(0);
	private LinkedBlockingQueue<String> idsQueue;                    //共享图片id阻塞队列，多个线程拼接url访问；
	private LinkedBlockingQueue<byte[]> contentQueue;                //共享内容存储区
	private ThreadLocal<Long> threadTime=new ThreadLocal<Long>(){	 //可记录单个线程运行的时间
		protected Long initialValue(){
			return System.currentTimeMillis();
		}
		
	};
	// 消费者生产者模式； 生产者从网页上抓取信息，消费者写入的过程中，生产者仍然可以向阻塞队列中写入内容；
	public Producer(LinkedBlockingQueue<String> idsQueue, LinkedBlockingQueue<byte[]> contentQueue) {
		this.idsQueue = idsQueue;
		this.contentQueue = contentQueue;
	}

	@Override
	public void run() {
		int i=0;
		while (true) {
			threadTime.set(System.currentTimeMillis());;
			i++;
			String ids = null;
			try {
				int tmpCount = count.addAndGet(1);
				if (tmpCount % 100 == 0) {
					System.out.println(tmpCount + " datas finished");
				}
				ids = idsQueue.take();
				String[] dir_id=ids.split("##");  // [0] 表示文件夹名称， [1] 表示imageid
				Connection.Response response = Jsoup.connect(Config.baseUrl+dir_id[1])
						.userAgent("'User-Agent':'Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.1.6) Gecko/20091201 Firefox/3.5.6'") // 设置 User-Agent
						.header("Accept", "application/xml")
						.ignoreContentType(true)
						.maxBodySize(20000000) //设置传输最大的限制；
						.timeout(210000)       // 设置连接超时时间
					    .execute();                  // 处理获取页面；
				byte[] jsonStr = response.bodyAsBytes();
				//System.out.println(jsonStr.length);
				byte[] imgBytes = addDcwHeadInfo(dir_id[0], jsonStr);
								
				//savaImage(jsonStr, "F:\\knowtion-spider\\DATA\\images", "test.jpeg");
				if (jsonStr != null && jsonStr.length > 0) {
					//放入共享内容存储区，等待消费者保存到文件系统
					//System.out.println(jsonStr.length);
					//System.out.println(i + " " + dir_id[1]);
					//System.out.println(this.contentQueue.size()); 
					//长度一直为0；--网速的问题是瓶颈
					try {
						this.contentQueue.put(imgBytes);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					System.out.println(i + " " + dir_id[1] + " null");
					continue;
				}
				Thread.sleep(1000);
				System.out.println(Thread.currentThread().getName()+"::"+
				(System.currentTimeMillis()-threadTime.get()));
			} catch (Exception e) {
				try {
					e.printStackTrace();
					Thread.sleep(10000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
	/**
	 * 在已经生成的图片的字节信息前面添加要放的目录信息；
	 * 1、将字符串转换成 byte数组
	 * 2、将两个byte数组拼成一个；
	 * @param dir_id 要追加的Dcw文件目录
	 * @param byteStr 原始的字节数据信息
	 * @return 转换后的byte数组
	 */
	private byte[] addDcwHeadInfo(String dir, byte[] byteStr) {
		//在每条消息前面加上一个 文件信息；
		byte[] dcwId=new byte[dir.length()+1];
		dcwId[0]=(byte) dir.length();
		for(int j=1;j<dcwId.length;j++)
			dcwId[j]=(byte)dir.charAt(j-1);
		byte[] imgBytes=new byte[dcwId.length+byteStr.length];
		System.arraycopy(dcwId, 0, imgBytes, 0,dcwId.length);
		System.arraycopy(byteStr, 0, imgBytes,dcwId.length ,byteStr.length);
		return imgBytes;
	}
	public static void savaImage(byte[] img,String filePath,String fileName) {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        File dir = new File(filePath);
        try {
            //判断文件目录是否存在
            if(!dir.exists() && dir.isDirectory()){
                dir.mkdir();
            }
            file = new File(filePath+"\\"+fileName);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(img);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally{
            if(bos!=null){
                try {
                    bos.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if(fos!=null){
                try {
                    fos.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        
        
        
    }

}
