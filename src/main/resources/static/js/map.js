var geocoder = new google.maps.Geocoder();
var geocodeInterval = null;

var map = null;
var marker = null;

var initialize = function () {
  var mapOptions = {
    center: mapCenter,
    zoom: 6,
    zoomControl: true,
    zoomControlOptions: {
      style: google.maps.ZoomControlStyle.SMALL
    },
    disableDoubleClickZoom: true,
    mapTypeControl: false,
    scaleControl: false,
    scrollwheel: false,
    streetViewControl: false,
    panControl: false,
    draggable: true,
    overviewMapControl: false,
    mapTypeId: google.maps.MapTypeId.ROADMAP
  };

  map = new google.maps.Map(document.getElementById("map-canvas"), mapOptions);

  // marker iniziale
  marker = new google.maps.Marker({
    animation: google.maps.Animation.DROP,
    draggable: true,
    icon: iconMarker,
    map: map,
    position: mapCenter
  });

  // add dragging event listeners.
  google.maps.event.addListener(marker, 'drag', function () {
    updateLatLngFields(marker.getPosition());
  });

  google.maps.event.addListener(marker, 'dragend', function () {
    geocodePosition(marker.getPosition());
  });

  if(rawAddressField.val() != null){
    geocodeAddress();
  }
};

// geocodifica
var geocodePosition = function (pos) {
  geocoder.geocode({'latLng': pos}, function (results, status) {
    if (status == 'OK' && results.length > 0) {
      updateAddressFields(results[0]);
    } else {
      console.log('Geocode was not successful for the following reason: ' + status);
    }
  });
}

// aggiornamento campi Lat e Lng
var updateLatLngFields = function (latLng) {
  latField.val(latLng.lat());
  lngField.val(latLng.lng());
}

// aggiornamento campo Indirizzo
var updateAddressFields = function (result) {
  var address, city, country;

  address = result.formatted_address;
  for (var i = 0; i < result.address_components.length; i++) {
    var component = result.address_components[i];
    var type = component.types[0];
    if (type === 'country') {
      country = component.long_name;
    } else if (type === 'administrative_area_level_3' || type === 'locality') {
      city = component.long_name;
    }
  }

  addressField.val(address);
  cityField.val(city);
  countryField.val(country);
}

// aggiornamento mappa in relazione alla via inserita
rawAddressField.blur(function () {
  if (geocodeInterval != null) {
    window.clearTimeout(geocodeInterval);
  }
  geocodeAddress();
});

rawAddressField.keypress(function () {
  if (geocodeInterval != null) {
    window.clearTimeout(geocodeInterval);
  }
  geocodeInterval = window.setTimeout(function () {
    geocodeAddress()
  }, 500);
});

var geocodeAddress = function () {
  var rawAddress = rawAddressField.val();

  geocoder.geocode({'address': rawAddress, 'partialmatch': true}, function (results, status) {
    if (status == 'OK' && results.length > 0) {
      console.log(results);
      var result = results[0];
      map.fitBounds(result.geometry.viewport);
      map.setCenter(result.geometry.location);
      marker.setPosition(result.geometry.location);

      // aggiorna i vari campi
      updateLatLngFields(result.geometry.location);
      updateAddressFields(result);
    } else {
      console.log('Geocode was not successful for the following reason: ' + status);
    }
  });
}