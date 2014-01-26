package no.hig.gsd.quizgame;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper {
	
	private static MySQLiteHelper singleton;
	
	private static final String DATABASE_NAME = "freya.db";
	private static final int DATABASE_VERSION = 1;
	
	public static final String TABLE_COLUMN_ID = "_id";
	public static final String [] TABLE_NAMES = {"game","players","questions","answers","topics","difficulty_levels"};
	public static final String [] TABLE_COLUMNS = {"session_id, gamemode, difficulty, turns",
													"player_id, player_name, turn",
													"question_id,question_text,difficulty_level_id,topic_id,is_answered NOT NULL DEFAULT '0'",
													"question_id,answer_text,is_right",
													"topic_id,topic_name",
													"difficulty_level_id,difficulty_level_name"};

	private MySQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public static synchronized MySQLiteHelper getInstance(Context context) {
		if (singleton == null) {
			singleton = new MySQLiteHelper(context);
		}
		return singleton;
	}

	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		for(int i=0; i < TABLE_NAMES.length; i++) {
			database.execSQL("CREATE TABLE IF NOT EXISTS "
				+ TABLE_NAMES[i]+"( " + TABLE_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " 
				+ TABLE_COLUMNS[i] + ");");
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(MySQLiteHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		drop(db);
	}

	public void drop(SQLiteDatabase db) {
		for(int i=0; i < TABLE_NAMES.length; i++) {
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAMES[i]);
		}
		onCreate(db);
	}
}
