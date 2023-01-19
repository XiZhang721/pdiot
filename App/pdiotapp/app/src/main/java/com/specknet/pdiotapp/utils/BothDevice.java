package com.specknet.pdiotapp.utils;

/**
 * The class of both device connected for serializing and deserializing
 */
public class BothDevice{
    /**
     * the  name of the user
     */
    public String username;
    /**
     * the device name of it is set as "both"
     */
    public String device = "both";
    /**
     * the data of noth of the sensors
     */
    public float[][] dataWindow;

    /**
     * Constructor of the class
     * @param username the user name
     * @param dataWindow the data of both sensors
     */
    public BothDevice(String username, float[][] dataWindow){
        this.username = username;
        this.dataWindow = dataWindow;
    }
}