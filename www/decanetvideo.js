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
	stopRecording : function(successFunction, errorFunction) {
		cordova.exec(successFunction, errorFunction, 'decanetvideo', 'stoprecording', []);
	},
    stop : function(successFunction, errorFunction) {
        cordova.exec(successFunction, errorFunction, 'decanetvideo','stop', []);
    },
	execFFMPEG : function(success, error, options) {
	  var self = this;
	  var win = function(result) {
		if (typeof result.progress !== 'undefined') {
		  if (typeof options.progress === 'function') {
			options.progress(result.progress);
		  }
		} else {
		  success(result);
		}
	  };
	  cordova.exec(win, error, 'decanetvideo', 'execFFMPEG', [options]);
	}
};

module.exports = decanetvideo;
