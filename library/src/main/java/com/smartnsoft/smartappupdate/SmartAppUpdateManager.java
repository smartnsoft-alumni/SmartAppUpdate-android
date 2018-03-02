package com.smartnsoft.smartappupdate;

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
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigInfo;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.smartnsoft.smartappupdate.bo.RemoteConfigMatchingInformation;
import com.smartnsoft.smartappupdate.bo.UpdatePopupInformations;

/**
 * @author Adrien Vitti
 * @since 2018.01.23
 */
@SuppressWarnings({ "unused", "WeakerAccess" })
public final class SmartAppUpdateManager
{

  @Retention(RetentionPolicy.SOURCE)
  @IntDef({ INFORMATION_ABOUT_UPDATE, RECOMMENDED_UPDATE, BLOCKING_UPDATE })
  public @interface UpdatePopupType {}


  private static boolean isUpdateTypeKnown(long updateType)
  {
    return updateType == INFORMATION_ABOUT_UPDATE
        || updateType == RECOMMENDED_UPDATE
        || updateType == BLOCKING_UPDATE;
  }

  public static class Builder
  {

    final SmartAppUpdateManager smartAppUpdateManager;

    public Builder(@NonNull Context context, boolean isInDevelopmentMode)
    {
      this.smartAppUpdateManager = new SmartAppUpdateManager(context, isInDevelopmentMode);
    }

    public Builder setFallbackUpdateApplicationId(@NonNull String applicationId)
    {
      smartAppUpdateManager.setFallbackUpdateApplicationId(applicationId);
      return this;
    }

    public Builder setUpdatePopupActivity(@NonNull Class<? extends SmartAppUpdateActivity> updatePopupActivity)
    {
      smartAppUpdateManager.setUpdatePopupActivityClass(updatePopupActivity);
      return this;
    }

    public Builder setRemoteConfigMatchingInformation(
        @NonNull RemoteConfigMatchingInformation remoteConfigMatchingInformation)
    {
      smartAppUpdateManager.setRemoteConfigMatchInformation(remoteConfigMatchingInformation);
      return this;
    }

    public Builder setSynchronousTimeoutDuration(@IntRange(from = 1) long synchronousTimeoutInMilliseconds)
    {
      if (synchronousTimeoutInMilliseconds <= 0)
      {
        throw new IllegalArgumentException("Timeout cannot be lower or equal to 0");
      }
      smartAppUpdateManager.setSynchronousTimeoutInMillisecond(synchronousTimeoutInMilliseconds);
      return this;
    }

    public Builder setMaxConfigCacheDuration(@IntRange(from = 1) long maxConfigCacheDurationInMillisecond)
    {
      if (maxConfigCacheDurationInMillisecond <= 0)
      {
        throw new IllegalArgumentException("Cache expiration duration cannot be lower or equal to 0");
      }
      smartAppUpdateManager.setMaxRemoteConfigCacheInMillisecond(maxConfigCacheDurationInMillisecond);
      return this;
    }

    public Builder setMinimumTimeBetweenTwoRecommendedPopup(
        @IntRange(from = 1) long minimumTimeBetweenTwoRecommendedPopupInMilliseconds)
    {
      if (minimumTimeBetweenTwoRecommendedPopupInMilliseconds <= 0)
      {
        throw new IllegalArgumentException("Time between two popup must be higher than 0");
      }
      smartAppUpdateManager.setMinimumTimeBetweenTwoRecommendedPopupInMilliseconds(minimumTimeBetweenTwoRecommendedPopupInMilliseconds);
      return this;
    }

    public SmartAppUpdateManager build()
    {
      return this.smartAppUpdateManager;
    }
  }

  static final int BLOCKING_UPDATE = 1;

  static final int RECOMMENDED_UPDATE = 2;

  static final int INFORMATION_ABOUT_UPDATE = 3;

  static final String UPDATE_INFORMATION_EXTRA = "updateInformationExtra";

  private static final long SYNCHRONISATION_TIMEOUT_IN_MILLISECONDS = 60 * 1000;

  private static final long MAXIMUM_CACHE_RETENTION_FOR_REMOTE_CONFIG_IN_MILLISECONDS = 24 * 60 * 60 * 1000;

  private static final long MINIMUM_TIME_BETWEEN_TWO_RECOMMENDED_POPUP_IN_MILLISECONDS = 3 * 24 * 60 * 60 * 1000;

  private static final long DAY_IN_MILLISECONDS = 24 * 60 * 60 * 1000;

  private static final String TAG = "SmartAppUpdateManager";

  private final FirebaseRemoteConfig firebaseRemoteConfig;

  private final boolean isInDevelopmentMode;

  private final Context applicationContext;

  private Class<? extends SmartAppUpdateActivity> updatePopupActivityClass = SmartAppUpdateActivity.class;

