package me.lightspeed7.mongofs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import me.lightspeed7.mongofs.util.FileUtil;

public class MongoZipFileExpander {

    private MongoFileStore store;
    private MongoFile zip;

    /* package */MongoZipFileExpander(final MongoFileStore store, final MongoFile zip) {
        this.store = store;
        this.zip = zip;
    }

    public MongoManifest expandFrom(final InputStream in) throws IOException {

        if (in == null) {
            throw new IllegalArgumentException("passed inputStream cannot be null");
        }

        MongoManifest manifest = new MongoManifest(zip);
        zip.put(MongoFileConstants.manifestId, zip.get(MongoFileConstants._id));
        zip.put(MongoFileConstants.manifestNum, 0);

        // start the stream
        ZipInputStream zipStream = new ZipInputStream(in);
        long readBytesTotal = 0;
        long storageBytesTotal = 0;

        // iterate of each file
        try {
            byte[] buff = new byte[2048];

            long fileNumber = 1;
            ZipEntry zipEntry = zipStream.getNextEntry();
            while (zipEntry != null) {

                // name of next entry
                String name = zipEntry.getName();
                if (!zipEntry.isDirectory()) {

                    MongoFileWriter writer = store.createNew(name, FileUtil.getContentType(name));

                    // set the manifest info
                    MongoFile mongoFile = writer.getMongoFile();
                    mongoFile.put(MongoFileConstants.manifestId, zip.get(MongoFileConstants._id));
                    mongoFile.put(MongoFileConstants.manifestNum, fileNumber);
                    manifest.addMongoFile(mongoFile);

                    OutputStream out = writer.getOutputStream();
                    try {
                        // write file
                        int l = zipStream.read(buff);
                        while (l > 0) {
                            out.write(buff, 0, l);
                            readBytesTotal += l;
                            l = zipStream.read(buff);
                        }
                        out.flush();

                    } finally {
                        if (out != null) {
                            out.close();
                        }
                        storageBytesTotal += mongoFile.getStorageLength();
                    }
                    ++fileNumber;
                }

                // setup next file
                zipEntry = zipStream.getNextEntry();
            }

        } finally {
            zipStream.close();
            zip.put(MongoFileConstants.storage, 0); // indicate file is expanded

            double ratio = ((double) storageBytesTotal) / readBytesTotal;

            zip.put(MongoFileConstants.ratio, ratio); // set the ratio of the files against the compressed size
            zip.save();
        }

        return manifest;
    }
}
