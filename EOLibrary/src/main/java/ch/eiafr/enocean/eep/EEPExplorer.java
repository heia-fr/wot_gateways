package ch.eiafr.enocean.eep;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import ch.eiafr.enocean.IEnoceanCommunicator;
import ch.eiafr.enocean.telegram.EEPTelegram;
import ch.eiafr.enocean.telegram.R_ORG;
import ch.eiafr.enocean.telegram.RadioTelegram;
import ch.eiafr.enocean.telegram.TelegramBuffers;

/**
 * Offer methods for decomposing telegrams and exploring the EEP
 * 
 * @author gb
 * 
 */
public class EEPExplorer {

	private static EEPExplorer instance;
	private Element rootElement;

	private EEPExplorer(String EEPXMLFile) throws JDOMException, IOException {
		rootElement = new SAXBuilder().build(new File(EEPXMLFile))
				.getRootElement();
	}

	/**
	 * Singleton for retrieving the instance
	 * 
	 * @param EEPXMLFile
	 *            The path to the EEP XML file
	 * @return The instance of the class
	 * @throws JDOMException
	 * @throws IOException
	 */
	public static EEPExplorer getInstance(String EEPXMLFile)
			throws JDOMException, IOException {
		if (instance == null)
			instance = new EEPExplorer(EEPXMLFile);

		return instance;
	}

	/**
	 * Decode a telegram according the the EEP number
	 * 
	 * @param radioTelegram
	 *            The telegram to decode
	 * @param RORG
	 *            The RORG number in hex representation (e.g. F6)
	 * @param function
	 *            The Function number in hex representation (e.g. 1F)
	 * @param type
	 *            The Type number in hex representation (e.g. 0A)
	 * @return The decoded telegram
	 */
	public EEPTelegram decodeRadioTelegram(RadioTelegram radioTelegram,
			String RORG, String function, String type) {
		EEPTelegram eepTelegram = new EEPTelegram(
				radioTelegram.getDataBuffer(), radioTelegram.getDataLength(),
				radioTelegram.getOptionLength());
		Map<String, Double> data = new HashMap<String, Double>();

		BitSet bits = byte2BitSet(eepTelegram.getDataBuffer(), 1, false);

		XPathExpression<Element> xpath = XPathFactory.instance().compile(
				"/eep/profile/rorg/number[. = '0x" + RORG.trim().toUpperCase()
						+ "']/../func/number[. = '0x"
						+ function.trim().toUpperCase()
						+ "']/../type/number[. = '0x"
						+ type.trim().toUpperCase() + "']/../case",
				Filters.element());
		List<Element> caseElements = xpath.evaluate(rootElement);

		for (Element caseElement : caseElements) {
			if (caseElement.getChild("direction") != null
					&& !caseElement.getChild("direction").getTextTrim()
							.equals("1"))
				continue;

			List<Element> statusFields = caseElement.getChildren("statusfield");

			// check status fields for right case
			boolean rightCase = true;
			BitSet statusBits = byte2BitSet(
					new byte[] { radioTelegram.getStatus() }, 0, false);
			for (Element statusField : statusFields) {
				int offset = Integer.parseInt(statusField.getChildText(
						"bitoffs").trim());
				int bitSize = Integer.parseInt(statusField.getChildText(
						"bitsize").trim());
				int value = Integer.parseInt(statusField.getChildText("value")
						.trim());

				if (bitSetToInt(statusBits.get(offset, offset + bitSize),
						bitSize) != value) {
					rightCase = false;
					break;
				}
			}

			if (!rightCase)
				continue;

			List<Element> elements = caseElement.getChildren("datafield");

			for (Element element : elements) {
				if (element.getChildText("data") == null
						|| element.getChildText("data").equals(""))
					continue;

				int offset = Integer.parseInt(element.getChildText("bitoffs")
						.trim());
				int bitSize = Integer.parseInt(element.getChildText("bitsize")
						.trim());
				double value = bitSetToInt(bits.get(offset, offset + bitSize),
						bitSize);
				if (element.getChild("range") != null
						&& element.getChild("scale") != null) {
					Element elmt = element.getChild("range");
					int rangeMin = Integer.parseInt(elmt.getChildText("min")
							.trim());
					int rangeMax = Integer.parseInt(elmt.getChildText("max")
							.trim());
					elmt = element.getChild("scale");
					try {
						double scaleMin = Double.parseDouble(elmt.getChildText(
								"min").trim());
						double scaleMax = Double
								.parseDouble(elmt.getChildText("max").trim()
										.startsWith("+") ? elmt
										.getChildText("max").trim()
										.substring(1) : elmt
										.getChildText("max").trim());
						value = value
								* ((scaleMax - scaleMin) / (rangeMax - rangeMin))
								+ scaleMin;
					} catch (Exception e) {

					}

				}
				data.put(element.getChildText("shortcut").trim(), value);
			}
		}
		eepTelegram.setData(data);
		return eepTelegram;
	}

