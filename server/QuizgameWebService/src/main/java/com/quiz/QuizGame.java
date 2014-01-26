package com.quiz;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import static com.quiz.Common.conn;
import static com.quiz.Common.statement;
import static com.quiz.DatabaseConnection.getConnection;
import static com.quiz.Common.title;
import static com.quiz.Common.content;

//The Java class will be hosted at the URI path //"/database"
@Path("/quizgame")
public class QuizGame {
	// The Java method will process HTTP GET requests
	// The Java method will produce content identified by the MIME Media
	// type "text/plain"

	private void CloseConnection() {
		try {
			statement.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path("/getstatistic")
	@POST
	public String getStatistic(@FormParam("user_name") String userName) {
		conn = getConnection();
		String stat = "noting";

		try {
			statement = conn.createStatement();
			
			String query = "select user_name, current_round, correct_answers, wrong_answers" + 
			"from users as u inner join user_game_session as ugs on u.user_id = ugs.user_id where u.user_name = '" + 
			userName + "'";
			
			ResultSet res = statement.executeQuery(query);
			
			while (res.next()) {
				stat = "current_round: " + res.getString("current_round") + 
						", correct_answers: + " + res.getString("correct_answers") +
						", wrong_answers: + " + res.getString("wrong_answers");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			CloseConnection();
		}
		
		return stat;
	}
	
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path("/updatename")
	@POST
	public String updateName(@FormParam("user_name") String userName, @FormParam("new_user_name") String newUserName) {

		conn = getConnection();

		try {
			statement = conn.createStatement();

			String query = "select * from users where user_name = ";

			ResultSet resOldUser = statement.executeQuery(query + "'" + userName + "'");
			ResultSet resNewUser = statement.executeQuery(query + "'" + newUserName + "'");

			int countUsers = 0;
			
			while(resOldUser.next())
				countUsers++;
			
			if (countUsers == 1 && !resNewUser.next())
				if(statement.executeUpdate("update users set user_name = '" + newUserName + "' where user_name = '" + userName + "'") != 0)
					return "success";

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			CloseConnection();
		}

		return "failure";
	}
	
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path("/updatemail")
	@POST
	public String updateEmail(@FormParam("user_name") String userName, @FormParam("new_email") String newEmail) {

		conn = getConnection();

		try {
			statement = conn.createStatement();

			ResultSet resUser = statement.executeQuery("select * from users where user_name = '" + userName + "'");

			int countUsers = 0;
			
			while(resUser.next())
				countUsers++;
			
			if (countUsers == 1)
				if(statement.executeUpdate("update users set email = '" + newEmail + "' where user_name = '" + userName + "'") != 0)
					return "success";

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			CloseConnection();
		}

		return "failure";
	}
	
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path("/updatepassword")
	@POST
	public String updatePassword(@FormParam("user_name") String userName, @FormParam("psw") String psw, @FormParam("new_psw") String newPsw) {

		conn = getConnection();

		try {
			statement = conn.createStatement();

			ResultSet resUser = statement.executeQuery("select password_hash from users where user_name = '" + userName + "'");

			int countUsers = 0;
			String tmpPsw = "";
			
			while(resUser.next()) {
				countUsers++;
				tmpPsw = resUser.getString("password_hash");
			}
			
			if (countUsers == 1 && psw.equals(tmpPsw))
				if(statement.executeUpdate("update users set password_hash = '" + newPsw + "' where user_name = '" + userName + "'") != 0)
					return "success";

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			CloseConnection();
		}

		return "failure";
	}
	
	
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path("/newuser")
	@POST
	// method must have a return value to the httpclient get request
	public String Postinfo(@FormParam("new_name") String username,
			@FormParam("new_psw") String psw,
			@FormParam("new_email") String email) {
		int rs = 0;
		String new_username = username;
		String new_password = psw;
		String new_email = email;
		conn = getConnection();// connecting to the database
		try {
			statement = conn.createStatement();// statement is used for
												// executing SQL commmand
			// check the user name and email have existed already or not, if
			// existed will return the "repeat" value to the client; it not,
			// register the new users
			String sql_check = "select * from users where user_name='"
					+ new_username + "' or email='" + new_email + "'";
			ResultSet result = statement.executeQuery(sql_check);
			if (result.next()) {
				return "repeat";
			} else {
				String sql = "insert into users (user_name,password_hash,email) values('"
						+ new_username
						+ "','"
						+ new_password
						+ "','"
						+ new_email + "')";
				rs = statement.executeUpdate(sql);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				statement.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (rs != 0) {
			return "success";
		} else {
			return "failure";
		}

	}

	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path("login")
	@POST
	// method must have a return value to the httpclient get request
	public String Postinfo2(@FormParam("username") String user_name,
			@FormParam("password") String user_psw) {
		String username = user_name;
		String psword = user_psw;
		ResultSet result = null;
		int rs = 0;
		String p = "";
		conn = getConnection();
		try {
			statement = conn.createStatement();
			// SQL command
			String sql = "select password_hash  from users where user_name='"
					+ username + "'";
			result = statement.executeQuery(sql);

			while (result.next()) {
				System.out.println(result.getString("password_hash"));
				p = result.getString("password_hash");

			}
			if (psword.equals(p)) {

				String sql_update = "update users set IS_ACTIVE=1 where user_name='"
						+ username + "'";
				rs = statement.executeUpdate(sql_update);
			} else {
				return "failure";
			}
			System.out.println(rs);

		} catch (Exception e) {
			System.out.println("failure");
		} finally {

			try {
				result.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				statement.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		if (rs != 0) {
			return "success";

		} else {

			return "failure";
		}

	}

	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path("/logout")
	@POST
	// method must have a return value to the httpclient get request
	public String Postinfo3(@FormParam("username") String user_name) {
		String username = user_name;
		int rs = 0;
		conn = getConnection();
		try {
			statement = conn.createStatement();
			// SQL command
			String sql = "update users set IS_ACTIVE=0  where user_name='"
					+ username + "'";
			rs = statement.executeUpdate(sql);
		} catch (Exception e) {
			System.out.println("failure");
		} finally {

			try {
				statement.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		if (rs != 0) {
			return "success";

		} else {

			return "failure";
		}

	}

	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path("/search")
	@POST
	public String search(@FormParam("username") String user_name) {
		String username = user_name;
		conn = getConnection();
		ResultSet rs = null;
		JSONObject jon = null;
		JSONArray array = new JSONArray();
		try {
			statement = conn.createStatement();
			String sql_search = "select  user_name from users where IS_ACTIVE=1 and user_name <> '"
					+ username + "'";
			rs = statement.executeQuery(sql_search);
			while (rs.next()) {
				jon = new JSONObject();
				String value = rs.getString("user_name");
				try {
					jon.put("username", value);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				array.put(jon);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {

			try {
				rs.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				statement.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return ("allusers:" + array).toString();
	}

	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path("/gcm")
	@POST
	public String gcm_regId(@FormParam("username") String username,
			@FormParam("regId") String regId) {
		String user_name = username;
		String reg_Id = regId;
		int row = 0;
		conn = getConnection();
		try {
			String regId_sql = "select regId from gcm_message where user_name='"
					+ user_name + "'";
			String gcm_sql = "update gcm_message set regId='" + reg_Id
					+ "' where user_name='" + user_name + "'";
			String in_sql = "insert  into gcm_message(regId, user_name) values('"
					+ reg_Id + "','" + user_name + "')";
			statement = conn.createStatement();
			ResultSet rs = statement.executeQuery(regId_sql);
			if (rs.next()) {
				row = statement.executeUpdate(gcm_sql);
			} else
				row = statement.executeUpdate(in_sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				statement.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		if (row != 0) {
			return "success";
		} else {
			return "failure";
		}
	}

	// pushing notification
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path("/notification")
	@POST
	public void post(@FormParam("list") String json) {
		JSONObject jObject = null;
		ResultSet rs = null;
		String name = null;
		try {
			jObject = new JSONObject(json);
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		JSONArray users = null;
		try {
			users = jObject.getJSONArray("users");
			System.out.println(users);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		conn = getConnection();
		try {
			statement = conn.createStatement();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {

			for (int i = 0; i < users.length(); i++) {
				try {
					name = users.getString(i);
					try {

						String sql = "select regId from gcm_message where user_name='"
								+ name + "'";
						rs = statement.executeQuery(sql);
						while (rs.next()) {
							String regId = rs.getString("regId");
							synchronized (regId) {
								Pushnotification.regIds.add(regId);
							}
						}
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		} finally {
			try {
				rs.close();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {

				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				statement.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		Pushnotification.send(title, content);

	}
	
	
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/test")
	@GET
	public String test(){
		
		
		
		return "hello";
	}

}
