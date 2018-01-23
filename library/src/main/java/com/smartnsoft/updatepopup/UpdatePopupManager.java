package com.smartnsoft.updatepopup;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.smartnsoft.updatepopup.bo.UpdatePopupInformations;

/**
 * The class description here.
 *
 * @author Adrien Vitti
 * @since 2018.01.23
 */

public final class UpdatePopupManager
    implements OnCompleteListener<Void>
{

  @Retention(RetentionPolicy.SOURCE)
  @IntDef({ Informative, Recommended, Blocking })
  public @interface UpdatePopupType {}

  static boolean isUpdateTypeKnown(long updateType)
  {
    return updateType == Informative
        || updateType == Recommended
        || updateType == Blocking;
  }

  static final int Informative = 1;

  static final int Recommended = 2;

  static final int Blocking = 3;

  static final String REMOTE_CONFIG_TITLE = "title";

  static final String REMOTE_CONFIG_IMAGE_URL = "image";

  static final String REMOTE_CONFIG_CONTENT = "content";

  static final String REMOTE_CONFIG_BUTTON_TEXT = "actionButton";

  private static final String REMOTE_CONFIG_UPDATE_NEEDED = "update_needed";

  static final String REMOTE_CONFIG_IS_BLOCKING_UPDATE = "is_blocking_update";

  static final String REMOTE_CONFIG_ACTION_URL = "deeplinkAndroid";

  static final String REMOTE_CONFIG_PACKAGE_NAME_FOR_UPDATE = "package_name_for_update";

  static final String REMOTE_CONFIG_DIALOG_TYPE = "dialogType";

  static final String UPDATE_INFORMATION_EXTRA = "updateInformationExtra";

  private final FirebaseRemoteConfig firebaseRemoteConfig;

  private final Context applicationContext;

  public UpdatePopupManager(@NonNull Context context, final boolean isInDevelopmentMode)
  {
    this.applicationContext = context.getApplicationContext();
    firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
    FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
        .setDeveloperModeEnabled(isInDevelopmentMode)
        .build();
    firebaseRemoteConfig.setConfigSettings(configSettings);
  }

  protected void fetchRemoteConfig()
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
      firebaseRemoteConfig.fetch(cacheExpiration).addOnCompleteListener(this);
    }
  }

  @Override
  public void onComplete(@NonNull Task<Void> task)
  {
    if (task.isSuccessful())
    {
      firebaseRemoteConfig.activateFetched();

      /**
       * Add Logic for the remote config :
       * is update needed
       * is update blocking
       * is information about features
       */

      final UpdatePopupInformations updatePopupInformations = new UpdatePopupInformations(firebaseRemoteConfig.getString(UpdatePopupManager.REMOTE_CONFIG_TITLE),
          firebaseRemoteConfig.getString(UpdatePopupManager.REMOTE_CONFIG_IMAGE_URL),
          firebaseRemoteConfig.getString(UpdatePopupManager.REMOTE_CONFIG_CONTENT),
          firebaseRemoteConfig.getString(UpdatePopupManager.REMOTE_CONFIG_BUTTON_TEXT),
          firebaseRemoteConfig.getString(UpdatePopupManager.REMOTE_CONFIG_ACTION_URL),
          firebaseRemoteConfig.getString(UpdatePopupManager.REMOTE_CONFIG_PACKAGE_NAME_FOR_UPDATE),
          (int) firebaseRemoteConfig.getLong(UpdatePopupManager.REMOTE_CONFIG_DIALOG_TYPE));

      if (TextUtils.isEmpty(firebaseRemoteConfig.getString(UpdatePopupManager.REMOTE_CONFIG_TITLE)) == false && isUpdateTypeKnown(updatePopupInformations.updatePopupType))
      {
        final Intent intent = new Intent(applicationContext, UpdatePopupActivity.class);
        intent.putExtra(UpdatePopupManager.REMOTE_CONFIG_UPDATE_NEEDED, firebaseRemoteConfig.getBoolean(UpdatePopupManager.REMOTE_CONFIG_UPDATE_NEEDED));
        intent.putExtra(UpdatePopupManager.REMOTE_CONFIG_IS_BLOCKING_UPDATE, firebaseRemoteConfig.getBoolean(UpdatePopupManager.REMOTE_CONFIG_IS_BLOCKING_UPDATE));
        intent.putExtra(UpdatePopupManager.UPDATE_INFORMATION_EXTRA, updatePopupInformations);
        applicationContext.startActivity(intent);
      }
    }
  }

}
