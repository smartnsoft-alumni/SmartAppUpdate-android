# SmartAppUpdate

Small library which can be used to display an update notification popup.

## Usage

### Remote Config variables

With version 1.0.0, the only way to use this library is via the Firebase Remote Config console of the app.

#####Mandatory variables are :

* `update_dialogType` as Integer
	* 1 is an informative popup which will display the changelog only once
	* 2 is a recommended update which will be displayed every 3 days by default
	* 3 is a blocking update which cannot be closed without closing the app
* `update_title` as String
 	* This is the title of the popup
* `update_content` as String
	* This is the main text that will be used for update type 2 and 3
* `update_changelog_content` as String
	* This is the main text that will be used for update type 1
* `update_actionButtonLabel` as String
	* This is the text to use in the action button
* `current_version_code` as Int
	* This is the most up-to-date version code which can be downloaded by user. It will be used to display the changelog after an update

#####Optional variables:

* `update_imageURL` as String
	* An optional image URL
* `update_ask_later_snooze_in_days`
	* The number of days we have to wait before displaying a new recommended popup if the user closed the previous one and did not update

### Gradle (Internal Nexus repository)

**Gradle :**

```groovy
implementation 'com.smartnsoft:smartappupdate:1.0-SNAPSHOT'
```

### Application configuration

**Application class :**

```java
final SmartAppUpdateManager smartAppUpdateManager = new SmartAppUpdateManager.Builder(this, BuildConfig.DEBUG)
        .setFallbackUpdateApplicationId(BuildConfig.APPLICATION_ID)
        .setSynchronousTimeoutDuration(Constants.HTTP_CONNECTION_TIMEOUT_IN_MILLISECONDS)
        .setMaxConfigCacheDuration(Constants.CACHE_RETENTION_DURATION_IN_MILLISECONDS)
        .build();
```
**Display the popup :**

* Asynchronous calls
	* If you want to retrieve the configuration and display the popup in one call

		```java
		smartAppUpdateManager.fetchRemoteConfig(true);
		```

	* If you want to retrieve the configuration and display the popup later
	
		```java
		// first
		smartAppUpdateManager.fetchRemoteConfig(false);
		...
		// then later
		smartAppUpdateManager.createAndDisplayPopup();
		```
		
* Synchronous calls
	* If you want to retrieve the configuration and display the popup in one call

		```java
		smartAppUpdateManager.fetchRemoteConfigSync(true);
		```

	* If you want to retrieve the configuration and display the popup later
	
		```java
		// first
		smartAppUpdateManager.fetchRemoteConfigSync(false);
		...
		// then later
		smartAppUpdateManager.createAndDisplayPopup();
		```
		
Note: If you need to know if the popup will be displayed, use the following method once the configuration has been retrieved.

```java
final Intent popupIntent = smartAppUpdateManager.createPopupIntent();
```
If the intent is not _null_, the popup will be displayed.


### Custom popup activity

If you want to use a custom layout or override specific parts of the popup activity :

* Extends from class SmartAppUpdateActivity

```java
public final class MyCustomAppUpdateActivity
    extends SmartAppUpdateActivity
{

  @Override
  protected int getLayoutId()
  {
    return R.layout.my_custom_layout;
  }
  
}
```

* And register the new one in the SmartAppUpdateManager Builder

```java
final SmartAppUpdateManager smartAppUpdateManager = new SmartAppUpdateManager.Builder(this, BuildConfig.DEBUG)
        .setUpdatePopupActivity(AppUpdateActivity.class)
        ...
        .build();
```

* And don't forget to declare it in your app Manifest ;)

## Releases

## 1.0.0 (2018-02-13)
* Initial release

## Author

The Android Team @ [Smart&Soft](http://www.smartnsoft.com/), software agency

## Contribution
All sorts of contributions are welcome. Please create a pull request and/or issue.
