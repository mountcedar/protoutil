package jp.kuis.protobuf.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

import jp.kuis.protobuf.data.DataBuilder;
import jp.kuis.protobuf.data.Receivable;
import jp.kuis.protobuf.data.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: 自動生成された Javadoc
/**
 * The Class RequestHandler.
 * 
 * @brief the request handler for receiving and sending the protocol buffer
 *        messages.
 * @details this code is original from -
 *          https://github.com/mountcedar/local.protobuf
 *          .socket/blob/master/src/local
 *          /protobuf/socket/server/RequestHandler.java
 * @author sugiyama
 */
public class RequestHandler extends Thread {

	/** for logging. */
	protected static Logger logger = LoggerFactory
			.getLogger(RequestHandler.class);

	/** The socket. */
	protected Socket socket = null;

	/** The builder. */
	protected DataBuilder builder = null;

	/** The in. */
	protected DataInputStream in = null;

	/** The out. */
	protected DataOutputStream out = null;

	/** The terminate. */
	protected boolean terminate = false;

	/** The receivers. */
	protected List<Receivable> receivers = null;

	/**
	 * Creates the.
	 * 
	 * @param socket
	 *            the socket
	 * @param builder
	 *            the builder
	 * @param recievers
	 *            the recievers
	 * @return the request handler
	 */
	public static RequestHandler create(Socket socket, DataBuilder builder,
			List<Receivable> recievers) {
		try {
			RequestHandler instance = new RequestHandler(socket, builder,
					recievers);
			instance.in = new DataInputStream(socket.getInputStream());
			instance.out = new DataOutputStream(socket.getOutputStream());
			return instance;
		} catch (IOException e) {
			logger.error("{}", e);
			return null;
		}
	}

	/**
	 * Instantiates a new request handler.
	 * 
	 * @param socket
	 *            the socket
	 * @param builder
	 *            the builder
	 * @param recirvers
	 *            the recirvers
	 */
	protected RequestHandler(Socket socket, DataBuilder builder,
			List<Receivable> recirvers) {
		this.socket = socket;
		this.builder = builder;
		this.receivers = recirvers;
		logger.info("Connected: {}", socket.getRemoteSocketAddress());
	}

	/**
	 * Shutdown.
	 */
	public void shutdown() {
		try {
			if (!this.isAlive())
				return;
			this.terminate = true;
			this.socket.close();
			this.join(100);
			this.terminate = false;
		} catch (Exception e) {
			logger.error("{}", e);
		}
	}

	/**
	 * Send.
	 * 
	 * @param data
	 *            the data
	 * @return true, if successful
	 */
	protected boolean send(Serializable data) {
		try {
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

	/*
	 * (非 Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		try {
			while (!terminate) {
				int size = 0;
				try {
					size = in.readInt();
				} catch (EOFException e) {
					continue;
				}
				logger.debug("data size: {}", size);
				byte[] buf = new byte[size];
				in.readFully(buf);
				logger.debug("data serial: {}", buf);
				Serializable data = builder.create(buf);
				logger.debug("data: {}", data);
				if (data == null)
					continue;
				for (Receivable receiver : receivers) {
					// logger.debug("calling reciever: {}", reciever);
					receiver.onRecv(data);
				}
			}
		} catch (IOException e) {
			logger.debug("{}", e);
		} finally {
			try {
				if (socket != null && !socket.isClosed()) {
					socket.close();
				}
			} catch (IOException e) {
			}

			logger.info("Disconnected: {}", socket.getRemoteSocketAddress());
		}
	}
}