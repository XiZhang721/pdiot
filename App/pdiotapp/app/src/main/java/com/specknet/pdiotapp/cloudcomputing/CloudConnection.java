package com.specknet.pdiotapp.cloudcomputing;
import com.google.gson.Gson;
import com.specknet.pdiotapp.utils.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class CloudConnection{
    private static CloudConnection instance = null;
    public HttpURLConnection sensorDataConnection;
    public HttpURLConnection userDataConnection;
    private URL serverUrl;
    private URL userRequestUrl;
    public String classificationResult;
    private CloudConnection(String serverUrl, String userRequestUrl){
        try {
            this.serverUrl = new URL(serverUrl);
            this.userRequestUrl = new URL(userRequestUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static CloudConnection setUpConnection(String serverUrl, String userRequestUrl){
        if(instance == null){
            instance = new CloudConnection(serverUrl, userRequestUrl);
        }
        assert instance != null;
        return instance;
    }

    public String sendTwoSensorDataPostRequest(float[] respeckWindow, float[] thingyWindow) throws IOException {
        String dataJson = Utils.twoSensorWindowToJSON(respeckWindow, thingyWindow);
        if (this.sensorDataConnection == null){
            this.sensorDataConnection = (HttpURLConnection)this.serverUrl.openConnection();
        }

        this.sensorDataConnection.setRequestMethod("POST");
        this.sensorDataConnection.setRequestProperty("Content-Type", "application/json");
        this.sensorDataConnection.setRequestProperty("Accept","application/json");
        this.sensorDataConnection.setDoOutput(true);
        OutputStream os = this.sensorDataConnection.getOutputStream();
        byte[] input = dataJson.getBytes(StandardCharsets.UTF_8);
        os.write(input, 0, input.length);
        os.flush();
        os.close();

        int responseCode = this.sensorDataConnection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK){
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(this.sensorDataConnection.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            System.out.println(response);
            br.close();
            return response.toString();
        }else{
            Exception e = new Exception("The POST request for sending both sensors' data is failed.");
            e.printStackTrace();
            return null;
        }
    }

    public String sendRespeckDataPostRequest(float[] respeckWindow) throws IOException{
        String dataJson = Utils.respeckWindowToJson(respeckWindow);
        if(this.sensorDataConnection==null){
            this.sensorDataConnection = (HttpURLConnection)this.serverUrl.openConnection();
        }
        this.sensorDataConnection.setRequestMethod("POST");
        this.sensorDataConnection.setRequestProperty("Content-Type", "application/json");
        this.sensorDataConnection.setRequestProperty("Accept","application/json");
        this.sensorDataConnection.setDoOutput(true);
        OutputStream os = this.sensorDataConnection.getOutputStream();
        byte[] input = dataJson.getBytes(StandardCharsets.UTF_8);
        os.write(input, 0, input.length);
        os.flush();
        os.close();

        int responseCode = this.sensorDataConnection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK){
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(this.sensorDataConnection.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            System.out.println(response);
            br.close();
            return response.toString();
        }else{
            Exception e = new Exception("The POST request for sending RESPECK data is failed.");
            e.printStackTrace();
            return null;
        }
    }

    public String sendThingyDataPostRequest(float[] thingyWindow) throws IOException{
        String dataJson = Utils.thingyWindowToJson(thingyWindow);
        if(this.sensorDataConnection==null){
            this.sensorDataConnection = (HttpURLConnection)this.serverUrl.openConnection();
        }
        this.sensorDataConnection.setRequestMethod("POST");
        this.sensorDataConnection.setRequestProperty("Content-Type", "application/json");
        this.sensorDataConnection.setRequestProperty("Accept","application/json");
        this.sensorDataConnection.setDoOutput(true);
        OutputStream os = this.sensorDataConnection.getOutputStream();
        byte[] input = dataJson.getBytes(StandardCharsets.UTF_8);
        os.write(input, 0, input.length);
        os.flush();
        os.close();

        int responseCode = this.sensorDataConnection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK){
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(this.sensorDataConnection.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            System.out.println(response);
            br.close();
            return response.toString();
        }else{
            Exception e = new Exception("The POST request for sending THINGY data is failed.");
            e.printStackTrace();
            return null;
        }
    }

    public String sendRegisterPostRequest(String username,String pwd) throws IOException {
        String usrRequestJson = Utils.toRegisterJson(username, pwd);
        if (this.userDataConnection == null){
            this.userDataConnection = (HttpURLConnection) this.userRequestUrl.openConnection();
        }
        this.userDataConnection.setRequestMethod("POST");
        this.userDataConnection.setRequestProperty("Content-Type", "application/json");
        this.userDataConnection.setRequestProperty("Accept", "application/json");
        this.userDataConnection.setDoOutput(true);
        OutputStream os = this.userDataConnection.getOutputStream();
        byte[] input = usrRequestJson.getBytes(StandardCharsets.UTF_8);
        os.write(input, 0, input.length);
        os.flush();
        os.close();

        int responseCode = this.userDataConnection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK){
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(this.userDataConnection.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            System.out.println(response);
            br.close();
            return response.toString();
        }else{
            Exception e = new Exception("The POST request for register account is failed.");
            e.printStackTrace();
            return null;
        }
    }

    public String sendLoginPostRequest(String username,String pwd) throws IOException {
        String usrRequestJson = Utils.toRegisterJson(username, pwd);
        if (this.userDataConnection == null){
            this.userDataConnection = (HttpURLConnection) this.userRequestUrl.openConnection();
        }
        this.userDataConnection.setRequestMethod("POST");
        this.userDataConnection.setRequestProperty("Content-Type", "application/json");
        this.userDataConnection.setRequestProperty("Accept", "application/json");
        this.userDataConnection.setDoOutput(true);
        OutputStream os = this.userDataConnection.getOutputStream();
        byte[] input = usrRequestJson.getBytes(StandardCharsets.UTF_8);
        os.write(input, 0, input.length);
        os.flush();
        os.close();

        int responseCode = this.userDataConnection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK){
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(this.userDataConnection.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            System.out.println(response);
            br.close();
            return response.toString();
        }else{
            Exception e = new Exception("The POST request for register account is failed.");
            e.printStackTrace();
            return null;
        }
    }

    public void disconnect(){
        if (this.sensorDataConnection!=null){
            this.sensorDataConnection.disconnect();
            this.sensorDataConnection = null;
        }
    }

}
