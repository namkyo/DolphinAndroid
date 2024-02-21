package com.gnbsoftec.dolphinnative.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import com.gnbsoftec.dolphinnative.common.DolphinApplication
import com.gnbsoftec.dolphinnative.util.GLog
import com.gnbsoftec.dolphinnative.util.PermissionUtil

abstract class BaseActivity<T: ViewBinding>(@LayoutRes private val layoutId:Int): AppCompatActivity() {
    //데이터 바인딩
    protected lateinit var binding  : T
    protected lateinit var dolphinApplication: DolphinApplication
    //context
    protected lateinit var activity : AppCompatActivity
    protected lateinit var context: Context

    //권한 허용여부 결과
    protected lateinit var permissionResult : ActivityResultLauncher<Array<String>>
    //설정(권한설정) 복귀 결과
    protected lateinit var permissionSettingResult : ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity = this@BaseActivity
        context = applicationContext
        dolphinApplication = application as DolphinApplication
        GLog.d("BaseActivity $activity , $layoutId")
        binding = DataBindingUtil.setContentView(this@BaseActivity, layoutId)

        initView()
        initViewModel()

        /** 퍼미션 권한 콜백1 */
        permissionSettingResult = PermissionUtil.requestPermission2(this@BaseActivity)
        /** 퍼미션 권한 콜백2 */
        permissionResult = PermissionUtil.responsePermission(this@BaseActivity,permissionSettingResult)

        initListener()


        hideKeyboard(activity)
        // 액션바 숨기기
        supportActionBar?.hide()
    }
    /**
     * 뷰 셋팅
     */
    protected open fun initView() {}
    /**
     * 뷰 모델 셋팅
     */
    protected open fun initViewModel() {}
    /**
     * 리스터 셋팅
     */
    protected open fun initListener() {}

    /**
     * Intent 결과
     */
    protected fun intentResult(callback : (Int, Intent?) -> Unit): ActivityResultLauncher<Intent> {
        val intent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            callback(result.resultCode,result.data)
        }
        return intent
    }

    /**
     * 토스트 메세지
     */
    protected fun showToast(msg:String){
        Toast.makeText(this,msg, Toast.LENGTH_SHORT).show()
    }

    protected fun loading(activity: AppCompatActivity, isHide: Boolean, delay: Long, callback: () -> Unit) {
        val handler = Handler(Looper.getMainLooper())

        activity.runOnUiThread {
            if (!activity.isFinishing && !activity.isDestroyed) {
//                if (!LoadingUtil.isShow()) {
//                    LoadingUtil.showLoading(activity)
//                }

                handler.postDelayed({
                    if (!activity.isFinishing && !activity.isDestroyed) {
//                        if (isHide) {
//                            LoadingUtil.hideLoading()
//                        }
                        callback()
                    }
                }, delay)
            }
        }

        // 액티비티가 종료될 때 핸들러의 메시지와 콜백을 취소하기 위한 처리
        activity.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
                handler.removeCallbacksAndMessages(null)
            }
        })
    }

    private fun hideKeyboard(activity: Activity) {
        val inputMethodManager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        // 현재 포커스를 가진 뷰에서 키보드를 숨깁니다. null인 경우 창 토큰에서 사용 중인 뷰를 사용합니다.
        val view = activity.currentFocus ?: View(activity)
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}