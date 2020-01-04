package frc.utilities;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.ArrayDeque;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebSocket {

	private static enum FrameState {
		CTRL_AND_OP,
		MASK_AND_DATA_LENGTH,
		EXTENDED_DATA_LENGTH,
		MASK_KEY,
		DATA
	}

	private AtomicBoolean isRunning;
	private HashMap<Integer,WebSocketHandler> handlerMap;
	private HashMap<WebSocketHandler,WebSocketSession> sessionMap;
	private int nextId;

	public WebSocket (int port) {

		handlerMap = new HashMap<>();
		sessionMap = new HashMap<>();

		isRunning = new AtomicBoolean(true);
		nextId = 0;

		new Thread(() -> {
			try {
				ServerSocket server = new ServerSocket(port);
				while (isRunning.get()) {
					Socket client = server.accept();
					handleClient(client);
				}
				server.close();
			} catch (Exception e) {
				// TODO: 
			}

		}).start();

	}
	
	public void stopServer () {
		isRunning.set(false);
	}

	public int registerHandler (WebSocketHandler handler) {
		int id = nextId++;
		synchronized (handlerMap) { handlerMap.put(id, handler); }
		return id;
	}

	public void unregisterHandler (int id) {
		WebSocketHandler h;
		synchronized (handlerMap) { h = handlerMap.remove(id); }
		if (h != null) { 
			WebSocketSession s; synchronized (sessionMap) { s = sessionMap.remove(h); }
			if (s != null) { s.invalidate(); }
		};
	}

	private void handleClient (final Socket client) {
		new Thread(() -> {

			try {
				InputStream is = client.getInputStream();
				OutputStream os = client.getOutputStream();

				handshake(is, os);
				handler(is, os);

				is.close();
				os.close();
			} catch (Exception e) {

			}

		}).start();
	}

	private boolean handshake (InputStream is, OutputStream os) {
		Scanner sc = new Scanner(is, "utf-8");
		sc.useDelimiter("\\r\\n\\r\\n");

		String data = sc.next();
		Matcher get = Pattern.compile("^GET").matcher(data);
		try {
			if (get.find()) {
				Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);
				match.find();

				String forDigest = match.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
				byte[] digested = MessageDigest.getInstance("SHA-1").digest(forDigest.getBytes());

				byte[] response1 = ("HTTP/1.1 101 Switching Protocols\r\n"
									+ "Upgrade: websocket\r\n"
									+ "Connection: Upgrade\r\n"
									+ "Sec-WebSocket-Accept: ").getBytes("utf-8");
				byte[] response2 = Base64.getEncoder().encode(digested);
				byte[] response3 = "\r\n\r\n".getBytes("utf-8");

				ByteBuffer bb = ByteBuffer.wrap(new byte[response1.length + response2.length + response3.length]);
				bb.put(response1); bb.put(response2); bb.put(response3); 
				byte[] ba2 = bb.array();

				os.write(ba2, 0, ba2.length);
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private void handler (InputStream is, OutputStream os) {
		AtomicBoolean connectionLive = new AtomicBoolean(true);

		try {
			FrameState fs = FrameState.CTRL_AND_OP;

			int opcode = 0;
			int dataLength = 0;

			ByteBuffer extDataLengthBuffer = ByteBuffer.allocate(0);
			ByteBuffer maskBuffer = ByteBuffer.allocate(4);
			ByteBuffer dataBuffer = ByteBuffer.wrap(new byte[0]);

			int byteCountdown = 0;
			boolean alreadySentClose = false;
			
			final ArrayDeque<String> incomingMQ = new ArrayDeque<>();
			final ArrayDeque<byte[]> outgoingMQ = new ArrayDeque<>();

			createMessageWriteThread(os, outgoingMQ, connectionLive);
			createIncomingMessageDispatchThread(incomingMQ, connectionLive);

			Collection<WebSocketHandler> hColl; synchronized (handlerMap) { hColl = handlerMap.values();}
			for (WebSocketHandler h : hColl) {
				new Thread(() -> {
					WebSocketSession s = new WebSocketSession(outgoingMQ);
					synchronized(sessionMap) { sessionMap.put(h,s); }
					h.onOpen(s);
				}).start();
			}

			int i;
			loop: while ((i = is.read()) != -1) {

				byte b = (byte) (i & 0xFF);

				switch (fs) {

					case CTRL_AND_OP: {
						if ((b & 0x80) == 0) { /* Error & Close */ }
						opcode = (b & 0x0F);
						if (opcode == 0x08) {
							if (!alreadySentClose) {
								// TODO: Write close packet
								synchronized (outgoingMQ) { outgoingMQ.add(buildClosePacket()); }
								alreadySentClose = true;
							}
							for (WebSocketHandler h : hColl) {
								new Thread(() -> { h.onClose(); }).start();
							}
							break loop;
						}
						fs = FrameState.MASK_AND_DATA_LENGTH;
					} break;

					case MASK_AND_DATA_LENGTH: {
						if ((b & 0x80) == 0) { /* Error & Close */ }
						dataLength = (b & 0x7F);
						if (dataLength >= 126) {
							fs = FrameState.EXTENDED_DATA_LENGTH;
							byteCountdown = (dataLength == 127) ? 8 : 2;
							extDataLengthBuffer = ByteBuffer.wrap(new byte[byteCountdown]);
						} else {
							fs = FrameState.MASK_KEY;
							byteCountdown = 4;
						}
					} break;

					case EXTENDED_DATA_LENGTH: {
						extDataLengthBuffer.put(b);
						if (--byteCountdown == 0) {
							extDataLengthBuffer.flip();
							if (extDataLengthBuffer.capacity() == 2) {
								dataLength = extDataLengthBuffer.asShortBuffer().get();
							} else {
								long edl = extDataLengthBuffer.asLongBuffer().get();
								if (edl > Integer.MAX_VALUE) { throw new Exception("Too big to handle."); }
								dataLength = (int) edl;
							}
							fs = FrameState.MASK_KEY;
							byteCountdown = 4;
						}
					} break;

					case MASK_KEY: {
						maskBuffer.put(b);
						if (--byteCountdown == 0) {
							fs = FrameState.DATA;
							byteCountdown = dataLength;
							// System.out.println("Data length: " + dataLength);
							dataBuffer = ByteBuffer.wrap(new byte[dataLength]);
						}
					} break;

					case DATA: {
						dataBuffer.put(b);
						if (--byteCountdown == 0) {
							byte[] data = dataBuffer.array();
							byte[] key = maskBuffer.array();

							for (int j = 0; j < data.length; j++) {
								data[j] = (byte) (data[j] ^ key[j & 0x3]);
							}

							if (opcode == 1) {
								synchronized (incomingMQ) { incomingMQ.add(new String(data, "utf-8")); }
								fs = FrameState.CTRL_AND_OP;
							}

							// Otherwise... :/

							extDataLengthBuffer.clear();
							maskBuffer.clear();
							dataBuffer.clear();
						}
					} break;

				}

			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}

		connectionLive.set(false);

	}

	private void createMessageWriteThread (final OutputStream os, final ArrayDeque<byte[]> mq, final AtomicBoolean connStatus) {
		new Thread(() -> {

			while (connStatus.get()) {
				int qLen; synchronized (mq) { qLen = mq.size(); }
				for (int i = 0; i < qLen; i++) {
					byte[] packet; synchronized (mq) { packet = mq.poll(); }
					try { os.write(packet, 0, packet.length); }
					catch (Exception e) { }
				}
				try { Thread.sleep(5); }
				catch (Exception e) { }
			}

		}).start();
	}

	private void createIncomingMessageDispatchThread (final ArrayDeque<String> mq, final AtomicBoolean connStatus) {
		new Thread(() -> {

			while (connStatus.get()) {
				int qLen; synchronized (mq) { qLen = mq.size(); }
				for (int i = 0; i < qLen; i++) {
					String data; synchronized (mq) { data = mq.poll(); }
					Collection<WebSocketHandler> hColl; synchronized (handlerMap) { hColl = handlerMap.values();}
					for (WebSocketHandler h : hColl) {
						new Thread(() -> { h.onMessage(data); }).start();
					}
				}
				try { Thread.sleep(5); }
				catch (Exception e) { }
			}

		}).start();
	}

	private static byte[] buildClosePacket () {
		byte[] ba = new byte[2];
		ba[0] = (byte) 0x88;
		ba[1] = (byte) 0x00;
		return ba;
	}

	private static byte[] buildStringPacket (String msg) {
		int ctrlLen = 1;
		int sizeLen = 1;
		int extLen = (ctrlLen > 125) ? 8 : 0;
		int dataLen = msg.length();

		ByteBuffer bb = ByteBuffer.wrap(new byte[ctrlLen + sizeLen + extLen + dataLen]);

		bb.put((byte) 0x81);
		bb.put((byte) ((extLen == 8) ? 127 : dataLen));
		if (extLen == 8) {
			ByteBuffer bb2 = ByteBuffer.wrap(new byte[extLen]);
			bb2.asLongBuffer().put(dataLen);
			bb.put(bb2);
		}

		try {
			bb.put(msg.getBytes("utf-8"));
		} catch (Exception e) {
			bb.put(msg.getBytes());
		}

		return bb.array();
	}

	public static void main (String[] args) {
		new WebSocket(9999);
	}

	public static interface WebSocketHandler {
		public void onOpen (WebSocketSession session);
		public void onMessage (String message);
		public void onError (String message);
		public void onClose ();
	}

	public class WebSocketSession {

		private ArrayDeque<byte[]> mq;
		private boolean isValid;

		private WebSocketSession (final ArrayDeque<byte[]> outgoingMQ) {
			mq = outgoingMQ;
			isValid = true;
		}

		public final void sendMessage (String msg) {
			if (!isValid) { return; }
			byte[] packet = buildStringPacket(msg);
			synchronized (mq) { mq.add(packet); }
		}

		public final void endSession () {
			if (!isValid) { return; }
			byte[] packet = buildClosePacket();
			synchronized (mq) { mq.add(packet); }
		}

		private void invalidate () {
			isValid = false;
		}

	}


}