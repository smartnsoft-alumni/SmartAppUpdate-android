package com.smartnsoft.updatepopupsample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.firebase.analytics.FirebaseAnalytics
import com.smartnsoft.smartappupdate.SmartAppUpdateManager

class MainActivity : AppCompatActivity()
{

  override fun onCreate(savedInstanceState: Bundle?)
  {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    FirebaseAnalytics.getInstance(this).setUserProperty("versionCode", BuildConfig.VERSION_CODE.toString())

    val updatePopupManager = SmartAppUpdateManager.Builder(this, BuildConfig.VERSION_CODE,BuildConfig.DEBUG)
        .setUpdatePopupActivity(CustomUpdatePopupActivity::class.java)
        .build();

    updatePopupManager.fetchRemoteConfig(true)
  }
}
