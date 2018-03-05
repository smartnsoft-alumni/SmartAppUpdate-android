package com.smartnsoft.smartappupdate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartnsoft.smartappupdate.bo.UpdatePopupInformations;

/**
 * @author Adrien Vitti
 * @since 2018.03.05
 */

public abstract class AbstractSmartAppUpdateActivity
    extends AppCompatActivity
    implements OnClickListener, UpdateScreenAnalyticsInterface
{

  protected TextView title;

  protected TextView paragraph;

  protected TextView action;

  protected TextView later;

  protected ImageView close;

  protected ImageView image;

  protected UpdatePopupInformations updateInformation;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(getLayoutId());
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

    bindViews();

    final Bundle bundle = getIntent().getExtras();
    if (bundle != null)
    {
      updateInformation = (UpdatePopupInformations) bundle.getSerializable(SmartAppUpdateManager.UPDATE_INFORMATION_EXTRA);
      if (updateInformation == null)
      {
        finish();
      }
    }

    switch (updateInformation.updatePopupType)
    {
      case SmartAppUpdateManager.BLOCKING_UPDATE:
        SettingsUtil.increaseBlockingScreenDisplayCount(PreferenceManager.getDefaultSharedPreferences(this));
        sendBlockingUpdateDisplayed();
        break;
      case SmartAppUpdateManager.INFORMATION_ABOUT_UPDATE:
        sendUpdateInfoDisplayed();
        break;
      case SmartAppUpdateManager.RECOMMENDED_UPDATE:
        SettingsUtil.increaseRecommendedScreenDisplayCount(PreferenceManager.getDefaultSharedPreferences(this));
        sendRecomendedUpdateDisplayed();
        break;
    }

    updateLayoutWithUpdateInformation(updateInformation);
  }

  protected void bindViews()
  {
    title = findViewById(R.id.title);
    paragraph = findViewById(R.id.paragraph);

    action = findViewById(R.id.actionButton);
    if (action != null)
    {
      action.setOnClickListener(this);
    }

    image = findViewById(R.id.image);

    later = findViewById(R.id.laterButton);
    if (later != null)
    {
      later.setOnClickListener(this);
    }

    close = findViewById(R.id.closeButton);
    if (close != null)
    {
      close.setOnClickListener(this);
    }
  }

  @LayoutRes
  protected int getLayoutId()
  {
    return R.layout.smartappupdate_popup_activity;
  }

  protected void updateLayoutWithUpdateInformation(UpdatePopupInformations updateInformation)
  {
    switch (updateInformation.updatePopupType)
    {
      case SmartAppUpdateManager.BLOCKING_UPDATE:
        later.setVisibility(View.GONE);
        close.setVisibility(View.GONE);
        setContent(updateInformation.updateContent);
        break;
      case SmartAppUpdateManager.RECOMMENDED_UPDATE:
        later.setVisibility(View.VISIBLE);
        setContent(updateInformation.updateContent);
        close.setVisibility(View.VISIBLE);
        break;
      case SmartAppUpdateManager.INFORMATION_ABOUT_UPDATE:
      default:
        close.setVisibility(View.VISIBLE);
        setContent(updateInformation.changelogContent);
        break;
    }

    setTitle(updateInformation.title);
    setButtonLabel(updateInformation.actionButtonLabel);
    setImage(updateInformation.imageURL);
  }

  @Override
  public void onClick(View view)
  {
    if (view == action)
    {
      onActionButtonClick(updateInformation);
    }
    else if (view == later || view == close)
    {
      closePopup();
    }
  }

  protected void onActionButtonClick(UpdatePopupInformations updateInformation)
  {
    if (updateInformation.updatePopupType == SmartAppUpdateManager.INFORMATION_ABOUT_UPDATE)
    {
      sendInformativeActionButtonEvent();
      closePopup();
    }
    else if (updateInformation.updatePopupType == SmartAppUpdateManager.RECOMMENDED_UPDATE)
    {
      openPlayStore();
      SettingsUtil.increaseActionOnRecommendedScreenDisplayCount(PreferenceManager.getDefaultSharedPreferences(this));
      sendRecommendedActionButtonEvent();
      finish();
    }
    else if (updateInformation.updatePopupType == SmartAppUpdateManager.BLOCKING_UPDATE)
    {
      SettingsUtil.increaseActionOnBlockingScreenDisplayCount(PreferenceManager.getDefaultSharedPreferences(this));
      sendBlockingActionButtonEvent();
    }
  }

  protected final void openPlayStore()
  {
    try
    {
      startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + updateInformation.packageName)));
    }
    catch (android.content.ActivityNotFoundException exception)
    {
      startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(updateInformation.deepLink)));
    }
  }

  @Override
  protected void onPause()
  {
    super.onPause();
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
  }

  @Override
  public void onBackPressed()
  {
    closePopup();
  }

  protected void closePopup()
  {
    if (updateInformation != null)
    {
      if (updateInformation.updatePopupType != SmartAppUpdateManager.BLOCKING_UPDATE)
      {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (updateInformation.updatePopupType == SmartAppUpdateManager.RECOMMENDED_UPDATE)
        {
          SettingsUtil.setUpdateLaterTimestamp(sharedPreferences, System.currentTimeMillis());
          sendAskLaterEvent();
        }
        else if (updateInformation.updatePopupType == SmartAppUpdateManager.INFORMATION_ABOUT_UPDATE)
        {
          // store current version information in shared_pref
          SettingsUtil.setLastSeenInformativeUpdate(sharedPreferences, updateInformation.versionCode);
        }
        // We can finally close this popup
        finish();
      }
      else
      {
        closeAppWithoutUpdateEvent();
        // Finish every activity from the app
        ActivityCompat.finishAffinity(this);
      }
    }
  }

  protected void setTitle(@Nullable final String titleFromRemoteConfig)
  {
    if (TextUtils.isEmpty(titleFromRemoteConfig) == false)
    {
      title.setText(titleFromRemoteConfig);
    }
  }

  protected void setContent(@Nullable final String contentFromRemoteConfig)
  {
    if (TextUtils.isEmpty(contentFromRemoteConfig) == false)
    {
      paragraph.setText(contentFromRemoteConfig);
    }
  }

  protected void setButtonLabel(@Nullable final String buttonLabelFromRemoteConfig)
  {
    if (TextUtils.isEmpty(buttonLabelFromRemoteConfig) == false)
    {
      action.setText(buttonLabelFromRemoteConfig);
    }
  }

  protected void setImage(@Nullable final String imageURLFromRemoteConfig)
  {
  }
}
