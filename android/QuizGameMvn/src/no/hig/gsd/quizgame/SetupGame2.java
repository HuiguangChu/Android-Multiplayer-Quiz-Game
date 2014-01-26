package no.hig.gsd.quizgame;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.StrictMode;

public class SetupGame2 {
	private int qlimit;
	private int turns;
	private String difficulty;
	private String gamemode;
	private Context context;
	
	private ArrayList<String> player_ids;
	private ArrayList<String> player_names;
	private DAO dao;
	private GameData data;
	
	private String playerid;
	private String playername;
	private String sessionid;
	private boolean clickable;
	
	private final int WAIT_TIME = 3000;
	
	private WaitForPlayers waitPlayers;
	private WaitForInvites waitInvites;
	
	public SetupGame2(Context context) {
		this.context = context;
		this.clickable = true;
		this.dao = DAO.getInstance(context);
		this.player_ids = new ArrayList<String>();
		this.player_names = new ArrayList<String>();
		
		this.data = GameData.getInstance(context);
		this.playerid = data.getPlayerId();
		this.playername = data.getPlayerName();

		listenForInvites();
	}
	/** What happens when you start... 
	 * getPlayers()
	 * startNewSession()
	 * sendInvitations()
	 * startSession()
	 * waitForInvitedPlayers()
	 * createNewGame()
	 */
	public void start(final String gamemode, final String difficulty, final int turns, final int qlimit, final ArrayList<String> invited) {
		if(clickable) {
			clickable = false;
			this.turns = turns;
			//this.difficulty = difficulty; 	// TODO
			this.difficulty = "1";
			data.setDifficulty("1");
			this.gamemode = gamemode;
			this.qlimit = qlimit;
			
			data.addPlayers(invited);
			this.player_ids = data.getPlayerIdList();
			this.player_names = data.getPlayerNameList();
			startNewSession();
			sendInvitations();
			startSession();
		}
	}
	
	//TODO end() remove session and user sessions...
	
	public void startNewSession() {
		sessionid = UUID.randomUUID().toString(); // TODO change back to int id
		data.setSessionId(sessionid);
		System.out.println("Creating game...");
		dao.doPost("INSERT INTO game_session(game_session_id,rounds_number) VALUES ('"
				+ sessionid + "','" + turns + "')");
		// register all players in the game session and pick a random starter 
		String values = "";
		String myturn;
		Random gen = new Random();
		int firstturn = gen.nextInt(player_ids.size());
		for(int i=0; i < player_ids.size(); i++) {
			if(i == firstturn) myturn = "1";
			else myturn = "0";
			values += "('" + sessionid + "','" + player_ids.get(i) + "','" + myturn + "','" + i + "'),";
			if(player_ids.get(i).contentEquals(playerid)) data.setMyTurn(""+i);
		}
		values = (String) values.subSequence(0, values.length()-1);
		dao.doPost("INSERT INTO user_game_session(game_session_id,user_id,my_turn,user_turn) VALUES " + values);
	}
	
	public void sendInvitations() {
		String values = "";
		System.out.println("Inviting players...");
		
		for(int i=1; i < player_ids.size(); i++) {
			values += "('" + sessionid + "','" + playerid + "','" + player_ids.get(i) + "'),";
			System.out.println("Inviting player " + player_ids.get(i) );
		}
		values = (String) values.subSequence(0, values.length()-1);
		dao.doPost("INSERT INTO invitations(game_session_id,from_user_id,to_user_id) VALUES " + values);
	}
	
	public void startSession() {
		System.out.println("Waiting for players..");
		
		String result = dao.doGet("SELECT user_turn FROM user_game_session WHERE user_id='"+ this.playerid +"'");
		@SuppressWarnings("rawtypes")
		ArrayList<Map> list = dao.parseJSON(result);
		// update the users turn
		dao.query("UPDATE players SET turn='"+ list.get(0).get("user_turn").toString()
				+"' WHERE player_id='"+ this.playerid + "'");
		
		// update local database
		dao.query("INSERT INTO game(session_id) VALUES (\"" + sessionid + "\");");
		
		dao.doPost("UPDATE user_game_session SET is_in_game=1 WHERE user_id='"+ this.playerid +"'");
		
		waitForInvitedPlayers();
	}
	
