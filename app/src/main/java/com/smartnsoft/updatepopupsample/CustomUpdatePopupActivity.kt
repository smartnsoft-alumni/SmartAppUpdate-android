package com.smartnsoft.updatepopupsample

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.View
import com.bumptech.glide.Glide
import com.smartnsoft.smartappupdate.AbstractSmartAppUpdateActivity

/**
 * @author Adrien Vitti
 * @since 2018.01.25
 */

class CustomUpdatePopupActivity : AbstractSmartAppUpdateActivity()
{

  override fun setImage(imageURLFromRemoteConfig: String?)
  {
    if (imageURLFromRemoteConfig.isNullOrBlank())
    {
      return
    }
    image.visibility = View.VISIBLE
    Glide.with(this).load(imageURLFromRemoteConfig).into(image)
  }

  override fun setContent(contentFromRemoteConfig: String?)
  {
    if (contentFromRemoteConfig.isNullOrBlank())
    {
      return
    }
    paragraph.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
    {
      Html.fromHtml(contentFromRemoteConfig, Html.FROM_HTML_MODE_LEGACY)
    }
    else
    {
      @Suppress("DEPRECATION")
      Html.fromHtml(contentFromRemoteConfig)
    }
  }

  override fun sendUpdateInfoDisplayed()
  {

  }

  override fun sendRecomendedUpdateDisplayed()
  {

  }

  override fun sendBlockingUpdateDisplayed()
  {

  }

  override fun sendAskLaterEvent()
  {

  }

  override fun closeAppWithoutUpdateEvent()
  {

  }

  override fun sendInformativeActionButtonEvent()
  {

  }

  override fun sendRecommendedActionButtonEvent()
  {

  }

  override fun sendBlockingActionButtonEvent()
  {

  }

  override fun getVersionCode(): Int
  {
    return 0
  }

  override fun getDateForAnalytics(): String?
  {
    return null
  }

  override fun generateAnalyticsExtraInfos(): Bundle?
  {
    return null
  }
}
