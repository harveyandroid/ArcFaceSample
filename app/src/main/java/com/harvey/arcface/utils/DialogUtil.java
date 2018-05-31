package com.harvey.arcface.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.WindowManager;

/**
 * Created by hanhui on 2016/6/27 0027 13:45
 */
public class DialogUtil {

	public static void showDialog(final Context context, String title, String content,
			DialogInterface.OnClickListener dialogInterface) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context.getApplicationContext()).setTitle(title)
				.setMessage(content).setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).setPositiveButton("确定", dialogInterface);
		final AlertDialog dialog = builder.create();
		dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		dialog.show();
	}

	public static void showDialog(final Context context, String title, String content,
			DialogInterface.OnClickListener positiveInterface, DialogInterface.OnClickListener negativeInterface) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context.getApplicationContext()).setTitle(title)
				.setMessage(content).setNegativeButton("取消", negativeInterface)
				.setPositiveButton("确定", positiveInterface);
		final AlertDialog dialog = builder.create();
		dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		dialog.show();
	}
}
