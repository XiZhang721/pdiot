package com.specknet.pdiotapp.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.specknet.pdiotapp.bluetooth.BluetoothSpeckService;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Utils {

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String getRESpeckUUID(Context context) {
        // this will always exist because the service is going to be started only after the respeck has been scanned\

        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.PREFERENCES_FILE, Context.MODE_PRIVATE);

        return sharedPreferences.getString(Constants.RESPECK_MAC_ADDRESS_PREF, "");
    }

    public static String getThingyUUID(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.PREFERENCES_FILE, Context.MODE_PRIVATE);

        return sharedPreferences.getString(Constants.THINGY_MAC_ADDRESS_PREF, "");
    }


    public static long getUnixTimestamp() {
        return System.currentTimeMillis();
    }

    public static boolean isServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        for(ActivityManager.RunningServiceInfo service: manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }

        return false;
    }

    public static String getStackTraceAsString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String bytesToHexNfc(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2 + bytes.length];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[hexChars.length - 3 - j * 3] = HEX_ARRAY[v >>> 4];
            hexChars[hexChars.length - 2 - j * 3] = HEX_ARRAY[v & 0x0F];
            hexChars[hexChars.length - 1 - j * 3] = ':';
        }
        return new String(Arrays.copyOfRange(hexChars,0,hexChars.length - 1));
    }

    public static int unsignedByteToInt(byte b) {
        return b & 0xFF;
    }

    /**
     * Convert signed bytes to a 16-bit unsigned int.
     */
    public static int unsignedBytesToInt(byte b0, byte b1) {
        return (unsignedByteToInt(b0) + (unsignedByteToInt(b1) << 8));
    }

    public static int unsignedToSigned(int unsigned, int size) {
        if ((unsigned & (1 << size - 1)) != 0) {
            unsigned = -1 * ((1 << size - 1) - (unsigned & ((1 << size - 1) - 1)));
        }
        return unsigned;
    }

    public static Integer getIntValue(byte[] mValue, int offset) {

        return unsignedToSigned(unsignedBytesToInt(mValue[offset],
                mValue[offset + 1]), 16);

    }

    public static float[] decodeThingyPacket(byte[] values) {
        float accel_x = (float) (getIntValue(values, 0)) / (1 << 10);
        float accel_y = (float) (getIntValue(values, 2)) / (1 << 10);
        float accel_z = (float) (getIntValue(values, 4)) / (1 << 10);

        float gyro_x = (float) (getIntValue(values, 6)) / (1 << 5);
        float gyro_y = (float) (getIntValue(values, 8)) / (1 << 5);
        float gyro_z = (float) (getIntValue(values, 10)) / (1 << 5);

        float mag_x = (float) (getIntValue(values, 12)) / (1 << 4);
        float mag_y = (float) (getIntValue(values, 14)) / (1 << 4);
        float mag_z = (float) (getIntValue(values, 16)) / (1 << 4);

        return new float[]{accel_x, accel_y, accel_z, gyro_x, gyro_y, gyro_z, mag_x, mag_y, mag_z};
    }

    /**
     * Convert data window to JSON when both sensors are connected
     * @param username the name of the user
     * @param respeckDataWindow the data window recorded by the respeck
     * @param thingy_dataWindow the data windwo recorded by the thingy
     * @return the json form of data window for both sensors
     */
    public static String twoSensorWindowToJSON(String username, float[] respeckDataWindow, float[] thingy_dataWindow){
        assert respeckDataWindow != null && thingy_dataWindow != null;
        String device1,device2;
        if(respeckDataWindow.length == 300 && thingy_dataWindow.length == 450){
            device1 = "respeck";
            device2 = "thingy";
        }
        else{
            throw new IllegalArgumentException("The length of data window should either be 300(50*6) for Respeck, " +
                    "or 450(50*9) for Thingy.");
        }
//        deviceWindow dataWindowWithRespeck= new deviceWindow(device1,respeckDataWindow);
//        deviceWindow dataWindowWithThingy = new deviceWindow(device2,thingy_dataWindow);
//        deviceWindow[] dataWindows = {dataWindowWithRespeck,dataWindowWithThingy};
        System.out.println(respeckDataWindow.length);
        System.out.println(thingy_dataWindow.length);
        float[][] dataWindows = {respeckDataWindow,thingy_dataWindow};
        System.out.println(dataWindows.length);

        BothDevice bothDevice = new BothDevice(username,dataWindows);

        Gson gson = new Gson();
        System.out.println(gson.toJson(bothDevice));
        return gson.toJson(bothDevice);
    }


    /**
     * Convert data window to Json when only respeck is needed for classification
     * @param username the name of the user
     * @param respeckWindow a float array of data window for respeck
     * @return the json form of respeck data window
     */
    public static String respeckWindowToJson(String username, float[] respeckWindow) {
        assert respeckWindow != null;
        String device;
        if(respeckWindow.length == 300){
            device = "respeck";
        }
        else{
            throw new IllegalArgumentException("The length of data window should be 300(50*6) for Respeck. ");
        }
        deviceWindow dataWindowWithRespeck= new deviceWindow(username,device,respeckWindow);
        Gson gson = new Gson();
        return gson.toJson(dataWindowWithRespeck);
    }

    /**
     * Convert data window to Json when only thingy is needed for classification
     * @param username the name of the user
     * @param thingyWindow a float array of data window for thingy
     * @return the json form of the thingy data window
     */
    public static String thingyWindowToJson(String username, float[] thingyWindow) {
        assert thingyWindow != null;
        String device;
        if(thingyWindow.length == 450){
            device = "thingy";
        }
        else{
            throw new IllegalArgumentException("The length of data window should be 450(50*9) for Thingy. ");
        }
        deviceWindow dataWindowWithThingy= new deviceWindow(username,device,thingyWindow);
        Gson gson = new Gson();
        return gson.toJson(dataWindowWithThingy);
    }



    /**
     * Convert the user data to json for registration
     * @param username the name of the user
     * @param pwd the password of the user
     * @return the json of the user for registration request
     */
    public static String toRegisterJson(String username, String pwd) {
        User user = new User(username, pwd);
        Gson gson = new Gson();
        return gson.toJson(user);
    }

    /**
     * Convert the user data to json for login
     * @param username the name of the user
     * @param pwd the password of the user
     * @return the json of the user for login request
     */
    public static String toLoginJson(String username,String pwd){
        User user = new User(username, pwd);
        Gson gson = new Gson();
        return gson.toJson(user);
    }

    /**
     * Covert the user name to json for sending historical data request
     * @param username the name of the user
     * @return the json form of the username
     */
    public static String toHistoricalJson(String username){
        Gson gson = new Gson();
        Map<String,String> user = new HashMap<>();
        user.put("username",username);
        return gson.toJson(user);
    }

    /**
     * Convert the user name to json for sending step count data request
     * @param username the name of the user
     * @return the json form of the usename
     */
    public static String toStepJson(String username) {
        Gson gson = new Gson();
        Map<String,String> user = new HashMap<>();
        user.put("username",username);
        return gson.toJson(user);
    }

}
