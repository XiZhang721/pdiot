package com.specknet.pdiotapp.utils;

/**
 * The class for serialization and deserialization when only one of the sensor is available
 */
public class deviceWindow{
    /**
     * The name of the user
     */
    String username;
    /**
     * The name of the device
     */
    String device;
    /**
     * the data of this kind of device
     */
    float[] dataWindow;

    /**
     * Constructor of this class
     * @param username the name of the user
     * @param device the name of the device, either be Respeck and Thingy
     * @param dataWindow the data window that measured by the corresponding device
     */
    public deviceWindow(String username,String device, float[] dataWindow){
        this.username = username;
        this.device = device;
        this.dataWindow = dataWindow;
    }
}
