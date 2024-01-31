package com.gnbsoftec.dolphinnative.web

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import com.gnbsoftec.dolphinnative.view.MainActivity
import com.gnbsoftec.dolphinnative.util.Logcat
import org.json.JSONObject

class NativeBridge(private val activity: Activity) {
    private val mHandler = Handler(Looper.myLooper()!!)

    private var bCmdProcess = false

    private var count = 0;
    @JavascriptInterface
    fun excute(inputData: String,succFunc: String, failFunc: String) {
        try {
            Logcat.d("excute start")
            if (bCmdProcess) return
            bCmdProcess = true

            mHandler.post {
                Logcat.d("===============================NativeBridge "+(++count)+"===============================")
                Logcat.d("inputData: $inputData")
                Logcat.d("succFunc : "+succFunc)
                Logcat.d("failFunc : "+failFunc)
                Logcat.d("============================================================================")
                val jsonObject = JSONObject(inputData)
                val serviceCd = jsonObject.optString("serviceCd", "")
                val params = jsonObject.optString("params", "")

                when (serviceCd) {
                    "TOKEN" -> {  //
                        (activity as MainActivity).getToken(params,succFunc,failFunc)
                    }
                    "TOAST" -> {  //
                        (activity as MainActivity).toast(params,succFunc,failFunc)
                    }
                    "APPDATA" -> {
                        (activity as MainActivity).appData(params,succFunc,failFunc)
                    }
                }

            }
        } catch (e: Exception) {
            Logcat.e("브릿지통신에러")
        } finally {
            bCmdProcess = false
        }
    }
}