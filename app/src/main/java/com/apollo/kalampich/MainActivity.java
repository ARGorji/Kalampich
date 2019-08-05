package com.apollo.kalampich;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.ColorInt;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.apollo.kalampich.model.ChapterItem;
import com.apollo.kalampich.util.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;



public class MainActivity extends AppCompatActivity {

    Context mContext;
    DrawingView dv ;
    private Paint mPaint;
    int ImageWidth;
    int ImageHeight;
    String Word1 = "مبارز";
    ArrayList<AlphabetPoint> ALAlpahbet = new ArrayList<>();
    ArrayList<String> ALOtherWords = new ArrayList<>();
    ArrayList<String> CurSolvedWords = new ArrayList<>();
    ArrayList<ImageView> ImageLetters = new ArrayList<>();

    protected String TryingWord = "";
    protected String Letter3Cordination = "1.7;0,0;2,3;2.8";
    protected String Letter4Cordination = "2;0,0;1,3.5;2,1.5;3";
    protected String Letter5Cordination = "2;0,3.5;1.5,2.2;3.2,0.7;2.7,0;0.8";
    protected List<Integer> RandomIndices = new ArrayList<Integer>();
    DBHelper mydb;
    MediaPlayer mpBell;
    MediaPlayer mpBubble;
    MediaPlayer mpError;
    MediaPlayer mpWin;
    MediaPlayer mpDropCoin;
    int HelperLetterCount = 1;
    static MediaPlayer mpBGSound = null;
    LinearLayout main_conainer;
    ViewGroup container;
    int ScreenWidth;
    int ScreenHeight;

    static final String TAG = "TrivialDrive";

    // Does the user have the premium upgrade?
    boolean mIsPremium = false;
    static boolean EnablePlayBackgroundMusic= true;
    static boolean EnableSoundNotificationMusic = true;

    // Does the user have an active subscription to the infinite gas plan?
    boolean mSubscribedToInfiniteGas = false;

    // SKUs for our products: the premium upgrade (non-consumable) and gas (consumable)
    static final String SKU_PREMIUM = "premium";
//    static final String SKU_COIN = "coin";
    static final String SKU_COIN1000 = "coin1000";
    static final String SKU_COIN2000 = "coin2000";
    static final String SKU_COIN3000 = "coin3000";
    static final String SKU_COIN5000 = "coin5000";
    static final String SKU_COIN10000 = "coin10000";
    static final String SKU_COIN15000 = "coin15000";

    // SKU for our subscription (infinite gas)
    static final String SKU_INFINITE_GAS = "infinite_gas";

    // (arbitrary) request code for the purchase flow
    static final int RC_REQUEST = 10001;

    // How many units (1/4 tank is our unit) fill in the tank.
    static final int TANK_MAX = 4;

    // Current amount of gas in tank, in units
    int mTank;
    int RandomAwad;

    PopupWindow popUpShop;
    PopupWindow popUpNextLevel;
    PopupWindow popUpRandomAward;
    PopupWindow popUpChapterList;
    PopupWindow popUpSettings;
    PopupWindow popUpAlert;
    PopupWindow popUpAboutUs;
    PopupWindow popUpHelp;
    PopupWindow popUpShowBulb;

    LinearLayout.LayoutParams params;
    boolean click = true;
    int OrderNum = 0;
    int ChapterNum = 0;
    int InitOrderNum = 0;
    int HighestOrderSolved = 0;
    int HighestChapterSolved = 0;

    // The helper object
    IabHelper mHelper;
    int Counter;

    Runnable runnable;
    Runnable runnableBulb;
    Runnable runnableShowBulbHelp;

    Handler handler= new Handler();
    int delayTimeSec = 50;

    private GridView mGridView;

    private ChapterListViewDataAdapter mGridViewAdapter;
    private ArrayList<ChapterItem> mListData;

