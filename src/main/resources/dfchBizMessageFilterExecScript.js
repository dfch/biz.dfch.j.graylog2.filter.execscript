print("Now this is a message: " + message + "\r\n");

// print some message properties
//print(message.id + "\r\n");
//print(message.source + "\r\n");
//print(message.message + "\r\n");

// get specific additional field in message
//print(message.getField("guid") + "\r\n");

// add field
//message.addField("FullName", "Edgar Schnittenfittich);

// remove a field (does not throw an error, even if it does not exist)
//message.removeField("myCustomField");

// get stream of message (only if filter priority > 40 / StreamMatcher has already run, 
// or if other plugin has already assigned streams 
// this can be used to have filters only work on specific streams
var streams = message.getStreams();
astreams = streams.toArray();
for(c = 0; c < astreams.length; c++)
{
        var stream = astreams[c];
// set first stream name and id as property
//      message.addField("DF_STREAM_ID", stream.id);
//      message.addField("DF_STREAM_NAME", stream.title);
        break;
}
