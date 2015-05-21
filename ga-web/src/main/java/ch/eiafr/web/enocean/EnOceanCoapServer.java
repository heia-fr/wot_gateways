package ch.eiafr.web.enocean;

import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.UUID;
import java.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ws4d.coap.common.CoapMessage;
import org.ws4d.coap.common.CoapRequest;
import org.ws4d.coap.common.CoapResponse;
import org.ws4d.coap.messages.CoapMediaType;
import org.ws4d.coap.messages.CoapResponseCode;
import org.ws4d.coap.server.interfaces.CoapServer;
import org.ws4d.coap.server.interfaces.ServerChannel;

import ch.eiafr.enocean.EnoceanCommunicator;
import ch.eiafr.enocean.IEnoceanCommunicator;
import ch.eiafr.web.enocean.admin.EnOceanConfig;

public class EnOceanCoapServer implements CoapServer {

	private static final Logger logger = LoggerFactory
			.getLogger(EnOceanCoapServer.class);
	private EnOceanStorage enoceanStorage = EnOceanStorage.getInstance();
	private EnOceanRegisters enoceanRegisters = EnOceanRegisters.getInstance();

	@Override
	public CoapServer onAccept(CoapRequest request) {
		logger.debug("Accept CoAP request");
		return this;
	}

	@Override
	public void doGet(ServerChannel channel, CoapRequest request) {
		String responseValue = null;

		EnOceanRequest enoceanRequest = new EnOceanRequest(request);

		CoapMessage response = channel.createResponse(request,
				CoapResponseCode.Content_205);
		switch (enoceanRequest.getRequest()) {
		case Location:
			responseValue = Utils.displayListOfChildren(enoceanStorage,
					enoceanRequest.getLocation()).toString();
			response.setContentFormat(CoapMediaType.json);
			break;
		case EEP:
			responseValue = Utils.displayListOfMeasures(enoceanStorage,
					enoceanRequest.getFunctionality(),
					enoceanRequest.getLocation()).toString();
			response.setContentFormat(CoapMediaType.json);
			break;
		case Value:
			responseValue = Utils.displayActionValue(enoceanStorage,
					enoceanRequest.getFunctionality(),
					enoceanRequest.getLocation(), enoceanRequest.getAction());
			response.setContentFormat(CoapMediaType.text_plain);
			break;
		case Storage:
			logger.debug("DoGet() Storage ");
			Vector<String> uriQuery = request.getUriQuery();
			Hashtable<String, String> options = new Hashtable<String, String>();
			for (int i = 0; i < uriQuery.size(); i++) {
				if (uriQuery.get(i).contains("=")) {
					String[] opts = uriQuery.get(i).split("=");
					options.put(opts[0], opts[1]);
				} else {
					options.put(uriQuery.get(i), null);
				}

			}

			if (options.containsKey("from") && options.containsKey("to")) {
				responseValue = Utils.getStorageByDates(enoceanStorage,
						enoceanRequest.getFunctionality(),
						enoceanRequest.getLocation(),
						enoceanRequest.getAction(), options.get("from"),
						options.get("to")).toString();
			} else if (options.containsKey("days")) {
				responseValue = Utils.getStorageByDays(enoceanStorage,
						enoceanRequest.getFunctionality(),
						enoceanRequest.getLocation(),
						enoceanRequest.getAction(), options.get("days"))
						.toString();
			} else {
				response = channel.createResponse(request,
						CoapResponseCode.Bad_Request_400);
			}
			response.setContentFormat(CoapMediaType.json);
			break;
		default:
			logger.error("Error with type of request");
			break;
		}

		// Have a better display for the urls
		if (responseValue == null) {
			response = channel.createResponse(request,
					CoapResponseCode.Not_Found_404);
		} else {
			responseValue = responseValue.replace("\\/", "/");
			logger.debug(responseValue);
			response.setPayload(responseValue);
		}
		channel.sendMessage(response);

	}

	@Override
	public void doPut(ServerChannel channel, CoapRequest request) {
		EnOceanRequest enoceanRequest = new EnOceanRequest(request);
		String payload = new String(request.getPayload());

		String url = enoceanRequest.getUrl();
		logger.debug("URL: " + url);
		switch (enoceanRequest.getRequest()) {
		case Value:
			logger.debug("Post value");
			try {
				// TODO !!
				/*
				 * knxManagement.writeDatapoint(datapoint, payload);
				 * response.setStatus(HttpServletResponse.SC_OK);
				 */
			} catch (Exception e1) {
				CoapMessage response = channel.createResponse(request,
						CoapResponseCode.Internal_Server_Error_500);
				channel.sendMessage(response);
				return;
			}
			break;
		case Unregister:
			logger.debug("Unregister a callback " + payload);
			try {
				enoceanRegisters.removeRegister(payload, url);
				CoapMessage response = channel.createResponse(request,
						CoapResponseCode.Changed_204);
				channel.sendMessage(response);
			} catch (Exception e) {
				CoapMessage response = channel.createResponse(request,
						CoapResponseCode.Internal_Server_Error_500);
				channel.sendMessage(response);
				return;
			}
			break;
		case Teach:
			try {
				long address = enoceanStorage.getAddress(
						enoceanRequest.getFunctionality(),
						enoceanRequest.getLocation());
				SensorEEP eep = enoceanStorage.getEEP(address);
				IEnoceanCommunicator enoceanCommunicator = EnoceanCommunicator
						.getInstance(EnOceanConfig.getSerialPort(),
								EnOceanConfig.getEepFile());
				enoceanCommunicator.sendLearn(eep.getRORG(), eep.getFunction(),
						eep.getType(), enoceanStorage.getManufacturer(address));
				CoapMessage response = channel.createResponse(request,
						CoapResponseCode.Changed_204);
				channel.sendMessage(response);
			} catch (SQLException e) {
				CoapMessage response = channel.createResponse(request,
						CoapResponseCode.Internal_Server_Error_500);
				channel.sendMessage(response);
				return;
			} catch (Exception e) {
				CoapMessage response = channel.createResponse(request,
						CoapResponseCode.Internal_Server_Error_500);
				channel.sendMessage(response);
				return;
			}
			break;
		default:
			logger.error("Not a POST request");
			CoapMessage response = channel.createResponse(request,
					CoapResponseCode.Method_Not_Allowed_405);
			channel.sendMessage(response);
			return;
		}
	}

