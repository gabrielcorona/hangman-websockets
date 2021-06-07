package com.coronadev.data;

import java.util.ArrayList;
import java.util.List;

public class Status {
	private String players;
	private String session;
	private String word;
	private String wordProgress;
	private List<String> uselessLetters;	
	private Integer damage;
	private GameState currentStatus;
	
	public Status() {
		initDefault();
	}
	
	public String getPlayerID() {
		return players;
	}
	public void setPlayerID(String playerID) {
		this.players = playerID;
	}
	public String getSession() {
		return session;
	}
	public void setSession(String session) {
		this.session = session;
	}
	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		this.word = word;
	}
	public String getWordProgress() {
		return wordProgress;
	}
	public void setWordProgress(String wordProgress) {
		this.wordProgress = wordProgress;
	}
	public List<String> getUselessLetters() {
		return uselessLetters;
	}
	public void setUselessLetters(List<String> uselessLetters) {
		this.uselessLetters = uselessLetters;
	}
	public Integer getDamage() {
		return damage;
	}
	public void setDamage(Integer damage) {
		this.damage = damage;
	}
	public GameState getCurrentStatus() {
		return currentStatus;
	}
	public void setCurrentStatus(GameState state) {
		this.currentStatus = state;
	}
	
	public void initDefault() {
		this.players = "";
		this.session = "";
		this.word = "";
		this.wordProgress = "";
		this.uselessLetters = new ArrayList<String>();
		this.damage = 0;
		this.currentStatus = GameState.STAND_BY;
	}
	
	@Override
	public String toString() {
		return 	"playerID:"+this.players+"|"+
				"session:"+this.session+"|"+
				"word:"+this.word+"|"+
				"wordProgress:"+this.wordProgress+"|"+
				"uselessLetters:"+this.uselessLetters.toString()+"|"+
				"damage:"+this.damage+"|"+
				"gameState:"+this.currentStatus;
	}
}
