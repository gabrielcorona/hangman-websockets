package com.coronadev.websocket;

import java.util.ArrayList;
import java.util.List;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.coronadev.data.Action;
import com.coronadev.data.Dao;
import com.coronadev.data.GameSession;
import com.coronadev.data.GameState;
import com.coronadev.data.Status;

@Controller
public class HangmanController {
	private Dao dao = new Dao();
	
	// This allows the status updates as well as subscriptions.
	@MessageMapping("/setAction")
	@SendTo("/hangman/updates")
	public Status setAction(Action action) {
		Status status = action.getStatus();
		if(action.getLetter().equals("[LOADING]")) {
			status = dao.getStatusBySession(status.getSession());
		}else {
			status = applyAction(action);		
			dao.updateStatus(status);
		}
		return status;
	}

	// This allows the user to subscribe to the new sessions
	@MessageMapping("/setGame")	
	@SendTo("/hangman/sessions")
	public GameSession setGame(Status status) {
		if(status.getSession().equals("CREATE")) {
			status = dao.generateSession(status.getPlayerID());
		}
		
		GameSession gameSession = new GameSession();
		gameSession.setUsername(status.getPlayerID());
		gameSession.setCurrentSessions(dao.getUserSessions().get(status.getPlayerID()));
		return gameSession;
	}
	
	
	// This method apply the actions to the current status and evaluate new status. 
	// For example depending on the user input this will evaluate the letter provided
	// and depending if it matches with the word it will add the letter to the word process
	// variable and will apply the Game State depending on the output.
	private Status applyAction(Action action) {
		Status status = action.getStatus();
		String letter = action.getLetter().toLowerCase();
		
		if(!action.getLetter().isEmpty())
			if(status.getWord().equals(letter)) {
				status.setWordProgress(letter);
				status.setCurrentStatus(GameState.WIN);
			}else if(status.getWord().contains(letter)){
				String word = status.getWord();
				char[] wordGuess = status.getWordProgress().toCharArray();
				int wordIdx = -1;
				while(word.lastIndexOf(letter) != wordIdx) {
					wordIdx = word.indexOf(letter, wordIdx+1);
					wordGuess[wordIdx] = letter.charAt(0);
					
				}
				status.setWordProgress(getWordProgress(wordGuess));
				if(status.getWordProgress().equals(word)) {
					status.setCurrentStatus(GameState.WIN);
				}
			}else {
				status.setDamage(status.getDamage()+1);
				List<String> usedLetters = status.getUselessLetters();
				if(usedLetters == null)
					usedLetters = new ArrayList<String>();
				usedLetters.add(letter);
				status.setUselessLetters(usedLetters);
				if(status.getDamage() == 7) {
					status.setCurrentStatus(GameState.LOSE);
				}
			}
		return status;
	}
	
	private String getWordProgress(char[] wordGuess) {
		String wordProgress ="";
		for(char c:wordGuess) {
			wordProgress += c+"";
		}
		return wordProgress;
	}
}
