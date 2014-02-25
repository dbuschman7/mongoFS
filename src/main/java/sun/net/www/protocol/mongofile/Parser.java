package sun.net.www.protocol.mongofile;

import java.net.MalformedURLException;
import java.net.URL;

import me.lightspeed7.mongofs.CompressionMediaTypes;
import me.lightspeed7.mongofs.MongoFileUrl;

public class Parser {

    public static final URL construct(String id, String fileName, String mediaType, String compressionFormat)
            throws MalformedURLException {

        String protocol = MongoFileUrl.PROTOCOL;
        String append = "";
        if (compressionFormat != null) {
            protocol += ":" + compressionFormat;
        } else {
            if (CompressionMediaTypes.isCompressable(mediaType)) {
                protocol += ":" + MongoFileUrl.GZ;
            }
        }
        return construct(String.format("%s:%s?%s#%s", protocol, fileName, id, mediaType.toString()));
    }

    public static URL construct(String spec)
            throws MalformedURLException {

        return new URL(null, spec, new Handler());
    }

}
