package com.apollo.kalampich;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.apollo.kalampich.model.ChapterItem;
import com.apollo.kalampich.util.Tools;

import java.util.ArrayList;

/**
 * Created by cpu on 1/15/2018.
 */


public class ChapterListViewDataAdapter extends CursorAdapter {

    private Context mContext;
    private Integer mLatestSolvedChapter;

    public ChapterListViewDataAdapter(Context mContext, Cursor cursor, int LatestSolvedChapter) {
        super(mContext, cursor, 0);
        this.mContext = mContext;
        this.mLatestSolvedChapter = LatestSolvedChapter;
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.chapterlist_item, parent, false);
    }

    // The bindView method is used to bind all data to a given view
    // such as setting the text on a TextView.
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        TextView ChapterNum = (TextView) view.findViewById(R.id.chapter_name);
        TextView ChapterCode = (TextView) view.findViewById(R.id.chapter_code);
        LinearLayout ChapterContainer = (LinearLayout) view.findViewById(R.id.llChapterContainer);

        // Extract properties from cursor
        String strChapterNum = cursor.getString(cursor.getColumnIndexOrThrow("_id"));
        //int priority = cursor.getInt(cursor.getColumnIndexOrThrow("priority"));
        int CurChapterNum = Integer.valueOf(strChapterNum);
        if(CurChapterNum > mLatestSolvedChapter)
            ChapterContainer.setBackground(mContext.getResources().getDrawable(R.drawable.bg_unsolved_chapter_ltem));
        else
            ChapterContainer.setBackground(mContext.getResources().getDrawable(R.drawable.bg_chapterlist_item));


        // Populate fields with extracted properties
        ChapterNum.setText("فصل " + Tools.ChangeEnc(strChapterNum));
        ChapterCode.setText(strChapterNum);
        //ChapterCode.setText("A");
    }



}