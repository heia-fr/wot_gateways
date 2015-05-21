package ch.eiafr.web.enocean.admin;

import java.io.IOException;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.ZoneTransferException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.eiafr.enocean.EnoceanCommunicator;
import ch.eiafr.enocean.IEnoceanCommunicator;
import ch.eiafr.enocean.eep.EEPField;
import ch.eiafr.enocean.telegram.LearnTelegram;
import ch.eiafr.web.dns.DNSUtils;
import ch.eiafr.web.enocean.admin.EnOceanConfig;
import ch.eiafr.web.enocean.EnOceanDispatcher;
import ch.eiafr.web.enocean.EnOceanStorage;
import ch.eiafr.web.enocean.admin.Group;
import ch.eiafr.web.enocean.admin.Location;
import ch.eiafr.web.enocean.admin.Manufacturer;
import ch.eiafr.web.enocean.admin.Measure;
import ch.eiafr.web.enocean.admin.Sensor;
import ch.eiafr.web.enocean.admin.User;

@Path("/")
public class RestInterface {
	private static final int TOKEN_DURATION = 60; // minutes
	private static String m_Token = null;
	private static Date m_TokenTime = null;

	private static final Logger logger = LoggerFactory
			.getLogger(RestInterface.class);
	private static final ObjectMapper m_mapper = new ObjectMapper();

	/**
	 * Verify the username and the password and generate the X-Token
	 * 
	 * @param payload
	 *            The request body
	 * @return Status code (200 or 401)
	 * @throws SQLException
	 * @throws JsonParseException
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	@POST
	@Path("login")
	public Response getXToken(String payload) throws SQLException,
			JsonParseException, IOException, NoSuchAlgorithmException {
		// parse the JSON to get the username and the password
		JsonFactory jfactory = new JsonFactory();
		JsonParser jParser = jfactory.createParser(payload);
		String l_username = "";
		String l_password = "";

		String l_jsonFieldName = new String();
		while (jParser.nextToken() != JsonToken.END_OBJECT) {
			l_jsonFieldName = jParser.getCurrentName();
			if ("username".equals(l_jsonFieldName)) {
				jParser.nextToken();
				l_username = jParser.getText();
			}
			if ("password".equals(l_jsonFieldName)) {
				jParser.nextToken();
				l_password = jParser.getText();
			}
		}
		jParser.close();

		// get the user info
		EnOceanStorage l_storage = EnOceanStorage.getInstance();
		User l_user = l_storage.getUserFromUsername(l_username);
		if (l_user == null)
			return Response.status(Status.UNAUTHORIZED).build();

		// convert the given password with sha1
		MessageDigest l_pwd = MessageDigest.getInstance("SHA1");
		l_pwd.update(l_password.getBytes());
		String l_passwordEncrypted = new BigInteger(1, l_pwd.digest())
				.toString(16);

		// check the password and if the user is active
		if (!(l_passwordEncrypted.equals(l_user.getPassword()) && l_user
				.isActive() == true)) {
			return Response.status(Status.UNAUTHORIZED).build();
		}

		// if the password is correct and that the user is active, we generate
		// the X-Token
		m_Token = UUID.randomUUID().toString();
		m_TokenTime = new Date();
		return Response.status(200).header("X-Token", m_Token).build();
	}

	/**
	 * Logout
	 * 
	 * @param p_request
	 *            The request header
	 * @return
	 */
	@POST
	@Path("logout")
	public Response logout(@Context HttpServletRequest p_request) {
		m_Token = UUID.randomUUID().toString();

		return Response.status(checkXToken(p_request)).entity(null).build();
	}

	/**
	 * Check the validity of the X-Token given into the request body
	 * 
	 * @param p_request
	 *            The request header
	 * @return Status code (200 or 401)
	 */
	public Status checkXToken(HttpServletRequest p_request) {
		// get the X-Token from the request body
		String l_xToken = p_request.getHeader("X-Token");

		// check the validity of the X-Token. If it's not valid, we return the
		// code 401
		if (!l_xToken.equals(m_Token)
				|| (new Date().getTime() - m_TokenTime.getTime()) / (60 * 1000) > TOKEN_DURATION) {
			return Status.UNAUTHORIZED;
		}
		return Status.ACCEPTED;
	}

	/**
	 * Return the Enocean configuration
	 * 
	 * @return JSON object : DNSIp, DNSZone
	 * @throws IOException
	 * @throws JsonProcessingException
	 */
	@GET
	@Path("config")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getConfig(@Context HttpServletRequest p_request)
			throws JsonProcessingException, IOException {
		// create the JSON manually
		String l_output = "{\"DNSIP\":\"" + EnOceanConfig.getDNSIP()
				+ "\",\"DNSZone\":\"" + EnOceanConfig.getDNSZone() + "\"}";

		return Response.status(checkXToken(p_request)).entity(l_output).build();
	}

