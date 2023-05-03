package com.example.p2p;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.text.format.Formatter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;

public class MyApplication extends Application {

    private static MyApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    // android available memory
    public static String getAvailMemory() {
        ActivityManager am = (ActivityManager) instance.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        //mi.availMem;
//        return mi.availMem / (1024*1024);
        return Formatter.formatFileSize(instance, mi.availMem);
    }

    public static float getCpuUsage() {
        Process process = null;
        BufferedReader reader = null;
        float cpuUsage = 0.0f;

        try {
            process = Runtime.getRuntime().exec("top -n 1");
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();

            while (line != null) {
                System.out.println(line);
                if (line.contains("CPU usage")) {
                    String[] values = line.split("%");
                    String[] subValues = values[0].split(":");

                    cpuUsage = Float.parseFloat(subValues[1].trim());
                    break;
                }

                line = reader.readLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (process != null) {
                process.destroy();
            }

            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return cpuUsage;
    }


    // cpu usage
//    public static float getCpuUsage() {
//        try {
//            RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
//            String load = reader.readLine();
//            String[] toks = load.split(" ");
//            long idle1 = Long.parseLong(toks[5]);
//            long cpu1 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4])
//                    + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);
//            try {
//                Thread.sleep(360);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            reader.seek(0);
//            load = reader.readLine();
//            reader.close();
//            toks = load.split(" ");
//            long idle2 = Long.parseLong(toks[5]);
//            long cpu2 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4])
//                    + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);
//            return (float) (cpu2 - cpu1) / ((cpu2 + idle2) - (cpu1 + idle1));
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//        return 0;
//    }

    // get gpu usage
    public static float getGpuUsage() {
        try {
            RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
//            BufferedReader br = new BufferedReader(reader);

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("gpu")) {
                    String[] fields = line.split(" ");
                    long gpuIdle = Long.parseLong(fields[4]);
                    long gpuTotal = 0;
                    for (int i = 1; i < fields.length; i++) {
                        gpuTotal += Long.parseLong(fields[i]);
                    }
                    return (gpuTotal - gpuIdle) * 100.0f / gpuTotal;
                }
            }

            reader.close();
//            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0.0f;
    }

}
