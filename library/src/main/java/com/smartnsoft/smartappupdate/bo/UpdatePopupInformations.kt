package com.smartnsoft.smartappupdate.bo

import com.smartnsoft.smartappupdate.SmartAppUpdateManager.UpdatePopupType
import java.io.Serializable

/**
 * @author Adrien Vitti
 * @since 2018.01.23
 */

data class UpdatePopupInformations(
    var title: String? = null,
    var imageURL: String? = null,
    var updateContent: String? = null,
    var changelogContent: String? = null,
    var actionButtonLabel: String? = null,
    var deepLink: String? = null,
    var packageName: String? = null,
    var versionCode: Long = 0,
    @UpdatePopupType
    var updatePopupType: Int = 0
) : Serializable
