package ch.eiafr.knx.utils;

import java.util.ArrayList;

import tuwien.auto.calimero.exception.KNXException;
import tuwien.auto.calimero.knxnetip.Discoverer;
import tuwien.auto.calimero.knxnetip.servicetype.SearchResponse;

public class KNXDiscoverer {

	/**
	 * Discover KNX IP gateways on the local network
	 * 
	 * @param p_SourcePort
	 *            The source port of the multicast
	 * @param p_Duration
	 *            The duration of the search
	 * @return An ArrayList of discovered gateways
	 * @throws KNXException 
	 */
	public static ArrayList<IPGateway> discoverGateways(int p_SourcePort,
			int p_Duration) throws KNXException {
		ArrayList<IPGateway> l_gateways = new ArrayList<IPGateway>();
		Discoverer l_disco = new Discoverer(p_SourcePort, true);
		l_disco.startSearch(p_Duration, true);

		SearchResponse[] l_results = l_disco.getSearchResponses();

		for (int i = 0; i < l_results.length; i++) {
			String l_ipAddr = l_results[i].getControlEndpoint().getAddress()
					.toString().substring(1);

			String l_KNXAddr = l_results[i].getDevice().getAddress().toString();
			short l_medium = l_results[i].getDevice().getKNXMedium();
			String l_name = l_results[i].getDevice().getName();

			l_gateways
					.add(new IPGateway(l_KNXAddr, l_ipAddr, l_name, l_medium));
		}

		return l_gateways;
	}

}
