package com.smartnsoft.smartappupdate

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager
import com.smartnsoft.smartappupdate.bo.UpdatePopupInformations

/**
 * @author Adrien Vitti
 * @since 2018.03.05
 */

@Suppress("MemberVisibilityCanBePrivate")
abstract class AbstractSmartAppUpdateActivity : AppCompatActivity(), OnClickListener, UpdateScreenAnalyticsInterface
{

  protected var title: TextView? = null

  protected var paragraph: TextView? = null

  protected var action: TextView? = null

  protected var later: TextView? = null

  protected var close: ImageView? = null

  protected var image: ImageView? = null

  protected lateinit var updateInformation: UpdatePopupInformations

  protected val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.applicationContext)

  protected val layoutId: Int
    @LayoutRes
    get() = R.layout.smartappupdate_popup_activity

  override fun onCreate(savedInstanceState: Bundle?)
  {
    super.onCreate(savedInstanceState)
    setContentView(layoutId)
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

    bindViews()

    intent.extras?.also { bundle ->
      (bundle.getSerializable(SmartAppUpdateManager.UPDATE_INFORMATION_EXTRA) as? UpdatePopupInformations)
          ?.also { updatePopupInformations ->
            updateInformation = updatePopupInformations
          } ?: finish()
    } ?: finish()

    updateInformation.also { updatePopupInformations ->
      when (updatePopupInformations.updatePopupType)
      {
        SmartAppUpdateManager.BLOCKING_UPDATE          ->
        {
          SettingsUtil.increaseBlockingScreenDisplayCount(preferences)
          sendBlockingUpdateDisplayed()
        }
        SmartAppUpdateManager.INFORMATION_ABOUT_UPDATE ->
        {
          // store current version information in shared_pref
          SettingsUtil.setLastSeenInformativeUpdate(preferences, updatePopupInformations.versionCode)
          sendUpdateInfoDisplayed()
        }
        SmartAppUpdateManager.RECOMMENDED_UPDATE       ->
        {
          SettingsUtil.increaseRecommendedScreenDisplayCount(preferences)
          //NOTE: We must store this timestamp because user can click without really updating
          SettingsUtil.setUpdateLaterTimestamp(preferences, System.currentTimeMillis())
          sendRecomendedUpdateDisplayed()
        }
      }
      updateLayoutWithUpdateInformation(updatePopupInformations)
    }
  }

  protected fun bindViews()
  {
    title = findViewById(R.id.title)
    paragraph = findViewById(R.id.paragraph)

    action = findViewById(R.id.actionButton)
    if (action != null)
    {
      action?.setOnClickListener(this)
    }

    image = findViewById(R.id.image)

    later = findViewById(R.id.laterButton)
    if (later != null)
    {
      later?.setOnClickListener(this)
    }

    close = findViewById(R.id.closeButton)
    if (close != null)
    {
      close?.setOnClickListener(this)
    }
  }

  protected fun updateLayoutWithUpdateInformation(updateInformation: UpdatePopupInformations)
  {
    when (updateInformation.updatePopupType)
    {
      SmartAppUpdateManager.BLOCKING_UPDATE          ->
      {
        later?.visibility = View.GONE
        close?.visibility = View.GONE
        setContent(updateInformation.updateContent)
      }
      SmartAppUpdateManager.RECOMMENDED_UPDATE       ->
      {
        later?.visibility = View.VISIBLE
        setContent(updateInformation.updateContent)
        close?.visibility = View.VISIBLE
      }
      SmartAppUpdateManager.INFORMATION_ABOUT_UPDATE ->
      {
        close?.visibility = View.VISIBLE
        setContent(updateInformation.changelogContent)
      }
      else                                           ->
      {
        close?.visibility = View.VISIBLE
        setContent(updateInformation.changelogContent)
      }
    }

    setTitle(updateInformation.title)
    setButtonLabel(updateInformation.actionButtonLabel)
    setImage(updateInformation.imageURL)
  }

  override fun onClick(view: View)
  {
    if (view === action)
    {
      onActionButtonClick(updateInformation)
    }
    else if (view === later || view === close)
    {
      closePopup()
    }
  }

  protected fun onActionButtonClick(updateInformation: UpdatePopupInformations)
  {
    when
    {
      updateInformation.updatePopupType == SmartAppUpdateManager.INFORMATION_ABOUT_UPDATE ->
      {
        sendInformativeActionButtonEvent()
        closePopup()
      }
      updateInformation.updatePopupType == SmartAppUpdateManager.RECOMMENDED_UPDATE       ->
      {
        openPlayStore()
        SettingsUtil.increaseActionOnRecommendedScreenDisplayCount(preferences)
        sendRecommendedActionButtonEvent()
        finish()
      }
      updateInformation.updatePopupType == SmartAppUpdateManager.BLOCKING_UPDATE          ->
      {
        openPlayStore()
        SettingsUtil.increaseActionOnBlockingScreenDisplayCount(preferences)
        sendBlockingActionButtonEvent()
      }
    }
  }

  protected fun openPlayStore()
  {
    try
    {
      startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + updateInformation.packageName)))
    }
    catch (exception: android.content.ActivityNotFoundException)
    {
      startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(updateInformation.deepLink)))
    }

  }

  override fun onPause()
  {
    super.onPause()
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
  }

  override fun onBackPressed()
  {
    closePopup()
  }

  protected fun closePopup()
  {
    if (updateInformation.updatePopupType != SmartAppUpdateManager.BLOCKING_UPDATE)
    {
      if (updateInformation.updatePopupType == SmartAppUpdateManager.RECOMMENDED_UPDATE)
      {
        sendAskLaterEvent()
      }
      // We can finally close this popup
      finish()
    }
    else
    {
      closeAppWithoutUpdateEvent()
      // Finish every activity from the app
      ActivityCompat.finishAffinity(this)
    }
  }

  protected fun setTitle(titleFromRemoteConfig: String?)
  {
    if (titleFromRemoteConfig.isNullOrBlank().not())
    {
      title?.text = titleFromRemoteConfig
    }
  }

  protected open fun setContent(contentFromRemoteConfig: String?)
  {
    if (contentFromRemoteConfig.isNullOrBlank().not())
    {
      paragraph?.text = contentFromRemoteConfig
    }
  }

  protected fun setButtonLabel(buttonLabelFromRemoteConfig: String?)
  {
    if (buttonLabelFromRemoteConfig.isNullOrBlank().not())
    {
      action?.text = buttonLabelFromRemoteConfig
    }
  }

  protected open fun setImage(imageURLFromRemoteConfig: String?)
  {
  }
}
