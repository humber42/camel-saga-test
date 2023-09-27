package cu.hash.camelsagatest;


import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;


public class NetworkUtils {

	public static String getInet4AddressFor(String interfaceName) throws SocketException {

		Enumeration<NetworkInterface> ifacesEnum = NetworkInterface.getNetworkInterfaces();
		while (ifacesEnum.hasMoreElements()) {
			NetworkInterface iface = ifacesEnum.nextElement();

			if (!interfaceName.equals(iface.getDisplayName()) && !interfaceName.equals(iface.getName())) {
				continue;
			}

			Enumeration<InetAddress> inetAddressesEnum = iface.getInetAddresses();
			while (inetAddressesEnum.hasMoreElements()) {
				InetAddress inetAddress = inetAddressesEnum.nextElement();
				
				if (!(inetAddress instanceof Inet4Address)) {
					continue;
				}
				
				return inetAddress.getHostAddress();
			}
		}
		
		return null;
		
	}
	
}
