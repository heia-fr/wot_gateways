package ch.eiafr.web.enocean;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage the update from the EnOcean library
 * 
 * @author Gerome Bovet
 * 
 */
public class EnOceanClientNotifier {
	private static final Logger logger = LoggerFactory
			.getLogger(EnOceanClientNotifier.class);
	private static EnOceanRegisters enoceanRegisters = EnOceanRegisters.getInstance();

	public static void notifyClient(String measureUrl, double value) {
		List<String> callbackUrls = enoceanRegisters
				.getAllRegisteredCallbacks(measureUrl);

		for (String callbackUrl : callbackUrls) {
			logger.debug("Send put request to " + callbackUrl);
			try {
				String data = new Double(value).toString();
				URL url = new URL(callbackUrl);
				HttpURLConnection urlConnection = (HttpURLConnection) url
						.openConnection();
				urlConnection.setRequestMethod("PUT");
				urlConnection.setDoOutput(true);
				urlConnection.setRequestProperty("Content-Type", "text/plain");
				urlConnection.setRequestProperty("charset", "utf-8");
				urlConnection.setRequestProperty("Referer", measureUrl);
				urlConnection.setRequestProperty("Content-Length",
						data.length() + "");

				OutputStreamWriter writer = new OutputStreamWriter(
						urlConnection.getOutputStream());
				writer.write(data);
				writer.flush();
				writer.close();

				urlConnection.getResponseCode();

				urlConnection.disconnect();
			} catch (MalformedURLException e) {
				logger.error("Put error", e);
			} catch (IOException e) {
				logger.error("Put error", e);
			}
		}

	}
}
