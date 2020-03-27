async function loadMapData() {
    return fetch('/heat')
        .then((response) => {
            return response.json();
        })
        .then((data) => {
            return data;
        });
}

window.onload = async function() {

    let mapData = await loadMapData();


    var baseLayer = L.tileLayer(
        'http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="http://cloudmade.com">CloudMade</a>',
            maxZoom: 10,
            noWrap: true
        },
    );

    var cfg = {
        // radius should be small ONLY if scaleRadius is true (or small radius is intended)
        "radius": 2,
        "maxOpacity": .8,
        // scales the radius based on map zoom
        "scaleRadius": true,
        // if set to false the heatmap uses the global maximum for colorization
        // if activated: uses the data maximum within the current map boundaries 
        //   (there will always be a red spot with useLocalExtremas true)
        "useLocalExtrema": true,
        // which field name in your data represents the latitude - default "lat"
        latField: 'lat',
        // which field name in your data represents the longitude - default "lng"
        lngField: 'lng',
        // which field name in your data represents the data value - default "value"
        valueField: 'count'
    };


    var heatmapLayer = new HeatmapOverlay(cfg);

    var map = new L.Map('map', {
        center: new L.LatLng(31.51073, -96.4247),
        zoom: 6,
        layers: [baseLayer, heatmapLayer]
    });

    let jsonData = null;

    fetch('/data')
    .then((response) => {
        return response.json();
    })
    .then((data) => {
        jsonData = data;
        for (var i = 0; i < jsonData.length; i++) {
            let json = jsonData[i];
            let option = document.createElement("p");
            option.classList.add("dropdown-item");
            let text = document.createTextNode(json.name);
            option.appendChild(text);
            option.classList.add("searchable")
            option.classList.add("font-weight-lighter");
            option.setAttribute("lat", json.latitude);
            option.setAttribute("lng", json.longitude);
            option.addEventListener('click', function() {
                setView(option.getAttribute("lat"), option.getAttribute("lng"));
            });
            document.getElementById("countries").appendChild(option);
        }
    });

    function setView(latitude, longitude) {
        console.log(latitude + " " + typeof(latitude) + " " + longitude + " " + typeof(longitude));
        map.setView({lat: latitude, lng:longitude}, 6);
    }


    var heatMapData = {
        max: 116000,
        data: mapData
    };

    console.log(heatMapData.data.length);

    heatmapLayer.setData(heatMapData);

    // make accessible for debugging
    layer = heatmapLayer;

};
