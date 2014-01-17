package main;

import jp.wandercode.protobuf.client.ProtocolBuffersClient;
import jp.wandercode.protobuf.data.Receivable;
import jp.wandercode.protobuf.data.Serializable;
import jp.wandercode.protobuf.data.Message;
import jp.wandercode.protobuf.server.ProtocolBuffersServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import comm.Comm.Data;

public class TestAppForComm {
	/** for logging */
	protected static Logger logger = LoggerFactory.getLogger(TestAppForComm.class);

	public static void main(String[] args) {
		try {
			comm.Comm.Message msg = comm.Comm.Message.newBuilder()
					.setId(1)
					.setType(comm.Comm.Message.Type.ACK)
					.addData(Data.newBuilder().setStatus("hogehoge").build())
					.build();

			Message<comm.Comm.Message> handler = new Message<comm.Comm.Message>(comm.Comm.Message.class);
			Receivable reciever = new Receivable() {
				@SuppressWarnings("unchecked")
				@Override
				public boolean onRecv(Serializable data) {
					Message<comm.Comm.Message> msg = (Message<comm.Comm.Message>)data;
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

			ProtocolBuffersServer server = new ProtocolBuffersServer(handler);
			server.register(reciever);
			server.start();

			ProtocolBuffersClient client = ProtocolBuffersClient.create("localhost", handler);
			if (client == null) {
				logger.error("cannot connect to the server.");
				server.shutdown();
				return;
			}
			for (int i = 0; i < 10; i++) {
				logger.debug("sending data [{}]", i);
				client.send(new Message<comm.Comm.Message>(msg));
				logger.debug("sended data [{}]", i);
			}

			Thread.sleep (500);
			server.shutdown();
			return;
		} catch (Exception e) {
			logger.error("{}", e);
		}
	}
}
