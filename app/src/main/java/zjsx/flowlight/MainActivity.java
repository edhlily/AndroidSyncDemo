package zjsx.flowlight;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import zjsx.flowlight.bean.GroupInfo;
import zjsx.flowlight.bean.IPInfo;
import zjsx.flowlight.bean.MasterInfo;
import zjsx.flowlight.bean.ProtocalCode;
import zjsx.flowlight.netty.TcpServer;
import zjsx.flowlight.service.SyncService;

import static zjsx.flowlight.bean.ProtocalCode.CODE_TEST;
import static zjsx.flowlight.common.Utils.LOCAL_IP;
import static zjsx.flowlight.common.Utils.MASTERINFO;
import static zjsx.flowlight.common.Utils.SYNC_GROUP;
import static zjsx.flowlight.common.Utils.isMaster;
import static zjsx.flowlight.common.Utils.show;

public class MainActivity extends AppCompatActivity {

    Intent intent;
    SyncService mSyncService;

    public ServiceConnection syncConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mSyncService = ((SyncService.SyncBinder) service).getService();

            mSyncService.startMulticastReceiver();

            mSyncService.sendMulticastMsg(ProtocalCode.CODE_IP, IPInfo.INSTANCE);
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        intent = new Intent(this,SyncService.class);
        bindService(intent,syncConn,BIND_AUTO_CREATE);
    }

    public void startTcpServer(View view) {
        if(isMaster()) {
            mSyncService.startTCPServer();
        }else{
            show("请先将本机设置为Master");
        }
    }

    public void connectTcpServer(View view) {
        if(MASTERINFO == null){
            show("无Master信息");
        }else {

            if (mSyncService.getTcpClient() != null && mSyncService.getTcpClient().isConnected()) {
                mSyncService.getTcpClient().disconnect();
            }

            mSyncService.connectTcpServer(MASTERINFO.getIp(),MASTERINFO.getPort());
        }
    }

    ProgressDialog mProgressDialog;
    public void asMaster(View view) {
        if (LOCAL_IP != null) {
            //确认0组的Master信息
            mProgressDialog = ProgressDialog.show(this, null, "查找Master中...");
            mSyncService.sendMulticastMsg(ProtocalCode.CODE_REQUEST_MASTER, new GroupInfo(SYNC_GROUP));

            App.getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mProgressDialog.dismiss();
                    if(isMaster()){
                        show("已经是Master了");
                    } else if(MASTERINFO != null) {
                        show("已经存在了一个Master");
                    }else{
                        mSyncService.sendMulticastMsg(ProtocalCode.CODE_MASTER, new MasterInfo(LOCAL_IP, TcpServer.PORT, SYNC_GROUP));
                    }
                }
            }, 5000);
        }else{
            mSyncService.sendMulticastMsg(ProtocalCode.CODE_IP,null);
            show("没有检测到IP地址,请重试");
        }
    }

    public void sendBoradCast(View view) {
        mSyncService.sendMulticastMsg(CODE_TEST,"TEST");
    }

    public void masterSend(View view) {
        if(mSyncService.getTcpServer() != null && mSyncService.getTcpServer().isLaunched()){
            mSyncService.getTcpServer().send(CODE_TEST,"TEST");
        }else{
            show("Master没有启动");
        }
    }

    public void clientSend(View view) {
        if(mSyncService.getTcpClient() != null && mSyncService.getTcpClient().isConnected()){
            mSyncService.getTcpClient().send(CODE_TEST,"TEST");
        }else{
            show("没有连接到Master");
        }
    }

    @Override
    public void finish() {
        unbindService(syncConn);
        super.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
