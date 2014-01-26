package no.hig.gsd.quizgame;

import java.util.ArrayList;
import java.util.Map;

import android.content.Context;

public class GameData {
	
	private static final String TAG = "Request";
	private static GameData singleton;
	private Context context;
	private DAO dao;
	
	private String playerid;
	private String playername;
	private String sessionid;
	private String difficulty;
	private String myturn;
	
	private ArrayList<String> player_ids;
	private ArrayList<String> player_names;
	
	private GameData(Context context) {
		this.context = context;
		this.dao = DAO.getInstance(context);
		this.player_ids = new ArrayList<String>();
		this.player_names = new ArrayList<String>();
		loadData();
	}
	
	public static synchronized GameData getInstance(Context context) {
		if (singleton == null) {
			singleton = new GameData(context);
		}
		singleton.context = context;
		return singleton;
	}

	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	@SuppressWarnings("rawtypes")
	public void loadData() {
		// get topics and difficulties
		String topicQuery = dao.doGet("SELECT * FROM topics");
		String diffQuery = dao.doGet("SELECT * FROM difficulty_levels");
		ArrayList<Map> topics = dao.parseJSON(topicQuery);
		ArrayList<Map> difficulties = dao.parseJSON(diffQuery);
		dao.query("DELETE FROM topics");
		dao.query("DELETE FROM difficulty_levels");
		for(int i = 0; i < topics.size(); i++) {
			dao.query("INSERT INTO topics(topic_id,topic_name) VALUES ('"
				+ topics.get(i).get("topic_id").toString() + "','" 
				+ topics.get(i).get("topic_name").toString() +"')" );
		}
		for(int i = 0; i < difficulties.size(); i++) {
			dao.query("INSERT INTO difficulty_levels(difficulty_level_id,difficulty_level_name) VALUES ('"
				+ difficulties.get(i).get("difficulty_level_id").toString() + "','" 
				+ difficulties.get(i).get("difficulty_level_name").toString() +"')" );
		}
	}
	
	public boolean addPlayer(String name) {
		if(playerid  == null && playername == null) {
			String result = dao.doGet("SELECT user_id,user_name FROM users WHERE user_name='" +name+"'");
			@SuppressWarnings("rawtypes")
			ArrayList<Map> list = dao.parseJSON(result);
			for(int i=0; i < list.size(); i++) {
				playerid = list.get(i).get("user_id").toString();
				playername =  list.get(i).get("user_name").toString();
			}
			return true;
		}
		return false;
	}
	
	public void addPlayers(ArrayList<String> invited) {
		String id, name;
		String values = "'" + playername + "',";
		for(int i=0; i < invited.size(); i++) {
			values += "'" + invited.get(i) + "',";
		}
		values = (String) values.subSequence(0, values.length()-1);
		String result = dao.doGet("SELECT user_id,user_name FROM users WHERE user_name IN (" + values + ")");
		@SuppressWarnings("rawtypes")
		ArrayList<Map> list = dao.parseJSON(result);
		for(int i=0; i < list.size(); i++) {
			id = list.get(i).get("user_id").toString();
			name = list.get(i).get("user_name").toString();
			player_ids.add(id);
			player_names.add(name);
			dao.query("INSERT INTO players(player_id,player_name) VALUES ('"
			+ id + "','" 
			+ name +"')");
		}
	}
	
	public String getPlayerId() {
		return playerid;
	};
	public String getPlayerName() {
		return playername;
	};
	public String getSessionId() {
		return sessionid;
	};
	public String getDifficulty() {
		return difficulty;
	}
	public String getMyTurn() {
		return myturn;
	};
	
	public ArrayList<String> getPlayerIdList() {
		return player_ids;
	};
	
	public ArrayList<String> getPlayerNameList() {
		return player_names;
	};
	
	public void setSessionId(String sessionid) {
		this.sessionid = sessionid;
	}
	
	public void setDifficulty(String difficulty) {
		this.difficulty = difficulty;
	}
	
	public void setMyTurn(String turn) {
		this.myturn = turn;
	}

}
