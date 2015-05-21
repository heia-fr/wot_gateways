package ch.eiafr.knx.utils;

import java.io.File;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.datapoint.CommandDP;
import tuwien.auto.calimero.datapoint.Datapoint;
import tuwien.auto.calimero.datapoint.StateDP;

public class DatapointLocator {

	private Document m_Document;

	/**
	 * Create a new locator
	 * 
	 * @param p_DatapointsXMLPath
	 *            The path to the xml file
	 * @throws JDOMException
	 * @throws IOException
	 */
	public DatapointLocator(String p_DatapointsXMLPath) throws JDOMException,
			IOException {
		// Load the xml document
		SAXBuilder l_sax = new SAXBuilder();
		m_Document = l_sax.build(new File(p_DatapointsXMLPath));
	}

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
			String p_action) throws Exception {
		// Create the xpath expression to find the datapoint
		XPathExpression<Element> l_xpath = XPathFactory
				.instance()
				.compile(
						"/datapoints/datapoint[lower-case(@actionName)='"
								+ p_action.toLowerCase()
								+ "' and lower-case(@location)='"
								+ p_location.toLowerCase()
								+ "' and translate(lower-case(@name), 'àäâéèêëüûùôöò', 'aaaeeeeuuuooo')='"
								+ p_functionality.toLowerCase() + "']",
						Filters.element());
		Element l_element = l_xpath.evaluateFirst(m_Document);
		if (l_element == null) {
			throw new Exception("Command not found");
		}

		// Build the datapoint
		Datapoint l_dp = null;
		GroupAddress l_grAddr = new GroupAddress(Integer.parseInt(l_element
				.getChild("knxAddress").getText()));
		if (l_element.getAttributeValue("stateBased").equals("true")) {
			l_dp = new StateDP(
					l_grAddr,
					l_element.getAttributeValue("name"),
					Integer.parseInt(l_element.getAttributeValue("mainNumber")),
					l_element.getAttributeValue("dptID"));
		} else {
			l_dp = new CommandDP(
					l_grAddr,
					l_element.getAttributeValue("name"),
					Integer.parseInt(l_element.getAttributeValue("mainNumber")),
					l_element.getAttributeValue("dptID"));
		}
		return l_dp;
	}

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
			String p_functionality, String p_location) {
		ArrayList<DatapointDescription> l_dps = new ArrayList<DatapointDescription>();
		// Create the xpath expression to find the datapoint
		XPathExpression<Element> l_xpath = XPathFactory
				.instance()
				.compile(
						"/datapoints/datapoint[lower-case(@location)='"
								+ p_location.toLowerCase()
								+ "' and translate(lower-case(@name), 'àäâéèêëüûùôöò', 'aaaeeeeuuuooo')='"
								+ p_functionality.toLowerCase() + "']",
						Filters.element());
		List<Element> l_elements = l_xpath.evaluate(m_Document);
		for (int i = 0; i < l_elements.size(); i++) {
			l_dps.add(new DatapointDescription(l_elements.get(i)
					.getAttributeValue("actionName"), l_elements.get(i)
					.getAttributeValue("dptID"), l_elements.get(i)
					.getAttributeValue("actionDesc"), l_elements.get(i)
					.getAttributeValue("dptDesc"), Integer.parseInt(l_elements
					.get(i).getAttributeValue("dptBitsSize"))));
		}

		return l_dps;
	}

	public ArrayList<ChildDescription> listChildren(String p_location)
			throws Exception {
		ArrayList<ChildDescription> l_cs = new ArrayList<ChildDescription>();
		ArrayList<String> l_foundChildren = new ArrayList<String>();
		// Create the xpath expression to find the functionalities in the
		// location
		XPathExpression<Element> l_xpath = XPathFactory.instance().compile(
				"/datapoints/datapoint[translate(lower-case(@location), 'àäâéèêëüûùôöò', 'aaaeeeeuuuooo')='"
						+ p_location.toLowerCase() + "']", Filters.element());
		List<Element> l_elements = l_xpath.evaluate(m_Document);
		for (int i = 0; i < l_elements.size(); i++) {
			String l_name = Normalizer.normalize(
					l_elements.get(i).getAttributeValue("name").toLowerCase(),
					Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
			if (!l_foundChildren.contains(l_name)) {
				l_cs.add(new ChildDescription(l_name, true));
				l_foundChildren.add(l_name);
			}
		}

		// Create the xpath expression to find the sub-zones in the location
		l_xpath = XPathFactory.instance().compile(
				"/datapoints/datapoint[ends-with(translate(lower-case(@location), 'àäâéèêëüûùôöò', 'aaaeeeeuuuooo'), '"
						+ p_location.toLowerCase() + "')]", Filters.element());
		l_elements = l_xpath.evaluate(m_Document);
		ArrayList<String> l_childs = new ArrayList<String>();
		for (int i = 0; i < l_elements.size(); i++) {
			// decompose location to find the sub-zone
			String l_loc = Normalizer.normalize(
					l_elements.get(i).getAttributeValue("location").toLowerCase(),
					Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");

			if (l_loc.lastIndexOf(p_location) == 0)
				continue;

			l_loc = l_loc.substring(0, l_loc.lastIndexOf(p_location) - 1);
			if (l_loc.lastIndexOf(".") != -1)
				l_loc = l_loc.substring(l_loc.lastIndexOf("."));
			if(l_loc.startsWith("."))
				l_loc = l_loc.substring(1);
			if (!l_childs.contains(l_loc))
				l_childs.add(l_loc);
		}

		for (int i = 0; i < l_childs.size(); i++) {
			l_cs.add(new ChildDescription(l_childs.get(i), false));
		}

		return l_cs;
	}

	/**
	 * List all possible URLs of groups and middle locations
	 * 
	 * @return The list of URLs
	 */
	public ArrayList<String> getAllUrls() {
		ArrayList<String> l_urls = new ArrayList<String>();
		ArrayList<String> l_locs = new ArrayList<String>();

		XPathExpression<Element> l_xpath = XPathFactory.instance().compile(
				"/datapoints/datapoint", Filters.element());
		List<Element> l_elements = l_xpath.evaluate(m_Document);
		for (int i = 0; i < l_elements.size(); i++) {
			String l_fonc = Normalizer.normalize(
					l_elements.get(i).getAttributeValue("name").toLowerCase(),
					Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
			String l_loc = Normalizer.normalize(
					l_elements.get(i).getAttributeValue("location")
							.toLowerCase(), Normalizer.Form.NFD).replaceAll(
					"[^\\p{ASCII}]", "");

			// add functionality url
			if (!l_urls.contains(l_fonc + "." + l_loc))
				l_urls.add(l_fonc + "." + l_loc);

			// add location url
			if (!l_locs.contains(l_loc))
				l_locs.add(l_loc);
		}

		// find middle locations
		for (int i = 0; i < l_locs.size(); i++) {
			String[] l_subs = l_locs.get(i).split("[.]");
			int k = l_subs.length - 1;
			String l_loc = l_subs[k];
			for (int j = 0; j < l_subs.length; j++) {
				if (!l_loc.equals("") && !l_urls.contains(l_loc))
					l_urls.add(l_loc);
				if (k - 1 >= 0)
					l_loc = l_subs[--k] + "." + l_loc;
			}
		}

		return l_urls;
	}

	/**
	 * Find url of knx group
	 * 
	 * @param p_group
	 *            THe group to find
	 * @return The url of the group
	 * @throws Exception
	 */
	public String findUrlByGroup(int p_group) throws Exception {
		// Create the xpath expression to find the datapoint
		XPathExpression<Element> l_xpath = XPathFactory.instance().compile(
				"/datapoints/datapoint/knxAddress[. = " + p_group + "]",
				Filters.element());
		Element l_element = l_xpath.evaluateFirst(m_Document);
		if (l_element == null) {
			throw new Exception("Group not found");
		}

		return (l_element.getParentElement().getAttributeValue("name") + "."
				+ l_element.getParentElement().getAttributeValue("location")).toLowerCase();

	}

}
