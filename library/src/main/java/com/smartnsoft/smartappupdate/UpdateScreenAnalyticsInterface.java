package com.smartnsoft.smartappupdate;

import android.os.Bundle;

/**
 * @author Adrien Vitti
 * @since 2018.03.05
 */

public interface UpdateScreenAnalyticsInterface
{

  // region Screen display

  void sendUpdateInfoDisplayed();

  void sendRecomendedUpdateDisplayed();

  void sendBlockingUpdateDisplayed();

  // endregion Screen display

  // region Action Events

  void sendAskLaterEvent();

  void closeAppWithoutUpdateEvent();

  void sendInformativeActionButtonEvent();

  void sendRecommendedActionButtonEvent();

  void sendBlockingActionButtonEvent();

  // endregion Action Events

  int getVersionCode();

  String getDateForAnalytics();

  Bundle generateAnalyticsExtraInfos();

}
