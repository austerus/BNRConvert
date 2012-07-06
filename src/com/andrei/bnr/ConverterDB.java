package com.andrei.bnr;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ConverterDB extends android.database.sqlite.SQLiteOpenHelper {
	static final String dbName = "CurrencyDB";
	
	static final String tblCurrencyList = "CurrencyDetails";
	static final String colCLId = "CurrencyId";
	static final String colCLShort = "CurrencyShortName";
	static final String colCLLong = "CurrencyLongName";
	static final String colCLMultiplier = "CurrencyMultiplier";
	
	static final String tblCurrencyEx = "CurrencyExchange";
	static final String colExId = "ExchangeId";
	static final String colExCurrencyId = "ExchangeCurrencyId";
	static final String colExValue = "ExchangeValue";
	static final String colExDate = "ExchangeDate";
	
	static final int version = 1;
	
	static Context cContext;

	public ConverterDB(Context context) {
		
		super(context, dbName, null,version); 
		cContext = context;
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL("CREATE TABLE "+tblCurrencyList+" ("+colCLId+ " INTEGER PRIMARY KEY AUTOINCREMENT, "+colCLShort+" TEXT, "+colCLLong+" TEXT, "+colCLMultiplier+" INTEGER);");
		
		db.execSQL("CREATE TABLE "+tblCurrencyEx+" ("+colExId+ " INTEGER PRIMARY KEY AUTOINCREMENT , "+colExCurrencyId+" INTEGER NOT NULL, "+colExValue+" REAL NOT NULL, "+colExDate+" TEXT, FOREIGN KEY("+colExCurrencyId+") REFERENCES "+tblCurrencyList+"("+colCLId+") );");
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}
	
	public void addCurrency(String myshort, String mylong, int multi) {
		SQLiteDatabase db=this.getWritableDatabase();
		 ContentValues cv=new ContentValues();
		   cv.put(colCLShort,myshort);
		   cv.put(colCLLong,mylong);
		   cv.put(colCLMultiplier,multi);
		   db.insert(tblCurrencyList, colCLId, cv);
		   db.close();
	}
	
	public void fetchCurrency(String currency) {
		SQLiteDatabase db=this.getReadableDatabase();
		
		Cursor c=db.query(tblCurrencyList, new String[]{colCLId+" as _id",colCLShort,colCLLong},
				colCLShort+"=?", new String[]{currency}, null, null, null);
		c.moveToFirst();
		new BNRErrorHandler(cContext, c.getString(c.getColumnIndex(colCLLong))); 
	}

}
