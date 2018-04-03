package com.todaysoft.image;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;


public class Main {
    public static void main(String[] args) {
        Main.MultiThreadCrawler();
    }

    public static void MultiThreadCrawler() {

        LinkedBlockingQueue<String> idQueue = new LinkedBlockingQueue<>();
        LinkedBlockingQueue<byte[]> contentQueue = new LinkedBlockingQueue<>(200);
        ExecutorService exec=Executors.newFixedThreadPool(5);//newCachedThreadPool(); //带缓存的线程池；
        try {
            fillIdQueue(idQueue);
            Producer producer = new Producer(idQueue, contentQueue);
            Consumer consumer = new Consumer(contentQueue);
            //ConsumerNIO consumer = new ConsumerNIO(contentQueue);
//            构造线程
            List<Thread> proThreadList = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                proThreadList.add(new Thread(producer));
            }   
            //启动线程
            for (int i = 0; i < 5; i++) {
                proThreadList.get(i).start();
            }
            
            //exec.execute(new Thread(producer));
            
            Thread conThread = new Thread(consumer); //消费者1个线程，都不够写；-阻塞队列一直为空；
            
//            execs.execute(conThread);
           // proTread.start();
            conThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //读取id到阻塞队列
    public static void fillIdQueue(LinkedBlockingQueue<String> idQueue) throws Exception {
    	File dir= new File(Config.idPath);
    	
    	//System.out.println(dir.getName());
    	File[] dcwCode=dir.listFiles();
    	for(File ids: dcwCode){
	        FileInputStream fis = new FileInputStream(ids);
	        InputStreamReader isr = new InputStreamReader(fis);
	        BufferedReader br = new BufferedReader(isr);
	
	        String line;
	        
	        while ((line = br.readLine()) != null) {
	            idQueue.put(ids.getName()+"##"+line);  //用 ## 分割 dcw案例和 对应img的拼接id；
	        }
	        br.close();
	        isr.close();
	        fis.close();
	    }
    }

}
