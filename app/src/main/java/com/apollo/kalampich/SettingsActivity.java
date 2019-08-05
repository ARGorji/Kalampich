package com.apollo.kalampich;

import android.content.Context;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.apollo.kalampich.util.Tools;

import java.util.Arrays;
import java.util.List;


public class SettingsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private WebView HeaderWebView;
    private WebView ContentWebView;
    private Context mContext;

    private void initView() {

        toolbar = (Toolbar) findViewById(R.id.toolbar_general);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        //toolbar.setNavigationIcon(R.drawable.ic_launcher);

        ImageView btnBack = (ImageView) toolbar
                .findViewById(R.id.btn_back);
        btnBack.setVisibility(View.VISIBLE);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    finish();
                    overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);

                } catch (Exception ex) {

                }

            }
        });

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_settings);
            mContext = getApplicationContext();

            initView();

            TextView txtCaption = (TextView) findViewById(R.id.toolbar_Caption);
            txtCaption.setText("تنظیمات");

            SeekBar DifficultySeekBar = (SeekBar) findViewById(R.id.settings_seekbar_difficulty);

            CheckBox chkPlayBackgroundMusic = (CheckBox) findViewById(R.id.settings_play_background_music);
            CheckBox chkSoundNotificationMusic = (CheckBox) findViewById(R.id.settings_sound_notification);

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


        } catch (Exception err1) {
            Toast.makeText(getApplicationContext(), err1.getMessage(),
                    Toast.LENGTH_LONG).show();

        }
    }
}
