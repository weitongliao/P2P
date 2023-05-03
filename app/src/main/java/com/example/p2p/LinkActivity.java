package com.example.p2p;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;

public class LinkActivity extends AppCompatActivity {

    private DistributedMap distro;
    private Handler mHandler = new Handler();
    private MyApplication myApplication=(MyApplication)getApplication();
//    private TextView usage = findViewById(R.id.ResourceUsage);
    String ip;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_link);

//        mUpdateTextViewRunnable.run();
        System.out.println(myApplication.getCpuUsage());


//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    String ipAddress = InetAddress.getLocalHost().getHostAddress();
//                    Log.e("ip", ipAddress);
//                    // 将获取到的IP地址进行广播
//                } catch (UnknownHostException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//        thread.start();
        ip = getIPAddress();
        TextView ip_text = findViewById(R.id.ip_text);
        ip_text.setText("IP: " + ip);

        Button connect_button = findViewById(R.id.Connect);
        Button disconnect_button = findViewById(R.id.Disconnect);
        Button put_button = findViewById(R.id.Put);
        Button find_button = findViewById(R.id.FindByKey);

        TextView key_text = findViewById(R.id.Key);
        TextView value_text = findViewById(R.id.Value);
        TextView target_key_text = findViewById(R.id.TargetKey);
        TextView target_value_text = findViewById(R.id.TargetValue);

        disconnect_button.setVisibility(View.INVISIBLE);
        put_button.setVisibility(View.INVISIBLE);
        find_button.setVisibility(View.INVISIBLE);
        key_text.setVisibility(View.INVISIBLE);
        value_text.setVisibility(View.INVISIBLE);
        target_key_text.setVisibility(View.INVISIBLE);
        target_value_text.setVisibility(View.INVISIBLE);

        connect_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    distro = new DistributedMap(ip);
                    Toast.makeText(LinkActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                    connect_button.setVisibility(View.INVISIBLE);
                    disconnect_button.setVisibility(View.VISIBLE);
                    put_button.setVisibility(View.VISIBLE);
                    find_button.setVisibility(View.VISIBLE);
                    key_text.setVisibility(View.VISIBLE);
                    value_text.setVisibility(View.VISIBLE);
                    target_key_text.setVisibility(View.VISIBLE);
                    target_value_text.setVisibility(View.VISIBLE);


                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }
        });

        disconnect_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                distro.finish();
                Toast.makeText(LinkActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
                connect_button.setVisibility(View.VISIBLE);
                disconnect_button.setVisibility(View.INVISIBLE);
                put_button.setVisibility(View.INVISIBLE);
                find_button.setVisibility(View.INVISIBLE);
                key_text.setVisibility(View.INVISIBLE);
                value_text.setVisibility(View.INVISIBLE);
                target_key_text.setVisibility(View.INVISIBLE);
                target_value_text.setVisibility(View.INVISIBLE);
            }
        });

        put_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    distro.put(key_text.getText().toString(),value_text.getText().toString());
                    Toast.makeText(LinkActivity.this, "Insert key: "+key_text.getText().toString() + " value: " + value_text.getText().toString(), Toast.LENGTH_SHORT).show();
                    Log.e("key value", key_text.getText().toString() + value_text.getText().toString());
                    key_text.setText("");
                    value_text.setText("");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        find_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String key = target_key_text.getText().toString();
                String value = distro.get(key);
                target_key_text.setText("");
                target_value_text.setText("The value of " + key + " is "+ value);
            }
        });
    }

    private void Connect() throws Exception {
//        DistributedMap distro = new DistributedMap();
//        Scanner scanner = new Scanner(System.in);
    }

    public static String getIPAddress() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress() && addr instanceof Inet4Address) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void startUpdatingTextView() {
        mHandler.postDelayed(mUpdateTextViewRunnable, 1000); //每秒更新一次
    }

    private Runnable mUpdateTextViewRunnable = new Runnable() {
        @Override
        public void run() {
            TextView usage = findViewById(R.id.ResourceUsage);
            //获取GPU使用率
            float gpuUsage = myApplication.getGpuUsage();
            float cpuUsage = myApplication.getCpuUsage();
            String memory = myApplication.getAvailMemory();
            //更新TextView的内容
            usage.setText("CPU:" + cpuUsage + "% " + "GPU:" + gpuUsage + "% " + "Memory:" + gpuUsage + "GB");
            //再次发送更新消息
            mHandler.postDelayed(mUpdateTextViewRunnable, 1000);
        }
    };


//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
//        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
//            View v = getCurrentFocus();
//            if (isShouldHideKeyboard(v, ev)) {
//                boolean res=hideKeyboard(v.getWindowToken());
//                if(res){
//                    //隐藏了输入法，则不再分发事件
//                    return true;
//                }
//            }
//        }
//        return super.dispatchTouchEvent(ev);
//    }
//
//    /**
//     * 根据EditText所在坐标和用户点击的坐标相对比，来判断是否隐藏键盘，因为当用户点击EditText时则不能隐藏
//     *
//     * @param v
//     * @param event
//     * @return
//     */
//    private boolean isShouldHideKeyboard(View v, MotionEvent event) {
//        if (v != null && (v instanceof EditText)) {
//            int[] l = {0, 0};
//            v.getLocationInWindow(l);
//            int left = l[0],
//                    top = l[1],
//                    bottom = top + v.getHeight(),
//                    right = left + v.getWidth();
//            if (event.getX() > left && event.getX() < right
//                    && event.getY() > top && event.getY() < bottom) {
//                // 点击EditText的事件，忽略它。
//                return false;
//            } else {
//                return true;
//            }
//        }
//        // 如果焦点不是EditText则忽略，这个发生在视图刚绘制完，第一个焦点不在EditText上，和用户用轨迹球选择其他的焦点
//        return false;
//    }
//
//    /**
//     * 获取InputMethodManager，隐藏软键盘
//     * @param token
//     */
//    private boolean hideKeyboard(IBinder token) {
//        if (token != null) {
//            InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//            return  im.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
//        }
//        return false;
//    }

}