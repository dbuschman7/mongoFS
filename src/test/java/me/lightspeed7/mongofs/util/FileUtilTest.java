package me.lightspeed7.mongofs.util;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;

public class FileUtilTest {

    private File file = new File("src/main/resources/test.zip");
    private String filename = file.getAbsolutePath();

    @Test
    public void testExtension() {

        assertEquals("zip", FileUtil.getExtension(filename));
        assertEquals("zip", FileUtil.getExtension(".zip"));
    }

    @Test
    public void testContentType() {
        assertEquals("application/zip", FileUtil.getContentType(filename));
    }

    @Test
    public void testMd5() {
        assertEquals("8d777f385d3dfec8815d20f7496026dc", FileUtil.hexMD5("data".getBytes()));
    }
}
