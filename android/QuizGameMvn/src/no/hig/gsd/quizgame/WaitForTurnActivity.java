package no.hig.gsd.quizgame;

import java.util.ArrayList;
import java.util.Map;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.widget.TextView;

public class WaitForTurnActivity extends Activity {
	
	private Context context;
	private DAO dao;
	private String sessionid;
	private String playerid;
	private int numplayers;
	private final String ENDED = "-1";
	private GameData data;
	
	private ArrayList<String> players;
	
	private final int WAIT_TIME = 3000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wait_for_turn);
		dao = DAO.getInstance(this);
		context = this;
		players = new ArrayList<String>();
		data = GameData.getInstance(this);
		
		playerid = data.getPlayerId();
		sessionid = data.getSessionId();
		players = data.getPlayerIdList();
		numplayers = players.size();
		//loadGameInfo();
		setupUI();
		listenForTurn();
	}
	
	public void setupUI() {
		//TODO make pretty
	}
	// update the UI with game status
	public void updateUI(final String remainingturns, final String currentplayer) {
		runOnUiThread(new Runnable() {
	        public void run() {
	        	TextView tv = (TextView) findViewById(R.id.stats);
				tv.setText("Turns ramaining: " + remainingturns + " Players turn: " + currentplayer);
	        }
	    }); 
	}
	
	public void listenForTurn() {
		new WaitTurn().execute();
	}
	
	public boolean getQuestion(final String questionid) {
		String [][] check = dao.query("SELECT question_id FROM questions WHERE question_id='"+ questionid +"'");
		// if you do not have the question in the local db get it
		if(!check[0][0].contentEquals(questionid)) {
			String questions = dao.doGet("SELECT * FROM questions WHERE question_id='"+ questionid +"'");
			String answers = dao.doGet("SELECT * FROM answers WHERE question_id='"+ questionid +"'");
			if(questions == null || answers == null) return false;
			ArrayList<Map> questionlist = dao.parseJSON(questions);
			ArrayList<Map> answerlist = dao.parseJSON(answers);
			
			String qid, qtext, difficulty, topic;
			for(int i=0; i < questionlist.size(); i++) {
				qid = questionlist.get(i).get("question_id").toString();
				qtext = questionlist.get(i).get("question_text").toString();
				difficulty = questionlist.get(i).get("difficulty_level_id").toString();
				topic = questionlist.get(i).get("topic_id").toString();
	
				dao.query("INSERT INTO questions(question_id,question_text,difficulty_level_id,topic_id) "
						+ "VALUES (\"" + qid + "\",\"" + qtext + "\",\"" 
						+ difficulty + "\",\"" + topic + "\");");
			}
			
			String aid, atext, isright;
			for(int i = 0; i < answerlist.size(); i++) {
				aid = answerlist.get(i).get("question_id").toString();
				atext = answerlist.get(i).get("answer_text").toString();
				isright = answerlist.get(i).get("is_right").toString();
				
				dao.query("INSERT INTO answers(question_id,answer_text,is_right) "
						+ "VALUES (\"" + aid + "\",\"" + atext + "\",\"" 
						+ isright + "\");");
			}
			System.out.println("q " + questionid +" " + questions);
		}
		Intent intent = new Intent(context, QuestionActivity.class);
		intent.putExtra("question", questionid);
		startActivity(intent);
		finish();
		return true;
	}
	
	private class WaitTurn extends AsyncTask<Void, String, String> {
		@Override
		protected void onPreExecute() {
			System.out.println("START WAITING TURN");
		}
		
		@SuppressWarnings("rawtypes")
		@Override
		protected String doInBackground(Void... params) {
			System.out.println(sessionid + " " + playerid);
			boolean found = false;
			String result;
			String pickedQuestion = "";
			String remainingturns = "";
			String currentplayer = "";
			int playerturn = 0;
			ArrayList<Map> list;
			synchronized (this) {
				while (!found) {
					result = dao.doGet("SELECT * FROM user_game_session INNER JOIN game_session"
									+ " ON user_game_session.game_session_id=game_session.game_session_id"
									+ " WHERE game_session.game_session_id='" + sessionid	+ "'"
									+ " AND user_game_session.user_id='" + playerid + "'");
					if(result != null) {
						list = dao.parseJSON(result);
						for (int i = 0; i < list.size(); i++) {
							playerturn = Integer.parseInt(list.get(i).get("my_turn").toString());
							remainingturns = list.get(i).get("rounds_number").toString();
							pickedQuestion = list.get(i).get("picked_question").toString();
						}
						result = dao.doGet("SELECT user_name FROM user_game_session INNER JOIN users"
								+ " ON user_game_session.user_id=users.user_id"
								+ " WHERE user_game_session.my_turn='1'"
								+ " AND user_game_session.game_session_id='" + sessionid + "'");
						if(result != null) {
							list = dao.parseJSON(result);
							for (int i = 0; i < list.size(); i++) {
								currentplayer = list.get(i).get("user_name").toString();
							}
							// Update the statistics
							updateUI(remainingturns, currentplayer); // TODO show online status
						}
					}
					if(Integer.parseInt(remainingturns) == 0) {
						pickedQuestion = ENDED;
						found = true;
					}
					if (playerturn == 1) {
						found = true;
					} else {
						try {
							Thread.sleep(WAIT_TIME);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
			return pickedQuestion;
		}

		@Override
		protected void onPostExecute(String result) {
			// Game has ended show results
			if(result.contentEquals(ENDED)) {
				Intent intent = new Intent(context, ResultActivity.class);
				startActivity(intent);
				finish();
			}
			else {
				// TODO give player an alert dialog to tell him it is his turn?
				if(!getQuestion(result)) listenForTurn();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.wait_for_turn, menu);
		return true;
	}
	
	@Override
	protected void onResume() {
	    super.onResume();
	    // TODO start listeners in setupgame if showing UI stuff
	}
	
	@Override
	protected void onPause() {
	    super.onPause();
	    // TODO stop dialog/listeners in setupgame if showing UI stuff
	}

}
