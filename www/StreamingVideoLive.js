var StreamingVideoLive = function() {};

var PLUGIN_NAME = 'StreamingVideoLive';

StreamingVideoLive.prototype.streaming = function(url, options) {
  cordova.exec(options.successCallback, options.errorCallback, PLUGIN_NAME, 'streaming', [url, options]);
};

StreamingVideoLive.prototype.powerOff = function(successCallback, errorCallback) {
  cordova.exec(successCallback, errorCallback, PLUGIN_NAME, 'powerOff', []);
};

StreamingVideoLive.prototype.resize = function(preview) {
  cordova.exec(null, null, PLUGIN_NAME, 'resize', [preview]);
};

if (!window.plugins) {
  window.plugins = {};
}

if (!window.plugins.streamingVideoLive) {
  window.plugins.streamingVideoLive = new StreamingVideoLive();
}

if (module.exports) {
  module.exports = StreamingVideoLive;
}