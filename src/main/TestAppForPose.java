package main;

import jp.kuis.protobuf.client.ProtocolBufferClient;
import jp.kuis.protobuf.data.DataBuilder;
import jp.kuis.protobuf.data.Receivable;
import jp.kuis.protobuf.data.Serializable;
import jp.kuis.protobuf.server.ProtocolBufferServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import comm.pose.Pose.Data;
import comm.pose.Pose.Message;

public class TestAppForPose {
	protected static Logger logger = LoggerFactory
			.getLogger(TestAppForPose.class);

	public static class PoseData implements Serializable {
		protected static Logger logger = LoggerFactory
				.getLogger(PoseData.class);
		public Message msg = null;

		public PoseData() {
		}

		public PoseData(Message msg) {
			this.msg = msg;
		}

		@Override
		public byte[] serialize() {
			if (msg != null) {
				return msg.toByteArray();
			}
			return null;
		}

		@Override
		public boolean deserialize(byte[] binaries) {
			try {
				this.msg = Message.parseFrom(binaries);
				return true;
			} catch (Exception e) {
				logger.error("stacktrace: {}", e);
				return false;
			}
		}

		@Override
		public int getSerializedSize() {
			if (msg != null) {
				return msg.getSerializedSize();
			}
			return 0;
		}
	}

	public static class PoseDataBuilder implements DataBuilder {
		protected static Logger logger = LoggerFactory
				.getLogger(PoseDataBuilder.class);

		@Override
		public Serializable create(byte[] binary) {
			try {
				return new PoseData(Message.parseFrom(binary));
			} catch (Exception e) {
				logger.error("stacktrace: {}", e);
				return null;
			}
		}
	}

	public static void main(String[] args) {
		try {

			Data data = Data.newBuilder().setCommand(Data.Command.CARIBRATE)
					.build();
			Message msg = Message.newBuilder().setType(Message.Type.COMMAND)
					.setData(data).build();

			DataBuilder builder = new PoseDataBuilder();
			Receivable reciever = new Receivable() {
				@Override
				public boolean onRecv(Serializable data) {
					PoseData pose = (PoseData) data;
					logger.debug("data:");
					logger.debug("\ttype: {},", pose.msg.getType());
					logger.debug("\tcontents: {}", pose.msg.getData()
							.getCommand());
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
				client.send(new PoseData(msg));
				logger.debug("sended data [{}]", i);
			}

			server.shutdown();
			return;
		} catch (Exception e) {
			logger.error("{}", e);
		}
	}
}
