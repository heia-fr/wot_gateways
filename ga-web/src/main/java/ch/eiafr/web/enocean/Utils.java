package ch.eiafr.web.enocean;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.eiafr.web.enocean.admin.EnOceanConfig;

public class Utils {
	
	
	private static final Logger logger = LoggerFactory
			.getLogger(Utils.class);
	
	/**
	 * Get all children of a location and add them to a JSONArray
	 * 
	 * @param location
	 *            the location
	 * @return All children in a JSON format
	 */
	public static JSONArray displayListOfChildren(EnOceanStorage enoceanStorage, String location) {
		JSONArray jsonChildren = new JSONArray();
		try {
			List<ChildDescription> children = enoceanStorage
					.listChildren(location);
			for (ChildDescription child : children) {
				JSONObject jsonChild = new JSONObject();
				jsonChild.put("name", child.getName());
				jsonChild.put("isDevice", child.isSensor());
				if (child.isSensor())
					jsonChild.put("url", child.getName() + "." + location + "."
							+ EnOceanConfig.getDNSZone() + "/*");
				else
					jsonChild.put("url", child.getName() + "." + location + "."
							+ EnOceanConfig.getDNSZone());
				jsonChildren.add(jsonChild);
			}

			return jsonChildren;
		} catch (Exception e) {
			logger.error("Error to get list measures with location: "
					+ location, e);
			return null;
		}
	}

	/**
	 * Add all measures of a sensor in a JSONArray
	 * 
	 * @param functionality
	 *            the functionality
	 * @param location
	 *            the location
	 * @return Measures in JSON
	 */
	public static JSONArray displayListOfMeasures(EnOceanStorage enoceanStorage, String functionality,
			String location) {
		try {
			List<MeasureDescription> measures = enoceanStorage.listMeasures(
					functionality, location);

			JSONArray jsonMeasures = new JSONArray();

			for (MeasureDescription measure : measures) {
				JSONObject jsonMeasure = new JSONObject();
				jsonMeasure.put("eep_shortcut", measure.getShortcut());
				jsonMeasure.put("unit", measure.getUnit());
				jsonMeasure.put("scale_max", measure.getScaleMax());
				jsonMeasure.put("scale_min", measure.getScaleMin());
				jsonMeasure.put("url", (functionality + "." + location + "."
						+ EnOceanConfig.getDNSZone() + "/" + measure
						.getShortcut()).toLowerCase());
				jsonMeasures.add(jsonMeasure);
			}

			return jsonMeasures;

		} catch (Exception e) {
			logger.error("Error to get list measures with functionality: "
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
	public static String displayActionValue(EnOceanStorage enoceanStorage, String functionality, String location,
			String action) {
		StorageData value = null;
		try {
			int idMeasure = enoceanStorage.findMeasure(functionality, location,
					action);

			value = enoceanStorage.getLastStorage(idMeasure);
		} catch (Exception e) {
			logger.error("Error to find measure with functionality: "
					+ functionality + " & location: " + location
					+ " & action: " + action, e);
		}

		return value == null ? null : value.getValue();
	}

	/**
	 * Get storage data for a measure by dates
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
	public static JSONArray getStorageByDates(EnOceanStorage enoceanStorage, String functionality, String location,
			String action, String from, String to) {
		int l_idMeasure;
		JSONArray l_result = new JSONArray();
		SimpleDateFormat l_df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		try {
			l_idMeasure = enoceanStorage.findMeasure(functionality, location,
					action);

			ArrayList<StorageData> l_sd = enoceanStorage.getStorage(
					l_idMeasure, l_df.parse(from), l_df.parse(to));
			for (StorageData l_data : l_sd) {
				JSONObject l_jsono = new JSONObject();
				l_jsono.put("value", l_data.getValue());
				l_jsono.put("timestamp", l_df.format(l_data.getDate()));
				l_result.add(l_jsono);
			}
		} catch (Exception e) {
			logger.error(
					"Error to read storage for measure with functionality: "
							+ functionality + " & location: " + location
							+ " & action: " + action + " & from: " + from
							+ " to: " + to, e);
		}

		return l_result;
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
	public static JSONArray getStorageByDays(EnOceanStorage enoceanStorage, String functionality, String location,
			String action, String days) {
		int l_days = Integer.parseInt(days);
		int l_idMeasure;
		JSONArray l_result = new JSONArray();
		SimpleDateFormat l_df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			l_idMeasure = enoceanStorage.findMeasure(functionality, location,
					action);

			ArrayList<StorageData> l_sd = enoceanStorage.getLastStorage(
					l_idMeasure, l_days);
			for (StorageData l_data : l_sd) {
				JSONObject l_jsono = new JSONObject();
				l_jsono.put("value", l_data.getValue());
				l_jsono.put("timestamp", l_df.format(l_data.getDate()));
				l_result.add(l_jsono);
			}
		} catch (Exception e) {
			logger.error(
					"Error to read storage for measure with functionality: "
							+ functionality + " & location: " + location
							+ " & action: " + action + " & days: " + l_days, e);
		}

		return l_result;
	}
}
