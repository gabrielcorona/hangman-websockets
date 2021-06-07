package com.coronadev.websocket;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import com.coronadev.data.Action;
import com.coronadev.data.Status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebsocketIntegrationTests {

	@LocalServerPort
	private int port;

	private SockJsClient sockJsClient;

	private WebSocketStompClient stompClient;

	private final WebSocketHttpHeaders headers = new WebSocketHttpHeaders();

	@BeforeEach
	public void setup() {
		List<Transport> transports = new ArrayList<>();
		transports.add(new WebSocketTransport(new StandardWebSocketClient()));
		this.sockJsClient = new SockJsClient(transports);

		this.stompClient = new WebSocketStompClient(sockJsClient);
		this.stompClient.setMessageConverter(new MappingJackson2MessageConverter());
	}

	@Test
	public void getStatus() throws Exception {

		final CountDownLatch latch = new CountDownLatch(1);
		final AtomicReference<Throwable> failure = new AtomicReference<>();

		StompSessionHandler handler = new TestSessionHandler(failure) {

			@Override
			public void afterConnected(final StompSession session, StompHeaders connectedHeaders) {
				session.subscribe("/hangman/updates", new StompFrameHandler() {
					@Override
					public Type getPayloadType(StompHeaders headers) {
						return Status.class;
					}

					@Override
					public void handleFrame(StompHeaders headers, Object payload) {
						Status status = (Status) payload;
						try {
							assertEquals("playerID:|session:|word:|wordProgress:|uselessLetters:[]|damage:0|gameState:STAND_BY",status.toString());
						} catch (Throwable t) {
							failure.set(t);
						} finally {
							session.disconnect();
							latch.countDown();
						}
					}
				});
				try {
					Status status = new Status();
					status.initDefault();
					Action action = new Action();
					action.setLetter("");
					action.setStatus(status);
					session.send("/app/setAction", action);
				} catch (Throwable t) {
					failure.set(t);
					latch.countDown();
				}
			}
		};

		this.stompClient.connect("ws://localhost:{port}/hangman-websocket", this.headers, handler, this.port);

		if (latch.await(3, TimeUnit.SECONDS)) {
			if (failure.get() != null) {
				throw new AssertionError("", failure.get());
			}
		}
		else {
			fail("Greeting not received");
		}

	}

	private class TestSessionHandler extends StompSessionHandlerAdapter {

		private final AtomicReference<Throwable> failure;

		public TestSessionHandler(AtomicReference<Throwable> failure) {
			this.failure = failure;
		}

		@Override
		public void handleFrame(StompHeaders headers, Object payload) {
			this.failure.set(new Exception(headers.toString()));
		}

		@Override
		public void handleException(StompSession s, StompCommand c, StompHeaders h, byte[] p, Throwable ex) {
			this.failure.set(ex);
		}

		@Override
		public void handleTransportError(StompSession session, Throwable ex) {
			this.failure.set(ex);
		}
	}
}
