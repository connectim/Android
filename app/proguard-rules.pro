# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-optimizationpasses 5          # Specifies the compression level of the code
-dontusemixedcaseclassnames   # mixed case
-dontpreverify           # pre check
-verbose                # Log

-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*  # The algorithm used in confusion

# wallet
-keep class connect.wallet.jni.** {*;}

# connect
-keep class protos.Connect {*;}

# Keep which classes are not confused
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService
-keep public class * extends connect.ui.base.BaseActivity

# R file
-keep class **.R$* {*;}

# Do not mapping can also display line numbers, avoid Unknown Source
-keepattributes SourceFile,LineNumberTable

# Keep the native method not confused
-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembers class * {   # Keep custom control classes not confused
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {# Keep custom control classes not confused
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclassmembers class * extends android.app.Activity { # Keep custom control classes not confused
    public void *(android.view.View);
}
-keepclassmembers enum * {     # Keep enumeration enum classes not confused
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep class * implements android.os.Parcelable { # Keep Parcelable not confused
    public static final android.os.Parcelable$Creator *;
}

# butterknife
-dontwarn butterknife.**
-keep class butterknife.** { *;}
-keep class **$$ViewBinder { *; }
-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}
-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

# greendao
-dontwarn org.greenrobot.greendao.**
-keep class org.greenrobot.greendao.** { *;}

# pinyin4j
-dontwarn demo.**
-keep class demo.** { *;}

# Zxing
-dontwarn com.google.zxing.**
-keep class com.google.zxing.** { *;}

# ProtoBuf
-dontwarn com.google.**
-keep class com.google.** { *;}

# OkHttp3
-dontwarn com.squareup.okhttp3.**
-keep class com.squareup.okhttp3.** { *;}
-dontwarn okio.**

# Okio
-dontwarn com.squareup.**
-dontwarn okio.**
-keep public class org.codehaus.* { *; }
-keep public class java.nio.* { *; }

# Bugly
-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}

# so
-libraryjars libs/armeabi/libadd.so
-libraryjars libs/armeabi-v7a/libadd.so
-libraryjars libs/x86/libadd.so

