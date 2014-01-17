package main;

import jp.wandercode.protobuf.client.ProtocolBufferClient;
import jp.wandercode.protobuf.data.Receivable;
import jp.wandercode.protobuf.data.Serializable;
import jp.wandercode.protobuf.server.ProtocolBufferServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import comm.Comm.Data;
import comm.Comm.Message;

public class TestAppForComm {
	/** for logging */
	protected static Logger logger = LoggerFactory.getLogger(TestAppForComm.class);

	public static void main(String[] args) {
		try {
			Message msg = Message.newBuilder().setId(1)
					.setType(Message.Type.ACK)
					.addData(Data.newBuilder().setStatus("hogehoge")).build();

			jp.wandercode.protobuf.data.Message<Message> handler = new jp.wandercode.protobuf.data.Message<Message>(Message.class);
			Receivable reciever = new Receivable() {
				@SuppressWarnings("unchecked")
				@Override
				public boolean onRecv(Serializable data) {
					jp.wandercode.protobuf.data.Message<Message> msg = (jp.wandercode.protobuf.data.Message<Message>)data;
					logger.debug("data:");
					logger.debug("\tid: {}, type: {},", msg.data.getId(),
							msg.data.getType());
					logger.debug("\tdata:");
					for (Data e : msg.data.getDataList()) {
						logger.debug("\t\tstatus: {}", e.getStatus());
					}
					return true;
				}
			};

			ProtocolBufferServer server = new ProtocolBufferServer(handler);
			server.register(reciever);
			server.start();

			ProtocolBufferClient client = ProtocolBufferClient.create("localhost", handler);
			if (client == null) {
				logger.error("cannot connect to the server.");
				server.shutdown();
				return;
			}
			for (int i = 0; i < 10; i++) {
				logger.debug("sending data [{}]", i);
				client.send(new jp.wandercode.protobuf.data.Message<Message>(msg));
				logger.debug("sended data [{}]", i);
			}

			Thread.sleep (3000);
			server.shutdown();
			return;
		} catch (Exception e) {
			logger.error("{}", e);
		}
	}
}
