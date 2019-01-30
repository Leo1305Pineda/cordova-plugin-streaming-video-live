var StreamingVideoLive = function() {};

StreamingVideoLive.prototype.streaming = function(success, fail) {
  cordova.exec(success, fail, 'StreamingVideoLive', 'streaming', []);
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