package no.hig.gsd.quizgame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.os.StrictMode;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class PickQuestionActivity extends Activity {
	
	private Context context;
	private DAO dao;
	private GameData data;
	private String playerid;
	private int nextplayer;
	private String sessionid;
	private int numplayers;
	private boolean clickable;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pick_question);
		this.context = this;
		this.dao = DAO.getInstance(this);
		this.data = GameData.getInstance(this);
		this.playerid = data.getPlayerId();
		this.numplayers = data.getPlayerIdList().size();
		this.nextplayer = (Integer.parseInt(data.getMyTurn()) + 1) % numplayers;
		this.sessionid = data.getSessionId();
		this.clickable = true;

		Intent intent = getIntent();
		setupUI(intent.getStringExtra("topic"));			
	}
	
	public void setupUI(String topic) {
		ListView listview= (ListView)findViewById(R.id.listview);
		// TODO add limit from setupgame
		final String [][] questions = dao.query("SELECT * FROM questions WHERE is_answered='0'");
        // create the grid item mapping
        String[] from = new String[] {"col_1"};
        int[] to = new int[] { R.id.item1 };
 
        // prepare the list of all records
        HashMap<String, String> map;
        List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();
        for(int i = 0; i < questions.length; i++) {     
        	map = new HashMap<String, String>();
            map.put("col_1", questions[i][2]);
            fillMaps.add(map);
        }
 
        // fill in the grid_item layout
        SimpleAdapter adapter = new SimpleAdapter(this, fillMaps, R.layout.listview_griditem_topic, from, to);
        listview.setAdapter(adapter);
        
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, final View view,
			          int position, long id) {
				if(clickable) {
					clickable = false;
					String chosenid = questions[(int)id][1];
					System.out.println("chosen "+(int)id);
					if(updateGameSession(chosenid)) {
						Intent intent = new Intent(context, WaitForTurnActivity.class);
						startActivity(intent);
						finish();
					}
					clickable = true;
				}
			}
		});
	}
	
	public boolean updateGameSession(String questionid) {
		String result;
		String playerturn, remainingturns;
		
		System.out.println("remove turn "+playerid + " next "+nextplayer);
		
		dao.doPost("UPDATE user_game_session SET my_turn='0'" 
				+" WHERE user_id='"+ playerid +"'"
				+" AND game_session_id='" + sessionid +"'");
		// TODO merge queries
		dao.doPost("UPDATE user_game_session SET picked_question='"+ questionid +"',my_turn='1'"
				+" WHERE user_turn='"+ nextplayer +"'"
				+" AND game_session_id='" + sessionid +"'");

		result = dao.doGet("SELECT * FROM user_game_session INNER JOIN game_session"
				+ " ON user_game_session.game_session_id=game_session.game_session_id"
				+ " WHERE game_session.game_session_id='" + sessionid	+ "'"
				+ " AND user_game_session.user_id='" + playerid + "'");
		
		@SuppressWarnings("rawtypes")
		ArrayList<Map> list = dao.parseJSON(result);
		for (int i = 0; i < list.size(); i++) {
			playerturn = list.get(i).get("user_turn").toString();
			remainingturns = list.get(i).get("rounds_number").toString();
			// update turns if you are the last player in the turn cycle
			if(Integer.parseInt(playerturn) == numplayers-1) {
				int newturn = Integer.parseInt(remainingturns) - 1;
				System.out.println("newturn "+newturn);
				dao.doPost("UPDATE game_session SET rounds_number='" + newturn + "'"
						+ " WHERE game_session_id='" + sessionid + "'");
			}
		}
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.pick_question, menu);
		return true;
	}

}
