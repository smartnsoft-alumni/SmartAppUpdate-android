package com.smartnsoft.smartappupdate;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.AnyThread;
import androidx.annotation.IntDef;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

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
  @IntDef({
      SmartAppUpdateManager.INFORMATION_ABOUT_UPDATE,
      SmartAppUpdateManager.RECOMMENDED_UPDATE,
      SmartAppUpdateManager.BLOCKING_UPDATE
  })
  public @interface UpdatePopupType {}


  private static boolean isUpdateTypeKnown(long updateType)
  {
    return updateType == SmartAppUpdateManager.INFORMATION_ABOUT_UPDATE
        || updateType == SmartAppUpdateManager.RECOMMENDED_UPDATE
        || updateType == SmartAppUpdateManager.BLOCKING_UPDATE;
  }

  public static class Builder
  {

    final SmartAppUpdateManager smartAppUpdateManager;

    public Builder(@NonNull Context context, final int currentVersionCode, boolean isInDevelopmentMode)
    {
      this.smartAppUpdateManager = new SmartAppUpdateManager(context, currentVersionCode, isInDevelopmentMode)
      ;
    }

    public Builder setFallbackUpdateApplicationId(@NonNull String applicationId)
    {
      smartAppUpdateManager.setFallbackUpdateApplicationId(applicationId);
      return this;
    }

    public Builder setUpdatePopupActivity(@NonNull Class<? extends AbstractSmartAppUpdateActivity> updatePopupActivity)
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

  private final int currentVersionCode;

  private final boolean isInDevelopmentMode;

  private final Context applicationContext;

  private Class<? extends AbstractSmartAppUpdateActivity> updatePopupActivityClass = SmartAppUpdateActivity.class;

  private RemoteConfigMatchingInformation remoteConfigMatchingInformation = new RemoteConfigMatchingInformation();

  private long synchronousTimeoutInMillisecond = SmartAppUpdateManager.SYNCHRONISATION_TIMEOUT_IN_MILLISECONDS;

  private long maxConfigCacheDurationInMillisecond = SmartAppUpdateManager.MAXIMUM_CACHE_RETENTION_FOR_REMOTE_CONFIG_IN_MILLISECONDS;

  private long minimumTimeBetweenTwoRecommendedPopupInMilliseconds = SmartAppUpdateManager.MINIMUM_TIME_BETWEEN_TWO_RECOMMENDED_POPUP_IN_MILLISECONDS;

  private String fallbackUpdateApplicationId;

  private UpdatePopupInformations updatePopupInformations;

  private SmartAppUpdateManager(@NonNull Context context, final int currentVersionCode,
      final boolean isInDevelopmentMode)
  {
    this.applicationContext = context.getApplicationContext();
    firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
    this.isInDevelopmentMode = isInDevelopmentMode;
    this.currentVersionCode = currentVersionCode;
    final FirebaseRemoteConfigSettings.Builder configBuilder = new FirebaseRemoteConfigSettings.Builder();
    if (isInDevelopmentMode)
    {
      configBuilder.setMinimumFetchIntervalInSeconds(0);
    }
    final FirebaseRemoteConfigSettings configSettings = configBuilder.build();
    firebaseRemoteConfig.setConfigSettingsAsync(configSettings);

    final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
    if (SettingsUtil.getLastSeenInformativeUpdate(sharedPreferences) == -1)
    {
      SettingsUtil.setLastSeenInformativeUpdate(sharedPreferences, currentVersionCode);
    }
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
      Class<? extends AbstractSmartAppUpdateActivity> updatePopupActivityClass)
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
      if (isInDevelopmentMode)
      {
        cacheExpiration = firebaseRemoteConfig.getInfo().getConfigSettings().getMinimumFetchIntervalInSeconds();
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
    firebaseRemoteConfig.activate();
    createUpdateInformations();
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

  private void createUpdateInformations()
  {
    updatePopupInformations = new UpdatePopupInformations();

    updatePopupInformations.setTitle(firebaseRemoteConfig.getString(remoteConfigMatchingInformation.getPopupTitle()));
    updatePopupInformations.setImageURL(firebaseRemoteConfig.getString(remoteConfigMatchingInformation.getImageURL()));
    updatePopupInformations.setUpdateContent(firebaseRemoteConfig.getString(remoteConfigMatchingInformation.getUpdateContent()));
    updatePopupInformations.setChangelogContent(firebaseRemoteConfig.getString(remoteConfigMatchingInformation.getChangelogContent()));
    updatePopupInformations.setActionButtonLabel(firebaseRemoteConfig.getString(remoteConfigMatchingInformation.getActionButtonText()));
    updatePopupInformations.setDeepLink(firebaseRemoteConfig.getString(remoteConfigMatchingInformation.getUpdateActionDeeplink()));

    final String packageNameFromRemoteConfig = firebaseRemoteConfig.getString(remoteConfigMatchingInformation.getPackageName());
    updatePopupInformations.setPackageName(TextUtils.isEmpty(packageNameFromRemoteConfig) ? fallbackUpdateApplicationId : packageNameFromRemoteConfig);
    updatePopupInformations.setVersionCode(firebaseRemoteConfig.getLong(remoteConfigMatchingInformation.getCurrentVersionCode()));
    updatePopupInformations.setUpdatePopupType((int) firebaseRemoteConfig.getLong(remoteConfigMatchingInformation.getDialogType()));
    final long minimumTimeBetweenTwoRecommendedPopupInDays = firebaseRemoteConfig.getLong(remoteConfigMatchingInformation.getAskLaterSnoozeInDays());

    if (minimumTimeBetweenTwoRecommendedPopupInDays != 0)
    {
      minimumTimeBetweenTwoRecommendedPopupInMilliseconds = minimumTimeBetweenTwoRecommendedPopupInDays * SmartAppUpdateManager.DAY_IN_MILLISECONDS;
    }

    if (updatePopupInformations.getVersionCode() == currentVersionCode)
    {
      final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
      SettingsUtil.resetAnalyticsPreferences(sharedPreferences);
      SettingsUtil.setUpdateLaterTimestamp(sharedPreferences, -1);
    }
  }

  @Nullable
  public final Intent createPopupIntent()
  {
    if (updatePopupInformations == null)
    {
      return null;
    }

    final boolean isUpdateTypeKnown = isUpdateTypeKnown(updatePopupInformations.getUpdatePopupType());
    if (isInDevelopmentMode)
    {
      Log.d(TAG, "UpdateType=" + updatePopupInformations.getUpdatePopupType() + " which can" + (isUpdateTypeKnown ? "" : "not ") + " be processed");
    }

    if (isUpdateTypeKnown)
    {
      final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
      if (
          isBlockingUpdate() // Forced to launch the popup
              ||
              isRecommendedUpdate(defaultSharedPreferences)
              ||
              isInformativeUpdate(defaultSharedPreferences)
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

  public boolean isBlockingUpdate()
  {
    return updatePopupInformations != null
        && updatePopupInformations.getUpdatePopupType() == SmartAppUpdateManager.BLOCKING_UPDATE
        && currentVersionCode < updatePopupInformations.getVersionCode();
  }

  public boolean isInformativeUpdate(final SharedPreferences defaultSharedPreferences)
  {
    return updatePopupInformations != null
        && updatePopupInformations.getVersionCode() == currentVersionCode
        && updatePopupInformations.getUpdatePopupType() == SmartAppUpdateManager.INFORMATION_ABOUT_UPDATE
        // A changelog message (Informative type) has not already been seen
        && SettingsUtil.shouldDisplayInformativeUpdate(defaultSharedPreferences, currentVersionCode, updatePopupInformations.getVersionCode());
  }

  public boolean isRecommendedUpdate(final SharedPreferences defaultSharedPreferences)
  {
    return
        updatePopupInformations != null
            && currentVersionCode < updatePopupInformations.getVersionCode()
            // It's recommended and the waiting delay after a "ask later" is over
            && updatePopupInformations.getUpdatePopupType() == SmartAppUpdateManager.RECOMMENDED_UPDATE
            && SettingsUtil.getUpdateLaterTimestamp(defaultSharedPreferences) + minimumTimeBetweenTwoRecommendedPopupInMilliseconds < System.currentTimeMillis();
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
      if (isInDevelopmentMode)
      {
        cacheExpiration = firebaseRemoteConfig.getInfo().getConfigSettings().getMinimumFetchIntervalInSeconds();
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
      createUpdateInformations();
      createAndDisplayPopup();
    }
    else if (firebaseRemoteConfigInfo.getLastFetchStatus() != FirebaseRemoteConfig.LAST_FETCH_STATUS_THROTTLED)
    {
      // need to re-fetch
      fetchRemoteConfig(true);
    }
  }

}
