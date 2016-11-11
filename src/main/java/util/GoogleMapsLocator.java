package util;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.AddressComponent;
import com.google.maps.model.AddressComponentType;
import com.google.maps.model.GeocodingResult;

import entity.Locator;

/**
 * Helper class to get correct address using google geocoding API.
 * @author vinid
 *
 */
public class GoogleMapsLocator {
	private GeoApiContext context;
	
	/**
	 * Constructor
	 */
	public GoogleMapsLocator() {
		context = new GeoApiContext().setApiKey("INSERT KEY-INSERT KEY");
	}
	
	/**
	 * Returns the complete location of a string given as address
	 * @param location
	 * @return
	 * @throws Exception
	 */
	public Locator getLocationData(String location)  {
		
		GeocodingResult[] results;
		try {
			results = GeocodingApi.geocode(context,
					location).await();


		double latitude = results[0].geometry.location.lat;
		double longitude = results[0].geometry.location.lng;
		String address = results[0].formattedAddress;
		String locality = "";
		String country = "";
		for (AddressComponent ac : results[0].addressComponents) {
			for (AddressComponentType type : ac.types) {
				if(type.name().equalsIgnoreCase("locality")) {
					System.out.println("Località trovata: " + ac.longName);
					locality = ac.longName;
				}
				if(type.name().equalsIgnoreCase("country")) {
					System.out.println("Nazione trovata: " + ac.longName);
					country = ac.longName;
				}
			}
		}
		
		Locator lc = new Locator();
		
		lc.setAddress(address);
		lc.setLongitude(longitude);
		lc.setLatitude(latitude);
		lc.setLocality(locality);
		lc.setCountry(country);
		return lc;
		} catch (Exception e) {
			//Se si genera un eccezione ritorno nullo

			return null;			 
		}
		
	}
}
