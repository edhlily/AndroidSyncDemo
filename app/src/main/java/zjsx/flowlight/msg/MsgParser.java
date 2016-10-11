package zjsx.flowlight.msg;

import java.net.DatagramPacket;
import io.netty.channel.ChannelHandlerContext;
import zjsx.flowlight.bean.GroupInfo;
import zjsx.flowlight.bean.IPInfo;
import zjsx.flowlight.bean.MasterInfo;
import zjsx.flowlight.bean.ProtocalMsg;

import static zjsx.flowlight.bean.ProtocalCode.CODE_IP;
import static zjsx.flowlight.bean.ProtocalCode.CODE_MASTER;
import static zjsx.flowlight.bean.ProtocalCode.CODE_REQUEST_MASTER;
import static zjsx.flowlight.bean.ProtocalCode.CODE_TEST;
import static zjsx.flowlight.common.Utils.LOCAL_IP;
import static zjsx.flowlight.common.Utils.MASTERINFO;
import static zjsx.flowlight.common.Utils.SYNCSERVICE;
import static zjsx.flowlight.common.Utils.SYNC_GROUP;
import static zjsx.flowlight.common.Utils.fromJson;
import static zjsx.flowlight.common.Utils.isMaster;
import static zjsx.flowlight.common.Utils.show;

/**
 * Created by Admin on 2016/10/11.
 */

public class MsgParser {

    public static void parse(DatagramPacket datagramPacket){
        String ip = datagramPacket.getAddress().getHostAddress();
        String data = new String(datagramPacket.getData(),0,datagramPacket.getLength());
        parse(ip,data);

    }

    public static void parse(ChannelHandlerContext ctx, String msg){
        String ip = ctx.channel().remoteAddress().toString();
        parse(ip,msg);
    }

    static void parse(String ip,String msg){
        System.out.println("接收到"+ip +"发来的消息:"+msg);
        if(msg.length() < 3){
            System.out.println("无效长度的消息:"+msg);
            return;
        }
        int code = -1;
        String json = null;
        try {
            code = Integer.parseInt(msg.substring(0,3));
            if(msg.length() > 3){
                json = msg.substring(3,msg.length());
            }
            parse(new ProtocalMsg(ip,code,json));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    static void parse(ProtocalMsg msg){
        switch (msg.getCode()){
            case CODE_IP:
                IPInfo ipInfo = fromJson(msg.getMsg(),IPInfo.class);
                if(ipInfo != null && ipInfo.getUuid() != null && ipInfo.getUuid().equals(IPInfo.INSTANCE.getUuid())){
                    LOCAL_IP = msg.getIp();
                }
                break;
            case CODE_MASTER:
                MasterInfo masterInfo = fromJson(msg.getMsg(),MasterInfo.class);
                if(masterInfo != null){
                    MASTERINFO = masterInfo;
                    if(isMaster()){
                        show("本机成功设置成为"+ SYNC_GROUP +"组的Master");
                    }
                }
                break;
            case CODE_TEST:
                show(msg.getMsg());
                break;
            case CODE_REQUEST_MASTER:
                GroupInfo groupInfo = fromJson(msg.getMsg(),GroupInfo.class);
                if(groupInfo != null && isMaster() && MASTERINFO.getGroup() == groupInfo.getGroup()){
                    //发送Master信息
                    if(SYNCSERVICE != null){
                        SYNCSERVICE.sendMulticastMsg(CODE_MASTER,MASTERINFO);
                    }
                }
                break;
        }
    }
}
