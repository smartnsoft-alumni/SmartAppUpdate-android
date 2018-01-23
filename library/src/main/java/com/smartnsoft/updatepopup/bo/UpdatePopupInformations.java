package com.smartnsoft.updatepopup.bo;

import java.io.Serializable;

import com.smartnsoft.updatepopup.UpdatePopupManager.UpdatePopupType;

/**
 * The class description here.
 *
 * @author Adrien Vitti
 * @since 2018.01.23
 */

public final class UpdatePopupInformations
    implements Serializable
{

  public final String title;

  public final String imageURL;

  public final String content;

  public final String actionButtonLabel;

  public final String deepLink;

  public final String packageName;

  @UpdatePopupType
  public final int updatePopupType;

  public UpdatePopupInformations(String title, String imageURL, String content, String actionButtonLabel,
      String deepLink, String packageName, int updatePopupType)
  {
    this.title = title;
    this.imageURL = imageURL;
    this.content = content;
    this.actionButtonLabel = actionButtonLabel;
    this.deepLink = deepLink;
    this.packageName = packageName;
    this.updatePopupType = updatePopupType;
  }
}