  private RemoteConfigMatchingInformation remoteConfigMatchingInformation = new RemoteConfigMatchingInformation();

  private long synchronousTimeoutInMillisecond = SmartAppUpdateManager.SYNCHRONISATION_TIMEOUT_IN_MILLISECONDS;

  private long maxConfigCacheDurationInMillisecond = SmartAppUpdateManager.MAXIMUM_CACHE_RETENTION_FOR_REMOTE_CONFIG_IN_MILLISECONDS;

  private long minimumTimeBetweenTwoRecommendedPopupInMilliseconds = SmartAppUpdateManager.MINIMUM_TIME_BETWEEN_TWO_RECOMMENDED_POPUP_IN_MILLISECONDS;

  private String fallbackUpdateApplicationId;

  private SmartAppUpdateManager(@NonNull Context context, final boolean isInDevelopmentMode)
  {
    this.applicationContext = context.getApplicationContext();
    firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
    this.isInDevelopmentMode = isInDevelopmentMode;
    FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
        .setDeveloperModeEnabled(isInDevelopmentMode)
        .build();
    firebaseRemoteConfig.setConfigSettings(configSettings);
  }

  public void setRemoteConfigMatchInformation(RemoteConfigMatchingInformation remoteConfigMatchInformation)
  {
    this.remoteConfigMatchingInformation = remoteConfigMatchInformation;
  }

  public void setFallbackUpdateApplicationId(String fallbackUpdateApplicationId)
  {
    this.fallbackUpdateApplicationId = fallbackUpdateApplicationId;
  }

  void setUpdatePopupActivityClass(
      Class<? extends SmartAppUpdateActivity> updatePopupActivityClass)
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

  public final void createAndDisplayPopup()
  {
    final Intent popupIntent = createPopupIntent();
    if (popupIntent != null)
    {
      applicationContext.startActivity(popupIntent);
    }
  }

  @Nullable
  public final Intent createPopupIntent()
  {
    final UpdatePopupInformations updatePopupInformations = new UpdatePopupInformations();

    updatePopupInformations.title = firebaseRemoteConfig.getString(remoteConfigMatchingInformation.popupTitle);
    updatePopupInformations.imageURL = firebaseRemoteConfig.getString(remoteConfigMatchingInformation.imageURL);
    updatePopupInformations.updateContent = firebaseRemoteConfig.getString(remoteConfigMatchingInformation.updateContent);
    updatePopupInformations.changelogContent = firebaseRemoteConfig.getString(remoteConfigMatchingInformation.changelogContent);
    updatePopupInformations.actionButtonLabel = firebaseRemoteConfig.getString(remoteConfigMatchingInformation.actionButtonText);
    updatePopupInformations.deepLink = firebaseRemoteConfig.getString(remoteConfigMatchingInformation.updateActionDeeplink);

    final String packageNameFromRemoteConfig = firebaseRemoteConfig.getString(remoteConfigMatchingInformation.packageName);
    updatePopupInformations.packageName = TextUtils.isEmpty(packageNameFromRemoteConfig) ? fallbackUpdateApplicationId : packageNameFromRemoteConfig;
    updatePopupInformations.versionCode = firebaseRemoteConfig.getLong(remoteConfigMatchingInformation.currentVersionCode);
    updatePopupInformations.updatePopupType = (int) firebaseRemoteConfig.getLong(remoteConfigMatchingInformation.dialogType);
    final long minimumTimeBetweenTwoRecommendedPopupInDays = firebaseRemoteConfig.getLong(remoteConfigMatchingInformation.askLaterSnoozeInDays);


    if (minimumTimeBetweenTwoRecommendedPopupInDays != 0)
    {
      minimumTimeBetweenTwoRecommendedPopupInMilliseconds = minimumTimeBetweenTwoRecommendedPopupInDays * SmartAppUpdateManager.DAY_IN_MILLISECONDS;
    }

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
                      && SettingsUtil.getUpdateLaterTimestamp(defaultSharedPreferences) + minimumTimeBetweenTwoRecommendedPopupInMilliseconds < System.currentTimeMillis()
              )
              ||
              (
                  // A changelog message (Informative type) has not already been seen
                  updatePopupInformations.updatePopupType == INFORMATION_ABOUT_UPDATE
                      && SettingsUtil.shouldDisplayInformativeUpdate(defaultSharedPreferences, updatePopupInformations.versionCode)
              )
          )
      {
        final Intent intent = new Intent(applicationContext, updatePopupActivityClass);
        intent.putExtra(SmartAppUpdateManager.UPDATE_INFORMATION_EXTRA, updatePopupInformations);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
      }
    }

    return null;
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

  public void refreshConfigIfNeededAndDisplayPopup()
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
