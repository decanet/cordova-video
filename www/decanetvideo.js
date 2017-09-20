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
	startRecording : function(filename, duration, successFunction, errorFunction) {
		cordova.exec(successFunction, errorFunction, 'decanetvideo', 'startrecording', [filename, duration]);
	},
    stop : function(successFunction, errorFunction) {
        cordova.exec(successFunction, errorFunction, 'decanetvideo','stop', []);
    }
};

module.exports = decanetvideo;
