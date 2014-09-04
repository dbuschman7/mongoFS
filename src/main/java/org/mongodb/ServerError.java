package org.mongodb;

import org.bson.BSONObject;

public final class ServerError {

    static String getMsg(final BSONObject o, final String def) {
        Object e = o.get("$err");
        if (e == null) {
            e = o.get("err");
        }
        if (e == null) {
            e = o.get("errmsg");
        }
        if (e == null) {
            return def;
        }
        return e.toString();
    }

    static int getCode(final BSONObject o) {
        Object c = o.get("code");
        if (c == null) {
            c = o.get("$code");
        }
        if (c == null) {
            c = o.get("assertionCode");
        }
        if (c == null) {
            return -5;
        }
        return ((Number) c).intValue();
    }

    private ServerError() {
        // empty
    }
}
