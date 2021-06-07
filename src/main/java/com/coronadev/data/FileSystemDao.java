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

/**
 * FileSystemDao.java
 * Implements the Dao interface and provides the required methods to store 
 * and process the data required by the application.
 * @author GabrielCorona
 *
 */
public class FileSystemDao implements Dao {
	
	private HashMap<String,Status> sessions = null;
	private static HashMap<String,List<String>> userSessions = null;
		
	public FileSystemDao() {
		initDao();
	}
	
	// restore the data that was previously stored and set Status elements in the sessions map.
	private void initDao(){
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
	
	// Given a raw string line it separate the properties and values recreating the Status.
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
			case "uselessLetters":status.setUselessLetters(Arrays.asList(data[1].split(",")));break;
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
	
	
	// Given the Status it will update the sessions list by user.
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
	
	// Save the session data contained in the sessions map to the data source.
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
	
	// Generates an empty word filled the spaces representing each missing letter.
	private String setEmptyProgress(int length) {
		String wordProgress = "";
		for(int n=0;n<length;n++) {
			wordProgress += "_";
		}
		return wordProgress;
	}
	
	// Generates a new the session and Status object and store it. 
	@Override
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
		return status;
	}

	// Gets the word to be guessed from an specified source
	// in this case it's requesting a random word to an external API.
	@Override
	public String getWord() {
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
	
	// Finds the Status by the provided session ID.
	@Override
	public Status getStatusBySession(String session) {
		return sessions.get(session);
	}
	
	// Gets a list of session related to an specified user name.
	@Override
	public List<String> getUserSessions(String username) {
		return userSessions.get(username);
	}
	
	// Updates the Status
	@Override
	public void updateStatus(Status status) {
		if(status.getPlayerID() != null && !status.getPlayerID().isEmpty()) {
			sessions.put(status.getSession(),status);
			saveSessionData();
		}
	}

}
