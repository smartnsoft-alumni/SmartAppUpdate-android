package com.smartnsoft.updatepopup;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.AnyThread;
import android.support.annotation.IntDef;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigInfo;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.smartnsoft.updatepopup.bo.UpdatePopupInformations;

/**
 * @author Adrien Vitti
 * @since 2018.01.23
 */
@SuppressWarnings("unused")
public final class UpdatePopupManager
{

  @Retention(RetentionPolicy.SOURCE)
  @IntDef({ INFORMATION_ABOUT_UPDATE, RECOMMENDED_UPDATE, BLOCKING_UPDATE })
  public @interface UpdatePopupType {}

  public static void setUpdateLaterTimestamp(@NonNull SharedPreferences preferences, long updateLaterTimestamp)
  {
    preferences.edit().putLong(UpdatePopupManager.LAST_UPDATE_POPUP_CLICK_ON_LATER_TIMESTAMP_PREFERENCE_KEY, updateLaterTimestamp).apply();
  }

  public static long getUpdateLaterTimestamp(@NonNull SharedPreferences preferences)
  {
    return preferences.getLong(UpdatePopupManager.LAST_UPDATE_POPUP_CLICK_ON_LATER_TIMESTAMP_PREFERENCE_KEY, -1);
  }

  public static long getLastSeenInformativeUpdate(@NonNull SharedPreferences preferences)
  {
    return preferences.getLong(UpdatePopupManager.LAST_SEEN_VERSION_UPDATE_INFORMATION_PREFERENCE_KEY, -1);
  }

  public static void setLastSeenInformativeUpdate(@NonNull SharedPreferences preferences, long versionCode)
  {
    preferences.edit().putLong(UpdatePopupManager.LAST_SEEN_VERSION_UPDATE_INFORMATION_PREFERENCE_KEY, versionCode).apply();
  }

  private static boolean isUpdateTypeKnown(long updateType)
  {
    return updateType == INFORMATION_ABOUT_UPDATE
        || updateType == RECOMMENDED_UPDATE
        || updateType == BLOCKING_UPDATE;
  }

  public static class Builder
  {

    final UpdatePopupManager updatePopupManager;

    public Builder(@NonNull Context context, boolean isInDevelopmentMode)
    {
      this.updatePopupManager = new UpdatePopupManager(context, isInDevelopmentMode);
    }

    public Builder setFallbackUpdateApplicationId(@NonNull String applicationId)
    {
      updatePopupManager.setFallbackUpdateApplicationId(applicationId);
      return this;
    }

    public Builder setUpdatePopupActivity(@NonNull Class<? extends UpdatePopupActivity> updatePopupActivity)
    {
      updatePopupManager.setUpdatePopupActivityClass(updatePopupActivity);
      return this;
    }

    public Builder setSynchronousTimeoutDuration(@IntRange(from = 1) long synchronousTimeoutInMilliseconds)
    {
      if (synchronousTimeoutInMilliseconds <= 0)
      {
        throw new IllegalArgumentException("Timeout cannot be lower or equal to 0");
      }
      updatePopupManager.setSynchronousTimeoutInMillisecond(synchronousTimeoutInMilliseconds);
      return this;
    }

    public Builder setMaxConfigCacheDuration(@IntRange(from = 1) long maxConfigCacheDurationInMillisecond)
    {
      if (maxConfigCacheDurationInMillisecond <= 0)
      {
        throw new IllegalArgumentException("Cache expiration duration cannot be lower or equal to 0");
      }
      updatePopupManager.setMaxRemoteConfigCacheInMillisecond(maxConfigCacheDurationInMillisecond);
      return this;
    }

    public Builder setMinimumTimeBetweenTwoRecommendedPopup(
        @IntRange(from = 1) long minimumTimeBetweenTwoRecommendedPopupInMilliseconds)
    {
      if (minimumTimeBetweenTwoRecommendedPopupInMilliseconds <= 0)
      {
        throw new IllegalArgumentException("Time between two popup must be higher than 0");
      }
      updatePopupManager.setMinimumTimeBetweenTwoRecommendedPopupInMilliseconds(minimumTimeBetweenTwoRecommendedPopupInMilliseconds);
      return this;
    }

    public UpdatePopupManager build()
    {
      return this.updatePopupManager;
    }
  }

  static final int INFORMATION_ABOUT_UPDATE = 1;

  static final int RECOMMENDED_UPDATE = 2;

  static final int BLOCKING_UPDATE = 3;

  static final String UPDATE_INFORMATION_EXTRA = "updateInformationExtra";

  private static final long SYNCHRONISATION_TIMEOUT_IN_MILLISECONDS = 60 * 1000;

  private static final long MAXIMUM_CACHE_RETENTION_FOR_REMOTE_CONFIG_IN_MILLISECONS = 24 * 60 * 60 * 1000;

  private static final long MINIMUM_TIME_BETWEEN_TWO_RECOMMENDED_POPUP_IN_MILLISECONS = 3 * 24 * 60 * 60 * 1000;

  private static final String TAG = "UpdatePopupManager";

  private static final String REMOTE_CONFIG_TITLE = "title";

