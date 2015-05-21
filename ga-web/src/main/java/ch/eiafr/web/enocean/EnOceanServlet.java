package ch.eiafr.web.enocean;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.eiafr.enocean.EnoceanCommunicator;
import ch.eiafr.enocean.IEnoceanCommunicator;
import ch.eiafr.web.enocean.admin.EnOceanConfig;

public class EnOceanServlet extends HttpServlet {

	/**
	 * Default servlet to catch all post and get requests
	 * 
	 * @author Gerome Bovet
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory
			.getLogger(EnOceanServlet.class);
	private EnOceanRegisters enoceanRegisters = EnOceanRegisters.getInstance();
	private EnOceanStorage enoceanStorage = EnOceanStorage.getInstance();

	/**
	 * Entry point of a GET request
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String responseValue = null;

		EnOceanRequest enoceanRequest = new EnOceanRequest(request);
		switch (enoceanRequest.getRequest()) {
		case Location:
			responseValue = Utils.displayListOfChildren(enoceanStorage,
					enoceanRequest.getLocation()).toString();
			break;
		case EEP:
			responseValue = Utils.displayListOfMeasures(enoceanStorage,
					enoceanRequest.getFunctionality(),
					enoceanRequest.getLocation()).toString();
			break;
		case Value:
			responseValue = Utils.displayActionValue(enoceanStorage,
					enoceanRequest.getFunctionality(),
					enoceanRequest.getLocation(), enoceanRequest.getAction());
			break;
		case Storage:
			logger.debug("DoGet() Storage ");
			if (request.getParameter("from") != null
					&& request.getParameter("to") != null) {
				responseValue = Utils.getStorageByDates(enoceanStorage,
						enoceanRequest.getFunctionality(),
						enoceanRequest.getLocation(),
						enoceanRequest.getAction(),
						request.getParameter("from"),
						request.getParameter("to")).toString();
			} else if (request.getParameter("days") != null) {
				responseValue = Utils.getStorageByDays(enoceanStorage,
						enoceanRequest.getFunctionality(),
						enoceanRequest.getLocation(),
						enoceanRequest.getAction(),
						request.getParameter("days")).toString();
			} else {
				response.setStatus(400);
			}
			break;
		default:
			logger.error("Error with type of request");
			break;
		}

		// Have a better display for the urls
		responseValue = responseValue.replace("\\/", "/");
		logger.debug(responseValue);

		response.setContentType("application/json;charset=UTF-8");
		PrintWriter out = response.getWriter();
		try {
			out.println(responseValue);
		} finally {
			out.close();
		}

	}

	/**
	 * Entry point of a PUT request
	 */
	protected void doPut(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		EnOceanRequest enoceanRequest = new EnOceanRequest(request);
		String payload = null;
		try {
			payload = getPayloadData(request);
		} catch (Exception e) {
			logger.error("Error to get the payload data");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}

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
				logger.error("Error to write data (" + payload
						+ ") to measure " + url);
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}
			break;
		case Unregister:
			logger.debug("Unregister a callback " + payload);
			try {
				enoceanRegisters.removeRegister(payload, url);
				response.setStatus(HttpServletResponse.SC_OK);
			} catch (Exception e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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
				enoceanCommunicator.sendLearn(eep.getRORG(), eep.getFunction(), eep.getType(), enoceanStorage.getManufacturer(address));
			} catch (SQLException e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			} catch (Exception e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}
			break;
		default:
			logger.error("Not a POST request");
			response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			return;
		}

	}

	/**
	 * Entry point of a POST request
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		EnOceanRequest enoceanRequest = new EnOceanRequest(request);
		String payload = null;
		try {
			payload = getPayloadData(request);
		} catch (Exception e) {
			logger.error("Error to get the payload data");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}

		int l_idMeasure;
		try {
			l_idMeasure = enoceanStorage.findMeasure(
					enoceanRequest.getFunctionality(),
					enoceanRequest.getLocation(), enoceanRequest.getAction());
		} catch (Exception e) {
			logger.error("Error to find measure", e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}

		String url = enoceanRequest.getUrl();
		logger.debug("URL: " + url);
		switch (enoceanRequest.getRequest()) {
		case Storage:
			String token = UUID.randomUUID().toString();
			logger.debug("Add storage " + payload + " " + token + " "
					+ enoceanRequest.getAction());
			try {
				enoceanStorage.addClient(l_idMeasure,
						Integer.parseInt(payload), token);
			} catch (NumberFormatException e) {
				logger.error("Impossible to parse days in integer");
			} catch (SQLException e) {
				logger.error("SQL exception " + e);
			}
			response.addHeader("X-Token", token);
			response.setStatus(HttpServletResponse.SC_CREATED);
			break;
		case Register:
			logger.debug("Register a callback " + payload);
			try {
				l_idMeasure = enoceanStorage.findMeasure(
						enoceanRequest.getFunctionality(),
						enoceanRequest.getLocation(),
						enoceanRequest.getAction());
				enoceanRegisters.addRegister(l_idMeasure, payload, url);
				response.setStatus(HttpServletResponse.SC_CREATED);
			} catch (Exception e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}
			break;
		default:
			logger.error("Not a PUT request");
			response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			return;
		}
	}

	/**
	 * Entry point of a DELETE request
	 */
	protected void doDelete(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		EnOceanRequest enoceanRequest = new EnOceanRequest(request);
		
		int l_idMeasure;
		try {
			l_idMeasure = enoceanStorage.findMeasure(
					enoceanRequest.getFunctionality(),
					enoceanRequest.getLocation(), enoceanRequest.getAction());
		} catch (Exception e) {
			logger.error("Error to find measure", e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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
			response.setStatus(HttpServletResponse.SC_OK);
			break;
		default:
			logger.error("Not a DELETE request");
			response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			return;
		}
	}

	/**
	 * Parse the data of the post request to create a string.
	 * 
	 * @param request
	 *            Http request
	 * @return the payload data
	 * @throws Exception
	 *             error when build the string
	 */
	private String getPayloadData(HttpServletRequest request) throws Exception {
		StringBuffer sb = new StringBuffer();
		String line = null;
		try {
			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null)
				sb.append(line);
		} catch (Exception e) {
			logger.error("Error to read the post request", e);
			throw e;
		}

		String payload = sb.toString();
		return payload;
	}

}
