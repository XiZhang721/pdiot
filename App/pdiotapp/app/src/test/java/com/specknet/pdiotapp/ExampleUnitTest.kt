package com.specknet.pdiotapp

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
        var serverUrl = "http://www.pdiot-c.ew.r.appspot.com/"
        var userRuestUrl = "http://www.pdiot-c.ew.r.appspot.com/"
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

}