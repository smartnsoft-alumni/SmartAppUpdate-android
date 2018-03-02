package com.smartnsoft.smartappupdate;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

/**
 * @author Adrien Vitti
 * @since 2018.03.01
 */

public final class SettingsUtil
{

  private static final String LAST_UPDATE_POPUP_CLICK_ON_LATER_TIMESTAMP_PREFERENCE_KEY = "smartappupdate_lastUpdatePopupClickOnLaterTimestamp";

  private static final String LAST_SEEN_VERSION_UPDATE_INFORMATION_PREFERENCE_KEY = "smartappupdate_lastSeenVersionUpdateInformation";

  public static void setUpdateLaterTimestamp(@NonNull SharedPreferences preferences, long updateLaterTimestamp)
  {
    preferences.edit().putLong(SettingsUtil.LAST_UPDATE_POPUP_CLICK_ON_LATER_TIMESTAMP_PREFERENCE_KEY, updateLaterTimestamp).apply();
  }

  public static long getUpdateLaterTimestamp(@NonNull SharedPreferences preferences)
  {
    return preferences.getLong(SettingsUtil.LAST_UPDATE_POPUP_CLICK_ON_LATER_TIMESTAMP_PREFERENCE_KEY, -1);
  }

  public static long getLastSeenInformativeUpdate(@NonNull SharedPreferences preferences)
  {
    return preferences.getLong(SettingsUtil.LAST_SEEN_VERSION_UPDATE_INFORMATION_PREFERENCE_KEY, -1);
  }

  public static boolean shouldDisplayInformativeUpdate(@NonNull SharedPreferences preferences,
      long versionCodeFromConfig)
  {
    if (preferences.contains(SettingsUtil.LAST_SEEN_VERSION_UPDATE_INFORMATION_PREFERENCE_KEY))
    {
      final long lastSeenChangelog = preferences.getLong(SettingsUtil.LAST_SEEN_VERSION_UPDATE_INFORMATION_PREFERENCE_KEY, -1);
      return lastSeenChangelog < versionCodeFromConfig;
    }
    else
    {
      setLastSeenInformativeUpdate(preferences, versionCodeFromConfig);
      return false;
    }
  }

  public static void setLastSeenInformativeUpdate(@NonNull SharedPreferences preferences, long versionCode)
  {
    preferences.edit().putLong(SettingsUtil.LAST_SEEN_VERSION_UPDATE_INFORMATION_PREFERENCE_KEY, versionCode).apply();
  }

}
