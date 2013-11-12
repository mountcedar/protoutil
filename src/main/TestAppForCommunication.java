package main;

import jp.kuis.protobuf.data.DataBuilder;
import jp.kuis.protobuf.data.Receivable;
import jp.kuis.protobuf.data.Serializable;
import jp.kuis.protobuf.server.ProtocolBufferServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.InvalidProtocolBufferException;

import communication.SensorComm.PBMultiSensorMessage;
import communication.SensorComm.PBSensorMessage;
import communication.SensorComm.PBRangeData;
import communication.SensorComm.PBPosition;

public class TestAppForCommunication {
	/** for logging */
	protected static Logger logger = LoggerFactory
			.getLogger(TestAppForCommunication.class);

	public static class SensorCommData implements Serializable {
		/** for logging */
		protected static Logger logger = LoggerFactory
				.getLogger(SensorCommData.class);

		public PBMultiSensorMessage data = null;

		public SensorCommData() {
		}

		public SensorCommData(PBMultiSensorMessage data) {
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
				this.data = PBMultiSensorMessage.parseFrom(binaries);
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

	public static class SensorCommDataBuilder implements DataBuilder {
		/** for logging */
		protected static Logger logger = LoggerFactory
				.getLogger(SensorCommDataBuilder.class);

		@Override
		public Serializable create(byte[] binary) {
			try {
				return new SensorCommData(
						PBMultiSensorMessage.parseFrom(binary));
			} catch (Exception e) {
				logger.error("{}", e);
				return null;
			}
		}
	}

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

			DataBuilder builder = new SensorCommDataBuilder();
			Receivable reciever = new Receivable() {
				@Override
				public boolean onRecv(Serializable data) {
					// ����͎g��Ȃ�
					logger.debug("onRecv");
					return true;
				}
			};

			ProtocolBufferServer server = new ProtocolBufferServer(builder);
			server.register(reciever);
			server.start();

			Thread.sleep(3000);

			for (int i = 0; i < 10; i++) {
				logger.debug("sending data [{}]", i);
				server.send(new SensorCommData(msg));
				// logger.debug("sended data [{}]", i);
				Thread.sleep(1000);
			}

			server.shutdown();
			return;
		} catch (Exception e) {
			logger.error("{}", e);
		}
	}
}
