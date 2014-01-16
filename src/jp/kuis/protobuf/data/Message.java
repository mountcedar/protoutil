package jp.kuis.protobuf.data;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Message<T extends com.google.protobuf.GeneratedMessage> implements Serializable, DataBuilder {
	protected static Logger logger = LoggerFactory.getLogger(Message.class);
	protected T data = null;
	
	public Message() {}
	
	public Message(T data) {
		this.data = data;
	}

	/*
	@SuppressWarnings({ "unchecked" })
	@Override
	public Serializable create(byte[] binary) {
		try {
			//Class<T> clazz = Message.class.getGenericInterfaces();
			//Method parseFrom_ = clazz.getDeclaredMethod("parseFrom");
			//return new Message<T>((T)parseFrom_.invoke(null, binary));
		} catch (Exception e) {
			logger.error("{}", e);
			return null;
		}

	}
	*/

	@Override
	public byte[] serialize() {
		try {
			if (data == null) return null;
			return data.toByteArray();
		} catch (Exception e) {
			logger.error ("{}", e);
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean deserialize(byte[] binaries) {
		try {
			Method parseFrom_ = data.getClass().getDeclaredMethod("parseFrom");
			this.data = (T)parseFrom_.invoke(binaries);
			return true;
		} catch (Exception e) {
			logger.error("{}", e);
			return false;
		}
	}

	@Override
	public int getSerializedSize() {
		if (this.data == null) return 0;
		return this.data.getSerializedSize();
	}
}
