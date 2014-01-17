/**
	@brief the server to stream protocol buffer message.
 */
package jp.wandercode.protobuf.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import jp.wandercode.protobuf.data.DataBuilder;
import jp.wandercode.protobuf.data.Receivable;
import jp.wandercode.protobuf.data.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: 自動生成された Javadoc
/**
 * The Class ProtocolBufferServer.
 * 
 * @brief the server to stream or receive protocol buffer
 * @details this code is original from -
 *          https://github.com/mountcedar/local.protobuf
 *          .socket/blob/master/src/local
 *          /protobuf/socket/server/ProtocolBufferServer.java
 * @author sugiyama
 */
public class ProtocolBuffersServer extends Thread {

	/** for logging. */
	protected static Logger logger = LoggerFactory
			.getLogger(ProtocolBuffersServer.class);

	/** The Constant PORT. */
	public static final int PORT = 1111;

	/** The terminate. */
	protected boolean terminate = false;

	/** The server socket. */
	protected ServerSocket serverSocket = null;

	/** The builder. */
	protected DataBuilder builder = null;

	/** The requests. */
	protected List<RequestHandler> requests = null;

	/** The recievers. */
	protected List<Receivable> recievers = null;

	/**
	 * Instantiates a new protocol buffer server.
	 * 
	 * @param builder
	 *            the builder
	 * @brief constructor
	 */
	public ProtocolBuffersServer(DataBuilder builder) {
		this.builder = builder;
		this.requests = new ArrayList<RequestHandler>();
		this.recievers = new ArrayList<Receivable>();
	}

	/**
	 * Register.
	 * 
	 * @param e
	 *            the e
	 */
	public void register(Receivable e) {
		this.recievers.add(e);
	}

	/**
	 * Shutdown.
	 */
	public void shutdown() {
		try {
			for (RequestHandler handler : requests) {
				handler.shutdown();
			}
			if (!this.isAlive())
				return;
			this.terminate = true;
			this.serverSocket.close();
			this.serverSocket = null;
			this.join(100);
			this.terminate = false;
		} catch (Exception e) {
			logger.error("{}", e);
		}
	}

	/**
	 * Send.
	 * 
	 * @param e
	 *            the e
	 */
	public void send(Serializable e) {
		for (RequestHandler handler : requests) {
			handler.send(e);
		}
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		try {
			serverSocket = new ServerSocket(PORT);
			logger.info("ProtocolBufferServer is (port={})",
					serverSocket.getLocalPort());
			while (!terminate) {
				Socket socket = serverSocket.accept();
				RequestHandler handler = RequestHandler.create(socket, builder,
						recievers);
				if (handler == null)
					continue;
				requests.add(handler);
				handler.start();
				for (RequestHandler e : requests) {
					if (!e.isAlive())
						requests.remove(handler);
				}
			}
		} catch (IOException e) {
			logger.error("{}", e.getMessage());
		} finally {
			try {
				if (serverSocket != null && !serverSocket.isClosed()) {
					serverSocket.close();
				}
			} catch (IOException e) {
			}
		}
	}
}
