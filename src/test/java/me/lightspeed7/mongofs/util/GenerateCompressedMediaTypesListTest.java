package me.lightspeed7.mongofs.util;

import org.junit.Test;

import com.google.common.net.MediaType;

public class GenerateCompressedMediaTypesListTest {

    private static final String TEMPLATE = "noCompressionTypes.add(\"%s\");";

    @Test
    public void generate() {

        System.out.println(String.format(TEMPLATE, MediaType.BZIP2));
        System.out.println(String.format(TEMPLATE, MediaType.GZIP));
        System.out.println(String.format(TEMPLATE, MediaType.OCTET_STREAM));
        System.out.println(String.format(TEMPLATE, MediaType.TAR));
        System.out.println(String.format(TEMPLATE, MediaType.ZIP));

    }
}
