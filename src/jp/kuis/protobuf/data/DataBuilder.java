package jp.kuis.protobuf.data;

// TODO: 自動生成された Javadoc
/**
 * The Interface DataBuilder.
 * 
 * @brief the builder for the serializable objects
 * @details this code is original from -
 *          https://github.com/mountcedar/local.protobuf
 *          .socket/blob/master/src/local/protobuf/socket/data/DataBuilder.java
 * @author sugiyama
 */
public interface DataBuilder {

	/**
	 * Creates the.
	 * 
	 * @param binary
	 *            the binary
	 * @return the serializable
	 */
	public Serializable create(byte[] binary);
}
