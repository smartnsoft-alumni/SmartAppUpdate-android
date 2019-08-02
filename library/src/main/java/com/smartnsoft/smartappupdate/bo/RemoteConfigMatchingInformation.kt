package com.smartnsoft.smartappupdate.bo

/**
 *
 * @author Adrien Vitti
 * @since 2018.03.01
 */
data class RemoteConfigMatchingInformation
@JvmOverloads
constructor(
    val popupTitle: String = RemoteConfigMatchingInformation.REMOTE_CONFIG_TITLE,
    val imageURL: String = RemoteConfigMatchingInformation.REMOTE_CONFIG_IMAGE_URL,
    val updateContent: String = RemoteConfigMatchingInformation.REMOTE_CONFIG_UPDATE_CONTENT,
    val changelogContent: String = RemoteConfigMatchingInformation.REMOTE_CONFIG_CHANGELOG_CONTENT,
    val actionButtonText: String = RemoteConfigMatchingInformation.REMOTE_CONFIG_BUTTON_TEXT,
    val updateActionDeeplink: String = RemoteConfigMatchingInformation.REMOTE_CONFIG_ACTION_URL,
    val currentVersionCode: String = RemoteConfigMatchingInformation.REMOTE_CONFIG_CURRENT_VERSION_CODE,
    val dialogType: String = RemoteConfigMatchingInformation.REMOTE_CONFIG_DIALOG_TYPE,
    val askLaterSnoozeInDays: String = RemoteConfigMatchingInformation.REMOTE_CONFIG_TIME_GAP_BETWEEN_TWO_POPUP_IN_DAYS,
    val packageName: String = RemoteConfigMatchingInformation.REMOTE_CONFIG_PACKAGE_NAME_FOR_UPDATE
)
{

  companion object
  {

    const val REMOTE_CONFIG_TITLE = "update_title"

    const val REMOTE_CONFIG_IMAGE_URL = "update_imageURL"

    const val REMOTE_CONFIG_UPDATE_CONTENT = "update_content"

    const val REMOTE_CONFIG_CHANGELOG_CONTENT = "update_changelog_content"

    const val REMOTE_CONFIG_BUTTON_TEXT = "update_actionButtonLabel"

    const val REMOTE_CONFIG_ACTION_URL = "update_deeplink"

    const val REMOTE_CONFIG_PACKAGE_NAME_FOR_UPDATE = "update_package_name"

    const val REMOTE_CONFIG_CURRENT_VERSION_CODE = "current_version_code"

    const val REMOTE_CONFIG_DIALOG_TYPE = "update_dialogType"

    const val REMOTE_CONFIG_TIME_GAP_BETWEEN_TWO_POPUP_IN_DAYS = "update_ask_later_snooze_in_days"

  }

}
