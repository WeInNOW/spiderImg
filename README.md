此项目是一个多线程爬虫，用来从img.hsgene.com接口抓取图片信息；
其中producer负责数据抓取，放到里面consumer负责保存数据到本地文件，线程设定还需改定；

1、在Config类中配置好相关路径，
2、在DATA中存入对应文件， imageId文件夹下放置dcw文件，以dcwId.txt为文件名称,文件内存储的是
    从MongoDB中对应的imgURL；- 在前面拼接http://XXXX.com/ 即可访问到对应的图片；（公司保密）
3、启动Main类中的main方法即可使用。
    最终运行结果在DATA/images下，一个dcw病例一个文件夹，当存储多种疾病类型时暂时需要分多次执行；