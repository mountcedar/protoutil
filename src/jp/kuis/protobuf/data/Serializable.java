package jp.kuis.protobuf.data;

// TODO: 自動生成された Javadoc
/**
 * The Interface Serializable.
 * 
 * @brief the wrapper for the protocol buffer class
 * @details this code is original from -
 *          https://github.com/mountcedar/local.protobuf
 *          .socket/blob/master/src/local/protobuf/socket/data/Serializable.java
 * @author sugiyama
 */
public interface Serializable {

	/**
	 * Serialize.
	 * 
	 * @return the byte[]
	 */
	public byte[] serialize();

	/**
	 * Deserialize.
	 * 
	 * @param binaries
	 *            the binaries
	 * @return true, if successful
	 */
	public boolean deserialize(byte[] binaries);

	/**
	 * Gets the serialized size.
	 * 
	 * @return the serialized size
	 */
	public int getSerializedSize();
}
