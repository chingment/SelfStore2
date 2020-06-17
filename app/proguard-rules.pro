# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/chingment/Library/Android/sdk/tools/proguard/proguard-android.txt
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

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-dontwarn com.squareup.okhttp.**
-dontwarn java.nio.**
-dontwarn org.codehaus.mojo.**

-keep class com.squareup.okhttp.**{*;}
-keep class java.nio.**{*;}
-keep class org.codehaus.mojo.**{*;}

-keepattributes Signature
-dontwarn com.alibaba.fastjson.**
-keep class com.alibaba.fastjson.**{*;}

-keep class com.uplink.selfstore.model.**{*;}
-keep class com.tamic.statinterface.**{*;}
-keep class net.sqlcipher.**{*;}
-keep class net.sqlcipher.database.**{*;}
-keep class android_serialport_api.**{*;}
-keep class com.lgh.uvccamera.**{*;}
-keep class com.serenegiant.usb.**{*;}
-keep class org.apache.**{*;}
-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}

-keep class com.hyphenate.** {*;}
-dontwarn  com.hyphenate.**
-keep class internal.org.apache.http.entity.** {*;}

-keep class com.superrtc.** {*;}
-dontwarn  com.superrtc.**