	/**
	 * Get all RORG number and title from the EEP file
	 * 
	 * @return The RORGs with title
	 */
	public Map<String, String> getAllRORG() {
		Map<String, String> RORGs = new HashMap<String, String>();
		XPathExpression<Element> xpath = XPathFactory.instance().compile(
				"/eep/profile/rorg", Filters.element());
		List<Element> elements = xpath.evaluate(rootElement);
		for (Element element : elements) {
			if (!element.getChildText("number").equals("0x32"))
				RORGs.put(element.getChildText("number"),
						element.getChildText("title"));
		}
		return RORGs;
	}

	/**
	 * Get all Function number and title by RORG
	 * 
	 * @param RORG
	 *            The RORG number to find all sub functions, number in hex
	 *            representation (e.g. F6)
	 * @return The Functions with title
	 */
	public Map<String, String> getFunctionByRORG(String RORG) {
		Map<String, String> functions = new HashMap<String, String>();
		XPathExpression<Element> xpath = XPathFactory.instance().compile(
				"/eep/profile/rorg/number[. = '0x" + RORG.trim().toUpperCase()
						+ "']/../func", Filters.element());
		List<Element> elements = xpath.evaluate(rootElement);
		for (Element element : elements) {
			functions.put(element.getChildText("number"),
					element.getChildText("title"));
		}
		return functions;
	}

	/**
	 * Get all Type number and title by RORG and Function
	 * 
	 * @param RORG
	 *            The RORG number to find all sub functions, number in hex
	 *            representation (e.g. F6)
	 * @param function
	 *            The function number to find all sub types, number in hex
	 *            representation (e.g. 0F)
	 * @return The Types with title
	 */
	public Map<String, String> getTypeByRORGAndFunction(String RORG,
			String function) {
		Map<String, String> types = new HashMap<String, String>();
		XPathExpression<Element> xpath = XPathFactory.instance().compile(
				"/eep/profile/rorg/number[. = '0x" + RORG.trim().toUpperCase()
						+ "']/../func/number[. = '0x"
						+ function.trim().toUpperCase() + "']/../type",
				Filters.element());
		List<Element> elements = xpath.evaluate(rootElement);
		for (Element element : elements) {
			types.put(element.getChildText("number"),
					element.getChildText("title"));
		}
		return types;
	}

