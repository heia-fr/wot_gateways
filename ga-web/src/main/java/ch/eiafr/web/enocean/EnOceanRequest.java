package ch.eiafr.web.enocean;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ws4d.coap.common.CoapRequest;

import ch.eiafr.web.enocean.admin.EnOceanConfig;

public class EnOceanRequest {
	private static final Logger logger = LoggerFactory
			.getLogger(EnOceanRequest.class);

	private String location;
	private String functionality;
	private String action;
	private String url;
	private String token;
	private TypeRequest request;

	public EnOceanRequest(HttpServletRequest httpServletRequest) {
		findActionName(httpServletRequest.getRequestURI());

		switch (request) {
		case EEP:
		case Register:
		case Unregister:
		case Storage:
		case Teach:
		case Value:
			findFunctionality(httpServletRequest.getServerName());
			findLocation(httpServletRequest.getServerName(), true);
			url = functionality + "." + location + "/" + action;
			break;
		case Location:
			findLocation(httpServletRequest.getServerName(), false);
			url = location;
			break;

		}
	}

	public EnOceanRequest(CoapRequest coapRequest) {
		String uriPath = coapRequest.getUriPath() == null ? "/" : coapRequest
				.getUriPath();
		findActionName(uriPath);

		switch (request) {
		case EEP:
		case Register:
		case Unregister:
		case Storage:
		case Teach:
		case Value:
			findFunctionality(coapRequest.getUriHost());
			findLocation(coapRequest.getUriHost(), true);
			url = functionality + "." + location + "/" + action;
			break;
		case Location:
			findLocation(coapRequest.getUriHost(), false);
			url = location;
			break;

		}
	}

	/**
	 * Find what the action is.
	 * 
	 * @param requestUri
	 *            The request URI
	 */
	private void findActionName(String requestUri) {
		logger.debug("RequestURI: " + requestUri);
		logger.debug("RequestURI, substring: "
				+ requestUri.substring(1, requestUri.length()));
		String[] requests = requestUri.substring(1, requestUri.length()).split(
				"/");
		for (int i = 0; i < requests.length; i++)
			logger.debug("Args " + i + " : " + requests[i]);

		logger.debug("Request length: " + requests.length);

		switch (requests.length) {
		case 1:
			if (requests[0].equals("")) {
				action = null;
				request = TypeRequest.Location;
				logger.debug("New location request");
			} else if (requests[0].equals("*")) {
				action = "*";
				request = TypeRequest.EEP;
				logger.debug("New datapoint request");
			} else if (requests[0].equals("teach")) {
				action = requests[0];
				request = TypeRequest.Teach;
				logger.debug("New teach request");
			} else {
				action = requests[0];
				request = TypeRequest.Value;
				logger.debug("New value request");
			}
			break;
		case 2:
			action = requests[0];
			if (requests[1].equals("register")) {
				request = TypeRequest.Register;
				logger.debug("New register request");
			} else if (requests[1].equals("unregister")) {
				request = TypeRequest.Unregister;
				logger.debug("New unregister request");
			} else if (requests[1].equals("history")) {
				request = TypeRequest.Storage;
				logger.debug("New storage request");
			}
			break;
		case 3:
			action = requests[0];
			if (requests[1].equals("history")) {
				request = TypeRequest.Storage;
				token = requests[2];
				logger.debug("New storage request");
			}
		}
	}

	/**
	 * Get the first part of the server name, before the first dot
	 * 
	 * @param servername
	 *            name of server
	 */
	private void findFunctionality(String servername) {
		int index = servername.indexOf(".");
		functionality = servername.substring(0, index);
		logger.debug("Functionality: " + functionality);
	}

	/**
	 * Get the location from the server name. If there isn't functionality, the
	 * servername is the location (without the ch) else the first part of server
	 * is deleted.
	 * 
	 * @param servername
	 *            name of server
	 * @param withFunctionality
	 *            if there is (or not) a functionality
	 */
	private void findLocation(String servername, boolean withFunctionality) {
		int startIndex = 0;
		if (withFunctionality)
			startIndex = servername.indexOf(".") + 1;
		int endIndex = servername.lastIndexOf("." + EnOceanConfig.getDNSZone());
		location = servername.substring(startIndex, endIndex);
		logger.debug("Location: " + location);
	}

	public String getLocation() {
		return location;
	}

	public String getFunctionality() {
		return functionality;
	}

	public String getAction() {
		return action;
	}

	public TypeRequest getRequest() {
		return request;
	}

	public String getUrl() {
		return url;
	}

	public String getToken() {
		return token;
	}

}
