package com.infoland.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import jpcap.JpcapCaptor;
//import jpcap.JpcapWriter;
import jpcap.NetworkInterface;
import jpcap.packet.IPPacket;
import jpcap.packet.Packet;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@RestController
public class MainController {

    /**
     * @author Aiden
     */

    private NetworkInterface[] devices = null;
    private JpcapCaptor jpcap = null;
//    private JpcapWriter writer = null;


    /**
     * 系统启动，提供设备选择
     */
    @RequestMapping(value = "/start")
    public JSONArray before() {
        devices = JpcapCaptor.getDeviceList();
        JSONArray jsonArray = new JSONArray();
        int i = 0;
        for (NetworkInterface n : devices) {
            JSONObject json = new JSONObject();
            json.put("id", i);
            json.put("state", new String("设备" + i + ":" + n.name + "     |     " + n.description));
            jsonArray.add(json);
            i++;
        }
        i = 0;
        return jsonArray;
    }

    /**
     * 选择设备并加载和启动资源
     * 没有选择默认启动设备5
     */
    @RequestMapping(value = "/choose")
    public String choose(@RequestParam(value = "deviceId", required = false, defaultValue = "5") int deviceId) {
        try {
            jpcap = JpcapCaptor.openDevice(devices[deviceId], 1314, true, 50);
//            writer = JpcapWriter.openDumpFile(jpcap, "test.pcapng");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "{\"state\":\"设备" + deviceId + "使用中\"}";
    }

    /**
     * 可以用来设置过滤器
     */
    @RequestMapping(value = "/setFilter")
    public String setFilter(@RequestParam(value = "filter", required = false, defaultValue = "") String filter) {
        try {
            jpcap.setFilter(filter, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "{\"state\":\"过滤规则:" + filter + "\"}";
    }

    /**
     * 正式运行
     * 在前台1/s
     * 实际应该是后台监听发给前台
     */
    @RequestMapping(value = "/intercept")
    public JSONObject start() {

        Packet packet = jpcap.getPacket();
        if (packet instanceof IPPacket && ((IPPacket) packet).version == 4) {
            //强转
            IPPacket ip = (IPPacket) packet;
            //byte类型转换
            Charset charset = Charset.defaultCharset();
            ByteBuffer buf = ByteBuffer.wrap(ip.data);
            CharBuffer cBuf = charset.decode(buf);
            //最后必须转换成String，否则前端可能不认识
            String data = String.valueOf(cBuf);
            if (ip.data.length != 0) {
                System.out.println(data);
                System.out.println("----------------------------------------------");
//                writer.writePacket(packet);
                JSONObject json = new JSONObject();
                json.put("state", data);
                return json;
            }
            return null;
        }
        return null;
    }

    /**
     * 关闭工程，关闭资源
     */
    @RequestMapping(value = "/close")
    public void after() {
        if (devices != null) {
            devices.clone();
        }
        if (jpcap != null) {
            jpcap.close();
        }
//        if (writer != null) {
//            writer.close();
//        }

    }

}
