package ch.eiafr.knx;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Observer;

import org.jdom2.JDOMException;

import tuwien.auto.calimero.datapoint.Datapoint;
import tuwien.auto.calimero.exception.KNXException;
import ch.eiafr.knx.utils.ChildDescription;
import ch.eiafr.knx.utils.DatapointDescription;
import ch.eiafr.knx.utils.IPGateway;

public interface IKNXManagement {

	/**
	 * Discover KNX IP gateways on the local network	
	 * @param p_SourcePort The source port of the multicast
	 * @param p_Duration The duration of the search
	 * @return An ArrayList of discovered gateways
	 * @throws KNXException 
	 */
	public ArrayList<IPGateway> discoverGateways(int p_SourcePort,
			int p_Duration) throws KNXException;

	/**
	 * Generate the xml file containing datapoints descriptions
	 * 
	 * @param p_ETSProjectFile
	 *            The path to the knxproj file
	 * @param p_TransformFile
	 *            The path to the xsl file
	 * @param p_OutputFilePath
	 *            The path where the xml file has to be written
	 * @throws Exception
	 */
	public void generateXMLDatapoints(String p_ETSProjectFile,
			String p_TransformFile, String p_OutputFilePath) throws Exception;
	
	/**
	 * Generate the xml file containing datapoints descriptions
	 * 
	 * @param p_ETSProjectFile
	 *            The path to the knxproj file
	 * @param p_TransformFile
	 *            The stream to the xsl file
	 * @param p_OutputFilePath
	 *            The path where the xml file has to be written
	 * @throws Exception
	 */
	public void generateXMLDatapoints(String p_ETSProjectFile,
			InputStream p_TransformFile, String p_OutputFilePath) throws Exception;

	/**
	 * Initiate the datapoint locator system
	 * @param p_DatapointsXMLPath Path to the xml file containing the datapoints descriptions
	 * @throws JDOMException
	 * @throws IOException
	 */
	public void initDatapointLocator(String p_DatapointsXMLPath)
			throws JDOMException, IOException;

	/**
	 * Find a datapoint into the xml datapoints description
	 * 
	 * @param p_functionality
	 *            The name of the group to perform the action on. For example in
	 *            the url kitchen_light.ground.home.com the functionality would
	 *            be kitchen_light
	 * @param p_location
	 *            The middle dns name of the url without the top level domain.
	 *            For example in the url kitchen_light.ground.home.com the
	 *            location would be ground.home
	 * @param p_action
	 *            The datapoint name specified after the dns name
	 * @return A representation of the corresponding KNX datapoint
	 * @throws Exception
	 */
	public Datapoint findDatapoint(String p_functionality, String p_location,
			String p_action) throws Exception;
	
	/**
	 * List all datapoints of a functionality
	 * 
	 * @param p_functionality
	 *            The name of the group to perform the action on. For example in
	 *            the url kitchen_light.ground.home.com the functionality would
	 *            be kitchen_light
	 * @param p_location
	 *            The middle dns name of the url without the top level domain.
	 *            For example in the url kitchen_light.ground.home.com the
	 *            location would be ground.home
	 * @return An ArrayList of datapoints descriptions
	 */
	public ArrayList<DatapointDescription> listDatapoints(
			String p_functionality, String p_location) throws Exception;
	
	/**
	 * List the childs contained in a zone
	 * @param p_location The location to search for
	 * @return An ArrayList of childs of the zone
	 * @throws Exception
	 */
	public ArrayList<ChildDescription> listChildren(String p_location) throws Exception;

	/**
	 * Get all the different urls and location urls
	 * @return An ArrayList of all urls combinations
	 * @throws Exception 
	 */
	public ArrayList<String> getAllUrls() throws Exception;
	
	/**
	 * Add a datapoint to listen for
	 * @param p_dp The datapoint to listen
	 * @param p_Url The Url associated with the datapoint
	 * @throws Exception 
	 */
	public void addDatapointListener(Datapoint p_dp, String p_Url) throws Exception;
	
	/**
	 * Remove a listening datapoint
	 * @param p_Url The Url associated with the datapoint
	 * @throws Exception 
	 */
	public void removeDatapointListener(String p_Url) throws Exception;
	
	/**
	 * Initiate the KNX communication system
	 * 
	 * @param p_GatewayIP
	 *            The IP of the gateway
	 * @param p_GatewayKNXAddr
	 *            The KNX individual address of the gateway
	 * @throws KNXException
	 * @throws SocketException 
	 */
	public void initDatapointComm(String p_GatewayIP, String p_GatewayKNXAddr) throws KNXException, SocketException;
	

	/**
	 * Read a value from a datapoint
	 * @param p_dp The datapoint to read
	 * @return The actual value
	 */
	public String readDatapoint(Datapoint p_dp) throws Exception;

	/**
	 * Write a value to a datapoint
	 * @param p_dp The datapoint to write to
	 * @param p_value The value to write
	 */
	public void writeDatapoint(Datapoint p_dp, String p_value) throws Exception;
	
	/**
	 * Close the tunnel to the KNX gateway
	 */
	public void closeDatapointComm();

	/**
	 * Add an Observer instance to the interface
	 * @param p_Observer The observer
	 */
	public void addObserver(Observer p_Observer);
	
	/**
	 * Find the URL of a group address
	 * @param p_group The group address
	 * @return The corresponding URL
	 * @throws Exception
	 */
	public String findUrlByGroup(int p_group) throws Exception;
}
