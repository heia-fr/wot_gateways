/* Copyright [2011] [University of Rostock]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *****************************************************************************/

/* WS4D Java CoAP Implementation
 * (c) 2011 WS4D.org
 * 
 * written by Sebastian Unger 
 */

package org.ws4d.coap.messages;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ws4d.coap.common.CoapChannel;
import org.ws4d.coap.common.CoapMessage;

/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 */

public abstract class AbstractCoapMessage implements CoapMessage {
	/* use the logger of the channel manager */
	private final static Logger logger = LoggerFactory
			.getLogger(AbstractCoapMessage.class);
	protected static final int HEADER_LENGTH = 4;

	/* Header */
	protected int version;
	protected CoapPacketType packetType;
	protected int messageCodeValue;
	protected int messageId;
	protected byte[] token = {};

	/* Options */
	protected CoapHeaderOptions options = new CoapHeaderOptions();

	/* Payload */
	protected byte[] payload = null;
	protected int payloadLength = 0;

	/* corresponding channel */
	CoapChannel channel = null;

	/* Retransmission State */
	int timeout = 0;
	int retransmissionCounter = 0;

	protected void serialize(byte[] bytes, int length, int offset) {
		/* check length to avoid buffer overflow exceptions */
		this.version = 1;
		this.packetType = (CoapPacketType
				.getPacketType((bytes[offset + 0] & 0x30) >> 4));
		int tokenLength = bytes[offset + 0] & 0x0F;
		this.messageCodeValue = (bytes[offset + 1] & 0xFF);
		this.messageId = ((bytes[offset + 2] << 8) & 0xFF00)
				+ (bytes[offset + 3] & 0xFF);
		this.token = Arrays.copyOfRange(bytes, HEADER_LENGTH, HEADER_LENGTH
				+ tokenLength);

		/* serialize options */
		this.options = new CoapHeaderOptions(bytes, offset + HEADER_LENGTH + tokenLength,
				length);

		/* get and check payload length */
		payloadLength = length - HEADER_LENGTH - tokenLength
				- options.getDeserializedLength();
		if (payloadLength < 0) {
			throw new IllegalStateException(
					"Invaldid CoAP Message (payload length negative)");
		}

		/* copy payload */
		if (payloadLength > 0) {
			int payloadOffset = offset + HEADER_LENGTH + tokenLength
					+ options.getDeserializedLength() + 1;
			payload = new byte[payloadLength - 1];
			for (int i = 0; i < payloadLength - 1; i++) {
				payload[i] = bytes[i + payloadOffset];
			}
		}
	}

	/* TODO: this function should be in another class */
	public static CoapMessage parseMessage(byte[] bytes, int length) {
		return parseMessage(bytes, length, 0);
	}

	public static CoapMessage parseMessage(byte[] bytes, int length, int offset) {
		/*
		 * we "peek" the header to determine the kind of message TODO: duplicate
		 * Code
		 */
		int messageCodeValue = (bytes[offset + 1] & 0xFF);

		if (messageCodeValue == 0) {
			return new CoapEmptyMessage(bytes, length, offset);
		} else if (messageCodeValue >= 0 && messageCodeValue <= 31) {
			return new BasicCoapRequest(bytes, length, offset);
		} else if (messageCodeValue >= 64 && messageCodeValue <= 191) {
			return new BasicCoapResponse(bytes, length, offset);
		} else {
			throw new IllegalArgumentException("unknown CoAP message");
		}
	}

	public int getVersion() {
		return version;
	}

	@Override
	public int getMessageCodeValue() {
		return messageCodeValue;
	}

	@Override
	public CoapPacketType getPacketType() {
		return packetType;
	}

	public byte[] getPayload() {
		return payload;
	}

	public int getPayloadLength() {
		return payloadLength;
	}

	@Override
	public int getMessageID() {
		return messageId;
	}

	@Override
	public void setMessageID(int messageId) {
		this.messageId = messageId;
	}

