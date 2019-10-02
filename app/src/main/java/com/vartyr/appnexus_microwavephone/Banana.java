package com.vartyr.appnexus_microwavephone;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.appnexus.opensdk.*;
import com.appnexus.opensdk.utils.Settings;
import com.inmobi.ads.InMobiAudienceBidder;

// DOC note: import this
import com.inmobi.plugin.appnexus.IMAudienceBidder;
import com.inmobi.sdk.InMobiSdk;


// Reference: https://support.aerserv.com/hc/en-us/articles/213736326

public class Banana extends Activity implements AdListener {

    final String logger = "[MICRO]";

    // InMobi Audience Bidder variables
    public String IMAB_SITE_ID = "1017739";                         // Sample InMobi Aerserv Platform Site ID (From the AerServ SSUI)
    private IMAudienceBidder inMobiAudienceBidder;                  // Keep a reference to the AB singleton
    public Boolean bannerLoaded = false;                            // Boolean to keep track of banner load status

    public String AB_BannerPLC = "1057270";                          // InMobi AerServ platform Banner PLC to update the banner bid parameter
    // public String AB_InterstitialPLC = "?";                      // InMobi AerServ platform Interstitial PLC to update the banner bid parameter TODO: Test and validate Interstitial bids

    private IMAudienceBidder.BidToken bannerBidToken;               // Reference to the banner bid token we can use for refreshing bids
//    private IMAudienceBidder.BidToken interstitialBidToken;         // Reference to the interstitial bid token we can use for refreshing bids TODO: Test and validate Interstitial bids

    // AppNexus BannerAd View
    public BannerAdView bav;
//    public InterstitialAdView iav;

    public String WBBannerPlacement = "9002202";
    public String APNSBannerPlacement = "12516242";
    public String APNSInterstitialPlacement = "13194659";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banana);

        initializeAdSDKs();
        initializeBannerView();
        initializeInterstitialView();
        getDisplaySDKVersions();
    }




    public void initializeAdSDKs(){

        // Init the Unified InMobi SDK
        InMobiAudienceBidder.initialize(this, IMAB_SITE_ID);

        // Get an instance of the IM Audience bidder
        inMobiAudienceBidder = IMAudienceBidder.getInstance();

    }


    public void initializeBannerView(){

        bav = new BannerAdView(this);
        bav.setPlacementID(WBBannerPlacement);

        bav.setAdSize(320, 50);     // TODO: Does the ad size impact fill rates?
        bav.setAutoRefreshInterval(15000);  // 15 sec to aid with testing
        bav.setShouldServePSAs(false);      // Sample app mentions setting this to false to ensure fill

        // Set up our ad listener
        AdListener bannerListener = new AdListener() {
            @Override

            // On request failed, we want
            public void onAdRequestFailed(AdView bav, ResultCode errorCode) {

                // Ensure that we do not call loadAd again on the AdView
                bannerLoaded = true;

                // Update the banner bid on onAdRequestFailed
                bannerBidToken.updateBid();
            }

            @Override
            public void onAdLoaded(AdView bav) {

                // Ensure that we do not call loadAd again on the AdView
                bannerLoaded = true;

                // Update the banner bid on onAdLoaded
                bannerBidToken.updateBid();
            }


            @Override
            public void onAdLoaded(NativeAdResponse bav) { Log.v(logger, "The Banner Native Ad Loaded!"); }

            @Override
            public void onAdExpanded(AdView bav) { Log.v(logger, "Ad expanded"); }

            @Override
            public void onAdCollapsed(AdView bav) { Log.v(logger, "Ad collapsed"); }

            @Override
            public void onAdClicked(AdView bav) { Log.v(logger, "Ad clicked; opening browser"); }

            @Override
            public void onAdClicked(AdView adView, String clickUrl) { Log.v(logger, "Ad clicked; app should handle url:" + clickUrl);}
        };

        // Set the banner ad listener on the banner
        bav.setAdListener(bannerListener);

    }


    public void setIMABForBanner() {

        Log.d(logger, "updateIMABForBanner has been called.");

        bannerBidToken = inMobiAudienceBidder.createBidToken(this, AB_BannerPLC,
                bav, new IMAudienceBidder.IMAudienceBidderBannerListener() {

                    @Override
                    public void onBidReceived(@NonNull BannerAdView bannerAdView) {
                        // Bid was received from Audience Bidder. Call loadAd on the updated bid object.

                        // If the banner has not yet been loaded, call loadAd to load the ad into the view
                        if (!bannerLoaded) {

                            // bav.loadAd();

                            // Do a runnable.

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    bav.loadAd();
                                }
                            }, 0);
                        }


                    }

                    @Override
                    public void onBidFailed(@NonNull BannerAdView bannerAdView, @NonNull Error error) {
                        // No Bid received from Audience Bidder. Call loadAd on the bid object.

                        // If the banner has not yet been loaded, call loadAd on the updated ad view
                        if (!bannerLoaded) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    bav.loadAd();
                                }
                            }, 0);
                        }

                    }


                });

        // Call update bid to start this process
        bannerBidToken.updateBid();
    }



    // Touch event to load the banner into the view
    public void loadBanner(View view){

        // Ensure BAV is not null
        if (bav != null){

            // Get the Layout
            FrameLayout layout = (FrameLayout)findViewById(android.R.id.content);

            // Inject the BAV into the layout
            layout.addView(bav);

            // Set up the audience bidder
            setIMABForBanner();
        } else {
            Log.e(logger, "BAV was null, not added to view");

        }
    }



    // TODO: Test and validate Interstitial bids
    public void initializeInterstitialView(){
//        iav = new InterstitialAdView(this);
    }

    // TODO: Test and validate Interstitial bids
    public void loadInterstitial(View view){ }

    // TODO: Test and validate Interstitial bids
    public void showInterstitial(View view){ }



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



    public void getDisplaySDKVersions() {

        TextView mpv = findViewById(R.id.ANSdkVersion);
        mpv.setText("AppNexus SDK Version:" + Settings.getSettings().sdkVersion);

        TextView imv = findViewById(R.id.IMSdkVersion);
        imv.setText("IM SDK Version:" + InMobiSdk.getVersion());

    }


}
