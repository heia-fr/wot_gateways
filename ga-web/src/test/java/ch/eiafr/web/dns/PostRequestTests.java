package ch.eiafr.web.dns;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class PostRequestTests {
	private int number;

	public PostRequestTests(int number) {
		this.number = number;
	}

	@Parameters
	public static Collection<Object[]> data1() {
		Object[][] data = { { 1 } };

		return Arrays.asList(data);
	}

	@Test
	public void testNormalRegisterCallback() throws IOException {
		try {
			String data = "data";
			URL url = new URL("http://192.168.1.37:6969/callback");
			HttpURLConnection urlConnection = (HttpURLConnection) url
					.openConnection();
			urlConnection.setRequestMethod("POST");
			urlConnection.setDoOutput(true);
			urlConnection.setRequestProperty("Content-Type", "text/plain");
			urlConnection.setRequestProperty("charset", "utf-8");
			urlConnection.setRequestProperty("Referer",
					"http://lampecouloir.05.00.c.eia-fr.ch/dpt_switch");
			urlConnection.setRequestProperty("Content-Length", data.length()
					+ "");

			OutputStreamWriter writer = new OutputStreamWriter(
					urlConnection.getOutputStream());
			writer.write(data);
			writer.flush();
			writer.close();

			urlConnection.getResponseCode();

			urlConnection.disconnect();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