	public byte[] serialize() {
		/* TODO improve memory allocation */

		/* serialize header options first to get the length */
		int optionsLength = 0;
		byte[] optionsArray = null;
		if (options != null) {
			optionsArray = this.options.serialize();
			optionsLength = this.options.getSerializedLength();
		}

		/* allocate memory for the complete packet */
		int length = HEADER_LENGTH + optionsLength + this.token.length + payloadLength;
		if (payloadLength > 0)
			length++;

		logger.info("options: " + optionsLength + " array: "
				+ optionsArray.length + " payload: " + payloadLength);

		byte[] serializedPacket = new byte[length];

		/* serialize header */
		serializedPacket[0] = (byte) ((this.version & 0x03) << 6);
		serializedPacket[0] |= (byte) ((this.packetType.getValue() & 0x03) << 4);
		serializedPacket[0] |= (byte) (this.token.length & 0x0F);
		serializedPacket[1] = (byte) (this.getMessageCodeValue() & 0xFF);
		serializedPacket[2] = (byte) ((this.messageId >> 8) & 0xFF);
		serializedPacket[3] = (byte) (this.messageId & 0xFF);

		// copy token
		int offset = HEADER_LENGTH;
		int tokenLength = 0;
		if (this.token != null) {
			tokenLength = this.token.length;
			for (int i = 0; i < tokenLength; i++)
				serializedPacket[i + offset] = this.token[i];
			offset += tokenLength;
		}

		/* copy serialized options to the final array */
		if (options != null) {
			for (int i = 0; i < optionsLength; i++)
				serializedPacket[i + offset] = optionsArray[i];
		}
		if (this.payloadLength > 0) {
			offset = HEADER_LENGTH + optionsLength + tokenLength;
			serializedPacket[offset++] = (byte) 0xff;
		}

		/* copy payload to the final array */
		for (int i = 0; i < this.payloadLength; i++) {
			serializedPacket[i + offset] = payload[i];
		}

		return serializedPacket;
	}

	public void setPayload(byte[] payload) {
		this.payload = payload;
		if (payload != null)
			this.payloadLength = payload.length;
		else
			this.payloadLength = 0;
	}

	public void setPayload(char[] payload) {
		this.payload = new byte[payload.length];
		for (int i = 0; i < payload.length; i++) {
			this.payload[i] = (byte) payload[i];
		}
		this.payloadLength = payload.length;
	}

	public void setPayload(String payload) {
		setPayload(payload.toCharArray());
	}

	@Override
	public void setContentFormat(CoapMediaType mediaType) {
		CoapHeaderOption option = options
				.getOption(CoapHeaderOptionType.Content_Format);
		if (option != null) {
			/* content Type MUST only exists once */
			throw new IllegalStateException("added content option twice");
		}

		if (mediaType == CoapMediaType.UNKNOWN) {
			throw new IllegalStateException("unknown content type");
		}
		/* convert value */
		byte[] data = long2CoapUint(mediaType.getValue());
		/* no need to check result, mediaType is safe */
		/* add option to Coap Header */
		options.addOption(new CoapHeaderOption(
				CoapHeaderOptionType.Content_Format, data));
	}

	@Override
	public CoapMediaType getContentFormat() {
		CoapHeaderOption option = options
				.getOption(CoapHeaderOptionType.Content_Format);
		if (option == null) {
			/* not content type TODO: return UNKNOWN ? */
			return null;
		}
		/* no need to check length, CoapMediaType parse function will do */
		int mediaTypeCode = (int) coapUint2Long(options.getOption(
				CoapHeaderOptionType.Content_Format).getOptionData());
		return CoapMediaType.parse(mediaTypeCode);
	}

	@Override
	public byte[] getToken() {
		return token;
	}

	@Override
	public void setToken(byte[] token) {
		if (token == null) {
			return;
		}
		if (token.length > 8) {
			throw new IllegalArgumentException("Invalid Token Length");
		}
		this.token = token;
	}

