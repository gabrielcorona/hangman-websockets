package com.coronadev.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public class Dao {
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	private HashMap<String,Status> sessions = null;
	private static HashMap<String,List<String>> userSessions = null;
	
	
	public Dao() {
		sessions = new HashMap<String, Status>();
		userSessions = new HashMap<String,List<String>>();
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(
					"c:/temp/hangmanDB.txt"));
			String line = reader.readLine();
			while (line != null) {
				Status status = parseStatus(line);
				sessions.put(status.getSession(),status);
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private Status parseStatus(String line) {
		Status status = new Status();
		String[] properties = line.split("\\|");
		for(String prop:properties) {
			String[] data = prop.split(":");
			switch(data[0]) {
			case "playerID":status.setPlayerID(data[1]);break;
			case "session":status.setSession(data[1]);break;
			case "word":status.setWord(data[1]);break;
			case "wordProgress":status.setWordProgress(data[1]);break;
			case "uselessLetters":status.setUselessLetters(toStringList(data[1]));break;
			case "damage":
				String damage = data[1].trim();
				if(damage.isEmpty() || damage.equals(" "))
					damage = "0";
				status.setDamage(Integer.parseInt(damage));
			break;
			case "gameState":status.setCurrentStatus(GameState.valueOf(data[1]));break;
			}
		}
		
		updateUserSessions(status);
		return status;
	}
	
	private void updateUserSessions(Status status) {
		List<String> currentSessions = null;
		if(userSessions.containsKey(status.getPlayerID())) {
			currentSessions = userSessions.get(status.getPlayerID());
		}else {
			currentSessions = new ArrayList<String>();
		}
		currentSessions.add(status.getSession());
		userSessions.put(status.getPlayerID(), currentSessions);		
	}

	private List<String> toStringList(String line){
		List<String> list = Arrays.asList(line.split(","));
		return list;
	}

	public void updateStatus(Status status) {
		if(status.getPlayerID() != null && !status.getPlayerID().isEmpty()) {
			sessions.put(status.getSession(),status);
			saveSessionData();
		}
	}
	
	private void saveSessionData() {
		File file = null;
		BufferedWriter bw = null;
		FileWriter fw = null;
		try {
			file = new File("c:/temp/hangmanDB.txt");
			fw = new FileWriter(file);
			bw = new BufferedWriter(fw);
			for(String session: sessions.keySet()) {
				bw.write(sessions.get(session).toString()+"\n");			}
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (bw != null) {
					bw.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}	
	}

	public Status generateSession(String username) {
		
		Status status = new Status();
		status.setPlayerID(username);
		status.setSession(username+System.currentTimeMillis());
		status.setWord(getWord());
		status.setWordProgress(setEmptyProgress(status.getWord().length()));
		status.setUselessLetters(new ArrayList<String>());
		status.setDamage(1);
		status.setCurrentStatus(GameState.START);
		updateStatus(status);
		updateUserSessions(status);
//		String sql = "INSERT INTO session (session, player, word, word-progress, used-letters, damage, game-state) VALUES (?, ?, ?, ?, ?, ?, ?)";
//		int res = jdbcTemplate.update(sql,
//										status.getSession(),
//										status.getPlayerID(),
//										status.getWord(),
//										status.getWordProgress(),
//										status.getUselessLetters(),
//										status.getDamage(),
//										status.getCurrentStatus()
//										);
//		if(res > 0) {
//			
//		}
		return status;
	}

	public Status getStatusBySession(String session) {
		return sessions.get(session);
	}
	
	private String setEmptyProgress(int length) {
		String wordProgress = "";
		for(int n=0;n<length;n++) {
			wordProgress += "_";
		}
		return wordProgress;
	}

	private String getWord() {
		String response = "";
		try {
			URL obj = new URL("https://random-word-api.herokuapp.com/word?number=1");
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setDoInput(true); 
			 
			con.setRequestMethod("GET"); 
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String output;
			StringBuffer buffer = new StringBuffer();

			while ((output = in.readLine()) != null) {
				buffer.append(output);
			}
			in.close();
			response = buffer.toString();
			response = response.substring(response.indexOf("\"")+1,response.lastIndexOf("\""));
			
			con.disconnect();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
	}
	
	public HashMap<String, List<String>> getUserSessions() {
		return userSessions;
	}

}
