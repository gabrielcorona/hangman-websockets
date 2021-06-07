package com.coronadev.data;

import java.util.ArrayList;
import java.util.List;

/**
 * GameLogic.java
 * This contains the game logic to evaluate player inputs and game outcomes.
 * @author GabrielCorona
 *
 */
public class GameLogic {

	// This method apply the actions to the current status and evaluate new status. 
	// For example depending on the user input this will evaluate the letter provided
	// and depending if it matches with the word it will add the letter to the word process
	// variable and will apply the Game State depending on the output.
	public Status evaluateAction(Action action) {
		Status status = action.getStatus();
		String letter = action.getLetter().toLowerCase();
		
		// If there is an action to evaluate we will evaluate the input, else we will just return the same status.
		if(!action.getLetter().isEmpty()) {
			// If the word guessed is the same as the word we are looking for the game ends in status "WIN" and we reveal the word.
			if(status.getWord().equals(letter)) { 
				status.setWordProgress(letter);
				status.setCurrentStatus(GameState.WIN);
				
				// if the guessed letter is part of the word it will look for the occurrences and it will reveal those letters in the word progress.
			}else if(status.getWord().contains(letter)){
				String word = status.getWord();
				char[] wordGuess = status.getWordProgress().toCharArray();
				int wordIdx = -1;
				while(word.lastIndexOf(letter) != wordIdx) { // Iterate over the occurrences of the character.
					wordIdx = word.indexOf(letter, wordIdx+1);
					wordGuess[wordIdx] = letter.charAt(0);
					
				}
				
				// Update the word progress with the updated letters.
				status.setWordProgress(new String(wordGuess));
				
				// if the word if fully guessed the game ends in "WIN" status.
				if(status.getWordProgress().equals(word)) {
					status.setCurrentStatus(GameState.WIN);
				}
				
				// else means that no occurrences of the letter or word matched the word we are looking for
				// in which case we will need to add damage which will imply one more element added to the hangman.
			}else {
				status.setDamage(status.getDamage()+1);		
				
				// in case the damage gets to the point the hangman is completed the game will end in status "LOSE"
				if(status.getDamage() == 7) {
					status.setCurrentStatus(GameState.LOSE);
				}
			}
			// the letter we used to evaluate the word will be added to the list of used letters.
			List<String> usedLetters = status.getUselessLetters();
			if(usedLetters == null)
				usedLetters = new ArrayList<String>();
			usedLetters.add(letter);
			status.setUselessLetters(usedLetters);
		}
		return status;
	}
}
