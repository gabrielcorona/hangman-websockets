let stompClient = null;
let status = {};
let headers = null;
let waitResponse =false;
let username = null;
let currentSessionID = null;
let sessionCreated = false;
let myTurn = false;

function setConnected(connected) {
    $("#connect").prop("disabled", connected);    
    if (connected) {
        $("#conversation").show();
    }
    else {
        $("#conversation").hide();
    }
    $("#log").html("");
}

// We connect to the socket responsible to manage the hangman game actions.
function connect() {
    var socket = new SockJS('/hangman-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true);
        stompClient.subscribe('/hangman/updates', function (status) {
        	showStatus(JSON.parse(status.body));
            
        });
    });
}

function checkComplete(){
	if(status.currentStatus == 'WIN' || status.currentStatus == 'LOSE'){
		$("#guess").attr("disabled","disabled");
		$("#send-guess").hide();
	}
	if(status.currentStatus == 'STAND_BY' || status.currentStatus == 'START'){
		if(myTurn)
			$("#guess").removeAttr("disabled");
			$("#send-guess").show();
	}
}

function sendName() {
	status.playerID = $("#username").val();
    stompClient.send("/app/setAction", headers, JSON.stringify({'letter':$("#guess").val(), 'status': status}));
    $("#guess").val("");
}

function showStatus(message) {
	if(status != null){
  		if(status.session==message.session){
  			setStatusObj(message)
  			updateStatus()
		}
  	}else{
  		setStatusObj(message)
  		updateStatus()
  	}
	
}

function updateStatus(){
	checkComplete()
    $("#log").append("<tr><td>"+status.playerID+"</td><td>"+status.session+"</td><td>" + status.uselessLetters + "</td><td>"+status.damage+"</td><td>"+status.currentStatus+"</td></tr>");
    $("#hangman").attr("class","status-"+status.damage);
    $("#word-guess").html(status.wordProgress);
}

function showSessions(message){
	if(message.username != username)
		return;
	var lastSession="";
	$(".session-option").remove();
	let foundSessions = message.currentSessions;
	for(var n=0;n<foundSessions.length;n++){		
		$("#user-sessions").append('<option class="session-option" value="'+foundSessions[n]+'">'+foundSessions[n]+'</option>')
		lastSession=foundSessions[n];
	}
	if(currentSessionID == null){
		status.session=lastSession;
	}else{
		status.session=currentSessionID;
	}
	waitResponse =false;
	console.info("Created Session: "+sessionCreated)
	if(sessionCreated)
		location.href='/?session='+status.session;
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $("#send-guess").click(function() { sendName(); });
});

function setStatusObj(message){
	status = {
		playerID:message.playerID,
		versus:message.versus,
		session:message.session,
		word:message.word,
		wordProgress:message.wordProgress,
		uselessLetters:message.uselessLetters,
		damage:message.damage,
		currentStatus:message.currentStatus
	}
	setPlayers()
}

function setPlayers(){
	let players = status.versus;
	var playersListHTML = '';
	for(var n=0;n < players.length;n++){
		var playerturn = '';
		if(players[n] == status.playerID){
			playerturn = ' player-turn'
			//if(status.playerID == username){
			//	console.info(username+" Turn")
			//	}
		}
		playersListHTML += '<div class="player'+playerturn+'">'+players[n]+'</div>'
	}
	$("#players-list").html(playersListHTML)
	if(username == status.playerID){
		$("#guess").removeAttr("disabled")
		$("#send-guess").show();
		myTurn=true;
		console.info(username+" Turn")
	}else{
		$("#guess").attr("disabled","disabled")
		$("#send-guess").hide();
		myTurn=false;
		console.info("Not your Turn")
	}
}

function validateSession(){
	const queryString = window.location.search;
	const urlParams = new URLSearchParams(queryString);
	const sessionID = urlParams.get('session')
	
	username = window.localStorage.getItem('username');
	console.info('User: '+username)
	var readyConn = setInterval(function(){
   		if(stompClient != null){
			if(username != null){
				$("#login-mask").hide();
				$("#username").val(username);
				headers = {
			      login: $("#username").val(),
			      passcode: 'guest',
			      'client-id': 'my-client-id'
			    };
				stompClient.subscribe('/hangman/sessions', function (status) {
		            showSessions(JSON.parse(status.body));
		        });
		        stompClient.send("/app/setGame", headers, JSON.stringify({'playerID': $("#username").val(), 'session': '', 'word': '', 'damage': 1, 'currentStatus': 'STAND_BY'}));
			}else{
				$("#login-mask").show();
				$("#login").show();
			}
			
			if(sessionID != null){
				// Loading Session
				currentSessionID = sessionID;				
				status.session = sessionID;
				status.playerID = $("#username").val();
				
				stompClient.send("/app/setAction", headers, JSON.stringify({'letter':'[LOADING]', 'status': status}));
						
			}
			clearInterval(readyConn)
    	}
    },500)
	
}

connect();

$( document ).ready(function() {
	// When the document is ready we will be listening to the Login button to be clicked and the user sessions section.
	
	// On click we will login the user and use the username to create future sessions.
	$("#login-button").click(function(){
		$("#login-mask").hide();
		$("#login").hide();
		headers = {
	      login: $("#username").val(),
	      passcode: 'guest',
	      'client-id': 'my-client-id'
	    };
	    window.localStorage.setItem('username', $("#username").val());
        stompClient.subscribe('/hangman/sessions', function (status) {
            showSessions(JSON.parse(status.body));
        });
		stompClient.send("/app/setGame", headers, JSON.stringify({'playerID': $("#username").val(), 'session': '', 'word': '', 'damage': 1, 'currentStatus': 'STAND_BY'}));
	    		
		
	});
	
	// When the user select a session this will trigger depending on the options, the session creation or to load the selected session.  
	$("#user-sessions").change(function(){
		if($(this).val() == 'CREATE'){
			waitResponse =true;
			stompClient.send("/app/setGame", headers, JSON.stringify({'playerID': $("#username").val(), 'session': 'CREATE', 'word': '', 'damage': 1, 'currentStatus': 'STAND_BY'}));			
			
			var loadingInterval = setInterval(function(){
				if(!waitResponse){
					stompClient.send("/app/setAction", headers, JSON.stringify({'letter':'[LOADING]', 'status': status}));
					sessionCreated = true;
					clearInterval(loadingInterval)
				}
			},500)
			
		}
		if($(this).val() != 'CREATE' && $(this).val() != '-1'){
			location.href='/?session='+$(this).val();
		}
	})
	$("#share").click(function(){
		$("#sessionID").show();
		$("#sessionID").val(window.location.href);
		$("#sessionID").focus();
		$("#sessionID").select();
		document.execCommand("copy");	
	})
	validateSession();
});