  private static final String REMOTE_CONFIG_IMAGE_URL = "imageURL";

  private static final String REMOTE_CONFIG_UPDATE_CONTENT = "update_content";

  private static final String REMOTE_CONFIG_CHANGELOG_CONTENT = "changelog_content";

  private static final String REMOTE_CONFIG_BUTTON_TEXT = "actionButtonLabel";

  private static final String REMOTE_CONFIG_ACTION_URL = "deeplink";

  private static final String REMOTE_CONFIG_PACKAGE_NAME_FOR_UPDATE = "package_name_for_update";

  private static final String REMOTE_CONFIG_CURRENT_VERSION_CODE = "current_version_code";

  private static final String REMOTE_CONFIG_DIALOG_TYPE = "dialogType";

  private static final String LAST_UPDATE_POPUP_CLICK_ON_LATER_TIMESTAMP_PREFERENCE_KEY = "lastUpdatePopupClickOnLaterTimestamp";

  private static final String LAST_SEEN_VERSION_UPDATE_INFORMATION_PREFERENCE_KEY = "lastSeenVersionUpdateInformation";

  private final FirebaseRemoteConfig firebaseRemoteConfig;

  private final boolean isInDevelopmentMode;

  private final Context applicationContext;

  private Class<? extends UpdatePopupActivity> updatePopupActivityClass = UpdatePopupActivity.class;

  private long synchronousTimeoutInMillisecond = UpdatePopupManager.SYNCHRONISATION_TIMEOUT_IN_MILLISECONDS;

  private long maxConfigCacheDurationInMillisecond = UpdatePopupManager.MAXIMUM_CACHE_RETENTION_FOR_REMOTE_CONFIG_IN_MILLISECONS;

  private long minimumTimeBetweenTwoRecommendedPopupInMilliseconds = UpdatePopupManager.MINIMUM_TIME_BETWEEN_TWO_RECOMMENDED_POPUP_IN_MILLISECONS;

  private String fallbackUpdateApplicationId;

  private UpdatePopupManager(@NonNull Context context, final boolean isInDevelopmentMode)
  {
    this.applicationContext = context.getApplicationContext();
    firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
    this.isInDevelopmentMode = isInDevelopmentMode;
    FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
        .setDeveloperModeEnabled(isInDevelopmentMode)
        .build();
    firebaseRemoteConfig.setConfigSettings(configSettings);
  }

  public void setFallbackUpdateApplicationId(String fallbackUpdateApplicationId)
  {
    this.fallbackUpdateApplicationId = fallbackUpdateApplicationId;
  }

  void setUpdatePopupActivityClass(
      Class<? extends UpdatePopupActivity> updatePopupActivityClass)
  {
    this.updatePopupActivityClass = updatePopupActivityClass;
  }

  void setSynchronousTimeoutInMillisecond(long synchronousTimeoutInMillisecond)
  {
    this.synchronousTimeoutInMillisecond = synchronousTimeoutInMillisecond;
  }

  void setMaxRemoteConfigCacheInMillisecond(long maxConfigCacheDurationInMillisecond)
  {
    this.maxConfigCacheDurationInMillisecond = maxConfigCacheDurationInMillisecond;
  }

  void setMinimumTimeBetweenTwoRecommendedPopupInMilliseconds(long minimumTimeBetweenTwoRecommendedPopupInMilliseconds)
  {
    this.minimumTimeBetweenTwoRecommendedPopupInMilliseconds = minimumTimeBetweenTwoRecommendedPopupInMilliseconds;
  }

  @AnyThread
  public void fetchRemoteConfig(final boolean shouldShowPopupAfterFetch)
  {
    if (firebaseRemoteConfig != null)
    {
      long cacheExpiration = 3600; // 1 hour in seconds.
      // If your app is using developer mode, cacheExpiration is set to 0, so each fetch will
      // retrieve values from the service.
      if (firebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled())
      {
        cacheExpiration = 0;
      }
      firebaseRemoteConfig.fetch(cacheExpiration).addOnCompleteListener(new OnCompleteListener<Void>()
      {
        @Override
        public void onComplete(@NonNull Task<Void> task)
        {
          if (task.isSuccessful())
          {
            onUpdateSucessful(shouldShowPopupAfterFetch);
          }
        }
      });
    }
  }

  private void onUpdateSucessful(final boolean shouldShowPopupAfterFetch)
  {
    firebaseRemoteConfig.activateFetched();
    if (shouldShowPopupAfterFetch)
    {
      createAndDisplayPopup();
    }
  }

