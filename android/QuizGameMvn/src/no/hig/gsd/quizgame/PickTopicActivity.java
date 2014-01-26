package no.hig.gsd.quizgame;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class PickTopicActivity extends Activity {
	
	//private List<TaskData> data;
	private Context context;
	private DAO dao;
	private GameData data;
	private String difficulty;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_topic);
		this.context = this;
		this.dao = DAO.getInstance(context);
		this.data = GameData.getInstance(context);
		this.difficulty = data.getDifficulty();
		setupUI();
	}
	
	public void setupUI() {
		ListView listview= (ListView)findViewById(R.id.listview);
        // TODO load from database / player picked from mode
		final String [][] topics = dao.query("SELECT topic_id,topic_name FROM topics");
		
		//final String [] topics = {"Sports","Entertainment","Math"};
		
        // create the grid item mapping
        String[] from = new String[] {"col_1"};
        int[] to = new int[] { R.id.item1 };
 
        // prepare the list of all records
        HashMap<String, String> map;
        List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();
        for(int i = 0; i < topics.length; i++){     
        	map = new HashMap<String, String>();
            //map.put("col_1", topics[i]);
        	map.put("col_1", topics[i][1]);
        	fillMaps.add(map);
        }
 
        // fill in the grid_item layout
        SimpleAdapter adapter = new SimpleAdapter(this, fillMaps, R.layout.listview_griditem_topic, from, to);
        listview.setAdapter(adapter);
        
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, final View view,
			          int position, long id) {
				getQuestions(topics[(int) id][0]);
			}
        	});  
	}
	
	@SuppressWarnings("rawtypes")
	public void getQuestions(String topic) {
		String id,text,difficulty, isright;
		String questions = "";
		ArrayList<Map> list;
		
		String result = dao.doGet("SELECT t0.* FROM questions t0 LEFT JOIN answered_questions t1"
				+ " ON t0.question_id=t1.question_id"
				+ " WHERE t0.difficulty_level_id='" + this.difficulty + "'"
				+ " AND t0.topic_id='" + topic + "'"
				+ " AND t1.question_id IS NULL"
				+ " ORDER BY RAND() LIMIT "+ 4);
		// Get the questions id
		list = dao.parseJSON(result);
		for(int i=0; i < list.size(); i++) {
			id = list.get(i).get("question_id").toString();
			text = list.get(i).get("question_text").toString();
			difficulty = list.get(i).get("difficulty_level_id").toString();
			topic = list.get(i).get("topic_id").toString();
			
			questions += "'"+ id + "',";
			dao.query("INSERT INTO questions(question_id,question_text,difficulty_level_id,topic_id) "
					+ "VALUES (\"" + id + "\",\"" + text + "\",\"" 
					+ difficulty + "\",\"" + topic + "\");");
		}
		questions = questions.substring(0, questions.length()-1);
		System.out.println(questions);
		result = dao.doGet("SELECT * FROM answers WHERE question_id IN ( " + questions + " )");
		
		list = dao.parseJSON(result);
		for(int i = 0; i < list.size(); i++) {
			id = list.get(i).get("question_id").toString();
			text = list.get(i).get("answer_text").toString();
			isright = list.get(i).get("is_right").toString();
			
			dao.query("INSERT INTO answers(question_id,answer_text,is_right) "
					+ "VALUES (\"" + id + "\",\"" + text + "\",\"" + isright + "\");");
		}

		Intent intent = new Intent(context, PickQuestionActivity.class);
		intent.putExtra("topic", topic);
		startActivity(intent);
		finish();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.topic, menu);
		return true;
	}

}
