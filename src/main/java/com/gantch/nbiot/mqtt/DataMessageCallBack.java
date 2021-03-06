package com.gantch.nbiot.mqtt;

import com.gantch.nbiot.httpRequest.httpRequest;
import com.gantch.nbiot.model.NbiotDevice;
import com.gantch.nbiot.model.NbiotTokenRelation;
import com.gantch.nbiot.service.DataService;
import com.gantch.nbiot.service.NbiotDeviceService;
import com.gantch.nbiot.service.NbiotTokenRelationService;
import org.eclipse.paho.client.mqttv3.MqttClient;

/**
 * Created by rongshuai on 2019/9/11
 */
public class DataMessageCallBack {
    private httpRequest hr = new httpRequest();
    public void device_CallBack(NbiotDevice device, NbiotTokenRelationService nbiotTokenRelationService, NbiotDeviceService nbiotDeviceService) throws Exception {
        String deviceMac = device.getMac();
        String deviceType = device.getDeviceType();
        NbiotTokenRelation nbiotTokenRelation = nbiotTokenRelationService.getRelationByMac(deviceMac);//根据设备的mac查询设备是否存在token
        if(nbiotTokenRelation == null){//如果设备的token不存在
            System.out.println("设备尚未在deviceaccess中创建");
            String token = null;
            String id = null;
            String type = DataService.deviceType2Type(deviceType);//根据设备的id获取设备的类型
            int device_number = nbiotDeviceService.getNbiotDeviceNumber();
            id = hr.httpcreate2(device,device_number,"",type,"");//获取设备的id
            device.setDeviceId(id);//将设备的id设置为从deviceaccess获取到的UUID
            System.out.println(device);
            nbiotDeviceService.addNbiotDevice(device);//向数据库中添加新创建的设备
            token = hr.httpfind(id);//获取设备的token
            NbiotTokenRelation newNbiotTokenRelation = new NbiotTokenRelation(token,deviceMac,type);
            nbiotTokenRelationService.addARelation(newNbiotTokenRelation);//将设备的token与设备关系信息入库
            DataMessageClient dataMessageClient = new DataMessageClient();
            MqttClient client = dataMessageClient.getClient();
            //向mqtt的服务端发送设备的属性信息，服务端（模拟deviceaccess）会实现设备属性信息的持久化
            DataMessageClient.publishAttribute(client,token,device.toString());
        }
        else{//如果设备的token已经存在
            System.out.println("设备已经在deviceaccess中创建了~");
            System.out.println("设备的类型为：" + DataService.deviceType2Type(deviceType));
        }
    }
}