    @Override
    protected void onStop() {
        super.onStop();
        mpBGSound.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(EnablePlayBackgroundMusic)
            mpBGSound.start();

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try
        {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);


            mContext = getApplicationContext();
            mpError = MediaPlayer.create(this, R.raw.error1);
            mpBell = MediaPlayer.create(this, R.raw.bell1);
            mpBubble = MediaPlayer.create(this, R.raw.bubble1);
            mpBGSound = MediaPlayer.create(this, R.raw.bensound_littleidea);
            mpWin = MediaPlayer.create(this, R.raw.slotmachinewin);
            mpDropCoin = MediaPlayer.create(this, R.raw.dropcoins);

            mpBGSound.setLooping(true);



            GetSettings();


            if(EnablePlayBackgroundMusic) {
                mpBGSound.start();
            }
            else
                mpBGSound.stop();;


            mydb = new DBHelper(this);
            getApplicationContext().deleteDatabase("wpdb");
            mydb.createDataBase();

            //MakeChapterList();

            InitializeWord();

            String AppDefaultKeyName = this.getString(R.string.app_default_key);

            Tools tools = new Tools();
            String strFirstTimeRun = tools.GetSettingVal(AppDefaultKeyName, this, "FirstTimeRun");
            if(strFirstTimeRun.equals(""))
            {
                tools.ChangeSetting(AppDefaultKeyName, this, "FirstTimeRun", "1");
                tools.ChangeSetting(AppDefaultKeyName, this, "CoinCount", "300");
            }


            String strCoinCount = tools.GetSettingVal(AppDefaultKeyName, this, "CoinCount");
            Integer CoinCount = 0;
            if(!strCoinCount.equals(""))
                CoinCount = Integer.parseInt(strCoinCount);
            else
                CoinCount = 0;


            TextView tvCoinCount =  (TextView)findViewById(R.id.txtCoinCount);
            tvCoinCount.setText(Tools.ChangeEnc(String.valueOf(CoinCount)));
            //Bazar
            //String base64EncodedPublicKey = "MIHNMA0GCSqGSIb3DQEBAQUAA4G7ADCBtwKBrwDQVxhkxvup8LlDee9R5EqKRUMomLfgzovthbyC554k8kVuZjoJW6tvqE5W96MY34TIbkDLV9yt3dqFUwITzyv41HZQe1X939qLDY4YZEcMrDbdK9Ex17cp9tKC+dR7vqw/TKMkM0NBy9NTTp7FIOTOokYQ9lZPimzxIXDtFg2GCTCUyzfR2Hqiaxt7gB0FD0Z3C+o/CXUI+r1jpUrmYcIty6DrpEnyQtGltkbKRXUCAwEAAQ==";

            //Myket
            String base64EncodedPublicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCx7PjFF16DcVjQ7qkNJrsfud3wiy0eXUKtZ7NVT2YPZjHQWFQ3gdfG6SBdfZhU/55GxgD+p/tWGN6qbpU8DDWpSW5AL+KpDn1HXrQx6vXQRUnAB0X5sjqnmVbMX9K029jlsTz1xFMr40TX63JAtTtu7jLBxFT/TtrD2rOAMPiurQIDAQAB";

            //IranApps
            //String base64EncodedPublicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC3DjANJ7H1Ul8Q1GJkBGnKk0FclEGQGL2DcLCl0wrjp74zMiHqiW8xsesqKyng6BR842ZZ2po2K0TBeDRJPQW42/aCwyT+l3VSc+ptlrJhvzwJ7GE2hHd438mhVvPeQtXiHGmjr6LwgBWTQRStn3NFfN/AgGS4zsDXJzkLuDP2WwIDAQAB";

            mHelper = new IabHelper(this, base64EncodedPublicKey);

            // enable debug logging (for a production application, you should set this to false).
            //mHelper.enableDebugLogging(true);

            Log.d(TAG, "Starting setup.");
            mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                public void onIabSetupFinished(IabResult result) {
                    Log.d(TAG, "Setup finished.");

                    if (!result.isSuccess()) {
                        // Oh noes, there was a problem.
                        Log.d(TAG, "Problem setting up In-app Billing: " + result);
                    }
                    // Hooray, IAB is fully set up!
                    mHelper.queryInventoryAsync(mGotInventoryListener);
                }
            });

            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            ScreenWidth = displayMetrics.widthPixels;
            ScreenHeight = displayMetrics.heightPixels;


            runnableBulb = new Runnable(){
                @Override
                public void run() {
                    try {
                        handler.postDelayed(runnableBulb, 10000);
                        LinearLayout bulb = (LinearLayout)findViewById(R.id.bulb);
                        Animation animScale = AnimationUtils.loadAnimation(getApplicationContext(),
                                R.anim.scale);
                        bulb.startAnimation(animScale);

                    }
                    catch (Exception err)
                    {
                        Toast.makeText(getApplicationContext(), err.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                }
            };
            handler.postDelayed(runnableBulb, 10);
            //ShowAlertPopup("this is test", true);

            LinearLayout bulb = (LinearLayout)findViewById(R.id.bulb);
            ImageView imgAboutUs = (ImageView)findViewById(R.id.imgAboutUs);

            bulb.bringToFront();
            imgAboutUs.bringToFront();
            ShowHelpPopup();




        }
        catch(Exception err2)
        {
            Toast.makeText(getApplicationContext(), err2.getMessage(),
                    Toast.LENGTH_LONG).show();
        }

    }



    public void onBulbClicked(View v0)
    {
        onBulbButtonClicked();
    }

    protected void MakeChapterList(){
        try {
            Cursor resultSet = mydb.GetChapters();

            mGridView = (GridView) findViewById(R.id.chapterlist_listview);

            mGridView.setAdapter(null);


            // Initialize with empty data
            mGridViewAdapter = new ChapterListViewDataAdapter(mContext, resultSet, ChapterNum);
            mGridView.setAdapter(mGridViewAdapter);

            mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View view,
                                        int position, long id) {

                    try {
//                        String strCode = ((TextView) view
//                                .findViewById(R.id.grid_item_code)).getText()
//                                .toString();
//
//                        String ItemType = ((TextView) view
//                                .findViewById(R.id.grid_item_itemtype))
//                                .getText().toString();
//
//                        String strTitle = ((TextView) view
//                                .findViewById(R.id.grid_item_text)).getText()
//                                .toString();

                    } catch (Exception err1) {
                        Toast.makeText(getApplicationContext(),
                                err1.getMessage(), Toast.LENGTH_LONG).show();

                    }
                    // Toast.makeText(getApplicationContext(), "CLICKED",
                    // Toast.LENGTH_SHORT).show();
                }
            });

        }
        catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
        }


    protected void ShowRandomAwardPopup()
    {
        main_conainer =  (LinearLayout)findViewById(R.id.main_conainer);
        LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService (LAYOUT_INFLATER_SERVICE);

        container = (ViewGroup) inflater.inflate(R.layout.popup_random_award, null);
        popUpRandomAward = new PopupWindow(container, ScreenWidth, ScreenHeight, true);
        popUpRandomAward.setAnimationStyle(R.style.PopupAnimation);


        final ImageButton btnRandomNumber = (ImageButton)container.findViewById(R.id.btnRandomNumber);
        final ImageButton btnFreeReceiveCoins = (ImageButton)container.findViewById(R.id.btnFreeReceiveCoins);
        RandomAwad = 0;
                btnRandomNumber.setOnClickListener(new ImageButton.OnClickListener(){
            @Override
            public void onClick(View v) {

                int Bonus = 10;
                if(HighestChapterSolved == 1)
                    Bonus = 300;
                else if(HighestChapterSolved == 2)
                    Bonus = 200;
                else if(HighestChapterSolved == 3)
                    Bonus = 100;
                else if(HighestChapterSolved == 4)
                    Bonus = 50;

                RandomAwad = ((int) (Math.random() * 40)) + Bonus;
                delayTimeSec = 2600 / RandomAwad;

                //Toast.makeText(getApplicationContext(), String.valueOf(delayTimeSec), Toast.LENGTH_LONG).show();
                mpWin.start();
                final TextView txtCoinAwardCounter = (TextView) container.findViewById(R.id.txtCoinAwardCounter);

                Counter = 1;
                runnable = new Runnable(){
                    @Override
                    public void run() {
                        try {
                            //  Your auto increment logic...
                            txtCoinAwardCounter.setText(Tools.ChangeEnc(String.valueOf(Counter)));
                            if(Counter < RandomAwad) {
                                Counter++;
                                handler.postDelayed(runnable, delayTimeSec);
                            }
                            else
                            {
                                btnFreeReceiveCoins.setVisibility(View.VISIBLE);
                                btnRandomNumber.setVisibility(View.GONE);

                            }
                        }
                        catch (Exception err)
                        {
                            Toast.makeText(getApplicationContext(), err.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                };
                handler.postDelayed(runnable, delayTimeSec);

            }});

        btnFreeReceiveCoins.setOnClickListener(new ImageButton.OnClickListener() {
           @Override
           public void onClick(View v) {
               mpDropCoin.start();
               IncrementCoinCount(RandomAwad);
               popUpRandomAward.dismiss();
               InitializeWord();
           }
       });


        popUpRandomAward.showAtLocation(main_conainer, Gravity.TOP | Gravity.CENTER,                0, 0);

    }

    void GetSettings()
    {
        Tools tools = new Tools();
        String strPlayBackgroundMusic = tools.GetSettingVal(
                getString(R.string.app_default_key), mContext,
                "PlayBackgroundMusic");
        String strSoundNotificationMusic = tools.GetSettingVal(
                getString(R.string.app_default_key), mContext,
                "SoundNotificationMusic");

        if (strPlayBackgroundMusic.equals(""))
            strPlayBackgroundMusic = "1";// default value
        if (strSoundNotificationMusic.equals(""))
            strSoundNotificationMusic = "1";// default value

        if (strPlayBackgroundMusic.equals("1")) {
            EnablePlayBackgroundMusic = true;
        }
        else
        {
            EnablePlayBackgroundMusic = false;
        }

        if (strSoundNotificationMusic.equals("1")) {
             EnableSoundNotificationMusic = true;
        }
        else
        {
            EnableSoundNotificationMusic = false;
        }
    }

    /** Verifies the developer payload of a purchase. */
    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();

        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */

        return true;
    }

    // Callback for when a purchase is finished

    void setWaitScreen(boolean set) {
        //findViewById(R.id.screen_main).setVisibility(set ? View.GONE : View.VISIBLE);
        //findViewById(R.id.screen_wait).setVisibility(set ? View.VISIBLE : View.GONE);
    }

    void complain(String message) {
        Log.e(TAG, "**** TrivialDrive Error: " + message);
        alert("Error: " + message);
    }

    void alert(String message) {
        ShowAlertPopup(message, false);
//        AlertDialog.Builder bld = new AlertDialog.Builder(this);
//        bld.setMessage(message);
//        bld.setNeutralButton("OK", null);
//        Log.d(TAG, "Showing alert dialog: " + message);
//        bld.create().show();
    }
    // Called when consumption is complete


    public void ShowShopPopup()
    {
        try {
            int PopupHeight = ScreenWidth * 542 / 450;

            // layout = new LinearLayout(this);
            main_conainer = (LinearLayout) findViewById(R.id.main_conainer);
            LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
            container = (ViewGroup) inflater.inflate(R.layout.popup_shop, null);

            popUpShop = new PopupWindow(container, ScreenWidth, PopupHeight, true);
            popUpShop.setAnimationStyle(R.style.PopupAnimation);

            ImageView imgbuy1000 = container.findViewById(R.id.imgbuy1000);
            ImageView imgbuy2000 = container.findViewById(R.id.imgbuy2000);
            ImageView imgbuy3000 = container.findViewById(R.id.imgbuy3000);
            ImageView imgbuy5000 = container.findViewById(R.id.imgbuy5000);
            ImageView imgbuy10000 = container.findViewById(R.id.imgbuy10000);
            ImageView imgbuy15000 = container.findViewById(R.id.imgbuy15000);

            imgbuy1000.setOnClickListener(new ImageButton.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popUpShop.dismiss();
                    onBuyCoinButtonClicked(SKU_COIN1000);
                }
            });
            imgbuy2000.setOnClickListener(new ImageButton.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popUpShop.dismiss();
                    onBuyCoinButtonClicked(SKU_COIN2000);
                }
            });
            imgbuy3000.setOnClickListener(new ImageButton.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popUpShop.dismiss();
                    onBuyCoinButtonClicked(SKU_COIN3000);
                }
            });
            imgbuy5000.setOnClickListener(new ImageButton.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popUpShop.dismiss();
                    onBuyCoinButtonClicked(SKU_COIN5000);
                }
            });
            imgbuy10000.setOnClickListener(new ImageButton.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popUpShop.dismiss();
                    onBuyCoinButtonClicked(SKU_COIN10000);
                }
            });
            imgbuy15000.setOnClickListener(new ImageButton.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popUpShop.dismiss();
                    onBuyCoinButtonClicked(SKU_COIN15000);
                }
            });

            popUpShop.showAtLocation(main_conainer, Gravity.TOP | Gravity.CENTER, 0, 0);

