package com.tusher.boiwatch.utils;

import android.text.TextUtils;
import android.util.Log;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

public class AdBlocker {

    private static final String TAG = "AdBlocker";
    private static final Set<String> AD_HOSTS = new HashSet<>();

    static {
        // Basic list of common ad/tracker domains
        AD_HOSTS.add("doubleclick.net");
        AD_HOSTS.add("googlesyndication.com");
        AD_HOSTS.add("google-analytics.com");
        AD_HOSTS.add("googleadservices.com");
        AD_HOSTS.add("googletagmanager.com");
        AD_HOSTS.add("ads.google.com");
        AD_HOSTS.add("admobs.com");
        AD_HOSTS.add("admob.com");
        AD_HOSTS.add("creative.ak.fbcdn.net");
        AD_HOSTS.add("ad.doubleclick.net");
        AD_HOSTS.add("pagead2.googlesyndication.com");
        AD_HOSTS.add("adservice.google.com");
        AD_HOSTS.add("tpc.googlesyndication.com");
        // Pop-up and redirect domains
        AD_HOSTS.add("popads.net");
        AD_HOSTS.add("popcash.net");
        AD_HOSTS.add("propellerads.com");
        AD_HOSTS.add("adsterra.com");
        AD_HOSTS.add("mtrack.me");
        AD_HOSTS.add("mc.yandex.ru");
        AD_HOSTS.add("an.yandex.ru");
        AD_HOSTS.add("onclickads.net");
        AD_HOSTS.add("exoclick.com");
        AD_HOSTS.add("juicyads.com");
        AD_HOSTS.add("clck.ru");
        AD_HOSTS.add("bit.ly");
        AD_HOSTS.add("tinyurl.com");
    }

    public static boolean isAd(String url) {
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            if (TextUtils.isEmpty(host)) {
                return false;
            }
            // Check exact match or suffix match
            for (String adHost : AD_HOSTS) {
                if (host.equals(adHost) || host.endsWith("." + adHost)) {
                    Log.d(TAG, "Blocking ad URL: " + url);
                    return true;
                }
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return false;
    }
}
