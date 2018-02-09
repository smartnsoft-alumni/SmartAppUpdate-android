package com.smartnsoft.smartappupdate.bo;

import java.io.Serializable;

import com.smartnsoft.smartappupdate.SmartAppUpdateManager.UpdatePopupType;

/**
 * @author Adrien Vitti
 * @since 2018.01.23
 */

public final class UpdatePopupInformations
    implements Serializable
{

  public String title;

  public String imageURL;

  public String updateContent;

  public String changelogContent;

  public String actionButtonLabel;

  public String deepLink;

  public String packageName;

  public long versionCode;

  @UpdatePopupType
  public int updatePopupType;

}
