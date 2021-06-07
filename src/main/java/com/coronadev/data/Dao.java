package com.coronadev.data;

import java.util.List;
/**
 * Dao.java
 * This interface defines the data access methods.
 * @author GabrielCorona
 *
 */
public interface Dao {	
	
	// Finds the Status by the provided session ID.
	public Status getStatusBySession(String session);
	
	// Gets a list of session related to an specified user name.
	public List<String> getUserSessions(String username);
	
	// Updates the Status
	public void updateStatus(Status status);
	
	// Generates a new the session and Status object and store it. 
	public Status generateSession(String username);
	
	// Gets the word to be guessed from an specified source
	public String getWord();
}
