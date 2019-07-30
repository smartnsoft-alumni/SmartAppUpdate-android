package com.smartnsoft.smartappupdate

import android.os.Bundle

/**
 * @author Adrien Vitti
 * @since 2018.03.05
 */

interface UpdateScreenAnalyticsInterface
{

  // region Screen display

  fun sendUpdateInfoDisplayed()

  fun sendRecomendedUpdateDisplayed()

  fun sendBlockingUpdateDisplayed()

  // endregion Screen display

  // region Action Events

  fun sendAskLaterEvent()

  fun closeAppWithoutUpdateEvent()

  fun sendInformativeActionButtonEvent()

  fun sendRecommendedActionButtonEvent()

  fun sendBlockingActionButtonEvent()

  // endregion Action Events

  fun getVersionCode(): Int

  fun getDateForAnalytics(): String?

  fun generateAnalyticsExtraInfos(): Bundle?

}