	/**
	 * Get all fields composing the corresponding EEP telegram
	 * 
	 * @param RORG
	 *            The RORG number in hex representation (e.g. F6)
	 * @param function
	 *            The Function number in hex representation (e.g. 1F)
	 * @param type
	 *            The Type number in hex representation (e.g. 0A)
	 * @param direction
	 * @return A Map of fields, Key is the shortcut and value is complete field
	 *         information
	 */
	public Map<String, EEPField> getEEPFieldsInfo(String RORG, String function,
			String type, int direction) {
		Map<String, EEPField> fields = null;
		XPathExpression<Element> xpath = XPathFactory.instance().compile(
				"/eep/profile/rorg/number[. = '0x" + RORG.trim().toUpperCase()
						+ "']/../func/number[. = '0x"
						+ function.trim().toUpperCase()
						+ "']/../type/number[. = '0x"
						+ type.trim().toUpperCase() + "']/../case",
				Filters.element());
		List<Element> elements = xpath.evaluate(rootElement);

		for (Element caseElement : elements) {
			if ((direction == IEnoceanCommunicator.RECEPTION && (caseElement
					.getChild("direction") == null || !caseElement
					.getChildText("direction").equals("2")))
					|| (direction == IEnoceanCommunicator.TRANSMISSION && (caseElement
							.getChild("direction") != null && !caseElement
							.getChildText("direction").equals("1"))))
				continue;
			List<Element> datafields = caseElement.getChildren("datafield");
			for (Element element : datafields) {
				if (element.getChild("reserved") != null)
					continue;

				if (fields == null)
					fields = new HashMap<String, EEPField>();

				ArrayList<FieldConversion> conversions = new ArrayList<FieldConversion>();
				double rangeMin = 0, rangeMax = 0, scaleMin = 0, scaleMax = 0;

				if (element.getChild("range") != null) {
					Element elmt = element.getChild("range");
					rangeMin = Double.parseDouble(elmt.getChildText("min")
							.trim());
					for (int i = 0; i < elmt.getChildText("max").split("or").length; i++) {
						rangeMax = Double.parseDouble(elmt.getChildText("max")
								.split("or")[i].trim());
						conversions.add(new FieldConversion());
						conversions.get(i).setValidRangeMax(rangeMax);
						conversions.get(i).setValidRangeMin(rangeMin);
					}
				}
				if (element.getChild("scale") != null) {
					Element elmt = element.getChild("scale");
					try {
						scaleMin = Double.parseDouble(elmt.getChildText("min")
								.trim());
						for (int i = 0; i < elmt.getChildText("max")
								.split("or").length; i++) {
							scaleMax = Double.parseDouble(elmt.getChildText(
									"max").split("or")[i].trim()
									.startsWith("+") ? elmt.getChildText("max")
									.split("or")[i].trim().substring(1) : elmt
									.getChildText("max").split("or")[i].trim());
							conversions.get(i).setScaleMax(scaleMax);
							conversions.get(i).setScaleMin(scaleMin);
						}
					} catch (Exception e) {
					}
				}

				if (element.getChild("unit") != null) {
					Element elmt = element.getChild("unit");
					for (int i = 0; i < elmt.getTextTrim().split("or").length; i++) {
						String unit = elmt.getTextTrim().split("or")[i].trim();
						conversions.get(i).setUnit(unit);
					}
				}

				Element posValues = element.getChild("enum");
				Map<Double, String> possibleValues = null;
				if (posValues != null) {
					possibleValues = new HashMap<Double, String>();
					List<Element> items = posValues.getChildren("item");
					for (Element posValue : items) {
						if (posValue.getChildText("value").trim()
								.startsWith("0x")) {
							int value = Integer.decode(posValue.getChildText(
									"value").trim());
							possibleValues.put(new Double(value),
									posValue.getChildText("description"));
						} else {
							possibleValues.put(new Double(posValue
									.getChildText("value").trim()), posValue
									.getChildText("description"));
						}
					}
				}

				EEPField field = new EEPField(
						Integer.parseInt(element.getChildText("bitoffs").trim()),
						Integer.parseInt(element.getChildText("bitsize").trim()),
						element.getChildText("data"), element
								.getChildText("shortcut"), element
								.getChildText("description"), conversions,
						possibleValues, false, 0);
				fields.put(element.getChildText("shortcut"), field);
			}
			List<Element> statusFields = caseElement.getChildren("statusfield");
			for (Element element : statusFields) {
				EEPField field = new EEPField(
						Integer.parseInt(element.getChildText("bitoffs").trim()),
						Integer.parseInt(element.getChildText("bitsize").trim()),
						element.getChildText("data"), null, null, null, null,
						true, 0);
				fields.put(element.getChildText("data"), field);
			}
		}
		return fields;
	}

