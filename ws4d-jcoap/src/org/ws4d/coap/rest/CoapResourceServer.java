package org.ws4d.coap.rest;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.ws4d.coap.Constants;
import org.ws4d.coap.common.CoapMessage;
import org.ws4d.coap.common.CoapRequest;
import org.ws4d.coap.messages.CoapResponseCode;
import org.ws4d.coap.server.BasicServerChannelManager;
import org.ws4d.coap.server.interfaces.CoapServer;
import org.ws4d.coap.server.interfaces.ServerChannel;
import org.ws4d.coap.server.interfaces.ServerChannelManager;

/**
 * @author Christian Lerche <christian.lerche@uni-rostock.de>
 * @author Gerome Bovet <gerome.bovet@hefr.ch>
 */

public class CoapResourceServer implements CoapServer, ResourceServer {
	private int port = 0;
	private final static Logger logger = Logger
			.getLogger(CoapResourceServer.class);

	protected HashMap<String, Resource> resources = new HashMap<String, Resource>();
	private CoreResource coreResource = new CoreResource(this);

	public CoapResourceServer() {
		logger.addAppender(new ConsoleAppender(new SimpleLayout()));
		logger.setLevel(Level.WARN);
	}

	public HashMap<String, Resource> getResources() {
		return resources;
	}

	private void addResource(Resource resource) {
		resource.registerServerListener(this);
		resources.put(resource.getPath(), resource);
		coreResource.registerResource(resource);
	}

	@Override
	public boolean createResource(Resource resource) {
		if (resource == null)
			return false;
		if (!resources.containsKey(resource.getPath())) {
			addResource(resource);
			logger.info("created resource: " + resource.getPath());
			return true;
		} else
			return false;
	}

	@Override
	public boolean updateResource(Resource resource) {
		if (resource == null)
			return false;
		if (resources.containsKey(resource.getPath())) {
			addResource(resource);
			logger.info("updated resource: " + resource.getPath());
			return true;
		} else
			return false;
	}

	@Override
	public boolean deleteResource(String path) {
		if (null != resources.remove(path)) {
			logger.info("deleted resource: " + path);
			return true;
		} else
			return false;
	}

	@Override
	public final Resource readResource(String path) {
		logger.info("read resource: " + path);
		return resources.get(path);
	}

	/* corresponding to the coap spec the put is an update or create (or error) */
	public CoapResponseCode CoapResponseCode(Resource resource) {
		Resource res = readResource(resource.getPath());
		// TODO: check results
		if (res == null) {
			createResource(resource);
			return CoapResponseCode.Created_201;
		} else {
			updateResource(resource);
			return CoapResponseCode.Changed_204;
		}
	}

	@Override
	public void start() throws Exception {
		start(Constants.COAP_DEFAULT_PORT);
	}

	public void start(int port) throws Exception {
		resources.put(coreResource.getPath(), coreResource);
		ServerChannelManager channelManager = BasicServerChannelManager
				.getInstance();
		this.port = port;
		channelManager.createServerListener(this, port);
	}

	@Override
	public void stop() {
	}

	public int getPort() {
		return port;
	}

	@Override
	public URI getHostUri() {
		URI hostUri = null;
		try {
			hostUri = new URI("coap://" + this.getLocalIpAddress() + ":"
					+ getPort());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return hostUri;
	}

	@Override
	public void resourceChanged(Resource resource) {
		logger.info("Resource changed: " + resource.getPath());
	}

	@Override
	public CoapServer onAccept(CoapRequest request) {
		return this;
	}

	private CoapResource parseRequest(CoapRequest request) {
		CoapResource resource = new BasicCoapResource(request.getUriPath(),
				request.getPayload(), request.getContentFormat());
		// TODO add content type
		return resource;
	}

	@Override
	public void onSeparateResponseFailed(ServerChannel channel) {
		logger.error("Separate response failed but server never used separate responses");

	}

	protected String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
		}
		return null;
	}

	@Override
	public void doGet(ServerChannel channel, CoapRequest request) {
		CoapMessage response = null;
		String targetPath = request.getUriPath();

		// TODO make this cast safe (send internal server error if it is not a
		// CoapResource)
		CoapResource resource = (CoapResource) readResource(targetPath);
		if (resource != null) {
			// URI queries
			Vector<String> uriQueries = request.getUriQuery();
			final byte[] responseValue;
			if (uriQueries != null) {
				responseValue = resource.getValue(uriQueries);
			} else {
				responseValue = resource.getValue();
			}
			response = channel.createResponse(request,
					CoapResponseCode.Content_205, resource.getCoapMediaType());
			response.setPayload(responseValue);

			if (request.getObserveOption() != null) {
				if (request.getObserveOption() == 0) {
					/* client wants to observe this resource */
					if (resource.addObserver(request)) {
						/* successfully added observer */
						System.out.println("Observer " + request.getChannel().getRemoteAddress().getHostAddress() + " on resource " + request.getUriPath());
						response.setObserveOption(resource
								.getObserveSequenceNumber());
					}
				}else if(request.getObserveOption() == 1){
					resource.removeObserver(channel);
				}
			}

		} else {
			response = channel.createResponse(request,
					CoapResponseCode.Not_Found_404);
		}
		channel.sendMessage(response);
	}

	@Override
	public void doPut(ServerChannel channel, CoapRequest request) {
		CoapMessage response = null;
		String targetPath = request.getUriPath();

		// TODO make this cast safe (send internal server error if it is not a
		// CoapResource)
		CoapResource resource = (CoapResource) readResource(targetPath);
		if (resource == null) {
			/* create */
			createResource(parseRequest(request));
			response = channel.createResponse(request,
					CoapResponseCode.Created_201);
		} else {
			/* update */
			updateResource(parseRequest(request));
			response = channel.createResponse(request,
					CoapResponseCode.Changed_204);
		}
		channel.sendMessage(response);
	}

	@Override
	public void doPost(ServerChannel channel, CoapRequest request) {
		CoapMessage response = null;
		String targetPath = request.getUriPath();

		// TODO make this cast safe (send internal server error if it is not a
		// CoapResource)
		CoapResource resource = (CoapResource) readResource(targetPath);
		if (resource != null) {
			resource.post(request.getPayload());
			response = channel.createResponse(request,
					CoapResponseCode.Changed_204);
		} else {
			/* if the resource does not exist, a new resource will be created */
			createResource(parseRequest(request));
			response = channel.createResponse(request,
					CoapResponseCode.Created_201);
		}
		channel.sendMessage(response);
	}

	@Override
	public void doDelete(ServerChannel channel, CoapRequest request) {
		CoapMessage response = null;
		String targetPath = request.getUriPath();

		// TODO make this cast safe (send internal server error if it is not a
		// CoapResource)
		deleteResource(targetPath);
		response = channel
				.createResponse(request, CoapResponseCode.Deleted_202);
		channel.sendMessage(response);
	}
}
