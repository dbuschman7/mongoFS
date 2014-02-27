package sun.net.www.protocol.mongofile;

import java.net.MalformedURLException;
import java.net.URL;

import me.lightspeed7.mongofs.CompressionMediaTypes;
import me.lightspeed7.mongofs.MongoFileUrl;

public class Parser {

    public static final URL construct(String id, String fileName, String mediaType, String compressionFormat,
            boolean compress)
            throws MalformedURLException {

        String protocol = MongoFileUrl.PROTOCOL;
        if (compressionFormat != null) {
            protocol += ":" + compressionFormat;
        } else {
            if (compress && CompressionMediaTypes.isCompressable(mediaType)) {
                protocol += ":" + MongoFileUrl.GZ;
            }
        }
        return construct(String.format("%s:%s?%s#%s", protocol, fileName, id,
                mediaType == null ? "" : mediaType.toString()));
    }

    public static URL construct(String spec)
            throws MalformedURLException {

        return new URL(null, spec, new Handler());
    }

}
