package com.example.testforble.utils;


import android.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;

import java.util.ArrayList;

public class DialogUtils {

	public static final int dStyle = com.example.testforble.R.style.AppCompatAlertDialogStyle;

	public static void confirm(Context ctx, String title, String msg, DialogInterface.OnClickListener yesClick, boolean cancelable) {

		AlertDialog alert = new AlertDialog.Builder(ctx, dStyle)
				.setTitle(title)
				.setMessage(msg)
				.setPositiveButton(ctx.getString(R.string.yes), yesClick)
				.setNegativeButton(ctx.getString(R.string.no), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				})
				.setCancelable(cancelable)
				.create();
		if (!((Activity) ctx).isFinishing()) alert.show();
	}

	public static void singleChoice(Context ctx, int title, String[] items, int clickedItem, DialogInterface.OnClickListener itemClick, boolean cancelable) {

		AlertDialog alert = new AlertDialog.Builder(ctx, dStyle)
				.setTitle(title)
				.setSingleChoiceItems(items, clickedItem, itemClick)
				.setCancelable(cancelable)
				.create();
		if (!((Activity) ctx).isFinishing()) alert.show();
	}

	public static void singleChoice(Context ctx, String title, String[] items, int clickedItem, DialogInterface.OnClickListener itemClick, boolean cancelable) {

		AlertDialog alert = new AlertDialog.Builder(ctx, dStyle)
				.setTitle(title)
				.setSingleChoiceItems(items, clickedItem, itemClick)
				.setCancelable(cancelable)
				.create();
		if (!((Activity) ctx).isFinishing()) alert.show();
	}

	public static void singleChoice(Context ctx, String title, String[] items, int clickedItem, DialogInterface.OnClickListener itemClick
			, DialogInterface.OnClickListener yesClick, boolean cancelable) {

		AlertDialog alert = new AlertDialog.Builder(ctx, dStyle)
				.setTitle(title)
				.setSingleChoiceItems(items, clickedItem, itemClick)
				.setPositiveButton(ctx.getString(R.string.yes), yesClick)
				.setCancelable(cancelable)
				.create();
		if (!((Activity) ctx).isFinishing()) alert.show();
	}

	public static void multipleChoice (Context ctx, String title, String[] items, boolean[] checkedItems, DialogInterface.OnMultiChoiceClickListener itemClick
			, DialogInterface.OnClickListener yesClick, boolean cancelable) {

		AlertDialog alert = new AlertDialog.Builder(ctx, dStyle)
				.setTitle(title)
				.setMultiChoiceItems(items, checkedItems, itemClick)
				.setPositiveButton(ctx.getString(R.string.yes), yesClick)
				.setCancelable(cancelable)
				.create();
		if (!((Activity) ctx).isFinishing()) alert.show();
	}

	public static void multipleChoice (Context ctx, String title, ArrayList<String> items, ArrayList<Boolean> checkedItems, DialogInterface.OnMultiChoiceClickListener itemClick
			, DialogInterface.OnClickListener yesClick, boolean cancelable) {

		String[] itms = new String[items.size()];
		boolean[] checkedItms = new boolean[items.size()];
		for (int i = 0; i < items.size(); i++) {
			itms[i] = items.get(i);
			checkedItms[i] = checkedItems.get(i).booleanValue();
		}


		AlertDialog alert = new AlertDialog.Builder(ctx, dStyle)
				.setTitle(title)
				.setMultiChoiceItems(itms, checkedItms, itemClick)
				.setPositiveButton(ctx.getString(R.string.yes), yesClick)
				.setCancelable(cancelable)
				.create();
		if (!((Activity) ctx).isFinishing()) alert.show();
	}

	public static AlertDialog alert(Context ctx, String msg, boolean cancelable) {

		AlertDialog alert = new AlertDialog.Builder(ctx, dStyle)
				.setTitle(com.example.testforble.R.string.app_name)
				.setMessage(msg)
				.setPositiveButton(ctx.getString(R.string.ok), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				})
				.setCancelable(cancelable)
				.create();
		if (!((Activity) ctx).isFinishing()) alert.show();

		return alert;
	}

	public static void alert(Context ctx, int msg, boolean cancelable) {

		AlertDialog alert = new AlertDialog.Builder(ctx, dStyle)

				.setTitle(com.example.testforble.R.string.app_name)
				.setMessage(msg)
				.setPositiveButton(ctx.getString(R.string.ok), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				})
				.setCancelable(cancelable)
				.create();
		alert.show();
	}

	public static AlertDialog alert(Context ctx, int msg, DialogInterface.OnClickListener click, boolean cancelable) {
		AlertDialog alert = new AlertDialog.Builder(ctx, dStyle)

				.setTitle(com.example.testforble.R.string.app_name)
				.setMessage(msg)
				.setPositiveButton(ctx.getString(R.string.ok), click)
				.setCancelable(cancelable)
				.create();
		if (!((Activity) ctx).isFinishing()) alert.show();
		return alert;
	}

	public static void alert(Context ctx, String msg, DialogInterface.OnClickListener click, boolean cancelable) {

		AlertDialog alert = new AlertDialog.Builder(ctx, dStyle)

				.setTitle(com.example.testforble.R.string.app_name)
				.setMessage(msg)
				.setPositiveButton(ctx.getString(R.string.ok), click)
				.setCancelable(cancelable)
				.create();
		if (!((Activity) ctx).isFinishing()) alert.show();
	}

	public static void confirm(Context ctx, String msg, DialogInterface.OnClickListener yesClick, boolean cancelable) {

		AlertDialog alert = new AlertDialog.Builder(ctx, dStyle)

				.setTitle(com.example.testforble.R.string.app_name)
				.setMessage(msg)
				.setPositiveButton(ctx.getString(R.string.yes), yesClick)
				.setNegativeButton(ctx.getString(R.string.no), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				})
				.setCancelable(cancelable)
				.create();
		if (!((Activity) ctx).isFinishing()) alert.show();
	}

	public static void confirm (Context ctx, String msg, DialogInterface.OnClickListener yesClick, DialogInterface.OnClickListener noClick, boolean cancelable) {

		AlertDialog alert = new AlertDialog.Builder(ctx, dStyle)

				.setTitle(com.example.testforble.R.string.app_name)
				.setMessage(msg)
				.setPositiveButton(ctx.getString(R.string.yes), yesClick)
				.setNegativeButton(ctx.getString(R.string.no), noClick)
				.setCancelable(cancelable)
				.create();
		if (!((Activity) ctx).isFinishing()) alert.show();
	}

	public static void confirm (Context ctx, int msg, DialogInterface.OnClickListener yesClick, DialogInterface.OnClickListener noClick, boolean cancelable) {
		AlertDialog alert = new AlertDialog.Builder(ctx, dStyle)

				.setTitle(com.example.testforble.R.string.app_name)
				.setMessage(msg)
				.setPositiveButton(ctx.getString(R.string.yes), yesClick)
				.setNegativeButton(ctx.getString(R.string.no), noClick)
				.setCancelable(cancelable)
				.create();
		if (!((Activity) ctx).isFinishing()) alert.show();
	}

	public static AlertDialog confirm(Context ctx, int msg, DialogInterface.OnClickListener yesClick, boolean cancelable) {
		AlertDialog alert = new AlertDialog.Builder(ctx, dStyle)

				.setTitle(com.example.testforble.R.string.app_name)
				.setMessage(msg)
				.setPositiveButton(ctx.getString(R.string.yes), yesClick)
				.setNegativeButton(ctx.getString(R.string.no), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				})
				.setCancelable(cancelable)
				.create();
		if (!((Activity) ctx).isFinishing()) alert.show();

		return alert;
	}

	public static void confirm (Context ctx, String title, String msg, String msgYes, String msgNo, DialogInterface.OnClickListener yesClick, DialogInterface.OnClickListener noClick, boolean cancelable) {
		AlertDialog alert = new AlertDialog.Builder(ctx, dStyle)

				.setTitle(title)
				.setMessage(msg)
				.setPositiveButton(msgYes, yesClick)
				.setNegativeButton(msgNo, noClick)
				.setCancelable(cancelable)
				.create();
		if (!((Activity) ctx).isFinishing()) alert.show();
	}

	//프로그레스 다이알로그
	public static ProgressDialog progress (Context ctx, int max, String title, String msg, boolean cancelable) {
		ProgressDialog progressDialog = new ProgressDialog(ctx);
		progressDialog.setMax(max);
		progressDialog.setTitle(title);
		progressDialog.setMessage(msg);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setCancelable(cancelable);
		if (!((Activity) ctx).isFinishing()) progressDialog.show();

		return progressDialog;
	}

	//넘버피커 다이알로그
	public static void numberPicker (Context ctx, String title, int minValue, int maxValue, int step, int currentValue, final NumberPickerDialogListener listener, boolean cancelable) {

		RelativeLayout linearLayout = new RelativeLayout(ctx);
		final NumberPicker aNumberPicker = new NumberPicker(ctx);

		//새로운 시도. ===========================================
		int NUMBER_OF_VALUES = Math.round((maxValue - minValue) / step) + 1;
		int PICKER_RANGE = step;

		final String[] displayedValues = new String[NUMBER_OF_VALUES];

		for (int i = 0; i < NUMBER_OF_VALUES; i++) {
			displayedValues[i] = String.valueOf((PICKER_RANGE * (i)) + minValue);
		}

		aNumberPicker.setMinValue(0);
		aNumberPicker.setMaxValue(displayedValues.length - 1);
		aNumberPicker.setDisplayedValues(displayedValues);

		for (int i = 0; i < displayedValues.length; i++) {
			if (displayedValues[i].equals(Integer.toString(currentValue)))
				aNumberPicker.setValue(i);
		}
		//새로운 시도. ===========================================

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(50, 50);
		RelativeLayout.LayoutParams numPicerParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		numPicerParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

		linearLayout.setLayoutParams(params);
		linearLayout.addView(aNumberPicker, numPicerParams);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ctx, dStyle);
		alertDialogBuilder.setTitle(title);
		alertDialogBuilder.setView(linearLayout);


		alertDialogBuilder
				.setPositiveButton("적용",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								/** 이제그만~*///LogUtil.E ("","New Quantity Value : "+ aNumberPicker.getValue());
								listener.yesClick(dialog, id, displayedValues[aNumberPicker.getValue()]);
							}
						})
				.setNegativeButton("취소",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								listener.noClick(dialog, id);
								dialog.cancel();
							}
						});
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.setCancelable(cancelable);
		if (!((Activity) ctx).isFinishing()) alertDialog.show();
	}

	//넘버피커 리스너
	public static interface NumberPickerDialogListener {
		public void yesClick(DialogInterface dialog, int id, String value);
		public void noClick(DialogInterface dialog, int id);
	}




	//유저 인풋
	public static void getUserInput (Context ctx, String title, int msg, final UserInputListener listener, boolean cancelable) {

		AlertDialog.Builder alert = new AlertDialog.Builder(ctx, dStyle)
				.setTitle(title)
				.setMessage(msg);

		// Set an EditText view to get user input
		final EditText input = new EditText(ctx);
		input.setBackgroundResource(com.example.testforble.R.drawable.common_bgbox);
		input.setLines(4);
		input.setPaddingRelative(30, 30, 30, 30);
		input.setTop(30);
		input.setBottom(30);
		input.setLeft(30);
		input.setRight(30);
		alert.setView(input);
		input.setTextColor(Color.BLACK);

		alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = input.getText().toString();

				listener.onYesClick(value);

				dialog.dismiss();
			}
		});


		alert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				dialog.dismiss();
			}
		});
		alert.setCancelable(cancelable);

		if (!((Activity) ctx).isFinishing()) alert.show();
	}


	public interface UserInputListener {
		public void onYesClick(String value);
	}


	public interface OrderNoInputListener {
		public void onYesClick(String orderNo, String boxSerial);
	}
}