	/**
	 * Update the configuration
	 * 
	 * @param payload
	 *            The request body
	 * @param p_request
	 *            The request header
	 * @return Status code (200 or 401)
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@POST
	@Path("config")
	public Response modifyConfig(String payload,
			@Context HttpServletRequest p_request) throws JsonParseException,
			JsonMappingException, IOException {
		JsonFactory jfactory = new JsonFactory();
		JsonParser jParser = jfactory.createParser(payload);

		String l_jsonFieldName = new String();
		while (jParser.nextToken() != JsonToken.END_OBJECT) {
			l_jsonFieldName = jParser.getCurrentName();
			if ("DNSIP".equals(l_jsonFieldName)) {
				jParser.nextToken();
				EnOceanConfig.setDNSIP(jParser.getText());
			}
			if ("DNSZone".equals(l_jsonFieldName)) {
				jParser.nextToken();
				EnOceanConfig.setDNSZone(jParser.getText());
			}
		}
		jParser.close();
		EnOceanConfig.writeConfig();

		return Response.status(checkXToken(p_request)).entity(null).build();
	}

	/**
	 * Return all the registered users
	 * 
	 * @param p_request
	 *            The request header
	 * @return JSON array : id, username, password, active, firstName, lastName,
	 *         email, admin
	 * @throws SQLException
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@GET
	@Path("users")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUsers(@Context HttpServletRequest p_request)
			throws SQLException, JsonGenerationException, JsonMappingException,
			IOException {
		EnOceanStorage l_storage = EnOceanStorage.getInstance();
		ArrayList<User> l_users = l_storage.getUsers();
		StringBuffer l_output = new StringBuffer();

		l_output.append("[");
		for (int i = 0; i < l_users.size(); i++) {
			
			l_output.append(m_mapper.writeValueAsString(l_users.get(i)));
			if (i < (l_users.size() - 1)) {
				l_output.append(',');
			}
		}
		l_output.append("]");

		return Response.status(checkXToken(p_request))
				.entity(l_output.toString()).build();
	}

	/**
	 * Return the specified user
	 * 
	 * @param p_id
	 *            The user id
	 * @param p_request
	 *            The request header
	 * @return JSON object : id, username, password, active, firstName,
	 *         lastName, email, admin
	 * @throws SQLException
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@GET
	@Path("user/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserFromId(@PathParam("id") int p_id,
			@Context HttpServletRequest p_request) throws SQLException,
			JsonGenerationException, JsonMappingException, IOException {
		EnOceanStorage l_storage = EnOceanStorage.getInstance();
		User l_user = l_storage.getUserFromId(p_id);
		StringBuffer l_output = new StringBuffer();

		l_output.append(m_mapper.writeValueAsString(l_user));

		return Response.status(checkXToken(p_request))
				.entity(l_output.toString()).build();
	}

	/**
	 * Return the specified user
	 * 
	 * @param p_username
	 *            The user username
	 * 
	 * @return JSON object : id, username, password, active, firstName,
	 *         lastName, email, admin
	 * @throws SQLException
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@GET
	@Path("user/username/{username}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUser(@PathParam("username") String p_username)
			throws SQLException, JsonGenerationException, JsonMappingException,
			IOException {
		EnOceanStorage l_storage = EnOceanStorage.getInstance();
		User l_user = l_storage.getUserFromUsername(p_username);
		StringBuffer l_output = new StringBuffer();

		l_output.append(m_mapper.writeValueAsString(l_user));

		return Response.status(200).entity(l_output.toString()).build();
	}

	/**
	 * Create a new user
	 * 
	 * @param payload
	 *            The request body
	 * @param p_request
	 *            The request header
	 * @Return Status code (200 0r 401)
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 * @throws SQLException
	 * @throws NoSuchAlgorithmException
	 */
	@PUT
	@Path("user")
	public Response addUser(String payload,
			@Context HttpServletRequest p_request) throws JsonParseException,
			JsonMappingException, IOException, SQLException,
			NoSuchAlgorithmException {
		EnOceanStorage l_storage = EnOceanStorage.getInstance();
		User l_user = m_mapper.readValue(payload, User.class);

		// convert the given password with sha1
		MessageDigest l_pwd = MessageDigest.getInstance("SHA1");
		l_pwd.update(l_user.getPassword().getBytes());
		String l_passwordEncrypted = new BigInteger(1, l_pwd.digest())
				.toString(16);
		l_user.setPassword(l_passwordEncrypted);

		l_storage.addUser(l_user);

		return Response.status(checkXToken(p_request)).entity(null).build();
	}

	/**
	 * Update the specified user
	 * 
	 * @param payload
	 *            The request body
	 * @param p_id
	 *            The user id
	 * @param p_request
	 *            The request header
	 * @return Status code (200 or 401)
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 * @throws SQLException
	 * @throws NoSuchAlgorithmException
	 */
	@POST
	@Path("user/{id}")
	public Response modifyUser(String payload, @PathParam("id") int p_id,
			@Context HttpServletRequest p_request) throws JsonParseException,
			JsonMappingException, IOException, SQLException,
			NoSuchAlgorithmException {
		User l_user = m_mapper.readValue(payload, User.class);
		EnOceanStorage l_storage = EnOceanStorage.getInstance();

		// convert the given password with sha1
		if (!l_user.getPassword().matches("[a-fA-F0-9]{40}")) {
			MessageDigest l_pwd = MessageDigest.getInstance("SHA1");
			l_pwd.update(l_user.getPassword().getBytes());
			String l_passwordEncrypted = new BigInteger(1, l_pwd.digest())
					.toString(16);
			l_user.setPassword(l_passwordEncrypted);
		}

		l_storage.updateUser(l_user, p_id);

		return Response.status(checkXToken(p_request)).entity(null).build();
	}

	/**
	 * Delete the specified user
	 * 
	 * @param p_id
	 *            The user id
	 * @param p_request
	 *            The request header
	 * @return Status code (200 or 401)
	 * @throws SQLException
	 */
	@DELETE
	@Path("user/{id}")
	public Response deleteUser(@PathParam("id") int p_id,
			@Context HttpServletRequest p_request) throws SQLException {
		EnOceanStorage l_storage = EnOceanStorage.getInstance();

		l_storage.deleteUser(p_id);

		return Response.status(checkXToken(p_request)).entity(null).build();
	}

	/**
	 * Return all the registered groups
	 * 
	 * @param p_request
	 *            The request header
	 * @return JSON array : id, name, description, lastModifier,
	 *         lastModification
	 * @throws SQLException
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@GET
	@Path("groups")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getGroups(@Context HttpServletRequest p_request)
			throws SQLException, JsonGenerationException, JsonMappingException,
			IOException {
		EnOceanStorage l_storage = EnOceanStorage.getInstance();
		ArrayList<Group> l_groups = l_storage.getGroups();
		StringBuffer l_output = new StringBuffer();

		l_output.append("[");
		for (int i = 0; i < l_groups.size(); i++) {
			l_output.append(m_mapper.writeValueAsString(l_groups.get(i)));
			if (i < (l_groups.size() - 1)) {
				l_output.append(',');
			}
		}
		l_output.append("]");

		return Response.status(checkXToken(p_request))
				.entity(l_output.toString()).build();
	}

	/**
	 * Return the specified group
	 * 
	 * @param p_id
	 *            The group ID
	 * @param p_request
	 *            The request header
	 * @return JSON object : id, name, description, lastModifier,
	 *         lastModification
	 * @throws SQLException
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@GET
	@Path("group/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getGroupFromId(@PathParam("id") int p_id,
			@Context HttpServletRequest p_request) throws SQLException,
			JsonGenerationException, JsonMappingException, IOException {
		EnOceanStorage l_storage = EnOceanStorage.getInstance();
		Group l_group = l_storage.getGroupFromId(p_id);
		StringBuffer l_output = new StringBuffer();

		l_output.append(m_mapper.writeValueAsString(l_group));

		return Response.status(checkXToken(p_request))
				.entity(l_output.toString()).build();
	}

	/**
	 * Create a new group with the payload
	 * 
	 * @param payload
	 *            The request body
	 * @param p_request
	 *            The request header
	 * @return JSON object : id, name, description, lastModifier,
	 *         lastModification
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 * @throws SQLException
	 */
	@PUT
	@Path("group")
	public Response addGroup(String payload,
			@Context HttpServletRequest p_request) throws JsonParseException,
			JsonMappingException, IOException, SQLException {
		EnOceanStorage l_storage = EnOceanStorage.getInstance();
		Group l_group = m_mapper.readValue(payload, Group.class);

		l_storage.addGroup(l_group);

		return Response.status(checkXToken(p_request)).entity(null).build();
	}

