package com.coronadev.data;

import java.util.List;

/*	This class is used to provide the user available sessions.
 * */
public class GameSession {
	private String username;
	private List<String> currentSessions;
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public List<String> getCurrentSessions() {
		return currentSessions;
	}
	public void setCurrentSessions(List<String> currentSessions) {
		this.currentSessions = currentSessions;
	}
	
}
