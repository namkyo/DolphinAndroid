package com.gnbsoftec.dolphinnative.activity

import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.gnbsoftec.dolphinnative.fragment.CustomDialogAlertBindingInterface
import com.gnbsoftec.dolphinnative.fragment.CustomDialogAlertDialog
import com.gnbsoftec.dolphinnative.util.Logcat
import com.gnbsoftec.dolphinnative.util.SharedPreferenceHelper
import kotlin.system.exitProcess

open class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    fun alertDlg(msg: String) {
        val dialog = CustomDialogAlertDialog(object :
            CustomDialogAlertBindingInterface {
            override fun onYesButtonClick() {
            }
        }, msg)
        // 알림창이 띄워져있는 동안 배경 클릭 막기
        dialog.isCancelable = false
        dialog.show(supportFragmentManager, "ConfirmAlertDialog")
    }

    fun alertDlg(msg: String,callback:(() -> Unit)) {
        val dialog = CustomDialogAlertDialog(object :
            CustomDialogAlertBindingInterface {
            override fun onYesButtonClick() {
                callback()
            }
        }, msg)
        // 알림창이 띄워져있는 동안 배경 클릭 막기
        dialog.isCancelable = false
        dialog.show(supportFragmentManager, "ConfirmAlertDialog")
    }

    fun alertDlgFinish(msg: String) {
        val dialog = CustomDialogAlertDialog(object :
            CustomDialogAlertBindingInterface {
            override fun onYesButtonClick() {
                exitProcess(0)
            }
        }, msg)
        // 알림창이 띄워져있는 동안 배경 클릭 막기
        dialog.isCancelable = false
        dialog.show(supportFragmentManager, "ConfirmAlertDialog")
    }

    fun showToast(msg:String){
        Toast.makeText(this,msg, Toast.LENGTH_SHORT).show()
    }
}