	/**
	 * Update the specified group
	 * 
	 * @param payload
	 *            The request body
	 * @param p_id
	 *            The group ID
	 * @param p_request
	 *            The request header
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 * @throws SQLException
	 */
	@POST
	@Path("group/{id}")
	public Response modifyGroup(String payload, @PathParam("id") int p_id,
			@Context HttpServletRequest p_request) throws JsonParseException,
			JsonMappingException, IOException, SQLException {
		Group l_group = m_mapper.readValue(payload, Group.class);
		EnOceanStorage l_storage = EnOceanStorage.getInstance();

		l_storage.updateGroup(l_group, p_id);

		return Response.status(checkXToken(p_request)).entity(null).build();
	}

	/**
	 * Delete the specified group
	 * 
	 * @param p_id
	 *            The group ID
	 * @param p_request
	 *            The request header
	 * @return Status code (200 or 401)
	 * @throws SQLException
	 */
	@DELETE
	@Path("group/{id}")
	public Response deleteGroup(@PathParam("id") int p_id,
			@Context HttpServletRequest p_request) throws SQLException {
		EnOceanStorage l_storage = EnOceanStorage.getInstance();

		l_storage.deleteGroup(p_id);

		return Response.status(checkXToken(p_request)).entity(null).build();
	}

	/**
	 * Return all the sensors
	 * 
	 * @param p_request
	 *            The request header
	 * @return JSON array : name, id, description, address, lastModifier,
	 *         manufacturer, actuator, hybridMode, measure, locationPath,
	 *         eepRorg, eepFunction, eepType, locationTypeName,
	 *         locationTypeImgUrl, lastModification
	 * @throws SQLException
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@GET
	@Path("sensors")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSensors(@Context HttpServletRequest p_request)
			throws SQLException, JsonGenerationException, JsonMappingException,
			IOException {
		EnOceanStorage l_storage = EnOceanStorage.getInstance();
		ArrayList<Sensor> l_sensors = l_storage.getSensors();
		StringBuffer l_output = new StringBuffer();

		l_output.append("[");
		for (int i = 0; i < l_sensors.size(); i++) {
			l_output.append(m_mapper.writeValueAsString(l_sensors.get(i)));
			if (i < (l_sensors.size() - 1)) {
				l_output.append(',');
			}
		}
		l_output.append("]");

		return Response.status(checkXToken(p_request))
				.entity(l_output.toString()).build();
	}

	/**
	 * Return the specified sensors (from group id)
	 * 
	 * @param p_groupId
	 *            The group id
	 * @param p_request
	 *            The request header
	 * @return JSON object : name, id, description, address, lastModifier,
	 *         manufacturer, actuator, hybridMode, measure, locationPath,
	 *         eepRorg, eepFunction, eepType, locationTypeName,
	 *         locationTypeImgUrl, lastModification, locationId
	 * @throws SQLException
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@GET
	@Path("sensors/{groupId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSensorsFromGroupId(@PathParam("groupId") int p_groupId,
			@Context HttpServletRequest p_request) throws SQLException,
			JsonGenerationException, JsonMappingException, IOException {
		EnOceanStorage l_storage = EnOceanStorage.getInstance();
		ArrayList<Sensor> l_sensors = l_storage
				.getSensorsFromGroupId(p_groupId);
		StringBuffer l_output = new StringBuffer();

		l_output.append("[");
		for (int i = 0; i < l_sensors.size(); i++) {
			l_output.append(m_mapper.writeValueAsString(l_sensors.get(i)));
			if (i < (l_sensors.size() - 1)) {
				l_output.append(',');
			}
		}

		l_output.append("]");

		return Response.status(checkXToken(p_request))
				.entity(l_output.toString()).build();
	}
	
	@GET
	@Path("sensors_learn")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSensorInLearnMode(@Context HttpServletRequest p_request) {

		EnOceanDispatcher enoceanDispatcher = null;
		try {
			enoceanDispatcher = EnOceanDispatcher.getInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Map<LearnTelegram, Date> l_sensors_learn = enoceanDispatcher
				.getDiscoveredDevices();

		StringBuffer l_output = new StringBuffer();
		l_output.append("[");
		for (LearnTelegram key : l_sensors_learn.keySet()) {
			l_output.append("{\"EEPRorg\":\"" + key.getRORGNumber()
					+ "\",\"EEPFunction\":\"" + key.getFunction()
					+ "\",\"EEPType\":\"" + key.getType()
					+ "\",\"manufacturer\":\"" + key.getManufacturerId()
					+ "\",\"address\":\"" + key.getSenderID() + "\"},");
		}

		l_output.append("]");

		// make the JSON correct (cut the end and add just a "]")
		String l_string = l_output.toString();

		if (l_string.length() < 3) {
			l_string = "[]";
		} else {
			l_string = l_string.substring(0, l_string.length() - 2);
			l_string += "]";
		}

		System.out.println(l_string.toString());
		return Response.status(checkXToken(p_request))
				.entity(l_string.toString()).build();
	}

	
	/*@GET
	@Path("sensors")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDiscoveredSensors(@Context HttpServletRequest p_request) throws Exception{
		Map<LearnTelegram, Date> l_discoveredDevices = EnOceanDispatcher.getInstance().getDiscoveredDevices();
		
		StringBuffer l_output = new StringBuffer();
		l_output.append("[");
		
		for(LearnTelegram l_learn : l_discoveredDevices.keySet()){
			l_output.append(String.format("{\"RORG\":\"%s\", \"Function\":\"%s\", \"Type\":\"%s\", \"Manufacturer\":\"%s\", \"Date\":\"%s\"}", l_learn.getr))
		}
	}/*

	/**
	 * Create a new sensor
	 * 
	 * @Path("sensors")
	 * 
	 * @Produces(MediaType.APPLICATION_JSON) public Response
	 * getDiscoveredSensors(@Context HttpServletRequest p_request) throws
	 * Exception{ Map<LearnTelegram, Date> l_discoveredDevices =
	 * EnOceanDispatcher.getInstance().getDiscoveredDevices();
	 * 
	 * StringBuffer l_output = new StringBuffer(); l_output.append("[");
	 * 
	 * for(LearnTelegram l_learn : l_discoveredDevices.keySet()){
	 * l_output.append(String.format(
	 * "{\"RORG\":\"%s\", \"Function\":\"%s\", \"Type\":\"%s\", \"Manufacturer\":\"%s\", \"Date\":\"%s\"}"
	 * , l_learn.getr)) } }/*
	 * 
	 * /** Create a new sensor
	 * 
	 * @param payload The request body
	 * 
	 * @param p_groupId The group id
	 * 
	 * @param p_request The request header
	 * 
	 * @return Status code (200 or 401)
	 * 
	 * @throws Exception
	 */
	@PUT
	@Path("sensor/{groupId}")
	public Response addSensor(String payload,
			@PathParam("groupId") int p_groupId,
			@Context HttpServletRequest p_request) throws Exception {
		EnOceanStorage l_storage = EnOceanStorage.getInstance();
		JsonFactory jfactory = new JsonFactory();
		JsonParser jParser = jfactory.createParser(payload);
		Sensor l_sensor = new Sensor();
		ArrayList<String> l_eepShortcuts = new ArrayList<String>();

		String l_jsonFieldName = new String();
		while (jParser.nextToken() != JsonToken.END_OBJECT) {
			l_jsonFieldName = jParser.getCurrentName();
			if ("description".equals(l_jsonFieldName)) {
				jParser.nextToken();
				l_sensor.setDescription(jParser.getText());
			}
			if ("address".equals(l_jsonFieldName)) {
				jParser.nextToken();
				l_sensor.setAddress(Integer.parseInt(jParser.getText()));
			}
			if ("name".equals(l_jsonFieldName)) {
				jParser.nextToken();
				l_sensor.setName(jParser.getText());
			}
			if ("locationPath".equals(l_jsonFieldName)) {
				jParser.nextToken();
				l_sensor.setLocationPath(jParser.getText().substring(0, jParser.getText().lastIndexOf(EnOceanConfig.getDNSZone())-1));
			}
			if ("locationId".equals(l_jsonFieldName)) {
				jParser.nextToken();
				l_sensor.setLocationId(Integer.parseInt(jParser.getText()));
			}
			if ("lastModifier".equals(l_jsonFieldName)) {
				jParser.nextToken();
				l_sensor.setLastModifier(jParser.getText());
			}
			if ("measure".equals(l_jsonFieldName)) {
				jParser.nextToken();
				String l_str[] = jParser.getText().split("/");
				for (int i = 0; i < l_str.length; i++) {
					l_eepShortcuts.add(l_str[i]);
				}
			}
			if ("manufacturer".equals(l_jsonFieldName)) {
				jParser.nextToken();
				l_sensor.setManufacturer(jParser.getText());
			}
			if ("eepRorg".equals(l_jsonFieldName)) {
				jParser.nextToken();
				l_sensor.setEepRorg((int) Long.parseLong(jParser.getText()
						.substring(2, 4), 16));
			}
			if ("eepFunction".equals(l_jsonFieldName)) {
				jParser.nextToken();
				l_sensor.setEepFunction((int) Long.parseLong(jParser.getText()
						.substring(2, 4), 16));
			}
			if ("eepType".equals(l_jsonFieldName)) {
				jParser.nextToken();
				l_sensor.setEepType((int) Long.parseLong(jParser.getText()
						.substring(2, 4), 16));
			}
			if ("actuator".equals(l_jsonFieldName)) {
				jParser.nextToken();
				l_sensor.setActuator(Boolean.valueOf(jParser.getText()));
			}
			if ("hybridMode".equals(l_jsonFieldName)) {
				jParser.nextToken();
				l_sensor.setHybridMode(Boolean.valueOf(jParser.getText()));
			}
			if ("locationTypeName".equals(l_jsonFieldName)) {
				jParser.nextToken();
				l_sensor.setLocationTypeName(jParser.getText());
			}
			if ("locationTypeImgUrl".equals(l_jsonFieldName)) {
				jParser.nextToken();
				l_sensor.setLocationTypeImgUrl(jParser.getText());
			}
		}
		jParser.close();

		ArrayList<Measure> l_measures = new ArrayList<Measure>();
		IEnoceanCommunicator enoceanComm = null;
		try {
			enoceanComm = EnoceanCommunicator.getInstance(
					EnOceanConfig.getSerialPort(), EnOceanConfig.getEepFile());
		} catch (Exception e) {
			logger.error("Add sensor", e);
		}

		for (int i = 0; i < l_eepShortcuts.size(); i++) {
			try {
				EEPField l_eepField = enoceanComm.getEEPFieldInfo(
						String.format("%02X ", l_sensor.getEepRorg()),
						String.format("%02X ", l_sensor.getEepFunction()),
						String.format("%02X ", l_sensor.getEepType()),
						l_eepShortcuts.get(i),
						IEnoceanCommunicator.TRANSMISSION);

				Measure l_measure;
				if (l_eepField.getFieldConversions().size() == 0)
					l_measure = new Measure(null, 0.0f, 0.0f,
							l_eepField.getShortcut());
				else
					l_measure = new Measure(l_eepField.getFieldConversions()
							.get(0).getUnit(), (float) l_eepField
							.getFieldConversions().get(0).getScaleMax(),
							(float) l_eepField.getFieldConversions().get(0)
									.getScaleMin(), l_eepField.getShortcut());

				l_measures.add(l_measure);
			} catch (Exception e) {
				logger.error("Add sensor", e);
			}
		}
		l_sensor.setMeasure(l_measures);
		l_storage.addSensor(l_sensor, p_groupId);

		DNSUtils.getInstance(EnOceanConfig.getDNSIP(), getIPAddr(),
				EnOceanConfig.getDNSZone()).addNewHost((
				l_sensor.getName() + "." + l_sensor.getLocationPath()).toLowerCase());

		return Response.status(checkXToken(p_request)).entity(null).build();
	}

