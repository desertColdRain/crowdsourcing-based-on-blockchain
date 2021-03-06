package com.blockchain.mcsblockchain.net.conf;


import com.blockchain.mcsblockchain.net.base.Node;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.*;

//网络框架配置信息
@Component
@ConfigurationProperties("tio")
public class TioProps {

    //心跳包时间间隔
    private int heartTimeout;

    //客户端分组名称
    private String clientGroupName;

    //服务端上下文名称
    private String serverGroupContextName;

    //服务端监听端口
    private int listenPort;

    //服务端绑定的ip
    private String serverIp;

    private LinkedHashMap<String, Object> nodes;

    public int getHeartTimeout() {
        return heartTimeout;
    }

    public void setHeartTimeout(int heartTimeout) {
        this.heartTimeout = heartTimeout;
    }

    public String getClientGroupName() {
        return clientGroupName;
    }

    public void setClientGroupName(String clientGroupName) {
        this.clientGroupName = clientGroupName;
    }

    public String getServerGroupContextName() {
        return serverGroupContextName;
    }

    public void setServerGroupContextName(String serverGroupContextName) {
        this.serverGroupContextName = serverGroupContextName;
    }

    public int getListenPort() {
        return listenPort;
    }

    public void setListenPort(int listenPort) {
        this.listenPort = listenPort;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public List<Node> getNodes() {

        if(nodes==null) return null;

        ArrayList<Node> nodeList = new ArrayList<>();
        Iterator<Map.Entry<String, Object>> iterator= nodes.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry entry = iterator.next();
            Map value = (Map)entry.getValue();
            nodeList.add(new Node(value.get("ip").toString(),Integer.parseInt(value.get("port").toString())));
        }
        return nodeList;
    }

    public void setNodes(LinkedHashMap<String, Object> nodes) {
        this.nodes = nodes;
    }
}
