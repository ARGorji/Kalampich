package com.apollo.kalampich.util;

import com.apollo.kalampich.R;

import android.app.AlarmManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.DisplayMetrics;
import android.widget.Toast;


public class Tools {

	public static String ChangeEnc(String str) {
		try {
			String Result = "";
			String CurChar = "";
			for (int i = 0; i < str.length(); i++) {
				CurChar = str.substring(i, i + 1);
				if (CurChar.equals("0"))
					Result += "۰";
				else if (CurChar.equals("1"))
					Result += "۱";

				else if (CurChar.equals("2"))
					Result += "۲";

				else if (CurChar.equals("3"))
					Result += "۳";

				else if (CurChar.equals("4"))
					Result += "۴";

				else if (CurChar.equals("5"))
					Result += "۵";

				else if (CurChar.equals("6"))
					Result += "۶";

				else if (CurChar.equals("7"))
					Result += "۷";

				else if (CurChar.equals("8"))
					Result += "۸";

				else if (CurChar.equals("9"))
					Result += "۹";
				else
					Result += CurChar;

			}
			return Result;
		} catch (Exception err) {
			return str;
		}
	}

	public int dpToPx(Context CurContext, int dp) {
		final float scale = CurContext.getResources().getDisplayMetrics().density;
		int px = (int) (dp * scale + 0.5f);
		return px;
	}

	public boolean ChangeSetting(String AppDefaultKey, Context CurContext,
			String KeyName, String NewData) {
		try {
			SharedPreferences sharedPref = CurContext.getSharedPreferences(
					AppDefaultKey, Context.MODE_PRIVATE);
			String CurrentFavCodes = sharedPref.getString(KeyName, "");

			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putString(KeyName, NewData);
			editor.commit();
			return true;
		} catch (Exception err) {
			return false;
		}

	}

	public String GetSettingVal(String AppDefaultKey, Context CurContext,
			String KeyName) {

		SharedPreferences sharedPref = CurContext.getSharedPreferences(
				AppDefaultKey, Context.MODE_PRIVATE);
		String Result = sharedPref.getString(KeyName, "");
		return Result;

	}

	public String PurifyText(String str) {
		if (str.equals(null))
			return "";
		str = str.replace("&quot;", "\"");
		str = str.replace("&nbsp;", " ");
		str = str.replace("&zwnj;", "");
		str = str.replace("&raquo;", "»");
		str = str.replace("&laquo;", "«");
		str = str.replace("&lrm;", "");
		str = str.replace("&gt;", ">");
		str = str.replace("&lt;", "<");
		str = str.replace("<br>", "\n");
		str = str.replace("<br />;", "\n");
		str = str.replace("</ br />;", "\n");

		return str;
	}

	public String RemoveTags(String InputHtml) {
		try {
			String InnerContent = InputHtml;
			int CutLen = 0;
			int StartIndex = 0;
			int EndIndex = 0;
			String TagContent = "";

			StartIndex = InnerContent.indexOf("<", 0);
			while (StartIndex >= 0) {
				EndIndex = InnerContent.indexOf(">", StartIndex + 1);
				if (EndIndex > 0) {
					CutLen = EndIndex - StartIndex - 1;
					TagContent = InnerContent.substring(StartIndex + 1,
							StartIndex + 1 + CutLen);
					InnerContent = InnerContent.replace("<" + TagContent + ">",
							"");
					StartIndex = StartIndex - TagContent.length();
				} else
					break;
				StartIndex = InnerContent.indexOf("<", 0);
			}
			return InnerContent;
		} catch (Exception err) {
			return InputHtml;

		}
	}

	public String GetDefaultFontName(Context CurContext) {
		try {
			Tools tools = new Tools();
			String strDefaultFontName = tools.GetSettingVal(
					CurContext.getString(R.string.app_default_key), CurContext,
					"DefaultFontName");
			if (strDefaultFontName.equals(""))
				strDefaultFontName = "irsans";
			return strDefaultFontName;
		} catch (Exception errLoad) {
			return "irsans";

		}

	}

	public static String GetDeviceInfo() {
		try {
			String _OSVERSION = System.getProperty("os.version");
			String _RELEASE = android.os.Build.VERSION.RELEASE;
			String _DEVICE = android.os.Build.DEVICE;
			String _MODEL = android.os.Build.MODEL;
			String _PRODUCT = android.os.Build.PRODUCT;
			String _BRAND = android.os.Build.BRAND;
			String _DISPLAY = android.os.Build.DISPLAY;
			String _CPU_ABI = android.os.Build.CPU_ABI;
			String _CPU_ABI2 = android.os.Build.CPU_ABI2;
			String _UNKNOWN = android.os.Build.UNKNOWN;
			String _HARDWARE = android.os.Build.HARDWARE;
			String _ID = android.os.Build.ID;
			String _MANUFACTURER = android.os.Build.MANUFACTURER;
			String _SERIAL = android.os.Build.SERIAL;
			String _USER = android.os.Build.USER;
			String _HOST = android.os.Build.HOST;
			String _SDK_INT = String.valueOf(android.os.Build.VERSION.SDK_INT);
			
			String Result = "";
			Result += "OSVERSION=" + _OSVERSION;
			Result += " RELEASE=" + _RELEASE;
			Result += " DEVICE=" + _DEVICE;
			Result += " MODEL=" + _MODEL;
			Result += " PRODUCT=" + _PRODUCT;
			Result += " BRAND=" + _BRAND;
			Result += " DISPLAY=" + _DISPLAY;
			Result += " CPU_ABI=" + _CPU_ABI;
			Result += " CPU_ABI2=" + _CPU_ABI2;
			Result += " UNKNOWN=" + _UNKNOWN;
			Result += " HARDWARE=" + _HARDWARE;
			Result += " ID=" + _ID;
			Result += " MANUFACTURER=" + _MANUFACTURER;
			Result += " SERIAL=" + _SERIAL;
			Result += " USER=" + _USER;
			Result += " HOST=" + _HOST;
			Result += " SDK_INT=" + _SDK_INT;

			return Result;
		} catch (Exception err) {
			return "";
		}
	}


}