	/**
	 * Add the existing sensor to another group
	 * 
	 * @param p_groupId
	 *            The group id
	 * @param p_sensorId
	 *            The sensor id
	 * @param p_request
	 *            The request header
	 * @return Status code (200 or 401)
	 * @throws SQLException
	 */
	@PUT
	@Path("sensor/{groupId}/{sensorId}")
	public Response addSensorToGroup(@PathParam("groupId") int p_groupId,
			@PathParam("sensorId") int p_sensorId,
			@Context HttpServletRequest p_request) throws SQLException {
		EnOceanStorage l_storage = EnOceanStorage.getInstance();

		l_storage.addSensorToGroup(p_groupId, p_sensorId);

		return Response.status(checkXToken(p_request)).entity(null).build();
	}

	/**
	 * Update the specified sensor
	 * 
	 * @param payload
	 *            The request body
	 * @param p_id
	 *            The sensor ID
	 * @param p_request
	 *            The request header
	 * @return Status code (200 or 401)
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 * @throws SQLException
	 */
	@POST
	@Path("sensor/{id}")
	public Response modifySensor(String payload, @PathParam("id") int p_id,
			@Context HttpServletRequest p_request) throws JsonParseException,
			JsonMappingException, IOException, SQLException {
		EnOceanStorage l_storage = EnOceanStorage.getInstance();
		JsonFactory jfactory = new JsonFactory();
		JsonParser jParser = jfactory.createParser(payload);
		Sensor l_sensor = new Sensor();
		ArrayList<String> l_eepShortcuts = new ArrayList<String>();

		String l_jsonFieldName = new String();
		while (jParser.nextToken() != JsonToken.END_OBJECT) {
			l_jsonFieldName = jParser.getCurrentName();
			if ("description".equals(l_jsonFieldName)) {
				jParser.nextToken();
				l_sensor.setDescription(jParser.getText());
			}
			if ("address".equals(l_jsonFieldName)) {
				jParser.nextToken();
				l_sensor.setAddress(Integer.parseInt(jParser.getText()));
			}
			if ("name".equals(l_jsonFieldName)) {
				jParser.nextToken();
				l_sensor.setName(jParser.getText());
			}
			if ("locationPath".equals(l_jsonFieldName)) {
				jParser.nextToken();
				l_sensor.setLocationPath(jParser.getText().substring(0, jParser.getText().lastIndexOf(EnOceanConfig.getDNSZone())-1));
			}
			if ("locationId".equals(l_jsonFieldName)) {
				jParser.nextToken();
				l_sensor.setLocationId(Integer.parseInt(jParser.getText()));
			}
			if ("lastModifier".equals(l_jsonFieldName)) {
				jParser.nextToken();
				l_sensor.setLastModifier(jParser.getText());
			}
			if ("measure".equals(l_jsonFieldName)) {
				jParser.nextToken();
				String l_str[] = jParser.getText().split("/");
				for (int i = 0; i < l_str.length; i++) {
					l_eepShortcuts.add(l_str[i]);
				}
			}
			if ("manufacturer".equals(l_jsonFieldName)) {
				jParser.nextToken();
				l_sensor.setManufacturer(jParser.getText());
			}
			if ("eepRorg".equals(l_jsonFieldName)) {
				jParser.nextToken();
				l_sensor.setEepRorg((int) Long.parseLong(jParser.getText()
						.substring(2, 4), 16));
			}
			if ("eepFunction".equals(l_jsonFieldName)) {
				jParser.nextToken();
				l_sensor.setEepFunction((int) Long.parseLong(jParser.getText()
						.substring(2, 4), 16));
			}
			if ("eepType".equals(l_jsonFieldName)) {
				jParser.nextToken();
				l_sensor.setEepType((int) Long.parseLong(jParser.getText()
						.substring(2, 4), 16));
			}
			if ("actuator".equals(l_jsonFieldName)) {
				jParser.nextToken();
				l_sensor.setActuator(Boolean.valueOf(jParser.getText()));
			}
			if ("hybridMode".equals(l_jsonFieldName)) {
				jParser.nextToken();
				l_sensor.setHybridMode(Boolean.valueOf(jParser.getText()));
			}
			if ("locationTypeName".equals(l_jsonFieldName)) {
				jParser.nextToken();
				l_sensor.setLocationTypeName(jParser.getText());
			}
			if ("locationTypeImgUrl".equals(l_jsonFieldName)) {
				jParser.nextToken();
				l_sensor.setLocationTypeImgUrl(jParser.getText());
			}
		}
		jParser.close();

		ArrayList<Measure> l_measures = new ArrayList<Measure>();
		IEnoceanCommunicator enoceanComm = null;
		try {
			enoceanComm = EnoceanCommunicator.getInstance(
					EnOceanConfig.getSerialPort(), EnOceanConfig.getEepFile());
		} catch (Exception e) {
			logger.error("Modify sensor", e);
		}

		for (int i = 0; i < l_eepShortcuts.size(); i++) {
			try {
				EEPField l_eepField = enoceanComm.getEEPFieldInfo(
						String.format("%02X ", l_sensor.getEepRorg()),
						String.format("%02X ", l_sensor.getEepFunction()),
						String.format("%02X ", l_sensor.getEepType()),
						l_eepShortcuts.get(i),
						IEnoceanCommunicator.TRANSMISSION);
				
				Measure l_measure;
				if (l_eepField.getFieldConversions().size() == 0)
					l_measure = new Measure(null, 0.0f, 0.0f,
							l_eepField.getShortcut());
				else
					l_measure = new Measure(l_eepField
						.getFieldConversions().get(0).getUnit(),
						(float) l_eepField.getFieldConversions().get(0)
								.getScaleMax(), (float) l_eepField
								.getFieldConversions().get(0).getScaleMin(),
						l_eepField.getShortcut());
				l_measures.add(l_measure);
			} catch (Exception e) {
				logger.error("Modify sensor", e);
			}
		}
		l_sensor.setMeasure(l_measures);
		l_storage.updateSensor(l_sensor, p_id);

		try {
			DNSUtils.getInstance(EnOceanConfig.getDNSIP(), getIPAddr(),
					EnOceanConfig.getDNSZone()).addNewHost(
					(l_sensor.getName() + "." + l_sensor.getLocationPath())
							.toLowerCase());
		} catch (ZoneTransferException e) {
			logger.error("Modify sensor", e);
		}

		return Response.status(checkXToken(p_request)).entity(null).build();
	}

