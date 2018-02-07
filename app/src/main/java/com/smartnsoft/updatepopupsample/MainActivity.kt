package com.smartnsoft.updatepopupsample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.firebase.analytics.FirebaseAnalytics
import com.smartnsoft.updatepopup.UpdatePopupManager

class MainActivity : AppCompatActivity()
{

  override fun onCreate(savedInstanceState: Bundle?)
  {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    FirebaseAnalytics.getInstance(this).setUserProperty("versionCode", BuildConfig.VERSION_CODE.toString())

    val updatePopupManager = UpdatePopupManager.Builder(this, BuildConfig.DEBUG)
        .setUpdatePopupActivity(CustomUpdatePopupActivity::class.java)
        .build();

    updatePopupManager.fetchRemoteConfig(true)
  }
}
