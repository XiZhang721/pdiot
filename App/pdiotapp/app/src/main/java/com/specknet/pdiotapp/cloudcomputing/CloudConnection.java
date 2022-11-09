package com.specknet.pdiotapp.cloudcomputing;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class CloudConnection{
    private static CloudConnection instance = null;
    public HttpURLConnection httpConnection;
    private URL serverUrl;
    private CloudConnection(String serverUrl){
        try {
            this.serverUrl = new URL(serverUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static CloudConnection setUpConnection(String serverUrl){
        if(instance == null){
            instance = new CloudConnection(serverUrl);
        }
        assert instance != null;
        return instance;
    }

    public boolean sendDataPostRequest(String dataJson) throws IOException {
        this.httpConnection = (HttpURLConnection)this.serverUrl.openConnection();
        this.httpConnection.setRequestMethod("POST");
        this.httpConnection.setRequestProperty("Content-Type", "application/json");
        this.httpConnection.setRequestProperty("Accept","application/json");
        this.httpConnection.setDoOutput(true);
        OutputStream os = this.httpConnection.getOutputStream();
        byte[] input = dataJson.getBytes(StandardCharsets.UTF_8);
        os.write(input, 0, input.length);
        os.flush();
        os.close();

        int responseCode = this.httpConnection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK){
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(this.httpConnection.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            System.out.println(response);
            return true;
        }else{
            Exception e = new Exception("The POST request is failed.");
            e.printStackTrace();
            return false;
        }
    }
}