	@Override
	public void doPost(ServerChannel channel, CoapRequest request) {
		EnOceanRequest enoceanRequest = new EnOceanRequest(request);
		if (request.getPayload() == null) {
			CoapMessage response = channel.createResponse(request,
					CoapResponseCode.Bad_Request_400);
			channel.sendMessage(response);
			return;
		}
		String payload = new String(request.getPayload());

		int l_idMeasure;
		try {
			l_idMeasure = enoceanStorage.findMeasure(
					enoceanRequest.getFunctionality(),
					enoceanRequest.getLocation(), enoceanRequest.getAction());
		} catch (Exception e) {
			logger.error("Error to find measure", e);
			CoapMessage response = channel.createResponse(request,
					CoapResponseCode.Internal_Server_Error_500);
			channel.sendMessage(response);
			return;
		}

		String url = enoceanRequest.getUrl();
		logger.debug("URL: " + url);
		switch (enoceanRequest.getRequest()) {
		case Storage:
			UUID uuid = UUID.randomUUID();
			String token = uuid.toString();
			ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
			bb.putLong(uuid.getMostSignificantBits());
			bb.putLong(uuid.getLeastSignificantBits());
			logger.debug("Add storage " + payload + " " + token + " "
					+ enoceanRequest.getAction());
			try {
				enoceanStorage.addClient(l_idMeasure,
						Integer.parseInt(payload), token);
				CoapResponse response = channel.createResponse(request,
						CoapResponseCode.Created_201);
				response.setLocationPath(enoceanRequest.getAction() + "/"
						+ token);
				logger.debug("Location path set");
				channel.sendMessage(response);
				logger.debug("Response sent");
			} catch (NumberFormatException e) {
				logger.error("Impossible to parse days in integer");
				CoapMessage response = channel.createResponse(request,
						CoapResponseCode.Bad_Request_400);
				channel.sendMessage(response);
			} catch (SQLException e) {
				logger.error("SQL exception " + e);
				CoapMessage response = channel.createResponse(request,
						CoapResponseCode.Internal_Server_Error_500);
				channel.sendMessage(response);
			}
			break;
		case Register:
			logger.debug("Register a callback " + payload);
			try {
				l_idMeasure = enoceanStorage.findMeasure(
						enoceanRequest.getFunctionality(),
						enoceanRequest.getLocation(),
						enoceanRequest.getAction());
				enoceanRegisters.addRegister(l_idMeasure, payload, url);
				CoapMessage response = channel.createResponse(request,
						CoapResponseCode.Created_201);
				channel.sendMessage(response);
			} catch (Exception e) {
				CoapMessage response = channel.createResponse(request,
						CoapResponseCode.Internal_Server_Error_500);
				channel.sendMessage(response);
				return;
			}
			break;
		default:
			logger.error("Not a PUT request");
			CoapMessage response = channel.createResponse(request,
					CoapResponseCode.Method_Not_Allowed_405);
			channel.sendMessage(response);
			return;
		}
	}

	@Override
	public void doDelete(ServerChannel channel, CoapRequest request) {
		EnOceanRequest enoceanRequest = new EnOceanRequest(request);
		int l_idMeasure;
		try {
			l_idMeasure = enoceanStorage.findMeasure(
					enoceanRequest.getFunctionality(),
					enoceanRequest.getLocation(), enoceanRequest.getAction());
		} catch (Exception e) {
			logger.error("Error to find measure", e);
			CoapMessage response = channel.createResponse(request,
					CoapResponseCode.Internal_Server_Error_500);
			channel.sendMessage(response);
			return;
		}

		String url = enoceanRequest.getUrl();
		String token = enoceanRequest.getToken();
		logger.debug("URL: " + url);
		switch (enoceanRequest.getRequest()) {
		case Storage:
			logger.debug("Remove storage " + token);
			try {
				enoceanStorage.removeClient(l_idMeasure, token);
			} catch (SQLException e) {
				logger.error("SQL exception " + e);
			}
			CoapMessage response = channel.createResponse(request,
					CoapResponseCode.Deleted_202);
			channel.sendMessage(response);
			break;
		default:
			logger.error("Not a DELETE request");
			response = channel.createResponse(request,
					CoapResponseCode.Method_Not_Allowed_405);
			channel.sendMessage(response);
			return;
		}

	}

	@Override
	public void onSeparateResponseFailed(ServerChannel channel) {
		// TODO Auto-generated method stub

	}
}