	/**
	 * Get a field composing the corresponding EEP telegram
	 * 
	 * @param RORG
	 *            The RORG number in hex representation (e.g. F6)
	 * @param function
	 *            The Function number in hex representation (e.g. 1F)
	 * @param type
	 *            The Type number in hex representation (e.g. 0A)
	 * @param shortcut
	 *            The shortcut of the field
	 * @param direction
	 * @return Complete field information
	 */
	public EEPField getEEPFieldInfo(String RORG, String function, String type,
			String shortcut, int direction) {
		EEPField field = null;
		XPathExpression<Element> xpath = XPathFactory.instance().compile(
				"/eep/profile/rorg/number[. = '0x" + RORG.trim().toUpperCase()
						+ "']/../func/number[. = '0x"
						+ function.trim().toUpperCase()
						+ "']/../type/number[. = '0x"
						+ type.trim().toUpperCase()
						+ "']/../case/datafield/shortcut[. = '"
						+ shortcut.trim() + "']/..", Filters.element());
		Element element = xpath.evaluateFirst(rootElement);
		if (element != null) {
			if (direction == IEnoceanCommunicator.RECEPTION
					&& (element.getParentElement().getChild("direction") == null || !element
							.getParentElement().getChildText("direction")
							.equals("2")))
				return field;

			ArrayList<FieldConversion> conversions = new ArrayList<FieldConversion>();
			double rangeMin = 0, rangeMax = 0, scaleMin = 0, scaleMax = 0;

			if (element.getChild("range") != null) {
				Element elmt = element.getChild("range");
				rangeMin = Double.parseDouble(elmt.getChildText("min").trim());
				for (int i = 0; i < elmt.getChildText("max").split("or").length; i++) {
					rangeMax = Double.parseDouble(elmt.getChildText("max")
							.split("or")[i].trim());
					conversions.add(new FieldConversion());
					conversions.get(i).setValidRangeMax(rangeMax);
					conversions.get(i).setValidRangeMin(rangeMin);
				}
			}
			if (element.getChild("scale") != null) {
				Element elmt = element.getChild("scale");
				try {
					scaleMin = Double.parseDouble(elmt.getChildText("min")
							.trim());
					for (int i = 0; i < elmt.getChildText("max").split("or").length; i++) {
						scaleMax = Double.parseDouble(elmt.getChildText("max")
								.split("or")[i].trim().startsWith("+") ? elmt
								.getChildText("max").split("or")[i].trim()
								.substring(1) : elmt.getChildText("max").split(
								"or")[i].trim());
						conversions.get(i).setScaleMax(scaleMax);
						conversions.get(i).setScaleMin(scaleMin);
					}
				} catch (Exception e) {
				}
			}
			if (element.getChild("unit") != null) {
				Element elmt = element.getChild("unit");
				for (int i = 0; i < elmt.getTextTrim().split("or").length; i++) {
					String unit = elmt.getTextTrim().split("or")[i].trim();
					conversions.get(i).setUnit(unit);
				}
			}

			Element posValues = element.getChild("enum");
			Map<Double, String> possibleValues = null;
			if (posValues != null) {
				possibleValues = new HashMap<Double, String>();
				List<Element> items = posValues.getChildren("item");
				for (Element posValue : items) {
					if (posValue.getChildText("value").trim().startsWith("0x")) {
						int value = Integer.decode(posValue.getChildText(
								"value").trim());
						possibleValues.put(new Double(value),
								posValue.getChildText("description"));
					} else {
						possibleValues.put(
								new Double(posValue.getChildText("value")
										.trim()), posValue
										.getChildText("description"));
					}
				}
			}

			field = new EEPField(Integer.parseInt(element.getChildText(
					"bitoffs").trim()), Integer.parseInt(element.getChildText(
					"bitsize").trim()), element.getChildText("data"),
					element.getChildText("shortcut"),
					element.getChildText("description"), conversions,
					possibleValues, false, 0);
		}

		return field;
	}

