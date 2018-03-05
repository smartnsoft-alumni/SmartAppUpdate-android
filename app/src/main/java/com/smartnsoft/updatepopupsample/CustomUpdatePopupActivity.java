package com.smartnsoft.updatepopupsample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;

import com.bumptech.glide.Glide;
import com.smartnsoft.smartappupdate.AbstractSmartAppUpdateActivity;

/**
 * @author Adrien Vitti
 * @since 2018.01.25
 */

public final class CustomUpdatePopupActivity
    extends AbstractSmartAppUpdateActivity
{

  @Override
  protected void setImage(@Nullable String imageURLFromRemoteConfig)
  {
    if (TextUtils.isEmpty(imageURLFromRemoteConfig) == false)
    {
      image.setVisibility(View.VISIBLE);
      Glide.with(this).load(imageURLFromRemoteConfig).into(image);
    }
  }

  @Override
  protected void setContent(@Nullable String contentFromRemoteConfig)
  {
    if (TextUtils.isEmpty(contentFromRemoteConfig) == false)
    {
      paragraph.setText(Html.fromHtml(contentFromRemoteConfig));
    }
  }

  @Override
  public void sendUpdateInfoDisplayed()
  {

  }

  @Override
  public void sendRecomendedUpdateDisplayed()
  {

  }

  @Override
  public void sendBlockingUpdateDisplayed()
  {

  }

  @Override
  public void sendAskLaterEvent()
  {

  }

  @Override
  public void closeAppWithoutUpdateEvent()
  {

  }

  @Override
  public void sendInformativeActionButtonEvent()
  {

  }

  @Override
  public void sendRecommendedActionButtonEvent()
  {

  }

  @Override
  public void sendBlockingActionButtonEvent()
  {

  }

  @Override
  public int getVersionCode()
  {
    return 0;
  }

  @Override
  public String getDateForAnalytics()
  {
    return null;
  }

  @Override
  public Bundle generateAnalyticsExtraInfos()
  {
    return null;
  }
}
