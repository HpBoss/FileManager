# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

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

# Native methods
# https://www.guardsquare.com/en/products/proguard/manual/examples#native
-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}

# App
-keep class com.baidu.duer.files.** implements androidx.appcompat.view.CollapsibleActionView { *; }
-keep class com.baidu.duer.files.provider.common.ByteString { *; }
-keep class com.baidu.duer.files.provider.linux.syscall.** { *; }
-keepnames class * extends java.lang.Exception
# For Class.getEnumConstants()
-keepclassmembers enum * {
  public static **[] values();
}
-keepnames class com.baidu.duer.files.** implements android.os.Parcelable

# Apache Commons Compress
-dontwarn org.apache.commons.compress.compressors.**
-dontwarn org.apache.commons.compress.archivers.**
# com.baidu.duer.files.provider.archive.archiver.ArchiveWriter.sTarArchiveEntryLinkFlagsField
-keepclassmembers class org.apache.commons.compress.archivers.tar.TarArchiveEntry {
    byte linkFlag;
}

# Apache FtpServer
-keepclassmembers class * implements org.apache.mina.core.service.IoProcessor {
    public <init>(java.util.concurrent.ExecutorService);
    public <init>(java.util.concurrent.Executor);
    public <init>();
}

# Bouncy Castle
-keep class org.bouncycastle.jcajce.provider.** { *; }
-keep class org.bouncycastle.jce.provider.** { *; }

# Stetho No-op
# This library includes the no-op for stetho-okhttp3 which requires okhttp3, but we never used it.
-dontwarn com.facebook.stetho.okhttp3.StethoInterceptor

# LiveEventBus
-dontwarn com.jeremyliao.liveeventbus.**
-keep class com.jeremyliao.liveeventbus.** { *; }
-keep class androidx.lifecycle.** { *; }
-keep class androidx.arch.core.** { *; }
