package com.specknet.pdiotapp.cloudcomputing;

import com.specknet.pdiotapp.utils.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class CloudConnection{
    private static CloudConnection instance = null;
    public HttpURLConnection sensorDataConnection;
    public HttpURLConnection usrRequestConnection;
    public HttpURLConnection historicalConnection;
    public HttpURLConnection stepConnection;
    private URL serverUrl;
    private URL userRequestUrl;
    private URL historicalUrl;
    private URL stepUrl;
    public String classificationResult;
    private CloudConnection(){
        try {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static CloudConnection setUpServerConnection(String serverUrl) throws MalformedURLException {
        if(instance == null){
            instance = new CloudConnection();
        }
        instance.serverUrl = new URL(serverUrl);

        assert instance != null;
        return instance;
    }

    public static CloudConnection setUpUserDataConnection(String userRequestUrl) throws MalformedURLException {
        if(instance == null){
            instance = new CloudConnection();
        }
        instance.userRequestUrl = new URL(userRequestUrl);
        System.out.println("setupuserdata success");

        assert instance != null;
        return instance;
    }

    public static CloudConnection setUpHistoricalConnection(String historicalUrl) throws MalformedURLException{
        if(instance == null){
            instance = new CloudConnection();
        }
        instance.historicalUrl = new URL(historicalUrl);

        assert instance != null;
        return instance;
    }

    public static CloudConnection setUpStepConnection(String stepUrl) throws MalformedURLException{
        if (instance == null){
            instance = new CloudConnection();
        }
        instance.stepUrl = new URL(stepUrl);

        assert instance != null;
        return instance;
    }

    public String sendTwoSensorDataPostRequest(String username, float[] respeckWindow, float[] thingyWindow) throws IOException {
        String dataJson = Utils.twoSensorWindowToJSON(username,respeckWindow, thingyWindow);
        if (this.sensorDataConnection == null){
            this.sensorDataConnection = (HttpURLConnection)this.serverUrl.openConnection();
            this.sensorDataConnection.setRequestMethod("POST");
            this.sensorDataConnection.setRequestProperty("Content-Type", "application/json");
            this.sensorDataConnection.setRequestProperty("Accept","application/json");
            this.sensorDataConnection.setDoOutput(true);
        }


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

    public String sendRespeckDataPostRequest(String username, float[] respeckWindow) throws IOException{
        String dataJson = Utils.respeckWindowToJson(username,respeckWindow);
        System.out.println(dataJson);
        if(this.sensorDataConnection==null){
            this.sensorDataConnection = (HttpURLConnection)this.serverUrl.openConnection();
            this.sensorDataConnection.setRequestMethod("POST");
            this.sensorDataConnection.setRequestProperty("Content-Type", "application/json");
            this.sensorDataConnection.setRequestProperty("Accept","application/json");
            this.sensorDataConnection.setDoOutput(true);
        }

        OutputStream os = this.sensorDataConnection.getOutputStream();
        byte[] input = dataJson.getBytes(StandardCharsets.UTF_8);
        os.write(input, 0, input.length);
        os.flush();
        os.close();

        int responseCode = this.sensorDataConnection.getResponseCode();
        System.out.println(responseCode);
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

    public String sendThingyDataPostRequest(String username, float[] thingyWindow) throws IOException{
        String dataJson = Utils.thingyWindowToJson(username,thingyWindow);
        if(this.sensorDataConnection==null){
            this.sensorDataConnection = (HttpURLConnection)this.serverUrl.openConnection();
            this.sensorDataConnection.setRequestMethod("POST");
            this.sensorDataConnection.setRequestProperty("Content-Type", "application/json");
            this.sensorDataConnection.setRequestProperty("Accept","application/json");
            this.sensorDataConnection.setDoOutput(true);
        }
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
        System.out.println(usrRequestJson);
        if (this.usrRequestConnection == null){
            this.usrRequestConnection = (HttpURLConnection) this.userRequestUrl.openConnection();
            this.usrRequestConnection.setRequestMethod("POST");
            this.usrRequestConnection.setRequestProperty("Content-Type", "application/json");
            this.usrRequestConnection.setRequestProperty("Accept", "application/json");
            this.usrRequestConnection.setDoOutput(true);
        }

        OutputStream os = this.usrRequestConnection.getOutputStream();
        byte[] input = usrRequestJson.getBytes(StandardCharsets.UTF_8);
        os.write(input, 0, input.length);
        os.flush();
        os.close();

        int responseCode = this.usrRequestConnection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK){
            System.out.print(responseCode);
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(this.usrRequestConnection.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            System.out.println(response);
            br.close();
            return response.toString();

        }else{
            System.out.print(responseCode);
            Exception e = new Exception("The POST request for register account is failed.");
            e.printStackTrace();
            return null;
        }
    }

    public String sendLoginPostRequest(String username,String pwd) throws IOException {
        String usrRequestJson = Utils.toRegisterJson(username, pwd);
        if (this.usrRequestConnection == null){
            this.usrRequestConnection = (HttpURLConnection) this.userRequestUrl.openConnection();
            this.usrRequestConnection.setRequestMethod("POST");
            this.usrRequestConnection.setRequestProperty("Content-Type", "application/json");
            this.usrRequestConnection.setRequestProperty("Accept", "application/json");
            this.usrRequestConnection.setDoOutput(true);
        }

        OutputStream os = this.usrRequestConnection.getOutputStream();
        byte[] input = usrRequestJson.getBytes(StandardCharsets.UTF_8);
        os.write(input, 0, input.length);
        os.flush();
        os.close();

        int responseCode = this.usrRequestConnection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK){
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(this.usrRequestConnection.getInputStream(), StandardCharsets.UTF_8));
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

    public String sendStepCountPostRequest(String username) throws IOException{
        String usrRequestJson = Utils.toStepJson(username);
        System.out.println(usrRequestJson);
        if (this.stepConnection == null){
            this.stepConnection = (HttpURLConnection) this.stepUrl.openConnection();
            this.stepConnection.setRequestMethod("POST");
            this.stepConnection.setRequestProperty("Content-Type", "application/json");
            this.stepConnection.setRequestProperty("Accept", "application/json");
            this.stepConnection.setDoOutput(true);
        }
        OutputStream os = this.stepConnection.getOutputStream();
        byte[] input = usrRequestJson.getBytes(StandardCharsets.UTF_8);
        os.write(input, 0, input.length);
        os.flush();
        os.close();
        int responseCode = this.stepConnection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK){
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(this.stepConnection.getInputStream(), StandardCharsets.UTF_8));
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

    public String sendHistoricalPostRequest(String username) throws IOException{
        String usrRequestJson = Utils.toHistoricalJson(username);
        if (this.historicalConnection == null){
            this.historicalConnection = (HttpURLConnection) this.historicalUrl.openConnection();
            this.historicalConnection.setRequestMethod("POST");
            this.historicalConnection.setRequestProperty("Content-Type", "application/json");
            this.historicalConnection.setRequestProperty("Accept", "application/json");
            this.historicalConnection.setDoOutput(true);
        }

        OutputStream os = this.historicalConnection.getOutputStream();
        byte[] input = usrRequestJson.getBytes(StandardCharsets.UTF_8);
        os.write(input, 0, input.length);
        os.flush();
        os.close();

        int responseCode = this.historicalConnection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK){
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(this.historicalConnection.getInputStream(), StandardCharsets.UTF_8));
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
        if(this.usrRequestConnection !=null){
            this.usrRequestConnection.disconnect();
            this.usrRequestConnection = null;
        }
        if (this.historicalConnection != null){
            this.historicalConnection.disconnect();
            this.historicalConnection = null;
        }
        if (this.stepConnection != null){
            this.stepConnection.disconnect();
            this.stepConnection = null;
        }
    }

}