	/**
	 * Build the data and optional data buffers according to the fields
	 * 
	 * @param RORG
	 *            RORG The RORG number
	 * @param data
	 *            The EEP fields to be included in the telegram
	 * @param destinationId
	 *            The destination Id
	 * @param senderId
	 *            The sender Id
	 * @return Data and optional data buffers
	 * @throws Exception
	 */
	public TelegramBuffers buildRadioDataBuffer(byte RORG, List<EEPField> data,
			int destinationId, int senderId) throws Exception {

		int dataSize = 0, optionSize = 7;

		switch (RORG) {
		case R_ORG.FOUR_BS:
			dataSize = 4;
			break;
		case R_ORG.ONE_BS:
		case R_ORG.RPS:
			dataSize = 1;
			break;
		case R_ORG.VLD:
			throw new Exception("Not yet supported");
		}
		BitSet dataBits = new BitSet(dataSize * 8);
		BitSet optionBits = new BitSet(optionSize * 8);
		BitSet statusBits = new BitSet(8);
		for (EEPField field : data) {
			if (field.isStatus()) {
				addValue2BitSet(statusBits, field.getValue(),
						field.getOffset(), field.getSize());
				continue;
			}
			addValue2BitSet(dataBits, field.getValue(), 8 + field.getOffset(),
					field.getSize());
		}
		// set RORG
		addValue2BitSet(dataBits, RORG & 0xFF, 0, 8);
		// set sender id
		addValue2BitSet(dataBits, senderId, 8 + 8 * dataSize, 32);
		// set status
		addValue2BitSet(dataBits, bitSetToInt(statusBits, 8) & 0xFF, 8 + 8
				* dataSize + 8 * 4, 8);

		// set SubTelNum
		addValue2BitSet(optionBits, 0x03, 0, 8);
		// set destination id
		addValue2BitSet(optionBits, destinationId, 8, 32);
		// set dBm
		addValue2BitSet(optionBits, 0xFF, 40, 8);

		byte[] dataBuffer = bitSet2ByteArray(dataBits, dataSize + 6);
		byte[] optionBuffer = bitSet2ByteArray(optionBits, optionSize);

		return new TelegramBuffers(dataBuffer, optionBuffer);
	}

	private BitSet addValue2BitSet(BitSet bits, int value, int offset, int size) {

		for (int i = 0; i < size; i++) {
			if ((value & 0x00000001) > 0)
				bits.set(offset + size - i - 1);
			value = value >> 1;
		}

		return bits;
	}

	private byte[] bitSet2ByteArray(BitSet bits, int size) {
		byte[] bytes = new byte[size];
		for (int i = 0; i < bits.length(); i++) {
			if (bits.get(i)) {
				bytes[i / 8] |= 1 << (7 - (i % 8));
			}
		}
		return bytes;
	}

	private BitSet byte2BitSet(byte[] b, int offset,
			boolean bitZeroMeansExtended) {
		/*
		 * int len = bitZeroMeansExtended ? ((b[offset] & 0x80) == 0x80 ? 128 :
		 * 64) : 64;
		 */
		int len = (b.length - offset) * 8;

		BitSet bmap = new BitSet(len);
		for (int i = 0; i < len; i++)
			if (((b[offset + (i >> 3)]) & (0x80 >> (i % 8))) > 0)
				bmap.set(i);
		return bmap;
	}

	private int bitSetToInt(BitSet bitSet, int size) {
		int bitInteger = 0;
		for (int i = 0; i < size; i++)
			if (bitSet.get(i))
				bitInteger |= (1 << size - 1 - i);

		return bitInteger;
	}
}
