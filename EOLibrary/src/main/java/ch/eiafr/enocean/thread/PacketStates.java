package ch.eiafr.enocean.thread;
/**
 * States of the incoming telegram parsing
 * @author gb
 *
 */
public enum PacketStates {
	GET_SYNC_STATE, GET_HEADER_STATE, CHECK_CRC8H_STATE, GET_DATA_STATE, CHECK_CRC8D_STATE
}
