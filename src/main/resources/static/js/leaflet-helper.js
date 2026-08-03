/**
 * Leaflet Helper - Reusable map initialization utilities
 * For Online Pharmacy Management System
 */

const LeafletHelper = {
    /**
     * Default configuration
     */
    config: {
        defaultCenter: [13.0827, 80.2707], // Chennai, India
        defaultZoom: 13,
        minZoom: 3,
        maxZoom: 19,
        tileLayerUrl: 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
        tileLayerAttribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
    },

    /**
     * Initialize a basic map
     * @param {string} containerId - ID of the map container
     * @param {object} options - Optional configuration overrides
     * @returns {object} - Leaflet map instance
     */
    initMap: function(containerId, options = {}) {
        const config = { ...this.config, ...options };
        
        // Initialize map
        const map = L.map(containerId).setView(
            config.center || config.defaultCenter, 
            config.zoom || config.defaultZoom
        );
        
        // Add OpenStreetMap tile layer
        L.tileLayer(config.tileLayerUrl, {
            attribution: config.tileLayerAttribution,
            maxZoom: config.maxZoom,
            minZoom: config.minZoom
        }).addTo(map);
        
        return map;
    },

    /**
     * Add a marker to the map
     * @param {object} map - Leaflet map instance
     * @param {array} latLng - [latitude, longitude]
     * @param {object} options - Marker options (popup, icon, etc.)
     * @returns {object} - Leaflet marker instance
     */
    addMarker: function(map, latLng, options = {}) {
        const marker = L.marker(latLng, options.markerOptions || {}).addTo(map);
        
        if (options.popup) {
            marker.bindPopup(options.popup);
            if (options.openPopup) {
                marker.openPopup();
            }
        }
        
        return marker;
    },

    /**
     * Create a custom icon
     * @param {object} options - Icon options
     * @returns {object} - Leaflet icon instance
     */
    createIcon: function(options = {}) {
        return L.icon({
            iconUrl: options.iconUrl || 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
            iconSize: options.iconSize || [25, 41],
            iconAnchor: options.iconAnchor || [12, 41],
            popupAnchor: options.popupAnchor || [1, -34],
            shadowUrl: options.shadowUrl || 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
            shadowSize: options.shadowSize || [41, 41]
        });
    },

    /**
     * Fit map bounds to show all markers
     * @param {object} map - Leaflet map instance
     * @param {array} markers - Array of marker instances
     */
    fitBounds: function(map, markers) {
        if (markers && markers.length > 0) {
            const group = L.featureGroup(markers);
            map.fitBounds(group.getBounds().pad(0.1));
        }
    },

    /**
     * Get user's current location (requires user permission)
     * @param {function} callback - Callback function(latitude, longitude)
     * @param {function} errorCallback - Error callback
     */
    getCurrentLocation: function(callback, errorCallback) {
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(
                function(position) {
                    callback(position.coords.latitude, position.coords.longitude);
                },
                function(error) {
                    if (errorCallback) {
                        errorCallback(error);
                    } else {
                        console.error('Geolocation error:', error);
                    }
                }
            );
        } else {
            if (errorCallback) {
                errorCallback(new Error('Geolocation not supported'));
            }
        }
    }
};

// Export for use in modules if needed
if (typeof module !== 'undefined' && module.exports) {
    module.exports = LeafletHelper;
}