//            LinearLayout toolbar_buy_coin_container = (LinearLayout) findViewById(R.id.toolbar_buy_coin_container);
//            toolbar_buy_coin_container.setOnClickListener(new View.OnClickListener() {
//                public void onClick(View v) {
//                }
//            });

            container.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    popUpShop.dismiss();
                    return true;
                }

            });
        }
        catch (Exception err1) {
            Toast.makeText(getApplicationContext(),
                    err1.getMessage(), Toast.LENGTH_LONG).show();

        }
    }

    public void onShowShopWindowClicked(View v0)
    {
        ShowShopPopup();
    }

    public void onBulbButtonClicked() {
        try {
            if(HelperLetterCount < (Word1.length() + 1))
                HelperLetterCount++;

            String AppDefaultKeyName = this.getString(R.string.app_default_key);

            Tools tools = new Tools();
            String strCoinCount = tools.GetSettingVal(AppDefaultKeyName, this, "CoinCount");
            Integer CoinCount;
            if(!strCoinCount.equals(""))
                CoinCount = Integer.parseInt(strCoinCount);
            else
                CoinCount = 0;

            if(CoinCount >= 100)
                CoinCount = CoinCount - 100;
            else
            {
                ShowShopPopup();
                //Toast.makeText(getApplicationContext(), "شما باید حداقل 100 سکه داشته باشید",                         Toast.LENGTH_LONG).show();
                return;
            }
            tools.ChangeSetting(AppDefaultKeyName, this, "CoinCount", String.valueOf(CoinCount));
            TextView tvCoinCount =  (TextView)findViewById(R.id.txtCoinCount);
            tvCoinCount.setText(Tools.ChangeEnc(String.valueOf(CoinCount)));

            LinearLayout final_word_conainer = (LinearLayout) findViewById(R.id.final_word_conainer);

            int ContainerChildCount = final_word_conainer.getChildCount();
            Integer ToLen = final_word_conainer.getChildCount() - (HelperLetterCount - 1 );
            for (int i = final_word_conainer.getChildCount() - 1; i >= ToLen ; i--) {
                ImageView CurImageView = (ImageView)final_word_conainer.getChildAt(i);
                int LetIn = GetLetterIndex(Word1.substring(ContainerChildCount - i - 1, ContainerChildCount - i ));
                int id =  this.getResources().getIdentifier("letter" + LetIn, "drawable", this.getPackageName());
                CurImageView.setImageResource(id);
            }


        }
        catch (Exception err)
        {
            Toast.makeText(getApplicationContext(), err.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    public void onShowSettingsButtonClicked(View v0) {
        try {
            ShowRandomAwardPopup();

//            Intent refresh = new Intent(mContext,
//                    SettingsActivity.class);
//            startActivity(refresh);
//            overridePendingTransition(R.anim.pull_in_right,
//                    R.anim.push_out_left);

        } catch (Exception ex) {

        }
    }


    public void ShowAlertPopup(String Message, boolean HasConfirm) {
        try {
            main_conainer =  (LinearLayout)findViewById(R.id.main_conainer);
            LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService (LAYOUT_INFLATER_SERVICE);

            container = (ViewGroup) inflater.inflate(R.layout.popup_alert, null);
            popUpAlert = new PopupWindow(container, ScreenWidth, ScreenHeight, true);
            popUpAlert.setAnimationStyle(R.style.PopupAnimation);

            TextView txtMessage = container.findViewById(R.id.txtMessage);
            ImageView btnConfirm = container.findViewById(R.id.btnConfirm);

            txtMessage.setText(Message);
            if(HasConfirm)
            {
                btnConfirm.setVisibility(View.VISIBLE);
                btnConfirm.setOnClickListener(new ImageButton.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        popUpAlert.dismiss();
                    }
                });
            }
            else
            {
                btnConfirm.setVisibility(View.GONE);
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Do something after 100ms
                        popUpAlert.dismiss();
                    }
                }, 3000);

            }



            popUpAlert.showAtLocation(main_conainer, Gravity.TOP | Gravity.CENTER,                0, 0);


        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }


    public void onShowChapterListClicked(View v0) {
        try {
            main_conainer =  (LinearLayout)findViewById(R.id.main_conainer);
            LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService (LAYOUT_INFLATER_SERVICE);

            container = (ViewGroup) inflater.inflate(R.layout.popup_chapter_list, null);
            popUpChapterList = new PopupWindow(container, ScreenWidth, ScreenHeight, true);
            popUpChapterList.setAnimationStyle(R.style.PopupAnimation);

            ImageView btnClose = container.findViewById(R.id.btnClose);
            btnClose.setOnClickListener(new ImageButton.OnClickListener() {

                @Override
                public void onClick(View v) {
                    popUpChapterList.dismiss();
                }
            });

            try {
                Cursor resultSet = mydb.GetChapters();

                mGridView = (GridView) container.findViewById(R.id.chapterlist_listview);
                mGridView.setAdapter(null);
                // Initialize with empty data
                mGridViewAdapter = new ChapterListViewDataAdapter(mContext, resultSet, HighestChapterSolved);
                mGridView.setAdapter(mGridViewAdapter);

                mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View view,
                                            int position, long id) {

                        try {
                            String strCode = ((TextView) view
                                    .findViewById(R.id.chapter_code)).getText()
                                    .toString();
                            int SelectedChapterNum = Integer.valueOf(strCode);

                            if(SelectedChapterNum <= HighestChapterSolved)
                            {
                                Cursor resultFirstOrderSet = mydb.GetFirstCapterOrder(SelectedChapterNum);
                                int FirstOrderNum = Integer.parseInt(resultFirstOrderSet.getString(resultFirstOrderSet.getColumnIndex("OrderNum")));

                                InitOrderNum = FirstOrderNum;
                                popUpChapterList.dismiss();
                                InitializeWord();
                            }
//                            Toast.makeText(getApplicationContext(), strCode,
//                                    Toast.LENGTH_LONG).show();

                        } catch (Exception err1) {
                            Toast.makeText(getApplicationContext(),
                                    err1.getMessage(), Toast.LENGTH_LONG).show();

                        }
                        // Toast.makeText(getApplicationContext(), "CLICKED",
                        // Toast.LENGTH_SHORT).show();
                    }
                });

            }
            catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }

            popUpChapterList.showAtLocation(main_conainer, Gravity.TOP | Gravity.CENTER,                0, 0);


        } catch (Exception ex) {

        }
    }

    public void onShowHelpClicked(View v0) {
        ShowHelpPopup();
    }


    public void ShowHelpPopup()
    {
        try {
            main_conainer =  (LinearLayout)findViewById(R.id.main_conainer);
            LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService (LAYOUT_INFLATER_SERVICE);

            container = (ViewGroup) inflater.inflate(R.layout.popup_help, null);
            popUpHelp = new PopupWindow(container, ScreenWidth, ScreenHeight, true);
            popUpHelp.setAnimationStyle(R.style.PopupAnimation);



            findViewById(R.id.main_conainer).post(new Runnable() {
                public void run() {
                    popUpHelp.showAtLocation(main_conainer, Gravity.TOP | Gravity.CENTER,                0, 0);
                }
            });


            container.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    popUpHelp.dismiss();
                    return true;
                }

            });

        }
        catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }


    public void ShowShowBulb()
    {
        try {
            main_conainer =  (LinearLayout)findViewById(R.id.main_conainer);
            LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService (LAYOUT_INFLATER_SERVICE);

            container = (ViewGroup) inflater.inflate(R.layout.popup_showbulb, null);
            popUpShowBulb= new PopupWindow(container, ScreenWidth, ScreenHeight, true);
            popUpShowBulb.setAnimationStyle(R.style.PopupAnimation);



            findViewById(R.id.main_conainer).post(new Runnable() {
                public void run() {
                    popUpShowBulb.showAtLocation(main_conainer, Gravity.TOP | Gravity.CENTER,                0, 0);
                }
            });


            container.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    popUpShowBulb.dismiss();
                    return true;
                }

            });

        }
        catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }
    public void onShowSettingsClicked(View v0) {
        try {
            main_conainer =  (LinearLayout)findViewById(R.id.main_conainer);
            LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService (LAYOUT_INFLATER_SERVICE);

            container = (ViewGroup) inflater.inflate(R.layout.popup_settings, null);
            popUpSettings = new PopupWindow(container, ScreenWidth, ScreenHeight, true);
            popUpSettings.setAnimationStyle(R.style.PopupAnimation);

            ImageView btnClose = container.findViewById(R.id.btnClose);
            btnClose.setOnClickListener(new ImageButton.OnClickListener() {

                @Override
                public void onClick(View v) {
                    popUpSettings.dismiss();
                }
            });


            SeekBar DifficultySeekBar = (SeekBar) container.findViewById(R.id.settings_seekbar_difficulty);

            CheckBox chkPlayBackgroundMusic = (CheckBox) container.findViewById(R.id.settings_play_background_music);
            CheckBox chkSoundNotificationMusic = (CheckBox) container.findViewById(R.id.settings_sound_notification);

            Tools tools = new Tools();
            String strPlayBackgroundMusic = tools.GetSettingVal(
                    getString(R.string.app_default_key), mContext,
                    "PlayBackgroundMusic");
            String strSoundNotificationMusic = tools.GetSettingVal(
                    getString(R.string.app_default_key), mContext,
                    "SoundNotificationMusic");

            if (strPlayBackgroundMusic.equals(""))
                strPlayBackgroundMusic = "1";// default value
            if (strSoundNotificationMusic.equals(""))
                strSoundNotificationMusic = "1";// default value

            if (strPlayBackgroundMusic.equals("1")) {
                chkPlayBackgroundMusic.setChecked(true);
            }
            else
            {
                chkPlayBackgroundMusic.setChecked(false);
            }

            if (strSoundNotificationMusic.equals("1")) {
                chkSoundNotificationMusic.setChecked(true);
            }
            else
            {
                chkSoundNotificationMusic.setChecked(false);
            }


            DifficultySeekBar
                    .setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        int progress = 0;

                        @Override
                        public void onProgressChanged(SeekBar seekBar,
                                                      int progresValue, boolean fromUser) {
                            progress = progresValue;
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                            try {
                                // textView.setText("Covered: " + progress + "/"
                                // +
                                // seekBar.getMax());
                                int FontSize = 12;
                                switch (progress) {
                                    case 0:
                                        FontSize = 12;
                                        break;
                                    case 1:
                                        FontSize = 14;
                                        break;
                                    case 2:
                                        FontSize = 16;
                                        break;
                                    case 3:
                                        FontSize = 18;
                                        break;
                                    case 4:
                                        FontSize = 20;
                                        break;
                                    case 5:
                                        FontSize = 22;
                                        break;
                                    case 6:
                                        FontSize = 24;
                                        break;
                                    case 7:
                                        FontSize = 26;
                                        break;
                                    case 8:
                                        FontSize = 28;
                                        break;
                                    case 9:
                                        FontSize = 30;
                                        break;
                                    case 10:
                                        FontSize = 32;
                                        break;

                                    default:
                                        break;
                                }
                                Tools tools = new Tools();
                                tools.ChangeSetting(
                                        getString(R.string.app_default_key),
                                        mContext, "HeaderFontSize",
                                        String.valueOf(FontSize));

                            } catch (Exception err) {
                                Toast.makeText(getApplicationContext(),
                                        err.getMessage(), Toast.LENGTH_SHORT)
                                        .show();

                            }
                        }
                    });

            chkPlayBackgroundMusic
                    .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                        @Override
                        public void onCheckedChanged(CompoundButton buttonView,
                                                     boolean isChecked) {
                            String PlayBackgroundMusic = "1";

                            if (isChecked) {
                                PlayBackgroundMusic = "1";
                                MainActivity.mpBGSound = MediaPlayer.create(getApplicationContext(), R.raw.bensound_littleidea);
                                MainActivity.mpBGSound.setLooping(true);

                                MainActivity.mpBGSound.start();
                            }
                            else {
                                PlayBackgroundMusic = "0";
                                MainActivity.mpBGSound.stop();
                                //MainActivity.mpBGSound.release();

                            }
                            Tools tools = new Tools();
                            tools.ChangeSetting(
                                    getString(R.string.app_default_key),
                                    mContext, "PlayBackgroundMusic",
                                    String.valueOf(PlayBackgroundMusic));

                        }
                    });


            chkSoundNotificationMusic
                    .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                        @Override
                        public void onCheckedChanged(CompoundButton buttonView,
                                                     boolean isChecked) {
                            String SoundNotificationMusic = "1";
                            if (isChecked) {
                                SoundNotificationMusic = "1";
                                MainActivity.EnableSoundNotificationMusic = true;
                            }
                            else {
                                SoundNotificationMusic = "0";
                                MainActivity.EnableSoundNotificationMusic = false;
                            }
                            Tools tools = new Tools();
                            tools.ChangeSetting(
                                    getString(R.string.app_default_key),
                                    mContext, "SoundNotificationMusic",
                                    String.valueOf(SoundNotificationMusic));

                        }
                    });




            popUpSettings.showAtLocation(main_conainer, Gravity.TOP | Gravity.CENTER,                0, 0);


        }
        catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }


    public void onShowAboutUsClicked(View v0) {
        try {
            main_conainer =  (LinearLayout)findViewById(R.id.main_conainer);
            LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService (LAYOUT_INFLATER_SERVICE);

            container = (ViewGroup) inflater.inflate(R.layout.popup_aboutus, null);
            popUpAboutUs = new PopupWindow(container, ScreenWidth, ScreenHeight, true);
            popUpAboutUs.setAnimationStyle(R.style.PopupAnimation);

            ImageView btnClose = container.findViewById(R.id.btnClose);
            btnClose.setOnClickListener(new ImageButton.OnClickListener() {

                @Override
                public void onClick(View v) {
                    popUpAboutUs.dismiss();
                }
            });

            popUpAboutUs.showAtLocation(main_conainer, Gravity.TOP | Gravity.CENTER,                0, 0);

            container.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    popUpAboutUs.dismiss();
                    return true;
                }

            });

        }
        catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    // User clicked the "Buy Gas" button
    public void onBuyCoinButtonClicked(String paramSKU) {
        try {
            Log.d(TAG, "Buy gas button clicked.");

            // launch the gas purchase UI flow.
            // We will be notified of completion via mPurchaseFinishedListener
            setWaitScreen(true);
            Log.d(TAG, "Launching purchase flow for gas.");

        /* TODO: for security, generate your payload here for verification. See the comments on
         *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
         *        an empty string, but on a production app you should carefully generate this. */
            String payload = "";
            
            mHelper.launchPurchaseFlow(this, paramSKU, RC_REQUEST,
                    mPurchaseFinishedListener, payload);
        }
        catch (Exception err)
        {
            Toast.makeText(getApplicationContext(), err.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            Log.d(TAG, "Query inventory finished.");
            if (result.isFailure()) {
                Log.d(TAG, "Failed to query inventory: " + result);
                return;
            }
            else {
                Log.d(TAG, "Query inventory was successful.");
                // does the user have the premium upgrade?
                mIsPremium = inventory.hasPurchase(SKU_PREMIUM);

                // update UI accordingly

                Log.d(TAG, "User is " + (mIsPremium ? "PREMIUM" : "NOT PREMIUM"));
            }

            Log.d(TAG, "Initial inventory query finished; enabling main UI.");
        }
    };

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isFailure()) {
                complain("بروز خطا در فرآیند خرید " + result);
                setWaitScreen(false);
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                complain("بروز خطا در فرآیند خرید");
                setWaitScreen(false);
                return;
            }

            Log.d(TAG, "Purchase successful.");

            if (purchase.getSku().equals(SKU_COIN1000) || purchase.getSku().equals(SKU_COIN2000) ||
                    purchase.getSku().equals(SKU_COIN3000) || purchase.getSku().equals(SKU_COIN5000)
                    || purchase.getSku().equals(SKU_COIN10000) || purchase.getSku().equals(SKU_COIN15000)) {
                // bought 1/4 tank of gas. So consume it.
                Log.d(TAG, "Purchase is coin. Starting gas consumption.");
                mHelper.consumeAsync(purchase, mConsumeFinishedListener);
            }
        }
    };

    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            Log.d(TAG, "Consumption finished. Purchase: " + purchase + ", result: " + result);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            // We know this is the "gas" sku because it's the only one we consume,
            // so we don't check which sku was consumed. If you have more than one
            // sku, you probably should check...
            if (result.isSuccess()) {
                // successfully consumed, so we apply the effects of the item in our
                // game world's logic, which in our case means filling the gas tank a bit
                Log.d(TAG, "Consumption successful. Provisioning.");

                if (purchase.getSku().equals(SKU_COIN1000))
                    IncrementCoinCount(500);
                if (purchase.getSku().equals(SKU_COIN2000))
                    IncrementCoinCount(1200);
                if (purchase.getSku().equals(SKU_COIN3000))
                    IncrementCoinCount(2000);
                if (purchase.getSku().equals(SKU_COIN5000))
                    IncrementCoinCount(3000);
                if (purchase.getSku().equals(SKU_COIN10000))
                    IncrementCoinCount(6000);
                if (purchase.getSku().equals(SKU_COIN15000))
                    IncrementCoinCount(10000);
                //alert("You filled 1/4 tank. Your tank is now " + String.valueOf(mTank) + "/4 full!");
                alert("خرید با موفقیت انجام شد" );
            }
            else {
                complain("خطا هنگام تکمیل فرآیند خرید " + result);
            }

            //alert("SKU =" + purchase.getSku());
            setWaitScreen(false);
            Log.d(TAG, "End consumption flow.");
        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            Log.d(TAG, "onActivityResult handled by IABUtil.");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mpBGSound.pause();
        // very important:
        Log.d(TAG, "Destroying helper.");
        if (mHelper != null) {
            mHelper.dispose();
            mHelper = null;
        }
