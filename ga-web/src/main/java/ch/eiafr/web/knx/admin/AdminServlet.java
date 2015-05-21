package ch.eiafr.web.knx.admin;

import java.io.BufferedReader;

import java.io.File;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.RollingFileAppender;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.exception.KNXException;

import ch.eiafr.knx.KNXLogger;
import ch.eiafr.knx.KNXManagement;
import ch.eiafr.knx.utils.IPGateway;
import ch.eiafr.web.dns.DNSUtils;
import ch.eiafr.web.knx.KNXStorage;

public class AdminServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory
			.getLogger(AdminServlet.class);
	private static final int TOKEN_DURATION = 60; // minutes

	private String m_Token = null;
	private Date m_TokenTime = null;
	private Thread m_Thread;
	private KNXFileWorker m_Worker;

	/**
	 * Entry point of a GET request
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// check token
		String l_Token = request.getHeader("X-Token");
		if (!l_Token.equals(m_Token)
				|| (new Date().getTime() - m_TokenTime.getTime()) / (60 * 1000) > TOKEN_DURATION) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		TypeRequest l_Request = parseRequest(request.getRequestURI());
		String responseValue = "";

		switch (l_Request) {
		case Gateways:
			ArrayList<IPGateway> l_Gateways;
			try {
				l_Gateways = KNXManagement.getInstance().discoverGateways(8749,
						10);
			} catch (KNXException e) {
				logger.error("Error finding gateways", e);
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}
			JSONArray l_Array = new JSONArray();
			for (int i = 0; i < l_Gateways.size(); i++) {
				JSONObject l_Obj = new JSONObject();
				l_Obj.put("IP", l_Gateways.get(i).getIPAddress());
				l_Array.add(l_Obj);
			}

			responseValue = l_Array.toString();
			response.setContentType("application/json");
			break;
		case Config:
			JSONObject l_Obj = new JSONObject();
			l_Obj.put("IPGateway", KNXConfig.getIPGateway());
			l_Obj.put("KNXAddr", KNXConfig.getKNXAddress());
			l_Obj.put("DNSIP", KNXConfig.getDNSIP());
			l_Obj.put("DNSZone", KNXConfig.getDNSZone());
			l_Obj.put("Storage", KNXConfig.hasStorage());
			l_Obj.put("DBUser", KNXConfig.getDBUser());
			l_Obj.put("DBPwd", KNXConfig.getDBPassword());
			responseValue = l_Obj.toString();
			response.setContentType("application/json");
			break;
		case Date:
			JSONObject l_DateObj = new JSONObject();
			Calendar l_Cal = Calendar.getInstance();
			l_DateObj.put("day", l_Cal.get(Calendar.DATE));
			l_DateObj.put("month", l_Cal.get(Calendar.MONTH) + 1);
			l_DateObj.put("year", l_Cal.get(Calendar.YEAR));
			l_DateObj.put("hours", l_Cal.get(Calendar.HOUR_OF_DAY));
			l_DateObj.put("min", l_Cal.get(Calendar.MINUTE));
			l_DateObj.put("sec", l_Cal.get(Calendar.SECOND));
			responseValue = l_DateObj.toString();
			response.setContentType("application/json");
			break;
		case Groups:
			JSONArray l_ArrayGroups = new JSONArray();
			try {
				ArrayList<Integer> l_groups = KNXStorage.getInstance()
						.getStorageGroups();
				for (int i = 0; i < l_groups.size(); i++) {
					String l_url = KNXManagement.getInstance().findUrlByGroup(
							l_groups.get(i));
					JSONObject l_ObjGroup = new JSONObject();
					l_ObjGroup.put("Group", l_url);
					l_ObjGroup.put("Id", l_groups.get(i));
					l_ArrayGroups.add(l_ObjGroup);
				}

			} catch (SQLException e) {
				logger.error("Error with SQL", e);
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			} catch (Exception e) {
				logger.error("Error", e);
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}
			responseValue = l_ArrayGroups.toString();
			response.setContentType("application/json");
			break;
		case Datapoints:
			JSONArray l_ArrayDP = new JSONArray();
			int l_Group = Integer.parseInt(request.getParameter("group"));
			try {
				ArrayList<Datapoint> l_DPs = KNXStorage.getInstance()
						.getDPByGroup(l_Group);
				for (int i = 0; i < l_DPs.size(); i++) {
					l_DPs.get(i).setSubscribers(
							KNXStorage.getInstance().getSubscribers(l_Group,
									l_DPs.get(i).getNumber()));
					JSONObject l_ObjDP = new JSONObject();
					l_ObjDP.put("DPTName", l_DPs.get(i).getName());
					l_ObjDP.put("DPTNumber", l_DPs.get(i).getNumber());
					l_ObjDP.put("LastRead", l_DPs.get(i).getLastRead()
							.toString());
					JSONArray l_ArraySubs = new JSONArray();
					for (int j = 0; j < l_DPs.get(i).getSubscribers().size(); j++) {
						JSONObject l_ObjSub = new JSONObject();
						l_ObjSub.put("Referer", l_DPs.get(i).getSubscribers()
								.get(j).getReferer());
						l_ObjSub.put("Days",
								l_DPs.get(i).getSubscribers().get(j).getDays());
						l_ArraySubs.add(l_ObjSub);
					}
					l_ObjDP.put("Subscribers", l_ArraySubs);
					l_ArrayDP.add(l_ObjDP);
				}

			} catch (SQLException e) {
				logger.error("Error with SQL", e);
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}

			responseValue = l_ArrayDP.toString();
			response.setContentType("application/json");
			break;
		case Log:
			String l_File = ((RollingFileAppender) org.apache.log4j.Logger
					.getRootLogger().getAppender("logfile")).getFile();
			BufferedReader l_Reader = new BufferedReader(new FileReader(l_File));
			String l_Line;
			StringBuilder l_Log = new StringBuilder();
			while ((l_Line = l_Reader.readLine()) != null)
				l_Log.append(l_Line);
			l_Reader.close();
			response.setContentType("text/plain");
			responseValue = l_Log.toString()
					.substring(
							Math.max(
									0,
									l_Log.length()
											- KNXConfig.getMaxCharsLogRead()));
			break;
		default:
			logger.error("Not an admin GET request");
			response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			return;
		}

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
		TypeRequest l_Request = parseRequest(request.getRequestURI());

		// for login
		if (l_Request == TypeRequest.Login) {
			if (!request.getParameter("password").equals(
					KNXConfig.getPassword())) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				return;
			}

			m_Token = UUID.randomUUID().toString();
			m_TokenTime = new Date();
			response.addHeader("X-Token", m_Token);
			return;
		}

		// check token
		String l_Token = request.getHeader("X-Token");
		if (!l_Token.equals(m_Token)
				|| (new Date().getTime() - m_TokenTime.getTime()) / (60 * 1000) > TOKEN_DURATION) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		switch (l_Request) {
		case Logout:
			m_Token = UUID.randomUUID().toString();
			break;
		case Date:
			String payload = null;
			try {
				payload = getPayloadData(request);
			} catch (Exception e) {
				logger.error("Error to get the payload data");
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}
			JSONObject l_DateObj = (JSONObject) JSONValue.parse(payload);

			Process l_Process = Runtime
					.getRuntime()
					.exec(new String[] {
							"sudo",
							"date",
							"-s",
							String.format("%s/%s/%s %s:%s:%s",
									l_DateObj.get("month"),
									l_DateObj.get("day"),
									l_DateObj.get("year"),
									l_DateObj.get("hours"),
									l_DateObj.get("min"), l_DateObj.get("sec")) });
			BufferedReader l_PReader = new BufferedReader(
					new InputStreamReader(l_Process.getInputStream()));
			String l_Line;
			while ((l_Line = l_PReader.readLine()) != null)
				logger.debug(l_Line);
			l_PReader.close();

			logger.debug("Set date: date -s "
					+ String.format("%s/%s/%s %s:%s:%s",
							l_DateObj.get("month"), l_DateObj.get("day"),
							l_DateObj.get("year"), l_DateObj.get("hours"),
							l_DateObj.get("min"), l_DateObj.get("sec"))
							.toString());
			break;
		case Config:
			String payloadConfig = null;
			try {
				payloadConfig = getPayloadData(request);
			} catch (Exception e) {
				logger.error("Error to get the payload data");
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}
			JSONObject l_Obj = (JSONObject) JSONValue.parse(payloadConfig);
			KNXConfig.setIPGateway((String) l_Obj.get("IPGateway"));
			KNXConfig.setKNXAddress((String) l_Obj.get("KNXAddr"));
			KNXConfig.setDNSIP((String) l_Obj.get("DNSIP"));
			KNXConfig.setDNSZone((String) l_Obj.get("DNSZone"));
			KNXConfig.hasStorage((Boolean) l_Obj.get("Storage"));
			KNXConfig.setDBUser((String) l_Obj.get("DBUser"));
			KNXConfig.setDBPassword((String) l_Obj.get("DBPwd"));
			KNXConfig.writeConfig();
			// restart knxmanagement initDatapointComm
			KNXManagement.getInstance().closeDatapointComm();
			try {
				KNXManagement.getInstance().initDatapointComm(
						KNXConfig.getIPGateway(), KNXConfig.getKNXAddress());
			} catch (KNXException e) {
				logger.error("Error to init knx", e);
			}

			// restart knxlogger initDatapointComm
			KNXLogger.getInstance().deleteObserver(KNXStorage.getInstance());
			try {
				KNXLogger.getInstance().initDatapointComm(
						KNXConfig.getIPGateway(), KNXConfig.getKNXAddress());
			} catch (KNXException e) {
				logger.error("Error to init knx", e);
			}

			// add-remove observer on knxlogger
			if (KNXConfig.hasStorage())
				KNXLogger.getInstance().addObserver(KNXStorage.getInstance());

			break;
		case KNXFile:
			if (!ServletFileUpload.isMultipartContent(request)) {
				logger.error("Try to upload without multipart");
				return;
			}
			FileItemFactory l_Factory = new DiskFileItemFactory();
			ServletFileUpload l_Upload = new ServletFileUpload(l_Factory);
			try {
				List l_Items = l_Upload.parseRequest(request);
				Iterator l_Iter = l_Items.iterator();
				while (l_Iter.hasNext()) {
					FileItem l_Item = (FileItem) l_Iter.next();
					if (!l_Item.isFormField()) {
						File l_UploadedFile = new File(
								KNXConfig.getDatapointFilePath()
										+ "/archive.knxproj");
						l_Item.write(l_UploadedFile);
					}
				}
			} catch (FileUploadException e) {
				logger.error("Error to upload file", e);
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			} catch (Exception e) {
				logger.error("Error to upload file", e);
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}

			m_Worker = new KNXFileWorker(true, getIPAddr());
			m_Thread = new Thread(m_Worker);
			m_Thread.start();
			break;
		case DPFile:
			if (!ServletFileUpload.isMultipartContent(request)) {
				logger.error("Try to upload without multipart");
				return;
			}
			FileItemFactory l_FactoryDP = new DiskFileItemFactory();
			ServletFileUpload l_UploadDP = new ServletFileUpload(l_FactoryDP);
			try {
				List l_Items = l_UploadDP.parseRequest(request);
				Iterator l_Iter = l_Items.iterator();
				while (l_Iter.hasNext()) {
					FileItem l_Item = (FileItem) l_Iter.next();
					if (!l_Item.isFormField()) {
						File l_UploadedFile = new File(
								KNXConfig.getDatapointFilePath()
										+ "/datapoints.xml");
						l_Item.write(l_UploadedFile);
					}
				}
			} catch (FileUploadException e) {
				logger.error("Error to upload file", e);
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			} catch (Exception e) {
				logger.error("Error to upload file", e);
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}

			m_Worker = new KNXFileWorker(false, getIPAddr());
			m_Thread = new Thread(m_Worker);
			m_Thread.start();
			break;
		default:
			logger.error("Not an admin POST request");
			response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			return;
		}
	}

	private TypeRequest parseRequest(String p_Url) {
		TypeRequest l_Request = null;
		String[] requests = p_Url.substring(1, p_Url.length()).split("/");
		switch (requests.length) {
		case 2:
			if (requests[1].equals("login"))
				l_Request = TypeRequest.Login;
			else if (requests[1].equals("logout"))
				l_Request = TypeRequest.Logout;
			else if (requests[1].equals("log"))
				l_Request = TypeRequest.Log;
			else if (requests[1].equals("config"))
				l_Request = TypeRequest.Config;
			else if (requests[1].equals("knxproj"))
				l_Request = TypeRequest.KNXFile;
			else if (requests[1].equals("dpfile"))
				l_Request = TypeRequest.DPFile;
			else if (requests[1].equals("date"))
				l_Request = TypeRequest.Date;
			break;
		case 3:
			if (requests[1].equals("config") && requests[2].equals("gateways"))
				l_Request = TypeRequest.Gateways;
			if (requests[1].equals("storage") && requests[2].equals("groups"))
				l_Request = TypeRequest.Groups;
			if (requests[1].equals("storage")
					&& requests[2].contains("datapoints"))
				l_Request = TypeRequest.Datapoints;
			break;
		}

		return l_Request;
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
