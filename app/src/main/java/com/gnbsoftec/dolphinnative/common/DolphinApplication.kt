package com.gnbsoftec.dolphinnative.common

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.gnbsoftec.dolphinnative.BuildConfig
import com.gnbsoftec.dolphinnative.util.GLog
import timber.log.Timber

class DolphinApplication : Application(), Application.ActivityLifecycleCallbacks {

    var isWebViewLoad = false//웹뷰 시작 여부

    override fun onCreate() {
        super.onCreate()

        //activity 라이프사이클
        registerActivityLifecycleCallbacks(this@DolphinApplication)

        if(BuildConfig.DEBUG){
            Timber.plant(Timber.DebugTree())
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        GLog.d("onActivityCreated : ${activity.localClassName}")
    }

    override fun onActivityStarted(activity: Activity) {
        GLog.d("onActivityStarted : ${activity.localClassName}")
    }

    override fun onActivityResumed(activity: Activity) {
        GLog.d("onActivityResumed : ${activity.localClassName}")
    }

    override fun onActivityPaused(activity: Activity) {
        GLog.d("onActivityPaused : ${activity.localClassName}")
    }

    override fun onActivityStopped(activity: Activity) {
        GLog.d("onActivityStopped : ${activity.localClassName}")
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        GLog.d("onActivitySaveInstanceState : ${activity.localClassName}")
    }

    override fun onActivityDestroyed(activity: Activity) {
        GLog.d("onActivityDestroyed : ${activity.localClassName}")
    }
}