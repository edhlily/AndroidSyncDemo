package zjsx.flowlight.common;

import android.os.Looper;
import android.widget.Toast;

import com.google.gson.Gson;

import zjsx.flowlight.App;
import zjsx.flowlight.bean.MasterInfo;
import zjsx.flowlight.netty.TcpClient;
import zjsx.flowlight.netty.TcpServer;
import zjsx.flowlight.service.SyncService;

/**
 * Created by Admin on 2016/10/11.
 */

public class Utils {
    public static int SYNC_GROUP;
    public static String LOCAL_IP;
    public static MasterInfo MASTERINFO;
    public static Gson gson = new Gson();
    public static SyncService SYNCSERVICE;

    public static void show(final String msg){
        App.getHandler().post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(App.get(),msg,Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static <T> T fromJson(String json,Class<T> clazz){
        try {
            return gson.fromJson(json,clazz);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isMaster(){
        return MASTERINFO!=null && LOCAL_IP != null && MASTERINFO.getIp().equals(LOCAL_IP);
    }
}
