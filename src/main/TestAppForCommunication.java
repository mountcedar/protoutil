package main;

import jp.wandercode.protobuf.client.ProtocolBufferClient;
import jp.wandercode.protobuf.data.Receivable;
import jp.wandercode.protobuf.data.Serializable;
import jp.wandercode.protobuf.data.Message;
import jp.wandercode.protobuf.server.ProtocolBufferServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import communication.SensorComm.PBMultiSensorMessage;
import communication.SensorComm.PBSensorMessage;
import communication.SensorComm.PBRangeData;
import communication.SensorComm.PBPosition;

public class TestAppForCommunication {
	/** for logging */
	protected static Logger logger = LoggerFactory
			.getLogger(TestAppForCommunication.class);

	public static void main(String[] args) {
		try {
			PBPosition pos = PBPosition.newBuilder().setX(1000).setY(1000)
					.setZ(1000).build();
			PBRangeData range = PBRangeData.newBuilder().setPosition(pos)
					.build();
			PBSensorMessage sensorMessage = PBSensorMessage.newBuilder()
					.addData(range).build();
			PBMultiSensorMessage msg = PBMultiSensorMessage.newBuilder()
					.addSensorData(sensorMessage).build();

			Message<PBMultiSensorMessage> builder = new Message<PBMultiSensorMessage>(PBMultiSensorMessage.class);
			Receivable reciever = new Receivable() {
				@Override
				public boolean onRecv(Serializable data) {
					logger.debug("onRecv");
					return true;
				}
			};

			ProtocolBufferServer server = new ProtocolBufferServer(builder);
			server.register(reciever);
			server.start();

			Thread.sleep(3000);

			ProtocolBufferClient client = ProtocolBufferClient.create("localhost", builder);
			if (client == null) {
				logger.error("cannot connect to the server.");
				server.shutdown();
				return;
			}
			for (int i = 0; i < 10; i++) {
				logger.debug("sending data [{}]", i);
				client.send(new Message<PBMultiSensorMessage>(msg));
			}

			Thread.sleep(3000);
			server.shutdown();
			return;
		} catch (Exception e) {
			logger.error("{}", e);
		}
	}
}
