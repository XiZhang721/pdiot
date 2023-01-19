package com.specknet.pdiotapp.utils;

/**
 * The class for serialization and deserialization for user login and register requests
 */
public class User {
    /**
     * The name of the user
     */
    String username;
    /**
     * The password of the user
     */
    String password;

    /**
     * The constructor of this class
     * @param username the name of the user
     * @param pwd the password of the user
     */
    public User(String username, String pwd){
        this.username = username;
        this.password = pwd;
    }


}
