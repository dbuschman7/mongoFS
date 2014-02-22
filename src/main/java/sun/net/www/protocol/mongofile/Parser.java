package sun.net.www.protocol.mongofile;

import java.net.MalformedURLException;
import java.net.URL;

import me.lightspeed7.mongofs.CompressionMediaTypes;
import me.lightspeed7.mongofs.MongoFile;

public class Parser {

    public static final URL construct(String id, String fileName, String mediaType)
            throws MalformedURLException {

        String protocol = MongoFile.PROTOCOL;
        if (CompressionMediaTypes.isCompressable(mediaType)) {
            protocol += ":" + MongoFile.GZ;
        }
        return construct(String.format("%s:%s?%s#%s", protocol, fileName, id, mediaType.toString()));
    }

    public static URL construct(String spec)
            throws MalformedURLException {

        return new URL(null, spec, new Handler());
    }

}
