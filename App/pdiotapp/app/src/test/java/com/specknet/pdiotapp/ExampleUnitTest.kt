package com.specknet.pdiotapp

import com.google.gson.Gson
import com.specknet.pdiotapp.cloudcomputing.CloudConnection
import com.specknet.pdiotapp.utils.Utils
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
        var userRuestUrl = "https://pdiot-c.ew.r.appspot.com/register"
        var ccon = CloudConnection.setUpUserDataConnection(userRuestUrl)
        var response = ccon.sendRegisterPostRequest("akaa","1234567")
        print(response)


    }

    @Test
    fun registerJsonCorrect(){
        var json = Utils.toRegisterJson("aka","123456")
        print(json);
        var correct = "{\"username\":\"aka\",\"password\":\"123456\"}"
        assertEquals(correct,json)

    }

    @Test
    fun login_Correct(){
        var userRuestUrl = "https://pdiot-c.ew.r.appspot.com/login"
        var ccon = CloudConnection.setUpUserDataConnection(userRuestUrl)
        var response = ccon.sendLoginPostRequest("aka","123456")
        print(response)


    }

    @Test
    fun loginJsonCorrect(){
        var json = Utils.toLoginJson("aka","123456")
        print(json);
        var correct = "{\"username\":\"aka\",\"password\":\"123456\"}"
        assertEquals(correct,json)

    }

    @Test
    fun twoSensorToJsonCorrect(){

        var f1= FloatArray(300){_ -> 1f};

        var f2 = FloatArray(450){_ -> 2f}
        f2[1] = 43f
        var json = Utils.twoSensorWindowToJSON("d",f1,f2)
        print(json)

    }

    @Test
    fun sendTwoDataWindow(){
        var f1= FloatArray(300){_ -> 1f};

        var f2 = FloatArray(450){_ -> 2f}
        var serverRequestUrl = "https://pdiot-c.ew.r.appspot.com/inference"
        var ccon = CloudConnection.setUpServerConnection(serverRequestUrl)
        ccon.sendTwoSensorDataPostRequest("d",f1,f2)
    }

    @Test
    fun sendThingyData(){
        var f2 = FloatArray(450){_ -> 2f}
        var serverRequestUrl = "https://pdiot-c.ew.r.appspot.com/inference"
        var ccon = CloudConnection.setUpServerConnection(serverRequestUrl)
        ccon.sendThingyDataPostRequest("d",f2)
    }

    @Test
    fun sendRespeckData(){
        var f1 = FloatArray(300){_ -> 1f}
        var serverRequestUrl = "https://pdiot-c.ew.r.appspot.com/inference"
        var ccon = CloudConnection.setUpServerConnection(serverRequestUrl)
        ccon.sendRespeckDataPostRequest("d",f1)
    }

    @Test
    fun toHistoicalJson(){
        var f1:String = "aaa"
        var r = Utils.toHistoricalJson("aaa")
        print(r)

    }

    @Test
    fun sendHistoricalRequest(){
        var f1:String = "d"
        var hisricalUrl = "https://pdiot-c.ew.r.appspot.com/history"
        var ccon = CloudConnection.setUpHistoricalConnection(hisricalUrl)
        var r = ccon.sendHistoricalPostRequest(f1);
        var gson:Gson = Gson();
        var rr = gson.fromJson(r, FloatArray::class.java)
        for (i in 0..rr.size-1) {
            print(rr[i])
        }
    }

    @Test
    fun sendStepRequest(){
        var f1:String = "d"
        var stepUrl = "https://pdiot-c.ew.r.appspot.com/step"
        var ccon = CloudConnection.setUpStepConnection(stepUrl)
        var r = ccon.sendStepCountPostRequest(f1);
        print(r)
        println("***********************************")
//        var gson:Gson = Gson();
//        var rr = gson.fromJson(r, FloatArray::class.java)
//        for (i in 0..rr.size-1){
//            print(rr[i])

    }



}