	/**
	 * Delete the specified sensor
	 * 
	 * @param p_groupId
	 *            The group id
	 * @param p_sensorId
	 *            The sensor id
	 * @param p_request
	 *            The request header
	 * @return Status code (200 or 401)
	 * @throws SQLException
	 */
	@DELETE
	@Path("sensor/{groupId}/{sensorId}")
	public Response deleteSensor(@PathParam("groupId") int p_groupId,
			@PathParam("sensorId") int p_sensorId,
			@Context HttpServletRequest p_request) throws SQLException {
		EnOceanStorage l_storage = EnOceanStorage.getInstance();

		l_storage.deleteSensor(p_groupId, p_sensorId);

		return Response.status(checkXToken(p_request)).entity(null).build();
	}

	/**
	 * Return the EEP Rorg list
	 * 
	 * @param p_request
	 *            The request header
	 * @return JSON array : eepRorg, description
	 * @throws Exception
	 */
	@GET
	@Path("eep_rorgs")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getEepRorg(@Context HttpServletRequest p_request)
			throws Exception {
		IEnoceanCommunicator enoceanComm = EnoceanCommunicator.getInstance(
				EnOceanConfig.getSerialPort(), EnOceanConfig.getEepFile());
		Map<String, String> l_eepRorgs = enoceanComm.getAllRORG();
		int l_mapSize = l_eepRorgs.entrySet().size();
		int i = 0;
		StringBuffer l_output = new StringBuffer();

		l_output.append("[");
		for (Map.Entry<String, String> value : l_eepRorgs.entrySet()) {
			if (i < (l_mapSize - 1)) {
				l_output.append("{\"eepRorg\":\"" + value.getKey()
						+ "\",\"description\":\"" + value.getValue() + "\"},");
			} else {
				l_output.append("{\"eepRorg\":\"" + value.getKey()
						+ "\",\"description\":\"" + value.getValue() + "\"}");
			}
			i++;
		}
		l_output.append("]");

		return Response.status(checkXToken(p_request))
				.entity(l_output.toString()).build();
	}