	@Override
	public CoapBlockOption getBlock1() {
		CoapHeaderOption option = options
				.getOption(CoapHeaderOptionType.Block1);
		if (option == null) {
			return null;
		}

		CoapBlockOption blockOpt = new CoapBlockOption(option.getOptionData());
		return blockOpt;
	}

	@Override
	public void setBlock1(CoapBlockOption blockOption) {
		CoapHeaderOption option = options
				.getOption(CoapHeaderOptionType.Block1);
		if (option != null) {
			// option already exists
			options.removeOption(CoapHeaderOptionType.Block1);
		}
		options.addOption(CoapHeaderOptionType.Block1, blockOption.getBytes());
	}

	@Override
	public CoapBlockOption getBlock2() {
		CoapHeaderOption option = options
				.getOption(CoapHeaderOptionType.Block2);
		if (option == null) {
			return null;
		}
		CoapBlockOption blockOpt = new CoapBlockOption(option.getOptionData());
		return blockOpt;
	}

	@Override
	public void setBlock2(CoapBlockOption blockOption) {
		CoapHeaderOption option = options
				.getOption(CoapHeaderOptionType.Block2);
		if (option != null) {
			// option already exists
			options.removeOption(CoapHeaderOptionType.Block2);
		}
		options.addOption(CoapHeaderOptionType.Block2, blockOption.getBytes());
	}

	@Override
	public Integer getObserveOption() {
		CoapHeaderOption option = options
				.getOption(CoapHeaderOptionType.Observe);
		if (option == null) {
			return null;
		}
		byte[] data = option.getOptionData();

		if (data.length < 0 || data.length > 3) {
			logger.warn("invalid observe option length, return null");
			return null;
		}
		return (int) AbstractCoapMessage.coapUint2Long(data);
	}

	@Override
	public void setObserveOption(int sequenceNumber) {
		CoapHeaderOption option = options
				.getOption(CoapHeaderOptionType.Observe);
		if (option != null) {
			options.removeOption(CoapHeaderOptionType.Observe);
		}

		byte[] data = long2CoapUint(sequenceNumber);

		if (data.length < 0 || data.length > 2) {
			throw new IllegalArgumentException("invalid observe option length");
		}

		options.addOption(CoapHeaderOptionType.Observe, data);
	}

	public void copyHeaderOptions(AbstractCoapMessage origin) {
		options.removeAll();
		options.copyFrom(origin.options);
	}

	public void removeOption(CoapHeaderOptionType optionType) {
		options.removeOption(optionType);
	}

	@Override
	public CoapChannel getChannel() {
		return channel;
	}

	@Override
	public void setChannel(CoapChannel channel) {
		this.channel = channel;
	}

	@Override
	public int getTimeout() {
		if (timeout == 0) {
			Random random = new Random();
			timeout = RESPONSE_TIMEOUT_MS
					+ random.nextInt((int) (RESPONSE_TIMEOUT_MS * RESPONSE_RANDOM_FACTOR)
							- RESPONSE_TIMEOUT_MS);
		}
		return timeout;
	}

	@Override
	public boolean maxRetransReached() {
		if (retransmissionCounter < MAX_RETRANSMIT) {
			return false;
		}
		return true;
	}

	@Override
	public void incRetransCounterAndTimeout() { /* TODO: Rename */
		retransmissionCounter += 1;
		timeout *= 2;
	}

	@Override
	public boolean isReliable() {
		if (packetType == CoapPacketType.NON) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((channel == null) ? 0 : channel.hashCode());
		result = prime * result + getMessageID();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractCoapMessage other = (AbstractCoapMessage) obj;
		if (channel == null) {
			if (other.channel != null)
				return false;
		} else if (!channel.equals(other.channel))
			return false;
		if (getMessageID() != other.getMessageID())
			return false;
		return true;
	}

