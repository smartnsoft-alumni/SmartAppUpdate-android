package com.smartnsoft.updatepopup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartnsoft.updatepopup.bo.UpdatePopupInformations;

/**
 * @author Adrien Vitti
 * @since 2018.01.23
 */
public class UpdatePopupActivity
    extends AppCompatActivity
    implements OnClickListener
{

  protected TextView title;

  protected TextView paragraph;

  protected TextView action;

  protected TextView later;

  protected ImageView close;

  protected ImageView image;

  protected UpdatePopupInformations updateInformation;

  @Override
  protected final void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(getLayoutId());
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

    bindViews();

    final Bundle bundle = getIntent().getExtras();
    if (bundle != null)
    {
      updateInformation = (UpdatePopupInformations) bundle.getSerializable(UpdatePopupManager.UPDATE_INFORMATION_EXTRA);
      if (updateInformation == null)
      {
        finish();
      }
    }

    updateLayoutWithUpdateInformation(updateInformation);
  }

  protected void bindViews()
  {
    title = findViewById(R.id.title);
    paragraph = findViewById(R.id.paragraph);

    action = findViewById(R.id.action_button);
    if (action != null)
    {
      action.setOnClickListener(this);
    }

    image = findViewById(R.id.image);

    later = findViewById(R.id.later);
    if (later != null)
    {
      later.setOnClickListener(this);
    }

    close = findViewById(R.id.close);
    if (close != null)
    {
      close.setOnClickListener(this);
    }
  }

  @LayoutRes
  protected int getLayoutId()
  {
    return R.layout.update_popup_activity;
  }

  protected void updateLayoutWithUpdateInformation(UpdatePopupInformations updateInformation)
  {
    switch (updateInformation.updatePopupType)
    {
      case UpdatePopupManager.BLOCKING_UPDATE:
        later.setVisibility(View.GONE);
        close.setVisibility(View.GONE);
        break;
      case UpdatePopupManager.RECOMMENDED_UPDATE:
      case UpdatePopupManager.INFORMATION_ABOUT_UPDATE:
      default:
        later.setVisibility(View.VISIBLE);
        close.setVisibility(View.VISIBLE);
        break;
    }

    setTitle(updateInformation.title);
    setButtonLabel(updateInformation.actionButtonLabel);
    setContent(updateInformation.content);
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
      askLater();
    }
  }

  protected void onActionButtonClick(UpdatePopupInformations updateInformation)
  {
    try
    {
      startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + updateInformation.packageName)));
    }
    catch (android.content.ActivityNotFoundException anfe)
    {
      startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(updateInformation.deepLink)));
    }
    if (updateInformation.updatePopupType != UpdatePopupManager.BLOCKING_UPDATE)
    {
      finish();
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
    if (updateInformation != null && updateInformation.updatePopupType != UpdatePopupManager.BLOCKING_UPDATE)
    {
      askLater();
    }
    // Else do nothing because user can't leave
  }

  protected void askLater()
  {
    final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    if (updateInformation.updatePopupType == UpdatePopupManager.RECOMMENDED_UPDATE)
    {
      UpdatePopupManager.setUpdateLaterTimestamp(sharedPreferences, System.currentTimeMillis());
    }
    else if (updateInformation.updatePopupType == UpdatePopupManager.INFORMATION_ABOUT_UPDATE)
    {
      // store current version information in shared_pref
    }
    finish();
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
