var stompClient = null;
let status = {};
var headers = null;
var waitResponse =false;

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
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
		$("#guess").attr("disabled","disabled")
	}
	if(status.currentStatus == 'STAND_BY' || status.currentStatus == 'START'){
		$("#guess").removeAttr("disabled")
	}
}

function sendName() {
	status.playerID = $("#username").val();
    stompClient.send("/app/setAction", headers, JSON.stringify({'letter':$("#guess").val(), 'status': status}));
    $("#guess").val("");
}

function showStatus(message) {
	setStatusObj(message)	
	checkComplete()
    $("#log").append("<tr><td>"+message.playerID+"</td><td>"+message.session+"</td><td>" + message.uselessLetters + "</td><td>"+message.damage+"</td><td>"+message.currentStatus+"</td></tr>");
    $("#hangman").attr("class","status-"+message.damage);
    $("#word-guess").html(message.wordProgress);
}

function showSessions(message){
	var lastSession="";
	$(".session-option").remove();
	foundSessions = message.currentSessions;
	for(var n=0;n<foundSessions.length;n++){		
		$("#user-sessions").append('<option class="session-option" value="'+foundSessions[n]+'">'+foundSessions[n]+'</option>')
		lastSession=foundSessions[n];
	}
	status.session=lastSession;
	waitResponse =false;
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $("#send").click(function() { sendName(); });
});

function setStatusObj(message){
	status = {
		playerID:message.playerID,
		session:message.session,
		word:message.word,
		wordProgress:message.wordProgress,
		uselessLetters:message.uselessLetters,
		damage:message.damage,
		currentStatus:message.currentStatus
	}
}

function validateSession(){
	const queryString = window.location.search;
	const urlParams = new URLSearchParams(queryString);
	const sessionID = urlParams.get('session')
	
	if(sessionID != null){
	// Loading Session
		status.session = sessionID;
		status.playerID = $("#username").val();
		var readyConn = setInterval(function(){
	    	if(stompClient != null){
				stompClient.send("/app/setAction", headers, JSON.stringify({'letter':'[LOADING]', 'status': status}));
				clearInterval(readyConn)
	    	}
	    },500)
	}
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
					clearInterval(loadingInterval)
				}
			},500)
			
		}
		if($(this).val() != 'CREATE' && $(this).val() != '-1'){
			location.href='/?session='+$(this).val();
		}
	})
	
	validateSession();
});