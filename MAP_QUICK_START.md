# 🗺️ Leaflet Map Quick Start Guide

## 5-Minute Integration

### Step 1: Add to Your Template

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <th:block th:replace="~{fragments/header :: head}"></th:block>
    
    <!-- Add Leaflet CSS -->
    <th:block th:replace="~{fragments/leaflet :: leaflet-css}"></th:block>
    <link rel="stylesheet" th:href="@{/css/leaflet-custom.css}">
</head>
<body>
    <nav th:replace="~{fragments/header :: navbar}"></nav>
    
    <!-- Your map container -->
    <div class="container my-5">
        <div id="myMap" class="map-container"></div>
    </div>
    
    <footer th:replace="~{fragments/header :: footer}"></footer>
    
    <!-- Standard scripts -->
    <div th:replace="~{fragments/header :: scripts}"></div>
    
    <!-- Add Leaflet JS -->
    <div th:replace="~{fragments/leaflet :: leaflet-js}"></div>
    <script th:src="@{/js/leaflet-helper.js}"></script>
    
    <!-- Initialize your map -->
    <script>
        document.addEventListener('DOMContentLoaded', function() {
            // Create map
            const map = LeafletHelper.initMap('myMap');
            
            // Add a marker
            LeafletHelper.addMarker(map, [13.0827, 80.2707], {
                popup: '<b>My Location</b>',
                openPopup: true
            });
        });
    </script>
</body>
</html>
```

### Step 2: That's It! 🎉

Your map is now fully functional with:
- ✅ Zoom controls
- ✅ Drag & pan
- ✅ OpenStreetMap tiles
- ✅ Responsive design

---

## Common Examples

### Example 1: Basic Map
```javascript
const map = LeafletHelper.initMap('mapId');
```

### Example 2: Custom Location
```javascript
const map = LeafletHelper.initMap('mapId', {
    center: [13.0827, 80.2707],  // Chennai
    zoom: 15
});
```

### Example 3: Add Marker with Popup
```javascript
LeafletHelper.addMarker(map, [13.0827, 80.2707], {
    popup: '<strong>Pharmacy Name</strong><br>Address here',
    openPopup: true
});
```

### Example 4: Multiple Markers
```javascript
const markers = [];

markers.push(LeafletHelper.addMarker(map, [13.0827, 80.2707], {
    popup: 'Location 1'
}));

markers.push(LeafletHelper.addMarker(map, [13.0900, 80.2800], {
    popup: 'Location 2'
}));

// Fit map to show all markers
LeafletHelper.fitBounds(map, markers);
```

### Example 5: Get User's Current Location
```javascript
LeafletHelper.getCurrentLocation(
    function(lat, lng) {
        const map = LeafletHelper.initMap('mapId', {
            center: [lat, lng],
            zoom: 15
        });
        LeafletHelper.addMarker(map, [lat, lng], {
            popup: 'You are here!'
        });
    },
    function(error) {
        console.error('Location error:', error);
        // Fallback to default location
        const map = LeafletHelper.initMap('mapId');
    }
);
```

---

## Map Container Sizes

```html
<!-- Small map (300px) -->
<div id="map1" class="map-container map-container-small"></div>

<!-- Medium map (500px) - Default -->
<div id="map2" class="map-container"></div>

<!-- Large map (700px) -->
<div id="map3" class="map-container map-container-large"></div>

<!-- Fullscreen map -->
<div id="map4" class="map-container map-container-fullscreen"></div>
```

---

## Helper Functions Reference

### `LeafletHelper.initMap(containerId, options)`
Initialize a new map instance.

**Parameters:**
- `containerId` (string): ID of the container element
- `options` (object, optional):
  - `center` [lat, lng]: Map center coordinates
  - `zoom` (number): Initial zoom level
  - `minZoom` (number): Minimum zoom level
  - `maxZoom` (number): Maximum zoom level

**Returns:** Leaflet map object

---

### `LeafletHelper.addMarker(map, latLng, options)`
Add a marker to the map.

**Parameters:**
- `map` (object): Leaflet map instance
- `latLng` (array): [latitude, longitude]
- `options` (object, optional):
  - `popup` (string): HTML content for popup
  - `openPopup` (boolean): Open popup immediately
  - `markerOptions` (object): Leaflet marker options

**Returns:** Leaflet marker object

---

### `LeafletHelper.fitBounds(map, markers)`
Adjust map to show all markers.

**Parameters:**
- `map` (object): Leaflet map instance
- `markers` (array): Array of marker objects

---

### `LeafletHelper.getCurrentLocation(callback, errorCallback)`
Get user's current location (requires permission).

**Parameters:**
- `callback` (function): Success function(lat, lng)
- `errorCallback` (function): Error function(error)

---

## Demo Page

Visit the demo page to see Leaflet in action:
```
http://localhost:8080/map-demo
```

---

## Need Help?

- 📖 Full documentation: `LEAFLET_INTEGRATION.md`
- 🔧 Helper source: `static/js/leaflet-helper.js`
- 🎨 Styles: `static/css/leaflet-custom.css`
- 📦 Fragments: `templates/fragments/leaflet.html`

---

**Happy Mapping! 🗺️**
