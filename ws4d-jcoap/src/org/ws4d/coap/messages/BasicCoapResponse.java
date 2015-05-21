package org.ws4d.coap.messages;

import java.io.UnsupportedEncodingException;

import org.ws4d.coap.common.CoapResponse;

/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 */

public class BasicCoapResponse extends AbstractCoapMessage implements
		CoapResponse {
	CoapResponseCode responseCode;

	public BasicCoapResponse(byte[] bytes, int length) {
		this(bytes, length, 0);
	}

	public BasicCoapResponse(byte[] bytes, int length, int offset) {
		serialize(bytes, length, offset);
		/*
		 * check if response code is valid, this function throws an error in
		 * case of an invalid argument
		 */
		responseCode = CoapResponseCode
				.parseResponseCode(this.messageCodeValue);

		// TODO: check integrity of header options
	}

	/* token can be null */
	public BasicCoapResponse(CoapPacketType packetType,
			CoapResponseCode responseCode, int messageId, byte[] requestToken) {
		this.version = 1;

		this.packetType = packetType;

		this.responseCode = responseCode;
		if (responseCode == CoapResponseCode.UNKNOWN) {
			throw new IllegalArgumentException(
					"UNKNOWN Response Code not allowed");
		}
		this.messageCodeValue = responseCode.getValue();

		this.messageId = messageId;

		setToken(requestToken);
	}

	@Override
	public CoapResponseCode getResponseCode() {
		return responseCode;
	}

	@Override
	public void setMaxAge(int maxAge) {
		if (options.optionExists(CoapHeaderOptionType.Max_Age)) {
			throw new IllegalStateException("Max Age option already exists");
		}
		if (maxAge < 0) {
			throw new IllegalStateException("Max Age MUST be an unsigned value");
		}
		options.addOption(CoapHeaderOptionType.Max_Age, long2CoapUint(maxAge));
	}

	@Override
	public long getMaxAge() {
		CoapHeaderOption option = options
				.getOption(CoapHeaderOptionType.Max_Age);
		if (option == null) {
			return -1;
		}
		return coapUint2Long((options.getOption(CoapHeaderOptionType.Max_Age)
				.getOptionData()));
	}

	@Override
	public void setETag(byte[] etag) {
		if (etag == null) {
			throw new IllegalArgumentException("etag MUST NOT be null");
		}
		if (etag.length < 1 || etag.length > 8) {
			throw new IllegalArgumentException("Invalid etag length");
		}
		options.addOption(CoapHeaderOptionType.ETag, etag);
	}

	@Override
	public byte[] getETag() {
		CoapHeaderOption option = options.getOption(CoapHeaderOptionType.ETag);
		if (option == null) {
			return null;
		}
		return option.getOptionData();
	}

	@Override
	public boolean isRequest() {
		return false;
	}

	@Override
	public boolean isResponse() {
		return true;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public String toString() {
		return packetType.toString() + ", " + responseCode.toString()
				+ ", MsgId: " + getMessageID() + ", #Options: "
				+ options.getOptionCount();
	}

	@Override
	public void setResponseCode(CoapResponseCode responseCode) {
		if (responseCode != CoapResponseCode.UNKNOWN) {
			this.responseCode = responseCode;
			this.messageCodeValue = responseCode.getValue();
		}
	}

	@Override
	public void setLocationPath(String path) {
		if (path == null)
			return;

		if (path.length() > CoapHeaderOption.MAX_LENGTH) {
			throw new IllegalArgumentException("Uri-Path option too long");
		}

		/* delete old options if present */
		options.removeOption(CoapHeaderOptionType.Location_Path);

		/*create substrings */
		String[] pathElements = path.split("/"); 
		/* add a Uri Path option for each part */
		for (String element : pathElements) {
			/* check length */
			if(element.length() < 0 || element.length() > CoapHeaderOption.MAX_LENGTH){
				throw new IllegalArgumentException("Invalid Location-Path");
			} else if (element.length() > 0){
				/* ignore empty substrings */
				options.addOption(CoapHeaderOptionType.Location_Path, element.getBytes());
			}
		}
	}

	@Override
	public String getLocationPath() {
		if (options.getOption(CoapHeaderOptionType.Location_Path) == null){
    		return null;
    	}
    	
		StringBuilder locationPathBuilder = new StringBuilder();
		for (CoapHeaderOption option : options) {
			if (option.getOptionType() == CoapHeaderOptionType.Location_Path) {
				String locationPathElement;
				try {
					locationPathElement = new String(option.getOptionData(), "UTF-8");
					locationPathBuilder.append("/");
					locationPathBuilder.append(locationPathElement);
				} catch (UnsupportedEncodingException e) {
					throw new IllegalArgumentException("Invalid Encoding");
				}
			}
		}
		return locationPathBuilder.toString();
	}
}
