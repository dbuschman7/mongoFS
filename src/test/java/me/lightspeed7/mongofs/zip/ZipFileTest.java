package me.lightspeed7.mongofs.zip;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import me.lightspeed7.mongofs.LoremIpsum;

import org.junit.BeforeClass;
import org.junit.Test;

public class ZipFileTest {

    private static final String TEST_ZIP = "/Users/dbusch/test.zip";

    @BeforeClass
    public static void before() throws IOException {

        final File f = new File(TEST_ZIP);
        f.delete();
        final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(f));
        try {
            createFile(out, "file1.txt", LoremIpsum.LOREM_IPSUM.getBytes());
            createFile(out, "file2.txt", LoremIpsum.LOREM_IPSUM.getBytes());
            createFile(out, "manifest.xml", XML.getBytes());
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    private static void createFile(final ZipOutputStream out, final String fileName, final byte[] data) throws IOException {
        ZipEntry e = new ZipEntry(fileName);
        out.putNextEntry(e);

        out.write(data, 0, data.length);
        out.closeEntry();
    }

    @Test
    // @Ignore
    public void test() throws IOException {

        ZipFile zip = new ZipFile(TEST_ZIP);
        try {
            assertEquals(3, zip.size());

            Enumeration<? extends ZipEntry> entries = zip.entries();
            assertEquals("file1.txt", entries.nextElement().getName());
            assertEquals("file2.txt", entries.nextElement().getName());
            assertEquals("manifest.xml", entries.nextElement().getName());

            entries = zip.entries();

            InputStream inputStream = new FileInputStream(new File(TEST_ZIP));
            inputStream = new LogItInputStream(inputStream);
            ZipInputStream zipStream = new ZipInputStream(inputStream);

            byte[] buff = new byte[2048];
            long counter = 0;
            ZipEntry zipEntry = zipStream.getNextEntry();
            while (zipEntry != null) {
                String name = zipEntry.getName();
                // long size = entry.getSize();
                counter = 0;
                // write buffer to file
                // CHECKSTYLE:OFF
                System.out.println(String.format("Reading file %s", name));
                // CHECKSTYLE:ON
                int l = zipStream.read(buff);
                while (l > 0) {
                    counter += l;
                    // CHECKSTYLE:OFF
                    System.out.println("Read Bytes = " + l);
                    // CHECKSTYLE:ON
                    l = zipStream.read(buff);
                }
                // CHECKSTYLE:OFF
                System.out.println(String.format("File read %s - length = %d", name, counter));
                // CHECKSTYLE:ON
                zipEntry = zipStream.getNextEntry();
            }

            zipStream.close();
        } finally {
            zip.close();
        }

    }

    //
    // ///////////////////
    //
    private static final String XML = "" //
            + " <card xmlns=\"http://businesscard.org\">\n" //
            + "   <name>John Doe</name>\n" //
            + "   <title>CEO, Widget Inc.</title>\n" //
            + "   <email>john.doe@widget.com</email>\n" //
            + "   <phone>(202) 456-1414</phone>\n" //
            + "   <logo url=\"widget.gif\"/>\n"//
            + " </card>";
}
