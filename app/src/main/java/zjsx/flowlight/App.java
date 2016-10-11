package zjsx.flowlight;

import android.app.Application;
import android.os.Handler;

/**
 * Created by Admin on 2016/10/11.
 */

public class App extends Application {
    private static App mApp;
    private static Handler mHandler;
    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler();
        mApp = this;
    }

    public static App get(){
        return mApp;
    }

    public static Handler getHandler(){
        return mHandler;
    }
}
