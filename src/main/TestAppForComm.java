package main;

import jp.kuis.protobuf.client.ProtocolBufferClient;
import jp.kuis.protobuf.data.DataBuilder;
import jp.kuis.protobuf.data.Receivable;
import jp.kuis.protobuf.data.Serializable;
import jp.kuis.protobuf.server.ProtocolBufferServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.InvalidProtocolBufferException;

import comm.Comm.Data;
import comm.Comm.Message;

public class TestAppForComm {
	/** for logging */
	protected static Logger logger = LoggerFactory
			.getLogger(TestAppForComm.class);

	public static class CommData implements Serializable {
		/** for logging */
		protected static Logger logger = LoggerFactory
				.getLogger(CommData.class);

		public Message data = null;

		public CommData() {
		}

		public CommData(Message data) {
			this.data = data;
		}

		@Override
		public byte[] serialize() {
			if (data == null)
				return null;
			return this.data.toByteArray();
		}

		@Override
		public boolean deserialize(byte[] binaries) {
			try {
				this.data = Message.parseFrom(binaries);
				return true;
			} catch (InvalidProtocolBufferException e) {
				logger.error("{}", e);
				return false;
			}
		}

		@Override
		public int getSerializedSize() {
			if (this.data == null)
				return 0;
			return this.data.getSerializedSize();
		}
	}

	public static class CommDataBuilder implements DataBuilder {
		/** for logging */
		protected static Logger logger = LoggerFactory
				.getLogger(CommDataBuilder.class);

		@Override
		public Serializable create(byte[] binary) {
			try {
				return new CommData(Message.parseFrom(binary));
			} catch (Exception e) {
				logger.error("{}", e);
				return null;
			}
		}
	}

	public static void main(String[] args) {
		try {
			Message msg = Message.newBuilder().setId(1)
					.setType(Message.Type.ACK)
					.addData(Data.newBuilder().setStatus("hogehoge")).build();

			DataBuilder builder = new CommDataBuilder();
			Receivable reciever = new Receivable() {
				@Override
				public boolean onRecv(Serializable data) {
					CommData comm = (CommData) data;
					logger.debug("data:");
					logger.debug("\tid: {}, type: {},", comm.data.getId(),
							comm.data.getType());
					logger.debug("\tdata:");
					for (Data e : comm.data.getDataList()) {
						logger.debug("\t\tstatus: {}", e.getStatus());
					}
					return true;
				}
			};

			ProtocolBufferServer server = new ProtocolBufferServer(builder);
			server.register(reciever);
			server.start();

			ProtocolBufferClient client = ProtocolBufferClient.create(
					"localhost", builder);
			if (client == null) {
				logger.error("cannot connect to the server.");
				server.shutdown();
				return;
			}
			for (int i = 0; i < 10; i++) {
				logger.debug("sending data [{}]", i);
				client.send(new CommData(msg));
				logger.debug("sended data [{}]", i);
			}

			server.shutdown();
			return;
		} catch (Exception e) {
			logger.error("{}", e);
		}
	}
}