	@SuppressWarnings("rawtypes")
	public boolean createNewGame(final boolean first) {
		// Pre load get questions and answers and insert them into the local database for 
		// faster and simpler queries.
		String text, difficulty, topic, isright;
		String id = "";
		ArrayList<Map> list;
		
		String result = dao.doGet("SELECT * FROM questions WHERE difficulty_level_id='" + this.difficulty + "'"
				//+ " AND topic_id='" + topicid + "'"
				+ " ORDER BY RAND() LIMIT "+ 1);		
		System.out.println("getting question");
		// Get the questions id
		list = dao.parseJSON(result);
		for(int i=0; i < list.size(); i++) {
			id = list.get(i).get("question_id").toString();
			text = list.get(i).get("question_text").toString();
			difficulty = list.get(i).get("difficulty_level_id").toString();
			topic = list.get(i).get("topic_id").toString();

			dao.query("INSERT INTO questions(question_id,question_text,difficulty_level_id,topic_id) "
					+ "VALUES (\"" + id + "\",\"" + text + "\",\"" 
					+ difficulty + "\",\"" + topic + "\");");
		}
		result = dao.doGet("SELECT * FROM answers WHERE question_id='"+ id + "'");
		list = dao.parseJSON(result);
		for(int i = 0; i < list.size(); i++) {
			id = list.get(i).get("question_id").toString();
			text = list.get(i).get("answer_text").toString();
			isright = list.get(i).get("is_right").toString();
			
			dao.query("INSERT INTO answers(question_id,answer_text,is_right) "
					+ "VALUES (\"" + id + "\",\"" + text + "\",\"" + isright + "\");");
		}
		if(first) {
			String [][] firstquestion = dao.query("SELECT question_id FROM questions LIMIT 1");	
			Intent intent = new Intent(context, QuestionActivity.class);
			intent.putExtra("question", firstquestion[0][0]);
			context.startActivity(intent);
		}
		else {	 
			Intent intent = new Intent(context, WaitForTurnActivity.class);
			context.startActivity(intent);
		}
		return true;
	}

	public void waitForInvitedPlayers() {	
		waitInvites.cancel(true);
		waitPlayers = new WaitForPlayers();
		waitPlayers.execute();
	}

	public void listenForInvites() {
		waitInvites = new WaitForInvites();
		waitInvites.execute();
	}
	
	public void onResume() {
		System.out.println("wait: " +waitPlayers);
		if(waitPlayers != null) {
			System.out.println(waitPlayers.getStatus());
			System.out.println(waitPlayers.isCancelled());
		}
		//waitForInvitedPlayers();
		// TODO if the dialog freeze stop on pause and start it on resume
	}

	public void onPause() {
		
	}

	public void onBackPressed() {
	    removeListeners();
	    
	}
	
	public void removeListeners() {
		if(waitPlayers != null) waitPlayers.cancel(true);
		if(waitInvites != null) waitInvites.cancel(true);
	}
	
	// TODO create Callback interface with a AsyncTask? close when onPause and resume onResume
	private class WaitForPlayers extends AsyncTask<Void, String, String> {
		private ProgressDialog pd;
		private boolean running;
		@Override
		protected void onPreExecute() {
			System.out.println("START WAITING FOR PLAYERS!");
			pd = ProgressDialog.show(context,
					"Loading...", "Waiting for players.",
					false, true,
					new DialogInterface.OnCancelListener(){
                @Override
                public void onCancel(DialogInterface dialog) {
                    // cancel current start listening for invitations again
                	cancel(true);
                }
            });
			running = true;
		}
		
		@Override
	    protected void onCancelled() {
			System.out.println("Cancel");
			running = false;
			pd.dismiss();
			listenForInvites();
	    }
		
