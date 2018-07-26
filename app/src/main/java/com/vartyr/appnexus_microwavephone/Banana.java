package com.vartyr.appnexus_microwavephone;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.FrameLayout;

import com.aerserv.sdk.AerServSdk;
import com.appnexus.opensdk.*;

// Reference: https://support.aerserv.com/hc/en-us/articles/213736326

public class Banana extends Activity implements AdListener {

    final String logger = "[MICRO]";

    // Set up an ad view with our placement ID.
    private BannerAdView bav = null;
    private InterstitialAdView iav = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banana);

        // Init the AerServ SDK
        AerServSdk.init(this, "1003150");


        bav = new BannerAdView(this);
        iav = new InterstitialAdView(this);

        // Configure placement IDs for banner and interstitial
        bav.setPlacementID("9002134");
//        iav.setPlacementID("13194659"); //

        Log.v(logger, "Set Placement ID");


        // Banner specific configurations
        bav.setAdSize(320, 50);
        bav.setAutoRefreshInterval(60000); // Set to 0 to disable auto-refresh
        bav.setShouldServePSAs(true);
        bav.setOpensNativeBrowser(true);

        // Keywords allow you to track placements on a more granular level
        bav.addCustomKeywords("fc0", "jc-aerserv-test");
        bav.addCustomKeywords("wo1", "jc-aerserv-test");


        // Interstitial specific configurations
//        iav.setAdListener(this);


        // Draw the banner
        FrameLayout layout = (FrameLayout)findViewById(android.R.id.content);
        layout.addView(bav);

        // Debug log
        Log.v(logger, "FrameLayout - Banner added to view");

        // Load / show the ads
        loadAds();


    }


    // Method to programatically load the ads
    public void loadAds() {

        // This handler is needed if the banner does not refresh (ie: setAutoRefreshInterval is set to 0)
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.v(logger, "postDelayed fired, loadAd");
                bav.loadAd();
            }
        }, 0);


//        iav.loadAd();


    }


    @Override
    public void onAdLoaded(AdView av) {
        Log.v(logger, "onAdLoaded fired, The interstitial ad has loaded");
        // Now that the ad has loaded, we can show it to the user.
        InterstitialAdView iav = (InterstitialAdView) av;
        iav.show();

        // Use this to show an interstitial with a delay instead
//        iav.showWithAutoDismissDelay(10);
    }

    @Override
    public void onAdLoaded(NativeAdResponse av) {
        Log.v(logger, "onAdLoaded fired, The NativeAd interstitial has loaded");
    }

    @Override
    public void onAdRequestFailed(AdView av, ResultCode rc) {
        Log.v(logger, "onAdRequestFailed fired, Return code ==> " + rc);

    }

    @Override
    public void onAdClicked(AdView av) {
        Log.v(logger, "onAdClicked fired, Congrats! " );
    }

    @Override
    public void onAdClicked(AdView av, String s) {
        Log.v(logger, "onAdClicked fired, Congrats! " );
    }

    @Override
    public void onAdCollapsed(AdView av) {
    }

    @Override
    public void onAdExpanded(AdView av) {
    }





    // Forwarding Lifecycle Callbacks
    /**
      * To be called by the developer when the fragment/activity's onDestroy() function is called.
      */
    protected void activityOnDestroy(){

     }

    /**
      * To be called by the developer when the fragment/activity's onPause() function is called.
      */
    protected void activityOnPause() {

    }
    /**
      * To be called by the developer when the fragment/activity's onResume() function is called.
      */
    protected void activityOnResume(){

    }


    // TODO: Implement this at a later date
    protected void checkForPermissions() {
//        if (ContextCompat.checkSelfPermission(thisActivity, Manifest.permission.WRITE_CALENDAR)
//                != PackageManager.PERMISSION_GRANTED) {
//            // Permission is not granted
//        }
    }
}