	/**
	 * Return the eep functions list
	 * 
	 * @param p_eepRorg
	 *            The eep rorg
	 * @param p_request
	 *            The request header
	 * @return JSON array : eepFunction, description
	 * @throws Exception
	 */
	@GET
	@Path("eep_functions/{eepRorg}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getEepFunction(@PathParam("eepRorg") String p_eepRorg,
			@Context HttpServletRequest p_request) throws Exception {
		IEnoceanCommunicator enoceanComm = EnoceanCommunicator.getInstance(
				EnOceanConfig.getSerialPort(), EnOceanConfig.getEepFile());
		Map<String, String> l_eepFunctions = enoceanComm
				.getFunctionByRORG(p_eepRorg.substring(2, 4));
		int l_mapSize = l_eepFunctions.entrySet().size(); // get the size of the
															// Map
		int i = 0;
		StringBuffer l_output = new StringBuffer();

		l_output.append("[");
		for (Map.Entry<String, String> value : l_eepFunctions.entrySet()) {
			if (i < (l_mapSize - 1)) {
				l_output.append("{\"eepFunction\":\"" + value.getKey()
						+ "\",\"description\":\"" + value.getValue() + "\"},");
			} else {
				l_output.append("{\"eepFunction\":\"" + value.getKey()
						+ "\",\"description\":\"" + value.getValue() + "\"}");
			}
			i++;
		}
		l_output.append("]");

		return Response.status(checkXToken(p_request))
				.entity(l_output.toString()).build();
	}

	/**
	 * Return the EEP Type list
	 * 
	 * @param p_eepRorg
	 *            The EEP rorg
	 * @param p_eepFunction
	 *            The EEP function
	 * @param p_request
	 *            The request header
	 * @return JSON array : eepType, description
	 * @throws Exception
	 */
	@GET
	@Path("eep_types/{eepRorg}/{eepFunction}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getEepType(@PathParam("eepRorg") String p_eepRorg,
			@PathParam("eepFunction") String p_eepFunction,
			@Context HttpServletRequest p_request) throws Exception {
		IEnoceanCommunicator enoceanComm = EnoceanCommunicator.getInstance(
				EnOceanConfig.getSerialPort(), EnOceanConfig.getEepFile());
		Map<String, String> l_eepTypes = enoceanComm.getTypeByRORGAndFunction(
				p_eepRorg.substring(2, 4), p_eepFunction.substring(2, 4));
		int l_mapSize = l_eepTypes.entrySet().size();
		int i = 0;
		StringBuffer l_output = new StringBuffer();

		l_output.append("[");
		for (Map.Entry<String, String> value : l_eepTypes.entrySet()) {
			if (i < (l_mapSize - 1)) {
				l_output.append("{\"eepType\":\"" + value.getKey()
						+ "\",\"description\":\"" + value.getValue() + "\"},");
			} else {
				l_output.append("{\"eepType\":\"" + value.getKey()
						+ "\",\"description\":\"" + value.getValue() + "\"}");
			}
			i++;
		}
		l_output.append("]");

		return Response.status(checkXToken(p_request))
				.entity(l_output.toString()).build();
	}