	protected static long coapUint2Long(byte[] data) {
		/* avoid buffer overflow */
		if (data.length > 8) {
			return -1;
		}

		/* fill with leading zeros */
		byte[] tmp = new byte[8];
		for (int i = 0; i < data.length; i++) {
			tmp[i + 8 - data.length] = data[i];
		}

		/* convert to long */
		ByteBuffer buf = ByteBuffer.wrap(tmp);
		/* byte buffer contains 8 bytes */
		return buf.getLong();
	}

	protected static byte[] long2CoapUint(long value) {
		/* only unsigned values supported */
		if (value < 0) {
			return null;
		}

		/* a zero length value implies zero */
		if (value == 0) {
			return new byte[0];
		}

		/* convert long to byte array with a fixed length of 8 byte */
		ByteBuffer buf = ByteBuffer.allocate(8);
		buf.putLong(value);
		byte[] tmp = buf.array();

		/* remove leading zeros */
		int leadingZeros = 0;
		for (int i = 0; i < tmp.length; i++) {
			if (tmp[i] == 0) {
				leadingZeros = i + 1;
			} else {
				break;
			}
		}
		/* copy to byte array without leading zeros */
		byte[] result = new byte[8 - leadingZeros];
		for (int i = 0; i < result.length; i++) {
			result[i] = tmp[i + leadingZeros];
		}

		return result;
	}

	public enum CoapHeaderOptionType {
		UNKNOWN(-1), If_Match(1), Uri_Host(3), ETag(4), If_None_Match(5), Observe(
				6), Uri_Port(7), Location_Path(8), Uri_Path(11), Content_Format(
				12), Max_Age(14), Uri_Query(15), Accept(17), Location_Query(20), Proxy_Uri(
				35), Proxy_Scheme(39), Size1(60), Block2(23), Block1(27);

		int value;

		CoapHeaderOptionType(int optionValue) {
			value = optionValue;
		}

		public static CoapHeaderOptionType parse(int optionTypeValue) {
			switch (optionTypeValue) {
			case 1:
				return If_Match;
			case 3:
				return Uri_Host;
			case 4:
				return ETag;
			case 5:
				return If_None_Match;
			case 6:
				return Observe;
			case 7:
				return Uri_Port;
			case 8:
				return Location_Path;
			case 11:
				return Uri_Path;
			case 12:
				return Content_Format;
			case 14:
				return Max_Age;
			case 15:
				return Uri_Query;
			case 17:
				return Accept;
			case 20:
				return Location_Query;
			case 23:
				return Block2;
			case 27:
				return Block1;
			case 35:
				return Proxy_Uri;
			case 39:
				return Proxy_Scheme;
			case 60:
				return Size1;
			default:
				return UNKNOWN;
			}
		}

		public int getValue() {
			return value;
		}
		/* TODO: implement validity checks */
		/* TODO: implement isCritical(int optionTypeValue), isElective() */

	}

	protected class CoapHeaderOption implements Comparable<CoapHeaderOption> {

		CoapHeaderOptionType optionType;
		int optionTypeValue; /* integer representation of optionType */
		byte[] optionData;
		int deserializedLength;
		int optionLength = 0;
		static final int MAX_LENGTH = 270;

		public int getDeserializedLength() {
			return deserializedLength;
		}

		public CoapHeaderOption(CoapHeaderOptionType optionType, byte[] value) {
			if (optionType == CoapHeaderOptionType.UNKNOWN) {
				/* TODO: implement check if it is a critical option */
				throw new IllegalStateException("Unknown header option");
			}
			if (value == null) {
				throw new IllegalArgumentException(
						"Header option value MUST NOT be null");
			}

			this.optionTypeValue = optionType.getValue();
			this.optionData = value;
		}

