package com.smartnsoft.updatepopup;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.smartnsoft.updatepopup.bo.UpdatePopupInformations;

/**
 * @author Adrien Vitti
 * @since 2018.01.23
 */
public final class UpdatePopupActivity
    extends AppCompatActivity
    implements OnClickListener
{

  private static final String REMOTE_CONFIG_LATER = "remoteConfigLater";

  private TextView title;

  private TextView paragraph;

  private TextView action;

  private TextView later;

  private ImageView image;

  private ImageView close;

  private ScrollView scrollView;

  private String titleString;

  private String paragraphString;

  private String actionButtonString;

  private String imageUrl;

  private String actionUrl;

  private boolean isBlocking;

  private String packageName;

  private int dialogType;

  @Override
  public void onClick(View view)
  {
    if (view == action)
    {
      try
      {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
      }
      catch (android.content.ActivityNotFoundException anfe)
      {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(actionUrl)));
      }
      if (dialogType != 1)
      {
        //        AnimationUtils.animationOut(scrollView);
        new Handler().postDelayed(new Runnable()
        {
          @Override
          public void run()
          {
            finish();
          }
        }, 500);
      }
    }
    else if (view == later)
    {
      dismiss();
    }
    else if (view == close)
    {
      dismiss();
    }
  }

  private void dismiss()
  {
    PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
        .edit().putBoolean(UpdatePopupActivity.REMOTE_CONFIG_LATER, true).apply();
    //    AnimationUtils.animationOut(scrollView);
    new Handler().postDelayed(new Runnable()
    {
      @Override
      public void run()
      {
        finish();
      }
    }, 500);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.update_popup_activity);
    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

    scrollView = findViewById(R.id.scrollContainer);

    title = findViewById(R.id.title);

    paragraph = findViewById(R.id.paragraph);

    action = findViewById(R.id.action_button);
    action.setOnClickListener(this);

    image = findViewById(R.id.image);

    later = findViewById(R.id.later);
    later.setOnClickListener(this);

    close = findViewById(R.id.close);
    close.setOnClickListener(this);

    final Bundle bundle = getIntent().getExtras();
    if (bundle != null)
    {
      final UpdatePopupInformations updateInformation = bundle.getSerializable(UpdatePopupManager.UPDATE_INFORMATION_EXTRA);
      titleString = updateInformation.title;
      paragraphString = bundle.getString(UpdatePopupManager.REMOTE_CONFIG_CONTENT);
      actionButtonString = bundle.getString(UpdatePopupManager.REMOTE_CONFIG_BUTTON_TEXT);
      imageUrl = bundle.getString(UpdatePopupManager.REMOTE_CONFIG_IMAGE_URL);
      isBlocking = bundle.getBoolean(UpdatePopupManager.REMOTE_CONFIG_IS_BLOCKING_UPDATE);
      actionUrl = bundle.getString(UpdatePopupManager.REMOTE_CONFIG_ACTION_URL);
      packageName = bundle.getString(UpdatePopupManager.REMOTE_CONFIG_PACKAGE_NAME_FOR_UPDATE);
      dialogType = (int) bundle.getLong(UpdatePopupManager.REMOTE_CONFIG_DIALOG_TYPE);
    }

  }

  @Override
  protected void onResume()
  {
    super.onResume();
    //    new Handler().postDelayed(new Runnable()
    //    {
    //      @Override
    //      public void run()
    //      {
    //        AnimationUtils.animationIn(scrollView);
    //      }
    //    }, 500);

    if (dialogType > 0)
    {
      switch (dialogType)
      {
        case 1:
          later.setVisibility(View.GONE);
          close.setVisibility(View.GONE);
          break;
        default:
          later.setVisibility(View.VISIBLE);
          close.setVisibility(View.VISIBLE);
          break;
      }
    }

    if (TextUtils.isEmpty(titleString) == false)
    {
      title.setText(titleString);
    }
    if (TextUtils.isEmpty(imageUrl) == false)
    {
      //      BitmapDownloader.getInstance().get(image, imageUrl, null, getHandler(), LCIApplication.CACHE_IMAGE_INSTRUCTIONS);
    }
    if (TextUtils.isEmpty(actionButtonString) == false)
    {
      action.setText(actionButtonString);
    }
    if (TextUtils.isEmpty(paragraphString) == false)
    {
      paragraph.setText(paragraphString);
    }
  }

  @Override
  protected void onPause()
  {
    super.onPause();
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
  }

}
