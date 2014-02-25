package sun.net.www.protocol.mongofile;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

import me.lightspeed7.mongofs.MongoFileUrl;

public class MongoFileURLStreamHandlerFactory implements URLStreamHandlerFactory {

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {

        if (protocol.equals(MongoFileUrl.PROTOCOL)) {
            return new Handler();
        }
        return null;
    }

}
