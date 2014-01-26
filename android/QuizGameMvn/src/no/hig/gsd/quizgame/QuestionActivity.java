package no.hig.gsd.quizgame;

import java.util.ArrayList;
import java.util.Map;

import no.hig.gsd.quizgame.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class QuestionActivity extends Activity {
	private static Context context;
	private int answerid;
	private String question;
	private ArrayList<String> answers;
	private Button [] buttons;
	
	private DAO dao;
	
	protected boolean loading = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_question);
		context = this;
		dao = DAO.getInstance(this);
		answers = new ArrayList<String>();
		buttons = new Button[] {
				(Button) findViewById(R.id.alternative1_button),
				(Button) findViewById(R.id.alternative2_button),
				(Button) findViewById(R.id.alternative3_button),
				(Button) findViewById(R.id.alternative4_button) };

		question = getIntent().getStringExtra("question");	
		
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
		setupUI();
	}
	
	private void setupUI() {	
		String questionText;
		
		OnClickListener buttonListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				handleChosenQuestion(v);
			}
		};
		// get the question text and answers
		questionText = getQuestion(question);
		TextView view = (TextView) findViewById(R.id.question_text);
		view.setText(questionText);
		for(int i = 0; i < answers.size(); i++) {
			buttons[i].setOnClickListener(buttonListener);
			buttons[i].setText(answers.get(i));
		}
	}
	
	private String getQuestion(String id) {
		String [][] questionQuery;
		String [][] answersQuery;
		String questionText;
		String answerText;
		int correctAnswer;

		questionQuery = dao.query("SELECT * FROM questions WHERE question_id='" + id + "'");	
		answersQuery = dao.query("SELECT * FROM answers WHERE question_id='" + id + "'" );
		
		questionText = questionQuery[0][2];
		
		for(int i=0; i < answersQuery.length; i++) {
			answerText = answersQuery[i][2];
			correctAnswer = Integer.parseInt(answersQuery[i][3]);
			if(correctAnswer == 1) answerid = i; 
			answers.add(answerText);
		}
		// remove redundant question
		dao.query("UPDATE questions SET is_answered='1' WHERE question_id='" + id + "'");
		return questionText;
	}
	
	private void handleChosenQuestion(View v) {
		if (v.getId() == R.id.alternative1_button) {
			checkAnswer(answerid,0);
		}
		if (v.getId() == R.id.alternative2_button) {
			checkAnswer(answerid,1);
		}
		if (v.getId() == R.id.alternative3_button) {
			checkAnswer(answerid,2);
		}
		if (v.getId() == R.id.alternative4_button) {
			checkAnswer(answerid,3);
		}
	}
	
	public void checkAnswer(int answer, int choice) {
		if(answer == choice) {
			Toast.makeText(context, "Correct", Toast.LENGTH_SHORT).show();
			updateScore(1);
		}
		else {
			Toast.makeText(context, "Wrong", Toast.LENGTH_SHORT).show();
			updateScore(0);
		}
	}
	//TODO update score and start next intent
	@SuppressWarnings("rawtypes")
	private void updateScore(int isRight) {
		//update dababase / score
		/**/
		String [][] sessionQuery =  dao.query("SELECT session_id FROM game");
		String sessionid = sessionQuery[0][0];
		String turnQuery = dao.doGet("SELECT rounds_number FROM game_session WHERE game_session_id='" + sessionid + "'");
		ArrayList<Map> list = dao.parseJSON(turnQuery);
		int turns = Integer.parseInt(list.get(0).get("rounds_number").toString());
		System.out.println("turns :: " +turns);
		System.out.println("answerdQ :: " +question + " " + isRight);
		/**/
		// TODO add user score in user session and used_help in answerd questions
		String answerdQuestion = dao.doPost("INSERT INTO answered_questions(game_session_id,user_id,question_id,is_right) VALUES ('"
		+ sessionid + "','"
		+ "12" + "','"
		+ question + "','"
		+ isRight + "');"); // TODO return false check for Internet connection.
		System.out.println("answerd "+answerdQuestion);
		if(turns > 0) {
			// pick new topic and finish current activity
			Intent intent = new Intent(context,PickTopicActivity.class);
			startActivity(intent);
			finish();
		}
		else {
			// Show result screen
			Intent intent = new Intent(context,ResultActivity.class);
			startActivity(intent);
			finish();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.question, menu);
		return true;
	}
}
