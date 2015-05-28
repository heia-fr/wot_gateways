package ch.eiafr.web.knx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.datapoint.Datapoint;
import ch.eiafr.knx.IKNXManagement;
import ch.eiafr.knx.KNXManagement;
import ch.eiafr.knx.utils.ChildDescription;
import ch.eiafr.knx.utils.DatapointDescription;
import ch.eiafr.web.knx.admin.KNXConfig;

/**
 * Default servlet to catch all post and get requests
 * 
 * @author Sebastien Baudin
 * 
 */
public class KNXServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory
			.getLogger(KNXServlet.class);
	private IKNXManagement knxManagement = KNXManagement.getInstance();
	private KNXRegisters knxRegisters = KNXRegisters.getInstance();
	private KNXStorage knxStorage = KNXStorage.getInstance();

	/**
	 * Entry point of a GET request
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String responseValue = null;

		KNXRequest knxRequest = new KNXRequest(request);
		switch (knxRequest.getRequest()) {
		case Location:
			responseValue = this
					.displayListOfChildren(knxRequest.getLocation()).toString();
			break;
		case Datapoint:
			responseValue = this.displayListOfDatapoints(
					knxRequest.getFunctionality(), knxRequest.getLocation())
					.toString();
			break;
		case Value:
			responseValue = this.displayActionValue(
					knxRequest.getFunctionality(), knxRequest.getLocation(),
					knxRequest.getAction());
			break;
		case Storage:
			logger.debug("DoGet() Storage ");
			if (request.getParameter("from") != null
					&& request.getParameter("to") != null) {
				responseValue = this.getStorageByDates(
						knxRequest.getFunctionality(),
						knxRequest.getLocation(), knxRequest.getAction(),
						request.getParameter("from"),
						request.getParameter("to")).toString();
			} else if (request.getParameter("days") != null) {
				responseValue = this.getStorageByDays(
						knxRequest.getFunctionality(),
						knxRequest.getLocation(), knxRequest.getAction(),
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
	 * Entry point of a POST request
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		KNXRequest knxRequest = new KNXRequest(request);
		String payload = null;
		try {
			payload = getPayloadData(request);
		} catch (Exception e) {
			logger.error("Error to get the payload data");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}

		Datapoint datapoint = null;
		try {
			datapoint = knxManagement.findDatapoint(
					knxRequest.getFunctionality(), knxRequest.getLocation(),
					knxRequest.getAction());
		} catch (Exception e) {
			logger.error("Error to find datapoint", e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}

		String url = knxRequest.getUrl();
		logger.debug("URL: " + url);
		switch (knxRequest.getRequest()) {
		case Register:
			logger.debug("Register a callback " + payload);
			try {
				knxRegisters.addRegister(datapoint, payload, url);
				response.setStatus(HttpServletResponse.SC_OK);
			} catch (Exception e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}
			break;
		case Unregister:
			logger.debug("Unregister a callback " + payload);
			try {
				knxRegisters.removeRegister(payload, url);
				response.setStatus(HttpServletResponse.SC_OK);
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
	 * Entry point of a PUT request
	 */
	protected void doPut(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		KNXRequest knxRequest = new KNXRequest(request);
		String referer = request.getHeader("Referer");
		String payload = null;
		try {
			payload = getPayloadData(request);
		} catch (Exception e) {
			logger.error("Error to get the payload data");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}

		Datapoint datapoint = null;
		try {
			datapoint = knxManagement.findDatapoint(
					knxRequest.getFunctionality(), knxRequest.getLocation(),
					knxRequest.getAction());
		} catch (Exception e) {
			logger.error("Error to find datapoint", e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}

		String url = knxRequest.getUrl();
		logger.debug("URL: " + url);
		switch (knxRequest.getRequest()) {
		case Value:
			logger.debug("Put value");
			try {
				knxManagement.writeDatapoint(datapoint, payload);
				response.setStatus(HttpServletResponse.SC_OK);
			} catch (Exception e1) {
				logger.error("Error to write data (" + payload
						+ ") to datapoint " + url);
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}
			break;
		case Storage:
			logger.debug("Add storage " + payload + " " + referer + " "
					+ knxRequest.getAction());
			try {
				knxStorage.addStorage(datapoint, Integer.parseInt(payload),
						referer, knxRequest.getAction());
			} catch (NumberFormatException e) {
				logger.error("Impossible to parse days in integer");
			} catch (SQLException e) {
				logger.error("SQL exception " + e);
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
		KNXRequest knxRequest = new KNXRequest(request);
		String referer = request.getHeader("Referer");
		Datapoint datapoint = null;
		try {
			datapoint = knxManagement.findDatapoint(
					knxRequest.getFunctionality(), knxRequest.getLocation(),
					knxRequest.getAction());
		} catch (Exception e) {
			logger.error("Error to find datapoint", e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}

		String url = knxRequest.getUrl();
		logger.debug("URL: " + url);
		switch (knxRequest.getRequest()) {
		case Storage:
			logger.debug("Remove storage " + referer);
			try {
				knxStorage.removeStorage(datapoint, referer);
			} catch (SQLException e) {
				logger.error("SQL exception " + e);
			}
			break;
		default:
			logger.error("Not a DELETE request");
			response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			return;
		}
	}

	/**
	 * Get storage data for a datapoint by last days
	 * 
	 * @param functionality
	 *            the functionality
	 * @param location
	 *            the location
	 * @param days
	 *            the days to search for
	 * @return Storage data in JSON
	 */
	private JSONArray getStorageByDays(String functionality, String location,
			String action, String days) {
		int l_days = Integer.parseInt(days);
		Datapoint l_dp;
		JSONArray l_result = new JSONArray();
		SimpleDateFormat l_df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			l_dp = knxManagement.findDatapoint(functionality, location, action);

			ArrayList<StorageData> l_sd = knxStorage.getLastStorage(l_dp,
					l_days);
			for (StorageData l_data : l_sd) {
				JSONObject l_jsono = new JSONObject();
				l_jsono.put("value", l_data.getValue());
				l_jsono.put("timestamp", l_df.format(l_data.getDate()));
				l_result.add(l_jsono);
			}
		} catch (Exception e) {
			logger.error(
					"Error to read storage for datapoint with functionality: "
							+ functionality + " & location: " + location
							+ " & action: " + action + " & days: " + l_days, e);
		}

		return l_result;
	}

	/**
	 * Get storage data for a datapoint by dates
	 * 
	 * @param functionality
	 *            the functionality
	 * @param location
	 *            the location
	 * @param from
	 *            the date from
	 * @param to
	 *            the date to
	 * @return Storage data in JSON
	 */
	private JSONArray getStorageByDates(String functionality, String location,
			String action, String from, String to) {
		Datapoint l_dp;
		JSONArray l_result = new JSONArray();
		SimpleDateFormat l_df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		try {
			l_dp = knxManagement.findDatapoint(functionality, location, action);

			ArrayList<StorageData> l_sd = knxStorage.getStorage(l_dp,
					l_df.parse(from), l_df.parse(to));
			for (StorageData l_data : l_sd) {
				JSONObject l_jsono = new JSONObject();
				l_jsono.put("value", l_data.getValue());
				l_jsono.put("timestamp", l_df.format(l_data.getDate()));
				l_result.add(l_jsono);
			}
		} catch (Exception e) {
			logger.error(
					"Error to read storage for datapoint with functionality: "
							+ functionality + " & location: " + location
							+ " & action: " + action + " & from: " + from
							+ " to: " + to, e);
		}

		return l_result;
	}

	/**
	 * Add all datapoints of a functionality in a JSONArray
	 * 
	 * @param functionality
	 *            the functionality
	 * @param location
	 *            the location
	 * @return Datapoints in JSON
	 */
	private JSONArray displayListOfDatapoints(String functionality,
			String location) {
		try {
			List<DatapointDescription> datapoints = knxManagement
					.listDatapoints(functionality, location);

			JSONArray jsonDatapoints = new JSONArray();

			for (DatapointDescription datapoint : datapoints) {
				JSONObject jsonDatapoint = new JSONObject();
				jsonDatapoint.put("datapoint_type", datapoint.getAction());
				jsonDatapoint.put("datapoint_number", datapoint.getId());
				jsonDatapoint.put("datapoint_info",
						datapoint.getDatapointDescription());
				jsonDatapoint.put("description", datapoint.getDescription());
				jsonDatapoint.put("bits_size", datapoint.getBitsSize());
				jsonDatapoint.put("url", functionality + "." + location + "."
						+ KNXConfig.getDNSZone() + "/" + datapoint.getAction().toLowerCase());
				jsonDatapoints.add(jsonDatapoint);
			}

			return jsonDatapoints;

		} catch (Exception e) {
			logger.error("Error to get list datapoints with functionality: "
					+ functionality + " & location: " + location, e);
			return null;
		}

	}

	/**
	 * Get the value of a specified action
	 * 
	 * @param functionality
	 *            the functionality
	 * @param location
	 *            the location
	 * @param action
	 *            the action
	 * @return A string which contains the value of action
	 */
	private String displayActionValue(String functionality, String location,
			String action) {
		String value = null;
		Datapoint datapoint;
		try {
			datapoint = knxManagement.findDatapoint(functionality, location,
					action);
			value = knxManagement.readDatapoint(datapoint);
		} catch (Exception e) {
			logger.error("Error to find datapoint with functionality: "
					+ functionality + " & location: " + location
					+ " & action: " + action, e);
		}

		return value;
	}

	/**
	 * Get all children of a location and add them to a JSONArray
	 * 
	 * @param location
	 *            the location
	 * @return All children in a JSON format
	 */
	private JSONArray displayListOfChildren(String location) {
		JSONArray jsonChildren = new JSONArray();
		try {
			List<ChildDescription> children = knxManagement
					.listChildren(location);
			for (ChildDescription child : children) {
				JSONObject jsonChild = new JSONObject();
				jsonChild.put("name", child.getName());
				jsonChild.put("isGroup", child.isFunctionality());
				if (child.isFunctionality())
					jsonChild.put("url", child.getName() + "." + location + "."
							+ KNXConfig.getDNSZone() + "/*");
				else
					jsonChild.put("url", child.getName() + "." + location + "."
							+ KNXConfig.getDNSZone());
				jsonChildren.add(jsonChild);
			}

			return jsonChildren;
		} catch (Exception e) {
			logger.error("Error to get list datapoints with location: "
					+ location, e);
			return null;
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