	/**
	 * Return the measure types
	 * 
	 * @param p_eepRorg
	 *            The EEP rorg
	 * @param p_eepFunction
	 *            The EEP function
	 * @param p_eepType
	 *            The EEP type
	 * @param p_request
	 *            The request header
	 * @return JSON array : shortcut, unit, scaleMax, scaleMin
	 */
	@GET
	@Path("measure_types/{eepRorg}/{eepFunction}/{eepType}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMeasureTypes(@PathParam("eepRorg") String p_eepRorg,
			@PathParam("eepFunction") String p_eepFunction,
			@PathParam("eepType") String p_eepType,
			@Context HttpServletRequest p_request) {
		IEnoceanCommunicator enoceanComm = null;
		try {
			enoceanComm = EnoceanCommunicator.getInstance(
					EnOceanConfig.getSerialPort(), EnOceanConfig.getEepFile());
		} catch (Exception e) {
			logger.error("Measure types", e);
		}

		Map<String, EEPField> l_measureTypes = null;
		try {
			l_measureTypes = enoceanComm.getEEPFieldsInfo(
					p_eepRorg.substring(2, 4), p_eepFunction.substring(2, 4),
					p_eepType.substring(2, 4),
					IEnoceanCommunicator.TRANSMISSION);

		} catch (Exception e) {
			logger.error("Modify sensor", e);
			return Response.status(404).entity(null).build();
		}

		StringBuffer l_output = new StringBuffer();
		l_output.append("[");
		for (String key : l_measureTypes.keySet()) {
			EEPField l_field = l_measureTypes.get(key);
			if (l_field.getFieldConversions() != null
					&& l_field.getFieldConversions().size() > 0) {
				l_output.append("{\"shortcut\":\"" + l_field.getShortcut()
						+ "\",\"unit\":\""
						+ l_field.getFieldConversions().get(0).getUnit()
						+ "\",\"scaleMin\":\""
						+ l_field.getFieldConversions().get(0).getScaleMin()
						+ "\",\"scaleMax\":\""
						+ l_field.getFieldConversions().get(0).getScaleMax()
						+ "\"},");
			} else {
				l_output.append("{\"shortcut\":\"" + l_field.getShortcut()
						+ "\",\"unit\":\"-" + "\",\"scaleMin\":\"-"
						+ "\",\"scaleMax\":\"-" + "\"},");
			}
		}
		l_output.append("]");

		// make the JSON correct (cut the end and add just a "]")
		String l_string = l_output.toString();
		l_string = l_string.substring(0, l_string.length() - 2);
		l_string += "]";

		return Response.status(checkXToken(p_request))
				.entity(l_string.toString()).build();
	}

	/**
	 * Return all the manufacturer
	 * 
	 * @param p_request
	 *            The request header
	 * @return JSON array : id, name
	 * @throws SQLException
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@GET
	@Path("manufacturers")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getManufacturers(@Context HttpServletRequest p_request)
			throws SQLException, JsonGenerationException, JsonMappingException,
			IOException {
		EnOceanStorage l_storage = EnOceanStorage.getInstance();
		ArrayList<Manufacturer> l_manufacturers = l_storage.getManufacturers();
		StringBuffer l_output = new StringBuffer();

		l_output.append("[");
		for (int i = 0; i < l_manufacturers.size(); i++) {
			l_output.append(m_mapper.writeValueAsString(l_manufacturers
					.get(i)));
			if (i < (l_manufacturers.size() - 1)) {
				l_output.append(',');
			}
		}
		l_output.append("]");

		return Response.status(checkXToken(p_request))
				.entity(l_output.toString()).build();
	}

	/**
	 * Return the root locations
	 * 
	 * @param p_request
	 *            The request header
	 * @return JSON array : id, name, path, typeImgUrl & typeName
	 * @throws SQLException
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@GET
	@Path("locations")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLocations(@Context HttpServletRequest p_request)
			throws SQLException, JsonGenerationException, JsonMappingException,
			IOException {
		EnOceanStorage l_storage = EnOceanStorage.getInstance();
		ArrayList<Location> l_locations = l_storage.getLocationRoot();
		StringBuffer l_output = new StringBuffer();

		l_output.append("[");
		for (int i = 0; i < l_locations.size(); i++) {
			l_output.append(m_mapper.writeValueAsString(l_locations.get(i)));
			if (i < (l_locations.size() - 1)) {
				l_output.append(',');
			}
		}
		l_output.append("]");

		return Response.status(checkXToken(p_request))
				.entity(l_output.toString()).build();
	}

	/**
	 * Return the child locations
	 * 
	 * @param p_id
	 *            The parent location id
	 * @param p_request
	 *            The request header
	 * @return JSON array : id, name, path, typeImgUrl & typeName
	 * @throws SQLException
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@GET
	@Path("locations/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLocationChilds(@PathParam("id") int p_id,
			@Context HttpServletRequest p_request) throws SQLException,
			JsonGenerationException, JsonMappingException, IOException {
		EnOceanStorage l_storage = EnOceanStorage.getInstance();
		ArrayList<Location> l_locations = l_storage.getLocationChilds(p_id);
		StringBuffer l_output = new StringBuffer();

		l_output.append("[");
		for (int i = 0; i < l_locations.size(); i++) {
			l_output.append(m_mapper.writeValueAsString(l_locations.get(i)));
			if (i < (l_locations.size() - 1)) {
				l_output.append(',');
			}
		}
		l_output.append("]");

		return Response.status(checkXToken(p_request))
				.entity(l_output.toString()).build();
	}

	/**
	 * Return the specified location
	 * 
	 * @param p_id
	 *            The location id
	 * @param p_request
	 *            The request header
	 * @return Status code (200 or 401)
	 * @throws SQLException
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@GET
	@Path("location/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLocationFromId(@PathParam("id") int p_id,
			@Context HttpServletRequest p_request) throws SQLException,
			JsonGenerationException, JsonMappingException, IOException {
		EnOceanStorage l_storage = EnOceanStorage.getInstance();
		Location l_location = l_storage.getLocationFromId(p_id);
		StringBuffer l_output = new StringBuffer();

		l_output.append(m_mapper.writeValueAsString(l_location));

		return Response.status(checkXToken(p_request))
				.entity(l_output.toString()).build();
	}

	/**
	 * Create a new location
	 * 
	 * @param payload
	 *            The request body
	 * @param p_parentId
	 *            The parent group's id
	 * @param p_request
	 *            The request header
	 * @return Satus code (200 or 401)
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 * @throws SQLException
	 */
	@PUT
	@Path("location/{parentId}")
	public Response addLocation(String payload,
			@PathParam("parentId") int p_parentId,
			@Context HttpServletRequest p_request) throws JsonParseException,
			JsonMappingException, IOException, SQLException {
		EnOceanStorage l_storage = EnOceanStorage.getInstance();
		Location l_location = m_mapper.readValue(payload, Location.class);

		String l_fullPath = l_storage.addLocation(l_location, p_parentId);

		String[] l_pathArray = l_fullPath.split("[.]");
		if (EnOceanConfig.getDNSZone().toLowerCase()
				.startsWith(l_pathArray[l_pathArray.length - 1].toLowerCase()))
			l_fullPath = l_fullPath.substring(0, l_fullPath.lastIndexOf("."));

		try {
			DNSUtils.getInstance(EnOceanConfig.getDNSIP(), getIPAddr(),
					EnOceanConfig.getDNSZone()).addNewHost(
					l_fullPath.toLowerCase());
		} catch (ZoneTransferException e) {
			logger.error("Add location", e);
		}

		return Response.status(checkXToken(p_request)).entity(null).build();
	}

