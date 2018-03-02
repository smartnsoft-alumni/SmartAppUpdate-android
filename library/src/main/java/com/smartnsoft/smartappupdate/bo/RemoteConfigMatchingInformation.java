package com.smartnsoft.smartappupdate.bo;

/**
 *
 * @author Adrien Vitti
 * @since 2018.03.01
 */
public class RemoteConfigMatchingInformation
{

  public static final String REMOTE_CONFIG_TITLE = "update_title";

  public static final String REMOTE_CONFIG_IMAGE_URL = "update_imageURL";

  public static final String REMOTE_CONFIG_UPDATE_CONTENT = "update_content";

  public static final String REMOTE_CONFIG_CHANGELOG_CONTENT = "update_changelog_content";

  public static final String REMOTE_CONFIG_BUTTON_TEXT = "update_actionButtonLabel";

  public static final String REMOTE_CONFIG_ACTION_URL = "update_deeplink";

  public static final String REMOTE_CONFIG_PACKAGE_NAME_FOR_UPDATE = "update_package_name";

  public static final String REMOTE_CONFIG_CURRENT_VERSION_CODE = "current_version_code";

  public static final String REMOTE_CONFIG_DIALOG_TYPE = "update_dialogType";

  public static final String REMOTE_CONFIG_TIME_GAP_BETWEEN_TWO_POPUP_IN_DAYS = "update_ask_later_snooze_in_days";

  public final String popupTitle;

  public final String imageURL;

  public final String updateContent;

  public final String changelogContent;

  public final String actionButtonText;

  public final String updateActionDeeplink;

  public final String currentVersionCode;

  public final String dialogType;

  public final String packageName;

  public final String askLaterSnoozeInDays;

  public RemoteConfigMatchingInformation(String popupTitle, String imageURL, String updateContent,
      String changelogContent, String actionButtonText, String updateActionDeeplink, String currentVersionCode,
      String dialogType, String askLaterSnoozeInDays, String packageName)
  {
    this.popupTitle = popupTitle;
    this.imageURL = imageURL;
    this.updateContent = updateContent;
    this.changelogContent = changelogContent;
    this.actionButtonText = actionButtonText;
    this.updateActionDeeplink = updateActionDeeplink;
    this.currentVersionCode = currentVersionCode;
    this.dialogType = dialogType;
    this.packageName = packageName;
    this.askLaterSnoozeInDays = askLaterSnoozeInDays;
  }

  public RemoteConfigMatchingInformation()
  {
    this(RemoteConfigMatchingInformation.REMOTE_CONFIG_TITLE,
        RemoteConfigMatchingInformation.REMOTE_CONFIG_IMAGE_URL,
        RemoteConfigMatchingInformation.REMOTE_CONFIG_UPDATE_CONTENT,
        RemoteConfigMatchingInformation.REMOTE_CONFIG_CHANGELOG_CONTENT,
        RemoteConfigMatchingInformation.REMOTE_CONFIG_BUTTON_TEXT,
        RemoteConfigMatchingInformation.REMOTE_CONFIG_ACTION_URL,
        RemoteConfigMatchingInformation.REMOTE_CONFIG_CURRENT_VERSION_CODE,
        RemoteConfigMatchingInformation.REMOTE_CONFIG_DIALOG_TYPE,
        RemoteConfigMatchingInformation.REMOTE_CONFIG_TIME_GAP_BETWEEN_TWO_POPUP_IN_DAYS,
        RemoteConfigMatchingInformation.REMOTE_CONFIG_PACKAGE_NAME_FOR_UPDATE
    );
  }
}
