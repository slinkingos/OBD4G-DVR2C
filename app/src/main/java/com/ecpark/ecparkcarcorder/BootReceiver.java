package com.ecpark.ecparkcarcorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.Method;


/**
 * Created by yamei on 2017/6/6.
 */
public class BootReceiver extends BroadcastReceiver {

    final String TAG="BootReceiver";

    private Context scontext;
    /*
     * wifi相关变量
     * */
    private WifiManager wifiManager;
    private WifiInfo wifiInfo;


    private static String hostip = "191.168.43.1"; // 本机IP
    private static final int PORT = 2222;

    // sd卡目录
    private static final String dirname = "/mnt/sdcard/ftp";
    // ftp服务器配置文件路径
    private static final String filename = dirname + "/users.properties";
    private FtpServer mFtpServer = null;


    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Log.v(TAG, "开机启动后台服务");

            /* 保存content值 */
            this.scontext = context;

            /* 启动WIFI SoftAP, 并设置好SSID,key等 */
            wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            setWifiApConf(true);

            /* 启动FTP服务器 */
            try {
                creatDirsFiles();
                onStartServer();
            } catch (IOException e) {
                e.printStackTrace();
            }

            /* 启动与APP网络通信服务 */
            Intent service = new Intent(context, NetService.class);
            context.startService(service);
        }
    }


    /**
     * 创建服务器配置文件
     */
    private void creatDirsFiles() throws IOException {
        File dir = new File(dirname);
        if (!dir.exists()) {
            dir.mkdir();
        }
        FileOutputStream fos = null;
        String tmp = scontext.getString(R.string.users);
        File sourceFile = new File(dirname + "/users.properties");
        fos = new FileOutputStream(sourceFile);
        fos.write(tmp.getBytes());
        if (fos != null) {
            fos.close();
        }
    }

    /**
     * 开启FTP服务器
     * @param hostip 本机ip
     */
    private void startFtpServer(String hostip) {
        FtpServerFactory serverFactory = new FtpServerFactory();

        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        File files = new File(filename);

        //设置配置文件
        userManagerFactory.setFile(files);
        serverFactory.setUserManager(userManagerFactory.createUserManager());

        // 设置监听IP和端口号
        ListenerFactory factory = new ListenerFactory();
        factory.setPort(PORT);
        factory.setServerAddress(hostip);

        // replace the default listener
        serverFactory.addListener("default", factory.createListener());

        // start the server
        mFtpServer = serverFactory.createServer();
        try {
            mFtpServer.start();
            Log.d(TAG, "开启了FTP服务器  ip = " + hostip);
        } catch (FtpException e) {
            System.out.println(e);
        }
    }

    /**
     * 关闭FTP服务器
     */
    private void stopFtpServer() {
        if (mFtpServer != null) {
            mFtpServer.stop();
            mFtpServer = null;
            Log.d(TAG, "关闭了FTP服务器 ip = " + hostip);
        }
    }

    public void onStartServer(){
        startFtpServer("192.168.43.1");
    }


    String getWlanMac() {
        String macSerial = null;
        String str = "";
        try {
            Process pp = Runtime.getRuntime().exec("cat /sys/class/net/wlan0/address");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);

            for (; null != str;) {
                str = input.readLine();
                if (str != null) {
                    macSerial = str.trim();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return macSerial;
    }

    public boolean setWifiApConf(boolean enabled) {
        String WSSID;
        wifiManager.setWifiEnabled(false);

        try {
            String wlanmac = getWlanMac();
            Log.v(TAG, "Wlan MAC:" + wlanmac);
            if (wlanmac != null) {
                wlanmac = wlanmac.replace(":", "");
                String lastMac = wlanmac.substring(6, 12);
                WSSID = "CZH-WIFI-DVR-" + lastMac.toUpperCase();
            } else{
                WSSID = "CZH-WIFI-DVRTEST";
            }

            Log.v(TAG, "Wifi SSID:" + WSSID);

            //热点的配置类
            WifiConfiguration apConfig = new WifiConfiguration();

            // 热点名称
            apConfig.SSID = WSSID;
            // 加密
            apConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            apConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            apConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            apConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            apConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            apConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            apConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

            // 热点的密码
            apConfig.preSharedKey = "12345678";

            //通过反射调用设置热点
            Method method = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);

            //返回热点打开状态
            return (boolean) method.invoke(wifiManager, apConfig, enabled);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}