var cordova = require('cordova');

var decanetvideo = {
	startPreview : function(camera, quality, successFunction, errorFunction) {
        camera = camera || 'front';
        cordova.exec(successFunction, errorFunction, 'decanetvideo', 'startpreview', [camera, quality]);
        window.document.body.style.opacity = .99;
        setTimeout(function () {
          window.document.body.style.opacity = 1;
        }, 23)
    },
    start : function(fileStorage, filename, camera, quality, successFunction, errorFunction) {
        camera = camera || 'front';
        cordova.exec(successFunction, errorFunction, 'decanetvideo', 'start', [fileStorage, filename, camera, quality]);
        window.document.body.style.opacity = .99;
        setTimeout(function () {
          window.document.body.style.opacity = 1;
        }, 23)
    },
    stop : function(successFunction, errorFunction) {
        cordova.exec(successFunction, errorFunction, 'decanetvideo','stop', []);
    }
};

module.exports = decanetvideo;
