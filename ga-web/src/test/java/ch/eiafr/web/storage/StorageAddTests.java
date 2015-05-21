package ch.eiafr.web.storage;

import java.io.IOException;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;

import org.jdom2.JDOMException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import tuwien.auto.calimero.datapoint.Datapoint;
import tuwien.auto.calimero.datapoint.StateDP;

import ch.eiafr.knx.IKNXManagement;
import ch.eiafr.knx.KNXManagement;
import ch.eiafr.web.knx.KNXStorage;

@RunWith(value = Parameterized.class)
public class StorageAddTests {
	private int number;
	private IKNXManagement knxManagement = KNXManagement.getInstance();
	KNXStorage m_storage = KNXStorage.getInstance();

	public StorageAddTests(int number) {
		this.number = number;
	}

	@Parameters
	public static Collection<Object[]> data1() {
		Object[][] data = { { 1 } };

		return Arrays.asList(data);
	}

	@Test
	public void testAddStorageCallback() throws IOException {
		String xmlPath = "/Users/gb/Documents/EIAFR.xml";
		try {
			knxManagement.initDatapointLocator(xmlPath);
			Datapoint l_dp = knxManagement.findDatapoint("lampecouloir", "05.00.c.eia-fr", "dpt_switch");
			
			m_storage.openConnection("com.mysql.jdbc.Driver", "root", "", "jdbc:mysql://localhost/KNXGateway");
			m_storage.addStorage(l_dp, 20, "test", "dpt_switch");
			m_storage.closeConnection();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
