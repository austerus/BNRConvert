package com.andrei.bnr;

import android.content.Context;
import android.widget.Toast;

public class BNRErrorHandler {
	private static final int duration = Toast.LENGTH_SHORT;
	private CharSequence message;
	
	public BNRErrorHandler(Context context, CharSequence message) {
		this.message = message;
		Toast toast = Toast.makeText(context, this.message, duration);
		toast.show();
	}
}
