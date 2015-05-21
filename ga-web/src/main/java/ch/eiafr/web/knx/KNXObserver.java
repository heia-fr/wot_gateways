package ch.eiafr.web.knx;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.eiafr.knx.utils.DatapointEvent;

/**
 * Manage the update from the KNX Library
 * 
 * @author sebastien baudin
 * 
 */
public class KNXObserver implements Observer {
	private static final Logger logger = LoggerFactory
			.getLogger(KNXObserver.class);
	private KNXRegisters knxRegisters = KNXRegisters.getInstance();

	@Override
	public void update(Observable o, Object arg) {
		DatapointEvent datapointEvent = (DatapointEvent) arg;
		String datapointUrl = datapointEvent.getUrl();
		String data = datapointEvent.getValue();

		logger.debug("Update datapointEvent (" + datapointUrl
				+ ") with value: " + data);

		List<String> callbackUrls = knxRegisters
				.getAllRegisteredCallbacks(datapointUrl);

		for (String callbackUrl : callbackUrls) {
			logger.debug("Send post request to " + callbackUrl);
			try {
				URL url = new URL(callbackUrl);
				HttpURLConnection urlConnection = (HttpURLConnection) url
						.openConnection();
				urlConnection.setRequestMethod("POST");
				urlConnection.setDoOutput(true);
				urlConnection.setRequestProperty("Content-Type", "text/plain");
				urlConnection.setRequestProperty("charset", "utf-8");
				urlConnection.setRequestProperty("Referer", datapointUrl);
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
				logger.error("Post error", e);
			} catch (IOException e) {
				logger.error("Post error", e);
			}
		}

	}
}
