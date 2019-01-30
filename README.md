# cordova-plugin-streaming-video-live

Plugin for streaming on live.

## Usage #

```typescript
  import { StreamingVideoLive, StreamingVideoLiveOptions } from '@ionic-native/streaming-video-live/ngx'';
 
  constructor(private streamingVideoLive: StreamingVideoLive) { }
 
  let options: StreamingVideoLiveOptions = {
    isTitle?: boolean; // default false
    title?: string; // disabled if isTitle == true
    color?: string; // disabled if isTitle == true; defaul = 'black'
  };
 
  this.streamingVideoLive.streaming('rtsp://ip:port/app_name/chanel_name', options)
  .then((res)  => console.log('res: ' + res))
  .catch((err) => console.log('err: ' + err));
```