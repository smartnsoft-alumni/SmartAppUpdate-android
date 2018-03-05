package com.smartnsoft.smartappupdate;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.annotation.NonNull;

/**
 * @author Adrien Vitti
 * @since 2018.03.01
 */
@SuppressWarnings({ "unused", "WeakerAccess" })
public final class SettingsUtil
{

  private static final String LAST_UPDATE_POPUP_CLICK_ON_LATER_TIMESTAMP_PREFERENCE_KEY = "smartappupdate_lastUpdatePopupClickOnLaterTimestamp";

  private static final String LAST_SEEN_VERSION_UPDATE_INFORMATION_PREFERENCE_KEY = "smartappupdate_lastSeenVersionUpdateInformation";

  private static final String RECOMMENDED_SCREEN_DISPLAY_COUNT_PREFERENCE_KEY = "smartappupdate_recommendedScreenDisplayCount";

  private static final String ACTION_ON_RECOMMENDED_SCREEN_DISPLAY_COUNT_PREFERENCE_KEY = "smartappupdate_actionOnRecommendedScreenDisplay";

  private static final String BLOCKING_SCREEN_DISPLAY_COUNT_PREFERENCE_KEY = "smartappupdate_blockingScreenDisplayCount";

  private static final String ACTION_ON_BLOCKING_SCREEN_DISPLAY_COUNT_PREFERENCE_KEY = "smartappupdate_actionOnBlockingScreenDisplay";

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
      int currentVersionCode,
      long versionCodeFromConfig)
  {
    if (preferences.contains(SettingsUtil.LAST_SEEN_VERSION_UPDATE_INFORMATION_PREFERENCE_KEY))
    {
      final long lastSeenChangelog = preferences.getLong(SettingsUtil.LAST_SEEN_VERSION_UPDATE_INFORMATION_PREFERENCE_KEY, -1);
      return lastSeenChangelog < versionCodeFromConfig;
    }
    else
    {
      setLastSeenInformativeUpdate(preferences, currentVersionCode);
      return false;
    }
  }

  public static void setLastSeenInformativeUpdate(@NonNull SharedPreferences preferences, long versionCode)
  {
    preferences.edit().putLong(SettingsUtil.LAST_SEEN_VERSION_UPDATE_INFORMATION_PREFERENCE_KEY, versionCode).apply();
  }

  // region analytics related information
  public static void setRecommendedScreenDisplayCount(@NonNull SharedPreferences preferences, long count)
  {
    preferences.edit().putLong(SettingsUtil.RECOMMENDED_SCREEN_DISPLAY_COUNT_PREFERENCE_KEY, count).apply();
  }

  public static void increaseRecommendedScreenDisplayCount(@NonNull SharedPreferences preferences)
  {
    preferences.edit().putLong(SettingsUtil.RECOMMENDED_SCREEN_DISPLAY_COUNT_PREFERENCE_KEY, getRecommendedScreenDisplayCount(preferences) + 1).apply();
  }

  public static long getRecommendedScreenDisplayCount(@NonNull SharedPreferences preferences)
  {
    return preferences.getLong(SettingsUtil.RECOMMENDED_SCREEN_DISPLAY_COUNT_PREFERENCE_KEY, 0);
  }

  public static void setActionOnRecommendedScreenDisplayCount(@NonNull SharedPreferences preferences, long count)
  {
    preferences.edit().putLong(SettingsUtil.ACTION_ON_RECOMMENDED_SCREEN_DISPLAY_COUNT_PREFERENCE_KEY, count).apply();
  }

  public static void increaseActionOnRecommendedScreenDisplayCount(@NonNull SharedPreferences preferences)
  {
    preferences.edit().putLong(SettingsUtil.ACTION_ON_RECOMMENDED_SCREEN_DISPLAY_COUNT_PREFERENCE_KEY, getActionOnRecommendedScreenDisplayCount(preferences) + 1).apply();
  }

  public static long getActionOnRecommendedScreenDisplayCount(@NonNull SharedPreferences preferences)
  {
    return preferences.getLong(SettingsUtil.ACTION_ON_RECOMMENDED_SCREEN_DISPLAY_COUNT_PREFERENCE_KEY, 0);
  }

  public static void setBlockingScreenDisplayCount(@NonNull SharedPreferences preferences, long count)
  {
    preferences.edit().putLong(SettingsUtil.BLOCKING_SCREEN_DISPLAY_COUNT_PREFERENCE_KEY, count).apply();
  }

  public static void increaseBlockingScreenDisplayCount(@NonNull SharedPreferences preferences)
  {
    preferences.edit().putLong(SettingsUtil.BLOCKING_SCREEN_DISPLAY_COUNT_PREFERENCE_KEY, getBlockingScreenDisplayCount(preferences) + 1).apply();
  }

  public static long getBlockingScreenDisplayCount(@NonNull SharedPreferences preferences)
  {
    return preferences.getLong(SettingsUtil.BLOCKING_SCREEN_DISPLAY_COUNT_PREFERENCE_KEY, 0);
  }

  public static void setActionOnBlockingScreenDisplayCount(@NonNull SharedPreferences preferences, long count)
  {
    preferences.edit().putLong(SettingsUtil.ACTION_ON_BLOCKING_SCREEN_DISPLAY_COUNT_PREFERENCE_KEY, count).apply();
  }

  public static void increaseActionOnBlockingScreenDisplayCount(@NonNull SharedPreferences preferences)
  {
    preferences.edit().putLong(SettingsUtil.ACTION_ON_BLOCKING_SCREEN_DISPLAY_COUNT_PREFERENCE_KEY, getActionOnBlockingScreenDisplayCount(preferences) + 1).apply();
  }

  public static long getActionOnBlockingScreenDisplayCount(@NonNull SharedPreferences preferences)
  {
    return preferences.getLong(SettingsUtil.ACTION_ON_BLOCKING_SCREEN_DISPLAY_COUNT_PREFERENCE_KEY, 0);
  }

  public static void resetAnalyticsPreferences(@NonNull SharedPreferences preferences)
  {
    setActionOnBlockingScreenDisplayCount(preferences, 0);
    setActionOnRecommendedScreenDisplayCount(preferences, 0);
    setRecommendedScreenDisplayCount(preferences, 0);
    setBlockingScreenDisplayCount(preferences, 0);
  }

  // endregion analytics related information

  public static Bundle backup(@NonNull SharedPreferences preferences)
  {
    final Bundle preferenceBackup = new Bundle();
    for (String key : preferences.getAll().keySet())
    {
      if (key.startsWith("smartappupdate_"))
      {
        preferenceBackup.putLong(key, ((Long) preferences.getAll().get(key)));
      }
    }
    return preferenceBackup;
  }

  public static void restore(@NonNull SharedPreferences preferences, @NonNull final Bundle backup)
  {
    final Editor edit = preferences.edit();
    for (String key : backup.keySet())
    {
      edit.putLong(key, backup.getLong(key));
    }
    edit.apply();
  }

}