  private void createAndDisplayPopup()
  {
    final UpdatePopupInformations updatePopupInformations = new UpdatePopupInformations();
    updatePopupInformations.title = firebaseRemoteConfig.getString(UpdatePopupManager.REMOTE_CONFIG_TITLE);
    updatePopupInformations.imageURL = firebaseRemoteConfig.getString(UpdatePopupManager.REMOTE_CONFIG_IMAGE_URL);
    updatePopupInformations.updateContent = firebaseRemoteConfig.getString(UpdatePopupManager.REMOTE_CONFIG_UPDATE_CONTENT);
    updatePopupInformations.changelogContent = firebaseRemoteConfig.getString(UpdatePopupManager.REMOTE_CONFIG_CHANGELOG_CONTENT);
    updatePopupInformations.actionButtonLabel = firebaseRemoteConfig.getString(UpdatePopupManager.REMOTE_CONFIG_BUTTON_TEXT);
    updatePopupInformations.deepLink = firebaseRemoteConfig.getString(UpdatePopupManager.REMOTE_CONFIG_ACTION_URL);
    final String packageNameFromRemoteConfig = firebaseRemoteConfig.getString(UpdatePopupManager.REMOTE_CONFIG_PACKAGE_NAME_FOR_UPDATE);
    updatePopupInformations.packageName = TextUtils.isEmpty(packageNameFromRemoteConfig) ? fallbackUpdateApplicationId : packageNameFromRemoteConfig;
    updatePopupInformations.versionCode = firebaseRemoteConfig.getLong(UpdatePopupManager.REMOTE_CONFIG_CURRENT_VERSION_CODE);
    updatePopupInformations.updatePopupType = (int) firebaseRemoteConfig.getLong(UpdatePopupManager.REMOTE_CONFIG_DIALOG_TYPE);

    final boolean isUpdateTypeKnown = isUpdateTypeKnown(updatePopupInformations.updatePopupType);
    if (isInDevelopmentMode)
    {
      Log.d(TAG, "UpdateType=" + updatePopupInformations.updatePopupType + " which can" + (isUpdateTypeKnown ? "" : "not ") + " be processed");
    }

    if (isUpdateTypeKnown)
    {
      final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
      if (
          updatePopupInformations.updatePopupType == BLOCKING_UPDATE // Forced to launch the popup
              ||
              (
                  // It's recommended and the waiting delay after a "ask later" is over
                  updatePopupInformations.updatePopupType == RECOMMENDED_UPDATE
                      && UpdatePopupManager.getUpdateLaterTimestamp(defaultSharedPreferences) + minimumTimeBetweenTwoRecommendedPopupInMilliseconds > System.currentTimeMillis()
              )
              ||
              (
                  // A changelog message (Informative type) has not already been seen
                  updatePopupInformations.updatePopupType == INFORMATION_ABOUT_UPDATE
                      && UpdatePopupManager.getLastSeenInformativeUpdate(defaultSharedPreferences) < updatePopupInformations.versionCode
              )
          )
      {
        final Intent intent = new Intent(applicationContext, updatePopupActivityClass);
        intent.putExtra(UpdatePopupManager.UPDATE_INFORMATION_EXTRA, updatePopupInformations);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        applicationContext.startActivity(intent);
      }
    }
  }

  @WorkerThread
  public void fetchRemoteConfigSync(final boolean shouldShowPopupAfterFetch)
  {
    if (firebaseRemoteConfig != null)
    {
      final Object mutex = new Object();
      long cacheExpiration = 3600; // 1 hour in seconds.
      // If your app is using developer mode, cacheExpiration is set to 0, so each fetch will
      // retrieve values from the service.
      if (firebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled())
      {
        cacheExpiration = 0;
      }

      final long startTime = System.currentTimeMillis();
      firebaseRemoteConfig.fetch(cacheExpiration).addOnCompleteListener(new OnCompleteListener<Void>()
      {
        @Override
        public void onComplete(@NonNull Task<Void> task)
        {
          if (isInDevelopmentMode)
          {
            Log.d(TAG, "Synchronous Remote Config retrieving task took " + (System.currentTimeMillis() - startTime) + "ms");
          }
          if (task.isSuccessful())
          {
            onUpdateSucessful(shouldShowPopupAfterFetch);
          }
          else
          {
            if (isInDevelopmentMode)
            {
              Log.w(TAG, "Synchronous Remote Config retrieving task has failed");
            }
          }
          synchronized (mutex)
          {
            mutex.notify();
          }
        }
      });

      try
      {
        synchronized (mutex)
        {
          mutex.wait(synchronousTimeoutInMillisecond);
        }
      }
      catch (InterruptedException exception)
      {
        if (isInDevelopmentMode)
        {
          Log.w(TAG, "An interruption was caught when retrieving remote config synchronously", exception);
        }
      }
    }
  }

  public void reShowUpdatePopupIfNeeded()
  {
    final FirebaseRemoteConfigInfo firebaseRemoteConfigInfo = firebaseRemoteConfig.getInfo();
    if (firebaseRemoteConfigInfo.getLastFetchStatus() == FirebaseRemoteConfig.LAST_FETCH_STATUS_SUCCESS
        && firebaseRemoteConfigInfo.getFetchTimeMillis() >= System.currentTimeMillis() - maxConfigCacheDurationInMillisecond)
    {
      createAndDisplayPopup();
    }
    else if (firebaseRemoteConfigInfo.getLastFetchStatus() != FirebaseRemoteConfig.LAST_FETCH_STATUS_THROTTLED)
    {
      // need to refetch
      fetchRemoteConfig(true);
    }
  }

}