//        if (mServiceConn != null) {
//            unbindService(mServiceConn);
//        }
    }

    protected void PutLetter(Context context, Canvas c, String strLetter, int LetterX, int LetterY)
    {

        int LetterIndex = GetLetterIndex(strLetter);
        Paint  p = new Paint();
        int id =  context.getResources().getIdentifier("letter" + LetterIndex, "drawable", context.getPackageName());
        Bitmap b= BitmapFactory.decodeResource(getResources(), id);

        RelativeLayout myCanvasContainer = (RelativeLayout) findViewById(R.id.myCanvasContainer);
        RelativeLayout myCanvas = (RelativeLayout) findViewById(R.id.myCanvas);

        //c.drawBitmap(b, LetterX, LetterY, p);

        float ScaleVal = (float) ((float) ScreenWidth / 1500F);
        ImageWidth =   (int)(b.getWidth() * ScaleVal);
        ImageHeight = (int)(b.getHeight() * ScaleVal);

        ImageView imgLetter = new ImageView(this);
        imgLetter.setImageResource(id);
        RelativeLayout.LayoutParams imglayoutParams = new RelativeLayout.LayoutParams(ImageWidth, ImageHeight);
        //imglayoutParams.setMargins(LetterX, LetterY ,0,0);

        imglayoutParams.leftMargin = LetterX;
        imglayoutParams.topMargin = LetterY;

        //imgLetter.getLayoutParams().width = ImageWidth;
        //imgLetter.getLayoutParams().height = ImageHeight;
        imgLetter.setLayoutParams(imglayoutParams);

        //c.drawBitmap(b, null, new RectF(LetterX, LetterY, LetterX + ImageWidth, LetterY + ImageHeight), p);

        AlphabetPoint AP = new AlphabetPoint(LetterX, LetterY , ImageWidth, LetterIndex, strLetter);
        ALAlpahbet.add(AP);


        //LinearLayout layout = new LinearLayout(context);

        //layout.addView(imgLetter);
        myCanvasContainer.addView(imgLetter);

        //layout.measure(c.getWidth(), c.getHeight());
        //layout.layout(0, 0, c.getWidth(), c.getHeight());

// To place the text view somewhere specific:
//canvas.translate(0, 0);
        //c.translate(LetterX, LetterY);
        //layout.draw(c);
        //c.translate(-LetterX, -LetterY);

        ImageLetters.add(imgLetter);
        //myCanvasContainer.bringToFront();

//        TextView tvCoinCount =  (TextView)findViewById(R.id.txtCoinCount);
//        tvCoinCount.setText(Tools.ChangeEnc(String.valueOf(LetterX)));
//
//        Toast.makeText(getApplicationContext(), String.valueOf(LetterX),
//                Toast.LENGTH_LONG).show();

    }

    protected void AddLetterToTryingBox(Canvas cn, String strLetter){
        try {
            int FilledBoxIndex = -1;
            LinearLayout llContainers = (LinearLayout) findViewById(R.id.llContainers);


            ImageView imgNewContainer = new ImageView(this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(10, 10, 10, 10);
            layoutParams.width = ScreenWidth / 7;
            layoutParams.height = ScreenWidth / 7;
            //imgNewContainer.setScaleType(ImageView.ScaleType.CENTER);
            layoutParams.weight = 1.0f;
            layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
            ;
            //layoutParams.layout
            imgNewContainer.setLayoutParams(layoutParams);
            imgNewContainer.setImageResource(R.drawable.empty);

            //imgNewContainer.setBackgroundResource(R.drawable.myborder1);

            llContainers.addView(imgNewContainer, 0);


            for (int i = TryingWord.length() - 1; i >= 0; i--) {
                ImageView CurImageView = (ImageView) llContainers.getChildAt(i);
                if (CurImageView.getDrawable().getConstantState() == getResources().getDrawable(R.drawable.empty).getConstantState()) {
                    int LetIn = GetLetterIndex(strLetter);
                    int id = this.getResources().getIdentifier("letter" + LetIn, "drawable", this.getPackageName());
                    CurImageView.setImageResource(id);
                    FilledBoxIndex = i;
                    break;
                }
            }

            //if(FilledBoxIndex == 0)// All Boxes have been filled
            if (TryingWord.length() == Word1.length()) {
                if (ALOtherWords.contains(TryingWord)) {
                    FillWord();

                    Animation animFadein = AnimationUtils.loadAnimation(getApplicationContext(),
                            R.anim.firework);
                    ImageView imageFirework = (ImageView) findViewById(R.id.imageFirework);
                    imageFirework.setVisibility(View.VISIBLE);
                    animFadein.setAnimationListener(new CompleteWordAnimationListener(imageFirework));
                    imageFirework.startAnimation(animFadein);

//                    if (!CurSolvedWords.contains(TryingWord)) {
//                        CurSolvedWords.add(TryingWord);
//                        Animation animFadein = AnimationUtils.loadAnimation(getApplicationContext(),
//                                R.anim.fade_and_move);
//                        ImageView Star = (ImageView) findViewById(R.id.imageStar);
//                        Star.setVisibility(View.VISIBLE);
//                        Star.startAnimation(animFadein);
//                    } else
//                        alert("شما قبلا این کلمه را پیدا کرده اید");
//                    MakeTryingBoxesEmpty();
                } else if (!TryingWord.equals(Word1)) {
                    MakeTryingBoxesEmpty();
                } else {
                    FillWord();
                    //cn.drawColor(Color.WHITE);


                    Animation animFadein = AnimationUtils.loadAnimation(getApplicationContext(),
                            R.anim.firework);
                    ImageView imageFirework = (ImageView) findViewById(R.id.imageFirework);
                    imageFirework.setVisibility(View.VISIBLE);
                    animFadein.setAnimationListener(new CompleteWordAnimationListener(imageFirework));
                    imageFirework.startAnimation(animFadein);


                }
            }
            //else
            //    mpBubble.start();

        }
        catch(Exception err)
        {
            Toast.makeText(getApplicationContext(), err.getMessage(),
                    Toast.LENGTH_LONG).show();
        }


    }

    public boolean IsChapterFinished()
    {
        if(HighestOrderSolved == InitOrderNum)
            InitOrderNum = 0;

        Cursor resultSet = mydb.GetUncompletedWord(InitOrderNum);

        if(resultSet.getCount() == 0) {
            if(InitOrderNum  != 0)
                InitOrderNum++;
            return true;
        }

        int NextChapterNum = Integer.parseInt(resultSet.getString(resultSet.getColumnIndex("ChapterNum")));
        if(NextChapterNum != ChapterNum)
            return true;
        else
            return false;

    }

    public class CompleteWordAnimationListener implements Animation.AnimationListener
    {

        ImageView _imageView;

        public CompleteWordAnimationListener(ImageView CurImageView) {
            this._imageView = CurImageView;
        }

        @Override
        public void onAnimationStart(Animation arg0) {
        }
        @Override
        public void onAnimationRepeat(Animation arg0) {
        }
        @Override
        public void onAnimationEnd(Animation arg0) {
            Integer WordLen = Word1.length();
            //InitializeWord();

            main_conainer =  (LinearLayout)findViewById(R.id.main_conainer);
            LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService (LAYOUT_INFLATER_SERVICE);

            if(!IsChapterFinished()) {
                container = (ViewGroup) inflater.inflate(R.layout.popup_next_letter, null);

                popUpNextLevel = new PopupWindow(container, ScreenWidth, ScreenHeight, true);

                RelativeLayout llNextLevel = container.findViewById(R.id.llNextLevel);
                llNextLevel.setOnClickListener(new ImageButton.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        popUpNextLevel.dismiss();
                        InitializeWord();
                    }
                });

                popUpNextLevel.showAtLocation(main_conainer, Gravity.TOP | Gravity.CENTER,
                        0, 0);
            }
            else
            {
                if(OrderNum == HighestOrderSolved)
                    ShowRandomAwardPopup();
                else
                {
                    popUpNextLevel.dismiss();
                    InitializeWord();
                }

            }

//            int IncrementCount = 0;
//            if(WordLen == 3)
//                IncrementCount = 5;
//            else if(WordLen == 4)
//                IncrementCount = 10;
//            else
//                IncrementCount = 15;

//            IncrementCoinCount(IncrementCount);
        }

    };

    private void IncrementCoinCount(Integer IncrementCount) {
        String AppDefaultKeyName = this.getString(R.string.app_default_key);

        Tools tools = new Tools();
        String strCoinCount = tools.GetSettingVal(AppDefaultKeyName, this, "CoinCount");
        Integer CoinCount = 0;
        if(!strCoinCount.equals(""))
            CoinCount = Integer.parseInt(strCoinCount);
        else
            CoinCount = 0;

        CoinCount = CoinCount + IncrementCount;

        tools.ChangeSetting(AppDefaultKeyName, this, "CoinCount", String.valueOf(CoinCount));
        TextView tvCoinCount =  (TextView)findViewById(R.id.txtCoinCount);
        tvCoinCount.setText(Tools.ChangeEnc(String.valueOf(CoinCount)));

    }

    protected void FillWord()
    {
        if(EnableSoundNotificationMusic) {
            if(mpBell.isPlaying()) {
                mpBell = MediaPlayer.create(this, R.raw.bell1);

            }

            mpBell.start();
        }
        LinearLayout final_word_conainer = (LinearLayout) findViewById(R.id.final_word_conainer);

        int ContainerChildCount = final_word_conainer.getChildCount();
        for (int i = final_word_conainer.getChildCount() - 1; i >= 0 ; i--) {
            ImageView CurImageView = (ImageView)final_word_conainer.getChildAt(i);
            int LetIn = GetLetterIndex(Word1.substring(ContainerChildCount - i - 1, ContainerChildCount - i ));
            int id =  this.getResources().getIdentifier("letter" + LetIn, "drawable", this.getPackageName());
            CurImageView.setImageResource(id);
        }
        mydb.MarkAsCompleted(Word1);


    }

    public class MyAnimationListener implements Animation.AnimationListener
    {

        ImageView _imageView;

        public MyAnimationListener(ImageView CurImageView) {
            this._imageView = CurImageView;
        }

        @Override
        public void onAnimationStart(Animation arg0) {
        }
        @Override
        public void onAnimationRepeat(Animation arg0) {
        }
        @Override
        public void onAnimationEnd(Animation arg0) {
            _imageView.setImageResource(R.drawable.empty);
        }

    };

    protected void MakeTryingBoxesEmpty()
    {

        LinearLayout llContainers = (LinearLayout) findViewById(R.id.llContainers);

//        for (int i = llContainers.getChildCount() - 1; i >= 0 ; i--) {
//            ImageView CurImageView = (ImageView)llContainers.getChildAt(i);
//            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(),
//                    R.anim.shake);
//
//            CurImageView.startAnimation(animation);
//
//
//            animation.setAnimationListener(new MyAnimationListener(CurImageView));
//
//        }
        llContainers.removeAllViews();



        for (int i = 0; i < ImageLetters.size(); i++) {
            ImageView CurImageLetter = ImageLetters.get(i);
                    Animation animFadein = AnimationUtils.loadAnimation(getApplicationContext(),
                            R.anim.shake2);
                    CurImageLetter.startAnimation(animFadein);

        }
        if(EnableSoundNotificationMusic)
            mpError.start();


    }


    protected void InitializeWord()
    {
        try {
            setContentView(R.layout.activity_main);
            TextView txtBulbCoinCount = (TextView) findViewById(R.id.txtBulbCoinCount);
            txtBulbCoinCount.setText(Tools.ChangeEnc(String.valueOf("100")));


            ALAlpahbet.clear();
            ALOtherWords.clear();
            CurSolvedWords.clear();
            TryingWord = "";
            HelperLetterCount = 1;

            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            ScreenWidth = displayMetrics.widthPixels;
            ScreenHeight = displayMetrics.heightPixels;


            Cursor resultSet = mydb.GetUncompletedWord(InitOrderNum);


            resultSet.moveToFirst();
            Word1 = resultSet.getString(1);
            int WordCode = Integer.parseInt(resultSet.getString(0));

            OrderNum = Integer.parseInt(resultSet.getString(resultSet.getColumnIndex("OrderNum")));
            ChapterNum = Integer.parseInt(resultSet.getString(resultSet.getColumnIndex("ChapterNum")));
            if(InitOrderNum != 0)
                InitOrderNum = OrderNum + 1;

            if(OrderNum > HighestOrderSolved)
                HighestOrderSolved = OrderNum;
            if(ChapterNum > HighestChapterSolved)
                HighestChapterSolved = ChapterNum;

            TextView txtLevelIndex = (TextView) findViewById(R.id.txtLevelIndex);
            txtLevelIndex.setText(Tools.ChangeEnc(String.valueOf(ChapterNum)));

            Cursor OtherWordSet = mydb.GetOtherWords(WordCode);

            if (OtherWordSet.moveToFirst()) {
                do {
                    String MyOtherWord = OtherWordSet.getString(OtherWordSet.getColumnIndex("Word"));
                    ALOtherWords.add(MyOtherWord);
                    // do what ever you want here
                } while (OtherWordSet.moveToNext());
            }
            OtherWordSet.close();

            dv = new DrawingView(this);
            //setContentView(dv);
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setDither(true);
            mPaint.setColor(Color.WHITE);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeJoin(Paint.Join.ROUND);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setStrokeWidth(40);
            RelativeLayout myCanvasContainer = (RelativeLayout) findViewById(R.id.myCanvas);
            LinearLayout llContainers = (LinearLayout) findViewById(R.id.llContainers);
            LinearLayout final_word_conainer = (LinearLayout) findViewById(R.id.final_word_conainer);

            llContainers.removeAllViews();
            final_word_conainer.removeAllViews();

            for (int i = 0; i < Word1.length(); i++) {
                ImageView imgNewContainer = new ImageView(this);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(10, 10, 10, 10);
                layoutParams.width = ScreenWidth / 7;
                layoutParams.height = ScreenWidth / 7;
                //imgNewContainer.setScaleType(ImageView.ScaleType.CENTER);
                layoutParams.weight = 1.0f;
                layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
                ;
                //layoutParams.layout
                imgNewContainer.setLayoutParams(layoutParams);
                imgNewContainer.setImageResource(R.drawable.empty);

                //imgNewContainer.setBackgroundResource(R.drawable.myborder1);

                //llContainers.addView(imgNewContainer);

                imgNewContainer = new ImageView(this);
//            layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//            layoutParams.setMargins(10, 10, 10 ,10);
//            layoutParams.width = 150;
//            layoutParams.height = 150;
                imgNewContainer.setLayoutParams(layoutParams);
                imgNewContainer.setImageResource(R.drawable.empty);
                final_word_conainer.addView(imgNewContainer);
            }
            myCanvasContainer.addView(dv);


            String AppDefaultKeyName = this.getString(R.string.app_default_key);
            Tools tools = new Tools();
            String strCoinCount = tools.GetSettingVal(AppDefaultKeyName, this, "CoinCount");
            Integer CoinCount = 0;
            if (!strCoinCount.equals(""))
                CoinCount = Integer.parseInt(strCoinCount);
            else
                CoinCount = 0;

            TextView tvCoinCount = (TextView) findViewById(R.id.txtCoinCount);
            tvCoinCount.setText(Tools.ChangeEnc(String.valueOf(CoinCount)));

            LinearLayout bulb = (LinearLayout)findViewById(R.id.bulb);
            ImageView imgAboutUs = (ImageView)findViewById(R.id.imgAboutUs);

            bulb.bringToFront();
            imgAboutUs.bringToFront();

            handler.removeCallbacks(runnableShowBulbHelp);
            runnableShowBulbHelp = new Runnable(){
                @Override
                public void run() {
                    ShowShowBulb();
                }
            };
            handler.postDelayed(runnableShowBulbHelp, 60000);
        }
        catch(Exception err)
        {
            Toast.makeText(getApplicationContext(), err.getMessage(),
                    Toast.LENGTH_LONG).show();
        }


    }

    public class AlphabetPoint{
        public int PointX;
        public int PointY;
        public int Len;
        public int LetterIndex;
        public String Letter;

        public AlphabetPoint(int x, int y, int len, int letterindex, String letter){
            PointX = x;
            PointY = y;
            Len = len;
            LetterIndex = letterindex;
            Letter = letter;
        }
    }

    public class DrawingView extends View {

        public int width;
        public  int height;
        private Bitmap mBitmap;
        private Canvas mCanvas;
        private Path mPath;
        private Path mPath2;
        private Paint   mBitmapPaint;
        Context context;
        private Paint circlePaint;
        private Path circlePath;
        protected int DrawCounter = 0;

        private int CanvasX;
        private int CanvasY;

        public DrawingView(Context c) {
            super(c);
            context=c;
            mPath = new Path();
            mPath2 = new Path();
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);
            circlePaint = new Paint();
            circlePath = new Path();
            circlePaint.setAntiAlias(true);
            circlePaint.setColor(Color.BLUE);
            circlePaint.setStyle(Paint.Style.STROKE);
            circlePaint.setStrokeJoin(Paint.Join.MITER);
            circlePaint.setStrokeWidth(3f);

        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);

            this.CanvasX = w;
            this.CanvasY = h;
            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
        }

        protected void DrawContainer(Canvas c, int x, int y)
        {
            Paint paint = new Paint();
            paint.setAlpha(10);
            Rect r = new Rect(x, y, x + 150, y + 150);

            paint.setStyle(Paint.Style.FILL);
            //paint.setColor(Color.YELLOW);
            c.drawRect(r, paint);

            // border
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.BLACK);
            c.drawRect(r, paint);

        }

        protected void RandomPlaceWord(int WordLen, String strLetter){
            int randomX = 0;//ThreadLocalRandom.current().nextInt(10, CanvasX - 350);// new Random().nextInt(CanvasX) - 100;
            int randomY = 0;//ThreadLocalRandom.current().nextInt(300, CanvasY - CanvasY / 2);//new Random().nextInt(CanvasY) + 300;

            //String[] ArCordiationCollection;
            if(WordLen == 3)
            {
                String[] ArCordiationCollection = Letter3Cordination.split(",");
                String[] CurCoridation = ArCordiationCollection[ALAlpahbet.size()].split(";");
                randomX = (int)(Float.valueOf(CurCoridation[0])  * CanvasX / 7) + CanvasX / 6;
                randomY = (int)(Float.valueOf(CurCoridation[1])  * CanvasY / 7)+ 100;
            }
            else if(WordLen == 4) {
                String[] ArCordiationCollection = Letter4Cordination.split(",");
                String[] CurCoridation = ArCordiationCollection[ALAlpahbet.size()].split(";");
                randomX = (int)(Float.valueOf(CurCoridation[0])  * CanvasX / 7) + CanvasX / 6;
                randomY = (int)(Float.valueOf(CurCoridation[1])  * CanvasY / 7)+ 100;
            }
            else// if(WordLen == 5)
            {
                String[] ArCordiationCollection = Letter5Cordination.split(",");
                String[] CurCoridation = ArCordiationCollection[ALAlpahbet.size()].split(";");
                randomX = (int)(Float.valueOf(CurCoridation[0])  * CanvasX / 7) + CanvasX / 6;
                randomY = (int)(Float.valueOf(CurCoridation[1])  * CanvasY / 7)+ 100;
            }



            PutLetter(context, dv.mCanvas, strLetter, randomX, randomY);
        }

        protected void RePlaceWord(int index, String strLetter){
            int CurX = (ALAlpahbet.get(index)).PointX;
            int CurY = (ALAlpahbet.get(index)).PointY;
            PutLetter(context, dv.mCanvas, strLetter, CurX, CurY);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            try {

                super.onDraw(canvas);

                canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
                canvas.drawPath(mPath, mPaint);
                canvas.drawPath(mPath2, mPaint);
                canvas.drawPath(circlePath, circlePaint);
                //canvas.drawText("TEST", CanvasX/2, CanvasY/2, mPaint);

                if (DrawCounter == 0) {
                    for (int k = 0; k < Word1.length(); k++) {
                        int RandomIndex = ((int) (Math.random() * (Word1.length() - 0))) + 0;
                        while (RandomIndices.contains(RandomIndex) )
                            RandomIndex = ((int) (Math.random() * (Word1.length() - 0))) + 0;
                        RandomIndices.add(RandomIndex);
                        RandomPlaceWord(Word1.length(), Word1.substring(RandomIndex, RandomIndex + 1));
                    }
                    RandomIndices.clear();
                }

//            DrawContainer(canvas, 10, 10);
//            DrawContainer(canvas, 210, 10);
//            DrawContainer(canvas, 410, 10);

//            for(int j=0; j< TryingWord.length(); j++)
//            {
//                int LetIn = GetLetterIndex(TryingWord.substring(j, j + 1));
//                Paint  p = new Paint();
//                int id =  context.getResources().getIdentifier("letter" + LetIn, "drawable", context.getPackageName());
//                Bitmap b= BitmapFactory.decodeResource(getResources(), id);
//                mCanvas.drawBitmap(b, 10 + (j * 200), 10, p);
//            }

                DrawCounter++;
            }
            catch(Exception err)
            {
                Toast.makeText(getApplicationContext(), err.getMessage(),
                        Toast.LENGTH_LONG).show();
            }

        }


        private float mX, mY;
        private static final float TOUCH_TOLERANCE = 4;

        private void touch_start(float x, float y) {
            mPath.reset();
            mPath.moveTo(x, y);
            mX = x;
            mY = y;
        }

        private void touch_move(float x, float y) {
            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {

                mPath.reset();
                mPath.moveTo(mX, mY);
                circlePath.reset();
                //mPath.quadTo( (x + mX)/2, (y + mY)/2 ,mX, mY);
                mPath.quadTo(mX, mY, x , y );
                //circlePath.addCircle(mX, mY, 30, Path.Direction.CW);


                for (int i = 0; i < ALAlpahbet.size(); i++)
                {
                    int CurX = (ALAlpahbet.get(i)).PointX;
                    int CurY = (ALAlpahbet.get(i)).PointY;
                    int CurLen = (ALAlpahbet.get(i)).Len;
                    int CurLetterIndex = (ALAlpahbet.get(i)).LetterIndex;
                    String CurLetter = (ALAlpahbet.get(i)).Letter;


                    if(x > CurX && x < (CurLen + CurX) &&  y > CurY && y < (CurLen + CurY)
                            ) {
                        if(TryingWord.indexOf(CurLetter) == -1) {
                            if(TryingWord.length() > 0)
                                mPath2.lineTo(CurX + CurLen/2, CurY + CurLen/2);
                                //mPath2.lineTo(x, y);
                            TryingWord += CurLetter;
                            AddLetterToTryingBox(mCanvas, CurLetter);
                            mPath2.moveTo(CurX + CurLen/2, CurY + CurLen/2);
                            mPath.moveTo(CurX + CurLen/2, CurY + CurLen/2);
                            mX = CurX + CurLen/2;
                            mY = CurY + CurLen/2;


                            if(EnableSoundNotificationMusic)
                                mpBubble.start();



                        }
                        // Toast.makeText(context, Float.toString(mX) , Toast.LENGTH_LONG).show();
                    }
                }
            }
        }

        private void ShakeLetters()
        {
            try {


                LinearLayout llContainers = (LinearLayout) findViewById(R.id.llContainers);

                for (int i = llContainers.getChildCount() - 1; i >= 0; i--) {
                    ImageView CurImageView = (ImageView) llContainers.getChildAt(i);
                    Animation animation = AnimationUtils.loadAnimation(getApplicationContext(),
                            R.anim.shake);

                    CurImageView.startAnimation(animation);


                    animation.setAnimationListener(new MyAnimationListener(CurImageView));

                }
            }
            catch(Exception err2)
                {
                    Toast.makeText(getApplicationContext(), err2.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
        }

        private void touch_up() {
            mPath.lineTo(mX, mY);
            circlePath.reset();
            // commit the path to our offscreen
            //mCanvas.drawPath(mPath,  mPaint);
            // kill this so we don't double draw
            mPath.reset();
            mPath2.reset();
            if(!TryingWord.equals(Word1)) {
                //mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                MakeTryingBoxesEmpty();
                //ShakeLetters();
                //for(int k=0; k< Word1.length(); k++)
                //    RePlaceWord(k, Word1.substring(k, k+1) );
            }
            //for(int k=0; k< Word1.length(); k++)
             //   RePlaceWord(k, Word1.substring(k, k+1) );
            TryingWord = "";

        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();


            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touch_start(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    touch_move(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
//                    if(x < 200 &&  y > mCanvas.getHeight() - 300) {
//                        onBulbButtonClicked();
//                        invalidate();
//                        return true;
//                    }
                    touch_up();
                    invalidate();
                    break;
            }
            return true;
        }
    }

    private int GetLetterIndex(String strLetter) {
        if(strLetter.equals("ا"))
            return 1;
        else if(strLetter.equals("ب"))
            return 2;
        else if(strLetter.equals("پ"))
            return 3;
        else if(strLetter.equals("ت"))
            return 4;
        else if(strLetter.equals("ث"))
            return 5;
        else if(strLetter.equals("ج"))
            return 6;
        else if(strLetter.equals("چ"))
            return 7;
        else if(strLetter.equals("ح"))
            return 8;
        else if(strLetter.equals("خ"))
            return 9;
        else if(strLetter.equals("د"))
            return 10;
        else if(strLetter.equals("ذ"))
            return 11;
        else if(strLetter.equals("ر"))
            return 12;
        else if(strLetter.equals("ز"))
            return 13;
        else if(strLetter.equals("ژ"))
            return 14;
        else if(strLetter.equals("س"))
            return 15;
        else if(strLetter.equals("ش"))
            return 16;
        else if(strLetter.equals("ص"))
            return 17;
        else if(strLetter.equals("ض"))
            return 18;
        else if(strLetter.equals("ط"))
            return 19;
        else if(strLetter.equals("ظ"))
            return 20;
        else if(strLetter.equals("ع"))
            return 21;
        else if(strLetter.equals("غ"))
            return 22;
        else if(strLetter.equals("ف"))
            return 23;
        else if(strLetter.equals("ق"))
            return 24;
        else if(strLetter.equals("ک"))
            return 25;
        else if(strLetter.equals("گ"))
            return 26;
        else if(strLetter.equals("ل"))
            return 27;
        else if(strLetter.equals("م"))
            return 28;
        else if(strLetter.equals("ن"))
            return 29;
        else if(strLetter.equals("و"))
            return 30;
        else if(strLetter.equals("ه"))
            return 31;
        else if(strLetter.equals("ی"))
            return 32;


        return 0;
    }
}
