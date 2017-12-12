-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-ignorewarnings
-dontwarn
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * extends android.app.Activity {
	public void *(android.view.View);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
	public static final android.os.Parcelable$Creator *;
}

-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}

-keepattributes JavascriptInterface

##---------------Begin: proguard configuration for BaiduMap  ----------
	#-libraryjars libs/BaiduLBS_Android.jar
	-keep class com.baidu.** { *; }
	-keep class vi.com.gdi.bgl.android.**{*;}
##---------------End: proguard configuration for BaiduMap  ----------

##--------------- Begin: proguard configuration for ShareDream ----------
#-renamesourcefileattribute SourceFile
#-keepattributes SourceFile,LineNumberTable
#-keep class com.sharedream.** {
# 	*;
#}
##--------------- End: proguard configuration for ShareDream ----------