		public CoapHeaderOption(byte[] bytes, int offset, int lastOptionNumber) {
			int headerLength = 1;
			/* parse option type */
			int delta = (bytes[offset] & 0xF0) >> 4;
			if (delta == 13) {
				delta = bytes[offset + 1] + 13;
				headerLength += 1;
			} else if (delta == 14) {
				delta = bytes[offset + 1] << 8 + bytes[offset + 2] + 269;
				headerLength += 2;
			} else if(delta == 15) {
				/* TODO: implement check if it is a critical option */
				throw new IllegalArgumentException("Unknown header option");
			}

			optionTypeValue = delta + lastOptionNumber;
			optionType = CoapHeaderOptionType.parse(optionTypeValue);
			if (optionType == CoapHeaderOptionType.UNKNOWN) {
				if (optionTypeValue % 14 == 0) {
					/* no-op: no operation for deltas > 14 */
				} else {
					/* TODO: implement check if it is a critical option */
					throw new IllegalArgumentException("Unknown header option");
				}
			}
			/* parse length */
			optionLength = bytes[offset] & 0x0F;
			if (optionLength == 13) {
				optionLength = bytes[offset + headerLength] + 13;
				headerLength += 1; /* additional length byte */
			} else if (optionLength == 14) {
				optionLength = bytes[offset +  headerLength] << 8 + bytes[offset
						+ 1 + headerLength] + 269;
				headerLength += 2; /* additional length byte */
			} else if(optionLength == 15) {
				/* TODO: implement check if it is a critical option */
				throw new IllegalArgumentException("Unknown header option");
			}

			/* copy value */
			optionData = new byte[optionLength];
			for (int i = 0; i < optionLength; i++) {
				optionData[i] = bytes[i + headerLength + offset];
			}

			deserializedLength += headerLength + optionLength;
		}

		@Override
		public int compareTo(CoapHeaderOption option) {
			/*
			 * compare function for sorting TODO: check what happens in case of
			 * equal option values IMPORTANT: order must be the same for e.g.,
			 * URI path
			 */
			if (this.optionTypeValue != option.optionTypeValue)
				return this.optionTypeValue < option.optionTypeValue ? -1 : 1;
			else
				return 0;
		}

		public int getOptionTypeValue() {
			return optionTypeValue;
		}

		public byte[] getOptionData() {
			return optionData;
		}

		public byte[] getOptionLength() {
			if (optionData == null) {
				logger.debug("Option data null");
				return new byte[] { 0 };
			} else if (optionData.length < 13) {
				logger.debug("Option data " + optionData.length);
				return new byte[] { (byte) optionData.length };
			} else if (optionData.length < 269) {
				return new byte[] { (byte) 13, (byte) (optionData.length - 13) };
			} else {
				return new byte[] { (byte) 14,
						(byte) ((optionData.length - 13) >> 8),
						(byte) (optionData.length - 13) };
			}
		}

		public int getSerializeLength() {
			int length = optionData.length + 1;
			if (length >= 13 && length < 269) {
				length += 1;
			} else if(length >= 269) {
				length += 2;
			}
			return length;
		}

		@Override
		public String toString() {
			char[] printableOptionValue = new char[optionData.length];
			for (int i = 0; i < optionData.length; i++)
				printableOptionValue[i] = (char) optionData[i];
			return "Option Number: " + " (" + optionTypeValue + ")"
					+ ", Option Value: "
					+ String.copyValueOf(printableOptionValue);
		}

		public CoapHeaderOptionType getOptionType() {
			return optionType;
		}
	}

	protected class CoapHeaderOptions implements Iterable<CoapHeaderOption> {

		private Vector<CoapHeaderOption> headerOptions = new Vector<CoapHeaderOption>();
		private int deserializedLength;
		private int serializedLength = 0;

		public CoapHeaderOptions(byte[] bytes, int offset, int length) {
			/* note: we only receive deltas and never concrete numbers */
			/* TODO: check integrity */
			deserializedLength = 0;
			int lastOptionNumber = 0;
			int optionOffset = offset;
			while (bytes[optionOffset] != (byte) 0xff && optionOffset < length) {
				CoapHeaderOption option = new CoapHeaderOption(bytes,
						optionOffset, lastOptionNumber);
				lastOptionNumber = option.getOptionTypeValue();
				deserializedLength += option.getDeserializedLength();
				optionOffset += option.getDeserializedLength();
				addOption(option);
			}
		}