	/**
	 * Create a new root location
	 * 
	 * @param payload
	 *            The request body
	 * @param p_request
	 *            The request header
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 * @throws SQLException
	 */
	@PUT
	@Path("location")
	public Response addLocation(String payload,
			@Context HttpServletRequest p_request) throws JsonParseException,
			JsonMappingException, IOException, SQLException {
		EnOceanStorage l_storage = EnOceanStorage.getInstance();
		Location l_location = m_mapper.readValue(payload, Location.class);

		l_storage.addLocation(l_location);

		try {
			DNSUtils.getInstance(EnOceanConfig.getDNSIP(), getIPAddr(),
					EnOceanConfig.getDNSZone()).addNewHost(
					l_location.getName().toLowerCase());
		} catch (ZoneTransferException e) {
			logger.error("Add location", e);
		}

		return Response.status(checkXToken(p_request)).entity(null).build();
	}

	/**
	 * Update the specified location
	 * 
	 * @param payload
	 *            The request body
	 * @param p_id
	 *            The location id
	 * @param p_request
	 *            The request header
	 * @return Status code (200 or 401)
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 * @throws SQLException
	 */
	@POST
	@Path("location/{id}")
	public Response modifyLocation(String payload, @PathParam("id") int p_id,
			@Context HttpServletRequest p_request) throws JsonParseException,
			JsonMappingException, IOException, SQLException {
		Location l_location = m_mapper.readValue(payload, Location.class);
		EnOceanStorage l_storage = EnOceanStorage.getInstance();

		l_storage.updateLocation(l_location, p_id);

		try {
			DNSUtils.getInstance(EnOceanConfig.getDNSIP(), getIPAddr(),
					EnOceanConfig.getDNSZone()).addNewHost(
					l_location.getName().toLowerCase());
		} catch (ZoneTransferException e) {
			logger.error("Add location", e);
		}

		return Response.status(checkXToken(p_request)).entity(null).build();
	}

	/**
	 * Delete the specified location
	 * 
	 * @param p_id
	 *            The location id
	 * @param p_request
	 *            The request header
	 * @return Status code (200 or 401)
	 * @throws SQLException
	 */
	@DELETE
	@Path("location/{id}")
	public Response deleteLocation(@PathParam("id") int p_id,
			@Context HttpServletRequest p_request) throws SQLException {
		EnOceanStorage l_storage = EnOceanStorage.getInstance();

		l_storage.deleteLocation(p_id);

		return Response.status(checkXToken(p_request)).entity(null).build();

	}

	/**
	 * Return the location's types list
	 * 
	 * @param p_request
	 *            The request header
	 * @return JSON array : id & name
	 * @throws SQLException
	 */
	@GET
	@Path("locationtypes")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLocationTypes(@Context HttpServletRequest p_request)
			throws SQLException {
		EnOceanStorage l_storage = EnOceanStorage.getInstance();

		ArrayList<String> l_locationTypes = l_storage.getLocationTypes();
		StringBuffer l_output = new StringBuffer();

		l_output.append("[");
		for (int i = 0; i < l_locationTypes.size(); i++) {
			l_output.append("{\"locationType\":\"" + l_locationTypes.get(i)
					+ "\"}");
			if (i < (l_locationTypes.size() - 1)) {
				l_output.append(',');
			}
		}
		l_output.append("]");

		return Response.status(checkXToken(p_request))
				.entity(l_output.toString()).build();
	}

	
	/**
     * Return the datas 
     * 
     * @param p_request
     *            The request header
     * @param p_measureId
     *            The id of the measure
     * @return JSON array : id & name
     * @throws SQLException
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    @GET
    @Path("datas/{measureId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDatas(@Context HttpServletRequest p_request,
            @PathParam("measureId") int p_measureId, @QueryParam("from") String from, @QueryParam("to") String to)
            throws SQLException, JsonGenerationException, JsonMappingException,
            IOException {
    	DateFormat l_df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	Date l_dateFrom = new Date();
    	Date l_dateTo = new Date();
    	try {
			l_dateFrom = l_df.parse(from);
			l_dateTo = l_df.parse(to);
		} catch (ParseException e) {
			logger.error("Wrong date format", e);
		}
    	logger.info("Dates for datas " + l_df.format(l_dateFrom) + " " + l_df.format(l_dateTo));
        EnOceanStorage l_storage = EnOceanStorage.getInstance();
        ArrayList<Data> l_datas = l_storage.getDatas(p_measureId, l_dateFrom, l_dateTo);
        StringBuffer l_output = new StringBuffer();
        
        l_output.append("[");
        for (int i = 0; i < l_datas.size(); i++) {
            l_output.append(m_mapper.writeValueAsString(l_datas.get(i)));
            if (i < (l_datas.size() - 1)) {
                l_output.append(',');
            }
        }
        l_output.append("]");

        return Response.status(checkXToken(p_request))
                .entity(l_output.toString()).build();
    }

	private String getIPAddr() throws SocketException {
		String addr = null;
		Enumeration<NetworkInterface> interfaces = NetworkInterface
				.getNetworkInterfaces();
		while (interfaces.hasMoreElements()) {
			NetworkInterface current = interfaces.nextElement();
			System.out.println(current);
			// current.getName().equals(arg0);
			if (!current.isUp() || current.isLoopback() || current.isVirtual())
				continue;
			Enumeration<InetAddress> addresses = current.getInetAddresses();
			while (addresses.hasMoreElements()) {
				InetAddress current_addr = addresses.nextElement();
				if (current_addr.isLoopbackAddress()
						|| current_addr.isAnyLocalAddress()
						|| current_addr.isLinkLocalAddress()
						|| !(current_addr instanceof Inet4Address))
					continue;
				addr = current_addr.getHostAddress();
			}
		}
		return addr;
	}
}
