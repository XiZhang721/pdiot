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

/**
 * This Class sets up connections for the App to connect to the cloud server
 */
public class CloudConnection{
    /**
     * The singleton instance for this class
     */
    private static CloudConnection instance = null;
    /**
     * The connection to send sensor data
     */
    public HttpURLConnection sensorDataConnection;
    /**
     * The connection to send user requests such as login and register
     */
    public HttpURLConnection usrRequestConnection;
    /**
     * The connection to request user's historical data
     */
    public HttpURLConnection historicalConnection;
    /**
     * The connection to request step counts
     */
    public HttpURLConnection stepConnection;
    /**
     * The url of the cloud prediction for request sending
     */
    private URL serverUrl;
    /**
     * The url of user login or register request processing
     */
    private URL userRequestUrl;
    /**
     * The url to request user's historical data
     */
    private URL historicalUrl;
    /**
     * The url to request for step counts
     **/
    private URL stepUrl;

    /**
     * The constructor for this class
     */
    private CloudConnection(){
        try {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Set up the connection between the app and the corresponding url address to perform activity recognition
     * @param serverUrl url address to perform activity recognition on the server
     * @return the CloudConnection's single instance with activity recognition url added
     * @throws MalformedURLException if the url is not in the correct form
     */
    public static CloudConnection setUpServerConnection(String serverUrl) throws MalformedURLException {
        if(instance == null){
            instance = new CloudConnection();
        }
        instance.serverUrl = new URL(serverUrl);

        assert instance != null;
        return instance;
    }

    /**
     * Set up the connection between the app and the corresponding url address to process login and register requests
     * @param userRequestUrl url address to process login and register requests
     * @return the CloudConnection's single instance with url for processing user login and register requests added
     * @throws MalformedURLException if the url in not in the correct form
     */
    public static CloudConnection setUpUserDataConnection(String userRequestUrl) throws MalformedURLException {
        if(instance == null){
            instance = new CloudConnection();
        }
        instance.userRequestUrl = new URL(userRequestUrl);
        System.out.println("setupuserdata success");

        assert instance != null;
        return instance;
    }

    /**
     * Set up the connection between the app and the corresponding url address to request user's historical data
     * @param historicalUrl url address to request user's historical data
     * @return the CloudConnection's single instance with url for requesting user's historical data
     * @throws MalformedURLException if the url is not in the correct form
     */
    public static CloudConnection setUpHistoricalConnection(String historicalUrl) throws MalformedURLException{
        if(instance == null){
            instance = new CloudConnection();
        }
        instance.historicalUrl = new URL(historicalUrl);

        assert instance != null;
        return instance;
    }

    /**
     * Set up the connection between the app and the corresponding url address to request user's step counts data
     * @param stepUrl url address to request user's step counts data
     * @return the CloudConnection's single instance with url for requesting user's step counts data
     * @throws MalformedURLException if the url is not in the correct form
     */
    public static CloudConnection setUpStepConnection(String stepUrl) throws MalformedURLException{
        if (instance == null){
            instance = new CloudConnection();
        }
        instance.stepUrl = new URL(stepUrl);

        assert instance != null;
        return instance;
    }

    /**
     * The method to send data to cloud server when two sensors are connected and get the response of prediction result from server
     * @param username the name of the user
     * @param respeckWindow the data window of respeck with size 300
     * @param thingyWindow the data window of thingy with size 450
     * @return the prediction result from server
     * @throws IOException
     */
    public String sendTwoSensorDataPostRequest(String username, float[] respeckWindow, float[] thingyWindow) throws IOException {
        String dataJson = Utils.twoSensorWindowToJSON(username,respeckWindow, thingyWindow);
        //Check if the sensor data connecton has been created or not. If not, create a new sensor data connection and set it up as POST request
        if (this.sensorDataConnection == null){
            this.sensorDataConnection = (HttpURLConnection)this.serverUrl.openConnection();
            this.sensorDataConnection.setRequestMethod("POST");
            this.sensorDataConnection.setRequestProperty("Content-Type", "application/json");
            this.sensorDataConnection.setRequestProperty("Accept","application/json");
            this.sensorDataConnection.setDoOutput(true);
        }

        // send the request
        OutputStream os = this.sensorDataConnection.getOutputStream();
        byte[] input = dataJson.getBytes(StandardCharsets.UTF_8);
        os.write(input, 0, input.length);
        os.flush();
        os.close();
        // Get the response code from server
        int responseCode = this.sensorDataConnection.getResponseCode();
        // If response code is OK, read the content of the response and return it, else throw the Exception
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

    /**
     * Send data to cloud server when only respeck is connected to the phone and retrieve the classification result
     * @param username the name of the user
     * @param respeckWindow the data window to send to the cloud server with size 300
     * @return the prediction result when only respeck is available
     * @throws IOException
     */
    public String sendRespeckDataPostRequest(String username, float[] respeckWindow) throws IOException{
        String dataJson = Utils.respeckWindowToJson(username,respeckWindow);
        //Check if the sensor data connecton has been created or not. If not, create a new sensor data connection and set it up as POST request
        if(this.sensorDataConnection==null){
            this.sensorDataConnection = (HttpURLConnection)this.serverUrl.openConnection();
            this.sensorDataConnection.setRequestMethod("POST");
            this.sensorDataConnection.setRequestProperty("Content-Type", "application/json");
            this.sensorDataConnection.setRequestProperty("Accept","application/json");
            this.sensorDataConnection.setDoOutput(true);
        }
        // send the request
        OutputStream os = this.sensorDataConnection.getOutputStream();
        byte[] input = dataJson.getBytes(StandardCharsets.UTF_8);
        os.write(input, 0, input.length);
        os.flush();
        os.close();
        // Get the response code from server
        int responseCode = this.sensorDataConnection.getResponseCode();
        System.out.println(responseCode);
        // If response code is OK, read the content of the response and return it, else throw the Exception
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

    /**
     * Send data to cloud server when only thingy is connected to the phone and retrieve the classification result
     * @param username the name of the user
     * @param thingyWindow the data window that would be sent to the cloud server with size 450
     * @return the prediction result from server when only Thingy is available
     * @throws IOException
     */
    public String sendThingyDataPostRequest(String username, float[] thingyWindow) throws IOException{
        String dataJson = Utils.thingyWindowToJson(username,thingyWindow);
        //Check if the sensor data connection has been created or not. If not, create a new sensor data connection and set it up as POST request
        if(this.sensorDataConnection==null){
            this.sensorDataConnection = (HttpURLConnection)this.serverUrl.openConnection();
            this.sensorDataConnection.setRequestMethod("POST");
            this.sensorDataConnection.setRequestProperty("Content-Type", "application/json");
            this.sensorDataConnection.setRequestProperty("Accept","application/json");
            this.sensorDataConnection.setDoOutput(true);
        }
        // send the request
        OutputStream os = this.sensorDataConnection.getOutputStream();
        byte[] input = dataJson.getBytes(StandardCharsets.UTF_8);
        os.write(input, 0, input.length);
        os.flush();
        os.close();
        // Get the response code from server
        int responseCode = this.sensorDataConnection.getResponseCode();
        // If the response,code is OK, read the content of the response and return it, else throw the Exception
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

    /**
     * Send registration request to the url that process user requests in the server and return server's response
     * @param username the name of the user
     * @param pwd the password of the user
     * @return the reponse of the server with a string value 1 or 0
     * @throws IOException
     */
    public String sendRegisterPostRequest(String username,String pwd) throws IOException {
        String usrRequestJson = Utils.toRegisterJson(username, pwd);
        System.out.println(usrRequestJson);
        //check the user request connection has been created or not. If not create the connection and set it up for sending POST request and receive response
        if (this.usrRequestConnection == null){
            this.usrRequestConnection = (HttpURLConnection) this.userRequestUrl.openConnection();
            this.usrRequestConnection.setRequestMethod("POST");
            this.usrRequestConnection.setRequestProperty("Content-Type", "application/json");
            this.usrRequestConnection.setRequestProperty("Accept", "application/json");
            this.usrRequestConnection.setDoOutput(true);
        }
        //send the request
        OutputStream os = this.usrRequestConnection.getOutputStream();
        byte[] input = usrRequestJson.getBytes(StandardCharsets.UTF_8);
        os.write(input, 0, input.length);
        os.flush();
        os.close();
        // Get the response code from server
        int responseCode = this.usrRequestConnection.getResponseCode();
        // If the response,code is OK, read the content of the response and return it, else throw the Exception
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

    /**
     * Send login request to the url that process user requests in the server and return server's response
     * @param username the name of the user
     * @param pwd the password of teh user
     * @return the response from the server with a content with string "1" or "0"
     * @throws IOException
     */
    public String sendLoginPostRequest(String username,String pwd) throws IOException {
        //check the user request connection has been created or not. If not create the connection and set it up for sending POST request and receive response
        String usrRequestJson = Utils.toRegisterJson(username, pwd);
        if (this.usrRequestConnection == null){
            this.usrRequestConnection = (HttpURLConnection) this.userRequestUrl.openConnection();
            this.usrRequestConnection.setRequestMethod("POST");
            this.usrRequestConnection.setRequestProperty("Content-Type", "application/json");
            this.usrRequestConnection.setRequestProperty("Accept", "application/json");
            this.usrRequestConnection.setDoOutput(true);
        }

        //send the request
        OutputStream os = this.usrRequestConnection.getOutputStream();
        byte[] input = usrRequestJson.getBytes(StandardCharsets.UTF_8);
        os.write(input, 0, input.length);
        os.flush();
        os.close();
        // Get the response code from server
        int responseCode = this.usrRequestConnection.getResponseCode();
        // If the response,code is OK, read the content of the response and return it, else throw the Exception
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

    /**
     * Send step count request to the url that process it in the cloud server, and return the calculated step counts
     * @param username the name of the user
     * @return the step count of the user
     * @throws IOException
     */
    public String sendStepCountPostRequest(String username) throws IOException{
        //check the step count connection has been created or not. If not create the connection and set it up for sending POST request and receive response
        String usrRequestJson = Utils.toStepJson(username);
        System.out.println(usrRequestJson);
        if (this.stepConnection == null){
            this.stepConnection = (HttpURLConnection) this.stepUrl.openConnection();
            this.stepConnection.setRequestMethod("POST");
            this.stepConnection.setRequestProperty("Content-Type", "application/json");
            this.stepConnection.setRequestProperty("Accept", "application/json");
            this.stepConnection.setDoOutput(true);
        }
        //send the request
        OutputStream os = this.stepConnection.getOutputStream();
        byte[] input = usrRequestJson.getBytes(StandardCharsets.UTF_8);
        os.write(input, 0, input.length);
        os.flush();
        os.close();
        // Get the response code from server
        int responseCode = this.stepConnection.getResponseCode();
        // If the response,code is OK, read the content of the response and return it, else throw the Exception
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

    /**
     * Send historical data request to the url that process it in the cloud server, and return the calculated step counts
     * @param username the name of the user
     * @return the historical data of the user
     * @throws IOException
     */
    public String sendHistoricalPostRequest(String username) throws IOException{
        String usrRequestJson = Utils.toHistoricalJson(username);
        //check the historical connection has been created or not. If not create the connection and set it up for sending POST request and receive response
        if (this.historicalConnection == null){
            this.historicalConnection = (HttpURLConnection) this.historicalUrl.openConnection();
            this.historicalConnection.setRequestMethod("POST");
            this.historicalConnection.setRequestProperty("Content-Type", "application/json");
            this.historicalConnection.setRequestProperty("Accept", "application/json");
            this.historicalConnection.setDoOutput(true);
        }

        //send the request
        OutputStream os = this.historicalConnection.getOutputStream();
        byte[] input = usrRequestJson.getBytes(StandardCharsets.UTF_8);
        os.write(input, 0, input.length);
        os.flush();
        os.close();

        // Get the response code from server
        int responseCode = this.historicalConnection.getResponseCode();
        // If the response,code is OK, read the content of the response and return it, else throw the Exception
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

    /**
     * Disconnect all the set up connections and set them as null
     */
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
