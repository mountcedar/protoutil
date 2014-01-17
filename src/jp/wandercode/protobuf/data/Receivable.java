package jp.wandercode.protobuf.data;

// TODO: 自動生成された Javadoc
/**
 * The Interface Receivable.
 * 
 * @brief the receivable interface for protocol buffer server.
 * @details this code is original from -
 *          https://github.com/mountcedar/local.protobuf
 *          .socket/blob/master/src/local/protobuf/socket/data/Recievable.java
 * @author sugiyama
 */
public interface Receivable {

	/**
	 * On recv.
	 * 
	 * @param data
	 *            the data
	 * @return true, if successful
	 */
	boolean onRecv(Serializable data);
}
