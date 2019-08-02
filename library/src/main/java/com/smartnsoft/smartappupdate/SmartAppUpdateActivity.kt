package com.smartnsoft.smartappupdate

import android.os.Bundle

/**
 * @author Adrien Vitti
 * @since 2018.01.23
 */
class SmartAppUpdateActivity : AbstractSmartAppUpdateActivity()
{

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
