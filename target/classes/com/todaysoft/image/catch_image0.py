# -*- coding: utf-8 -*-
import codecs
import os
import tool.mgojson as mgojson
import urllib.request
import socket

from bson.objectid import ObjectId
from mylib.mysql_class import *
from mylib.mongo_class import *
'''
用于从mongo中获取图片的唯一标识，为以后拼接成URL
之前是使用caseNo 来查，，现在由于数据量较大，现在已有 200M 的数据
使用到了索引主键来查： 主键存储在dcw_base库中，获取之后需要转换为ObjectId才可以使用；
'''

def load_query(filename, encoding = "utf-8"):
    """
    从指定的txt文档中加载sql语句
    @return: query
    """
    dcw_set=[]
    with codecs.open(filename, encoding = encoding) as f:
        for each_line in f:
            dcw_set.append(each_line.strip())
    return dcw_set    
def download(url, picname,fileIndex,caseNo):
    #一个dcwNo创建一个文件夹；
    dir="F://pics/"+fileIndex+'/'+caseNo +'/image/' # 自定义指定目录；
    if os.path.exists(dir) and os.path.isdir(dir):
        pass
    else:
        os.mkdir(dir)
    socket.setdefaulttimeout(20) # 20s 超时没反应后，3次后就进行下一个加载
    try:
        urllib.request.urlretrieve(url, dir+'/'+picname)#应该分别放到一个文件夹中，
    except socket.timeout:
        count = 1
        while count <= 3: #重试3次，否则跳过；
            try:
                urllib.request.urlretrieve(url,dir+'/'+picname)                                                
                break
            except socket.timeout:    
                count += 1
        if count > 3:
            pass

def get_picture(dcw_dict,fileIndex, is_hide=False):
    """
    从mongo中读取「病例单号」与mysql中一致的数据.
    每条数据按照json块解析，并写入单独的txt文档中.
    @is_hide: 是否隐藏敏感信息
    """
    strServer = "http://img.hsgene.com/"  #网址
#     url=[]# 做一个列表，每个 
    mgo=mongobase()
    coll = mgo.get_collection()
    #js = mgojson.Json()
    for caseId in dcw_dict:
        case=coll.find({"_id":caseId})
        #print(type(case.next())==type({}) ) # .next() 是向后的
        m=0
        try:
            caseNo=case.next()
            with open('F://knowtion-spider/DATA/imageId/'+caseNo.get('caseNo')+'.txt', 'w',encoding='utf-8') as f:
                for url in caseNo.get('images'):
                    picName=url
#                     if url.find('.')<0:
#                         picName += '.jpeg'
                    print(picName)    
                    f.write(picName)
                    f.write('\n')
                    #download(strServer + url,picName,fileIndex,dcw_dict.get(caseId)) #如果url没有后缀就不能正确获取文件
                    #print(m)
                    m += 1
        except Exception as e:
            print(e)  
def get_dict(dcw_set):
    mysql=mysqlbase()
    dcw_dict={}
    for dcwCode in dcw_set: # 使用ref_id 主键查询会更快一点吧；
        query= "select code,ref_id from dcw_base where code='"+dcwCode+"'";
        data= mysql.get_data_by_query(query)
        #print(data)
        dcw_dict[ObjectId(data[0][1])]=dcwCode
    return dcw_dict             

if __name__ == '__main__':

# 2
    fileDir='F://pics/2/'
    filePath= fileDir
    dcw_set=set()
    filenames=os.listdir(filePath)
    for filename in filenames:    
        dcw_set.add(filename[:-4])
    dcw_dict=get_dict(dcw_set) 
    print('mongo2主键映射完成')
    get_picture(dcw_dict,str(2))

