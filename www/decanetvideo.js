var cordova = require('cordova');
var pluginName = 'DecanetVideo';

var decanetvideo = {
	startPreview : function(camera, quality, successFunction, errorFunction) {
        camera = camera || 'front';
        cordova.exec(successFunction, errorFunction, 'decanetvideo', 'startpreview', [camera, quality]);
        window.document.body.style.opacity = .99;
        setTimeout(function () {
          window.document.body.style.opacity = 1;
        }, 23)
    },
	startRecording : function(filename, successFunction, errorFunction) {
		cordova.exec(successFunction, errorFunction, 'decanetvideo', 'startrecording', [filename]);
	},
	stopRecording : function(successFunction, errorFunction) {
		cordova.exec(successFunction, errorFunction, 'decanetvideo', 'stoprecording', []);
	},
    stop : function(successFunction, errorFunction) {
        cordova.exec(successFunction, errorFunction, 'decanetvideo','stop', []);
    },
	transcodeVideo : function(success, error, options) {
		cordova.exec(success, error, 'decanetvideo', 'transcodeVideo', [options]);
	}
	
	
};

module.exports = decanetvideo;
