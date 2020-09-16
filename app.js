const express = require('express');
let app = express();
const path = require('path');
const bodyParser = require('body-parser');
const fs = require('fs');

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({extended: true}));
app.use("/", express.static(path.join(__dirname, "/public")));
app.use("/", express.static(path.join(__dirname, "/node_modules")));

app.get("/", (req, res) => {
    console.log("Route hit");
    res.sendFile('index.html');
});

app.get('/data', (req, res) => {
    fs.readFile('./heatmap.json', (err, data) => {
        if (err) throw err;
        res.send(JSON.parse(data));
     });
});

//This route provides test data for viewing the maps configuration and current state
app.get('/mapdata', (req, res) => {
    fs.readFile('./heatmap.json', (err, d) => {
        if (err) throw err;
        let data = JSON.parse(d);
        let mapData = [];
        for (var i = 0; i < data.length; i++) {
            let json = data[i];
            var sw =  {lat: json.boundingBox.sw.latitude, lng: json.boundingBox.sw.longitude};
            var ne = {lat: json.boundingBox.ne.latitude, lng: json.boundingBox.ne.longitude};
            var heatmapDataPoints = generateRandomData(1000, sw, ne);
            var heat = randomNumber(json.dayToCases[Object.keys(json.dayToCases)[0]], json.dayToCases[Object.keys(json.dayToCases)[Object.keys(json.dayToCases).length-1]]);

            for (var j = 0; j < heatmapDataPoints.length; j++) {
                let current = heatmapDataPoints[j];
                let lati = current[0];
                let longi = current[1];
                mapData.push( {
                    lat: lati,
                    lng: longi,
                    count: heat
                });
            }
        }
        res.send(JSON.stringify(mapData));
        console.log(mapData.length);
     });
});

app.get('/heat', (req, res) => {
    fs.readFile('./heatpoints.json', (err, d) => {
        if (err) throw err;
        let data = JSON.parse(d);
        let mapData = [];
        for (var i = 0; i < data.length; i++) {
            let json = data[i];
            var heat = json.heat;
            var latitude = json.latitude;
            var longitude = json.longitude;
            mapData.push( {
                lat: latitude,
                lng: longitude,
                count: heat
            });
        }
        res.send(JSON.stringify(mapData));
     });
});

function randomNumber(min, max) {  
    return Math.random() * (max - min) + min; 
}  
  
function generateRandomData(count, sw, ne) {
    var points = [];
    for (var i = 0; i < count; ++i) {
        var lat = Math.random() * (ne.lat - sw.lat) + sw.lat;
        var lng = Math.random() * (ne.lng - sw.lng) + sw.lng;
        points.push([lat, lng]);
    }

    return points;
};

module.exports = app;
