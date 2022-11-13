package com.specknet.pdiotapp.utils;

public class BothDevice{
    public String username;
    public String device = "both";
    public float[][] dataWindow;
    public BothDevice(String username, float[][] dataWindow){
        this.username = username;
        this.dataWindow = dataWindow;
    }
}