		public CoapHeaderOptions() {
			/* creates empty header options */
		}

		public CoapHeaderOption getOption(int optionNumber) {
			for (CoapHeaderOption headerOption : headerOptions) {
				if (headerOption.getOptionTypeValue() == optionNumber) {
					return headerOption;
				}
			}
			return null;
		}

		public CoapHeaderOption getOption(CoapHeaderOptionType optionType) {
			for (CoapHeaderOption headerOption : headerOptions) {
				if (headerOption.getOptionType() == optionType) {
					return headerOption;
				}
			}
			return null;
		}

		public boolean optionExists(CoapHeaderOptionType optionType) {
			CoapHeaderOption option = getOption(optionType);
			if (option == null) {
				return false;
			} else
				return true;
		}

		public void addOption(CoapHeaderOption option) {
			headerOptions.add(option);
			/* TODO: only sort when options are serialized */
			Collections.sort(headerOptions);
		}

		public void addOption(CoapHeaderOptionType optionType, byte[] value) {
			addOption(new CoapHeaderOption(optionType, value));
		}

		public void removeOption(CoapHeaderOptionType optionType) {
			CoapHeaderOption headerOption;
			// get elements of Vector

			/*
			 * note: iterating over and changing a vector at the same time is
			 * not allowed
			 */
			int i = 0;
			while (i < headerOptions.size()) {
				headerOption = headerOptions.get(i);
				if (headerOption.getOptionType() == optionType) {
					headerOptions.remove(i);
				} else {
					/* only increase when no element was removed */
					i++;
				}
			}
			Collections.sort(headerOptions);
		}

		public void removeAll() {
			headerOptions.clear();
		}

		public void copyFrom(CoapHeaderOptions origin) {
			for (CoapHeaderOption option : origin) {
				addOption(option);
			}
		}

		public int getOptionCount() {
			return headerOptions.size();
		}

		public byte[] serialize() {
			/*
			 * options are serialized here to be more efficient (only one byte
			 * array necessary)
			 */
			int length = 0;

			/* calculate the overall length first */
			for (CoapHeaderOption option : headerOptions) {
				length += option.getSerializeLength();
			}
		
			byte[] data = new byte[length];
			int arrayIndex = 0;

			int lastOptionNumber = 0; /* let's keep track of this */
			for (CoapHeaderOption headerOption : headerOptions) {
				/*
				 * TODO: move the serialization implementation to
				 * CoapHeaderOption
				 */
				int optionDelta = headerOption.getOptionTypeValue()
						- lastOptionNumber;
				lastOptionNumber = headerOption.getOptionTypeValue();
				// set length(s)
				byte[] optionLength = headerOption.getOptionLength();
				data[arrayIndex++] = (byte) (((optionDelta & 0x0F) << 4) | (optionLength[0] & 0x0F));
				for (int i = 1; i < optionLength.length; i++) {
					data[arrayIndex++] = optionLength[i];
				}
				// copy option value
				byte[] value = headerOption.getOptionData();
				if (value != null)
					for (int i = 0; i < value.length; i++) {
						data[arrayIndex++] = value[i];
					}
			}
			serializedLength = length;
			return data;
		}

		public int getDeserializedLength() {
			return deserializedLength;
		}

		public int getSerializedLength() {
			return serializedLength;
		}

		@Override
		public Iterator<CoapHeaderOption> iterator() {
			return headerOptions.iterator();
		}

		@Override
		public String toString() {
			String result = "\tOptions:\n";
			for (CoapHeaderOption option : headerOptions) {
				result += "\t\t" + option.toString() + "\n";
			}
			return result;
		}
	}

}
