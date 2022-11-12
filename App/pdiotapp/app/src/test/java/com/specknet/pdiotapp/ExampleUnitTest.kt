package com.specknet.pdiotapp

import com.google.gson.Gson
import com.specknet.pdiotapp.cloudcomputing.CloudConnection
import com.specknet.pdiotapp.utils.Utils
import okhttp3.internal.Util
import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun register_Correct(){
        var serverUrl = "http://www.pdiot-c.ew.r.appspot.com/"
        var userRuestUrl = "http://www.pdiot-c.ew.r.appspot.com/register"
        var ccon = CloudConnection.setUpUserDataConnection(userRuestUrl)
        var response = ccon.sendRegisterPostRequest("aka","123456")
        print(response)


    }

    @Test
    fun registerJsonCorrect(){
        var json = Utils.toRegisterJson("aka","123456")
        print(json);
        var correct = "{\"requestType\":\"register\",\"user\":{\"username\":\"aka\",\"pwd\":\"123456\"}}"
        assertEquals(correct,json)

    }

    @Test
    fun login_Correct(){
        var serverUrl = "http://www.pdiot-c.ew.r.appspot.com/"
        var userRuestUrl = "http://www.pdiot-c.ew.r.appspot.com/"
        var ccon = CloudConnection.setUpUserDataConnection(userRuestUrl)
        var response = ccon.sendLoginPostRequest("aka","123456")
        print(response)


    }

    @Test
    fun loginJsonCorrect(){
        var json = Utils.toLoginJson("aka","123456")
        print(json);
        var correct = "{\"requestType\":\"login\",\"user\":{\"username\":\"aka\",\"pwd\":\"123456\"}}"
        assertEquals(correct,json)

    }

    @Test
    fun twoSensorToJsonCorrect(){

        var f1= FloatArray(300){_ -> 1f};

        var f2 = FloatArray(450){_ -> 2f}
        f2[1] = 43f
        var json = Utils.twoSensorWindowToJSON(f1,f2)
        print(json)

    }

    @Test
    fun sendTwoDataWindow(){
        var f1= FloatArray(300){_ -> 1f};

        var f2 = FloatArray(450){_ -> 2f}
        var serverRequestUrl = "http://www.pdiot-c.ew.r.appspot.com/inference"
        var ccon = CloudConnection.setUpServerConnection(serverRequestUrl)
        ccon.sendTwoSensorDataPostRequest(f1,f2)
    }

    @Test
    fun sendThingyData(){
        var f2 = FloatArray(450){_ -> 2f}
        var serverRequestUrl = "http://www.pdiot-c.ew.r.appspot.com/inference"
        var ccon = CloudConnection.setUpServerConnection(serverRequestUrl)
        ccon.sendThingyDataPostRequest(f2)
    }

    @Test
    fun sendRespeckData(){
        var f1 = FloatArray(300){_ -> 1f}
        var serverRequestUrl = "http://www.pdiot-c.ew.r.appspot.com/inference"
        var ccon = CloudConnection.setUpServerConnection(serverRequestUrl)
        ccon.sendRespeckDataPostRequest(f1)
    }




}