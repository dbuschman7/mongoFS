package me.lightspeed7.mongofs.url;

public enum StorageFormat {

    GRIDFS(null, false, false), //
    GZIPPED("gz", true, false), //
    ENCRYPTED("enc", false, true), //
    ENCRYPTED_GZIP("encgz", true, true) //
    //
    /* */;

    private final String code;
    private final boolean compressed;
    private final boolean encrypted;

    private StorageFormat(final String code, final boolean compressed, final boolean encrypted) {
        this.code = code;
        this.compressed = compressed;
        this.encrypted = encrypted;
    }

    public String getCode() {
        return code;
    }

    public boolean isCompressed() {
        return compressed;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public static final StorageFormat find(final String str) {

        if (str == null) {
            return GRIDFS;
        }

        for (StorageFormat fmt : StorageFormat.values()) {
            if (fmt.name().equals(str)) {
                return fmt;
            }

            if (fmt.getCode() != null && fmt.getCode().equals(str)) {
                return fmt;
            }
        }
        return null;
    }

    public static final StorageFormat detect(final boolean compress, final boolean encrypt) {
        for (StorageFormat fmt : StorageFormat.values()) {
            if (fmt.isCompressed() == compress && fmt.isEncrypted() == encrypt) {
                return fmt;
            }
        }
        return GRIDFS;
    }
}
