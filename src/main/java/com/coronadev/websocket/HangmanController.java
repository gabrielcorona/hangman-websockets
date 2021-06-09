package com.coronadev.websocket;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.coronadev.data.Action;
import com.coronadev.data.FileSystemDao;
import com.coronadev.data.GameLogic;
import com.coronadev.data.GameSession;
import com.coronadev.data.Status;

@Controller
public class HangmanController {
	private FileSystemDao dao = new FileSystemDao();
	private GameLogic logic = new GameLogic();
	
	// This allows the status updates as well as subscriptions.
	@MessageMapping("/setAction")
	@SendTo("/hangman/updates")
	public Status setAction(Action action) {
		Status status = action.getStatus();
		if(action.getLetter().equals("[LOADING]")) {
			status = dao.getStatusBySession(status);
		}else {
			status = logic.evaluateAction(action);
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
		gameSession.setCurrentSessions(dao.getUserSessions(status.getPlayerID()));
		return gameSession;
	}
}