		@Override
		protected String doInBackground(Void... params) {
			int i, ingame, isactive, userturn;
			String result, firstplayer, userid;
			boolean active = true;
			boolean found = false;
			firstplayer = null;
			while (!found && running) {
				if (isCancelled()) break;
				result = dao.doGet("SELECT * FROM user_game_session"
						+ " INNER JOIN users ON user_game_session.user_id=users.user_id"
						+ " WHERE IS_ACTIVE = 1"
						+ "	AND game_session_id ='"+ sessionid +"'");
				if(result != null) {
					@SuppressWarnings("rawtypes")
					ArrayList<Map> list = dao.parseJSON(result);
					i = 0;
					active = true;
					while(i < list.size() && active) {
						ingame = Integer.parseInt(list.get(i).get("is_in_game").toString());
						isactive = Integer.parseInt(list.get(i).get("IS_ACTIVE").toString());
						userturn = Integer.parseInt(list.get(i).get("my_turn").toString());
						userid = list.get(i).get("user_id").toString();
						if(ingame == 0 || isactive == 0) active = false;
						if(userturn == 1) firstplayer = userid;
						i++;
					}
					// everybody is active start the game
					if(active) {
						found = true;
						running = false;
					} else {
						try {
							Thread.sleep(WAIT_TIME);		
						} catch (InterruptedException e) {
							cancel(true);
							System.out.println("Cant sleep WaitPlayers thread!");
							//e.printStackTrace();
						}
					}
				}
				// If no connection dismiss dialog and show a new dialog
				else {
					//TODO
				}
			}
			return firstplayer;
		}

		@Override
		protected void onPostExecute(String result) {
			System.out.println("DONE WAITING FOR PLAYERS!");
			pd.dismiss();
			if(playerid.contentEquals(result)) createNewGame(true);
			else createNewGame(false);
		}
	}
	
	private class WaitForInvites extends AsyncTask<Void, String, Boolean> {
		private AlertDialog alert; 
		private boolean running;
		
		@Override
		protected void onPreExecute() {
			System.out.println("START WAITING INVITATION");

			AlertDialog.Builder builder = new AlertDialog.Builder(context);
	        builder.setTitle("Invite")
	        .setMessage("A player invites you to a game.")
	        .setCancelable(false)
	        .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
	                System.out.println("Invite accepted!");
	                startSession();
				}
			})
	        .setNegativeButton("Decline",new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int id) {
	            	System.out.println("Invite declined!");
	                listenForInvites();		
	            }
	        });
	        alert = builder.create();   
	        running = true;
		}
		
		@Override
	    protected void onCancelled() {
			System.out.println("Cancel invites");
	        running = false;
	    }

		@Override
		protected Boolean doInBackground(Void... params) {
			int i;
			String userid = "";
			String sender = "";
			String invid = "";
			String session = "";
			String result;
			boolean found = false;
			while (!found && running) {
				if (isCancelled()) break;
				result = dao.doGet("SELECT * FROM invitations");
				if(result != null && playerid != null) {
					@SuppressWarnings("rawtypes")
					ArrayList<Map> list = dao.parseJSON(result);
					i = 0;
					while(i < list.size() && !found) {
						invid = list.get(i).get("invitation_id").toString();
						userid = list.get(i).get("to_user_id").toString();
						sender = list.get(i).get("from_user_id").toString();
						session = list.get(i).get("game_session_id").toString();
						if(playerid.contentEquals(userid)) found = true;
						i++;
					}
					if(found) {				
						running = false;
						// Add the session id if you did not create the game
						sessionid = session;
						data.setSessionId(sessionid);
						result = dao.doGet("SELECT user_name FROM users WHERE user_id='" + sender + "'");
						list = dao.parseJSON(result);
						String sendername = list.get(0).get("user_name").toString();
						result = dao.doPost("DELETE FROM invitations WHERE invitation_id='" + invid +"'");	
						alert.setMessage(sendername +" invites you to a game.");	
					} else {
						try {
							Thread.sleep(WAIT_TIME);	
						} catch (InterruptedException e) {
							cancel(true);
							System.out.println("Cant sleep Invitation thread!");
						}
					}
				}
			}
			return found;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if(result) alert.show();
		}
	}

}
