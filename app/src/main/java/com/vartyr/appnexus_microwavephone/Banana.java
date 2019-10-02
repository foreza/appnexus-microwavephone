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

import com.inmobi.plugin.appnexus.IMAudienceBidder;
import com.inmobi.sdk.InMobiSdk;


public class Banana extends Activity implements AdListener {

    final String TAG = "[SampleApp]";

    // InMobi Audience Bidder variables
    public String IMAB_SITE_ID = "1017739";                         // Sample InMobi Aerserv SSUI Platform Site ID
    private IMAudienceBidder inMobiAudienceBidder;                  // Maintain a reference to the AB singleton
    public Boolean bannerLoaded = false;                            // Boolean to keep track of banner load status

    public String AB_BannerPLC = "1057270";                         // InMobi AerServ platform Banner PLC to update the banner bid parameter
    private IMAudienceBidder.BidToken bannerBidToken;               // Reference to the banner bid token we can use for refreshing bids

    public BannerAdView bav;

    //    private IMAudienceBidder.BidToken interstitialBidToken;   // TODO: Test and validate Interstitial bids
    //    public String AB_InterstitialPLC = "?";                   // TODO: Test and validate Interstitial bids
    //    public InterstitialAdView iav;

    public String bannerPlacementID = "9002202";

    // public String APNSBannerPlacement = "12516242";
    // public String APNSInterstitialPlacement = "13194659";




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

        // REQUIRED: Init the InMobiAudienceBidder. Without initialization, all InMobi bid requests *WILL* fail
        InMobiAudienceBidder.initialize(this, IMAB_SITE_ID);

        // Get the singleton instance of the IM Audience bidder for later
        inMobiAudienceBidder = IMAudienceBidder.getInstance();

    }


    public void initializeBannerView(){

        bav = new BannerAdView(this);
        bav.setPlacementID(bannerPlacementID);

        bav.setAdSize(320, 50);     // TODO: Does the ad size impact fill rates?
        bav.setAutoRefreshInterval(15000);  // We'll set this to 15 sec refresh to aid with testing.
        bav.setShouldServePSAs(false);      // Sample app mentions setting this to false to ensure fill

        // Set up our ad listener
        AdListener bannerListener = new AdListener() {
            @Override

            // If the banner ad request FAILS, we want to still load the banner into the view so future banner ad refreshes can utilize the view
            public void onAdRequestFailed(AdView bav, ResultCode errorCode) {

                // Ensure that we do not call loadAd again on the AdView
                bannerLoaded = true;

                // Update the InMobiAudienceBidder banner bid on onAdRequestFailed
                bannerBidToken.updateBid();

                Log.d(TAG, "Banner onAdRequestFailed with code: " + errorCode.toString());
            }

            @Override
            // If the banner ad request load successfully, load the banner into the view so future banner ad refreshes can utilize the view
            public void onAdLoaded(AdView bav) {

                // Ensure that we do not call loadAd again on the AdView
                bannerLoaded = true;

                // Update the InMobiAudienceBidder banner bid on onAdLoaded
                bannerBidToken.updateBid();

                Log.d(TAG, "Banner onAdLoaded");
            }


            @Override
            public void onAdLoaded(NativeAdResponse bav) { Log.v(TAG, "The Banner Native Ad Loaded!"); }

            @Override
            public void onAdExpanded(AdView bav) { Log.v(TAG, "Ad expanded"); }

            @Override
            public void onAdCollapsed(AdView bav) { Log.v(TAG, "Ad collapsed"); }

            @Override
            public void onAdClicked(AdView bav) { Log.v(TAG, "Ad clicked; opening browser"); }

            @Override
            public void onAdClicked(AdView adView, String clickUrl) { Log.v(TAG, "Ad clicked; app should handle url:" + clickUrl);}
        };

        // Set the banner ad listener on the banner
        bav.setAdListener(bannerListener);
    }


    public void setIMABForBanner() {

        Log.d(TAG, "updateIMABForBanner has been called.");

        bannerBidToken = inMobiAudienceBidder.createBidToken(this, AB_BannerPLC,
                bav, new IMAudienceBidder.IMAudienceBidderBannerListener() {

                    @Override
                    public void onBidReceived(@NonNull BannerAdView bannerAdView) {
                        // Bid was received from Audience Bidder. Call loadAd on the updated bid object.

                        // If the banner has not yet been loaded, call loadAd to load the ad into the view
                        if (!bannerLoaded) {

                            // Use a handler to load the ad into the view. AN documentation mentions that if this is not done, the ad request may fail.
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

                                // Use a handler to load the ad into the view. AN documentation mentions that if this is not done, the ad request may fail.
                                @Override
                                public void run() {
                                    bav.loadAd();
                                }
                            }, 0);
                        }

                    }


                });

        // Call update bid to start the bid process.
        // Note: On subsequent banner ad refreshes, we'll use the banner listener to trigger future bid updates.
        bannerBidToken.updateBid();
    }



    // Sample touch event to load the banner into the view
    public void loadBanner(View view){

        // Ensure BAV is not null
        if (bav != null){

            // Get the Layout
            FrameLayout layout = (FrameLayout)findViewById(android.R.id.content);

            // Inject the BAV into the layout
            layout.addView(bav);

            // Start the bidding process
            setIMABForBanner();

            Log.d(TAG, "BAV is now loading");


        } else {
            Log.e(TAG, "BAV was null for some bizarre reason, not added to view. Initializing BAV");
            initializeBannerView();
        }
    }

    // Sample touch event to remove the banner from the view
    public void killBanner(View view){

        if (bav != null) {

            FrameLayout layout = (FrameLayout)findViewById(android.R.id.content);
            layout.removeView(bav);

            bav.destroy();

            Log.d(TAG, "BAV destroyed");

            initializeBannerView();         // Note: Doing this to make the demo seamless. Since we tore down the banner view, we should re-init it.

        } else {

            Log.e(TAG, "BAV was null, not destroyed. We shouldn't be here!");

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


    // TODO: Implement this section properly
    @Override
    public void onAdLoaded(AdView av) {

        Log.v(TAG, "onAdLoaded fired, The interstitial ad has loaded");
        // Now that the ad has loaded, we can show it to the user.
        InterstitialAdView iav = (InterstitialAdView) av;
        iav.show();

        // Use this to show an interstitial with a delay instead
//        iav.showWithAutoDismissDelay(10);
    }

    @Override
    public void onAdLoaded(NativeAdResponse av) {
        Log.v(TAG, "onAdLoaded fired, The NativeAd interstitial has loaded");
    }

    @Override
    public void onAdRequestFailed(AdView av, ResultCode rc) {
        Log.v(TAG, "onAdRequestFailed fired, Return code ==> " + rc);

    }


    @Override
    public void onAdClicked(AdView av) {
        Log.v(TAG, "onAdClicked fired, Congrats! " );
    }

    @Override
    public void onAdClicked(AdView av, String s) {
        Log.v(TAG, "onAdClicked fired, Congrats! " );
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
