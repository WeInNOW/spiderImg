package com.todaysoft.image;

/**
 * Created by wy on 2017/5/26.
 */

/**
 * 相关路径的配置类
 * @author lb
 */

public class Config {

    public static String baseUrl;       //基础url路径
    public static String idPath;        //id文件文件夹
    public static String filePath;      //内容保存路径
    public static String failureIdPath;   //失败id文件

    static {
        baseUrl = "http://img.hsgene.com/"; //图片的网址；
        idPath = "F:\\knowtion-spider\\DATA\\imageId";  
        filePath = "F:\\knowtion-spider\\DATA\\images\\"; 


    }

}
