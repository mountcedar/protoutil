package jp.wandercode.protobuf.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import jp.wandercode.protobuf.data.DataBuilder;
import jp.wandercode.protobuf.data.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: 自動生成された Javadoc
/**
 * The Class ProtocolBufferClient.
 * 
 * @brief the client for receiving and sending the protocol buffer messages.
 * @details this code is original from -
 *          https://github.com/mountcedar/local.protobuf
 *          .socket/blob/master/src/local
 *          /protobuf/socket/client/ProtocolBufferClient.java
 * @author sugiyama
 */
public class ProtocolBuffersClient {

	/** for logging. */
	protected static Logger logger = LoggerFactory
			.getLogger(ProtocolBuffersClient.class);

	/** The Constant PORT. */
	public static final int PORT = 1111;

	/** The builder. */
	protected DataBuilder builder = null;

	/** The socket. */
	protected Socket socket = null;

	/** The host. */
	protected String host = null;

	/** The is connected. */
	protected boolean isConnected = false;

	/** The in. */
	protected DataInputStream in = null;

	/** The out. */
	protected DataOutputStream out = null;

	private boolean recvErrorHappend = false;

	/**
	 * Creates the.
	 * 
	 * @param hostname
	 *            the hostname
	 * @param builder
	 *            the builder
	 * @return the protocol buffer client
	 */
	public static ProtocolBuffersClient create(String hostname,
			DataBuilder builder) {
		try {
			ProtocolBuffersClient instance = new ProtocolBuffersClient(hostname,
					builder);
			instance.socket = new Socket(hostname, PORT);
			logger.info("Connected: {}", instance.socket.getInetAddress()
					.getHostName());
			return instance;
		} catch (Exception e) {
			logger.error("{}", e);
			return null;
		}
	}

	/**
	 * Creates the.
	 * 
	 * @param socket
	 *            the socket
	 * @param builder
	 *            the builder
	 * @return the protocol buffer client
	 */
	public static ProtocolBuffersClient create(Socket socket, DataBuilder builder) {
		try {
			ProtocolBuffersClient instance = new ProtocolBuffersClient("",
					builder);
			instance.socket = socket;
			return instance;
		} catch (Exception e) {
			logger.error("{}", e);
			return null;
		}
	}

	/**
	 * Instantiates a new protocol buffer client.
	 * 
	 * @param hostname
	 *            the hostname
	 * @param builder
	 *            the builder
	 */
	protected ProtocolBuffersClient(String hostname, DataBuilder builder) {
		this.host = hostname;
		this.builder = builder;
	}

	/**
	 * Send.
	 * 
	 * @param data
	 *            the data
	 * @return true, if successful
	 */
	public boolean send(Serializable data) {
		try {
			if (out == null)
				out = new DataOutputStream(socket.getOutputStream());

			int size = data.getSerializedSize();
			out.writeInt(size);
			out.flush();
			out.write(data.serialize());
			out.flush();
			return true;
		} catch (Exception e) {
			logger.error("{}", e);
			return false;
		}
	}

	/**
	 * Recv.
	 * 
	 * @return the serializable
	 */	
	public Serializable recv() {
		try {
			if (in == null)
				in = new DataInputStream(socket.getInputStream());

			int size = in.readInt();
			//size = Integer.reverseBytes(size);
			//logger.debug("size: {}", size);
			byte[] buf = new byte[size];
			in.readFully(buf);
			//logger.debug("buf: {}", buf);
			recvErrorHappend = false;
			return builder.create(buf);
		} catch (Exception e) {
			if (!recvErrorHappend) {
				recvErrorHappend = true;
				logger.error("{}", e);
			}
			return null;
		}
	}
}
