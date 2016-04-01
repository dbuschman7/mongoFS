package me.lightspeed7.mongofs;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

// Test data for testing chunking in MongoFile impl
public final class LoremIpsum {

    private static final String TEXT_FILE = "./src/test/resources/loremIpsum.txt";

    private LoremIpsum() {
        // hidden
    }

    public static File getFile() throws IOException {
        File file = new File(TEXT_FILE);
        if (!file.exists()) {
            FileOutputStream out = new FileOutputStream(file);
            try {
                out.write(LoremIpsum.getBytes(), 0, LoremIpsum.LOREM_IPSUM.length());
            } finally {
                out.close();
            }
        }
        return file;
    }

    public static void createFile(final MongoFileStore store, final String filename, final String mediaType) throws IOException {

        MongoFileWriter writer = store.createNew(filename, mediaType, null, true);
        writer.write(new ByteArrayInputStream(LOREM_IPSUM.getBytes()));
    }

    public static void createTempFile(final MongoFileStore store, final String filename, final String mediaType, final Date expiresAt)
            throws IOException {

        MongoFileWriter writer = store.createNew(filename, mediaType, expiresAt, true);
        writer.write(new ByteArrayInputStream(LOREM_IPSUM.getBytes()));
    }

    public static byte[] getBytes() {
        return LOREM_IPSUM.getBytes();
    }

    public static String getString() {
        return LOREM_IPSUM;
    }

    public static final String LOREM_IPSUM = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Maecenas facilisis leo "
            + " porttitor, dignissim enim venenatis, commodo justo. Donec blandit vel purus nec consequat. Mauris vitae semper "
            + "sapien. In feugiat lectus id posuere convallis. Maecenas ornare condimentum dolor eget lobortis. Duis tempus "
            + "est in turpis fermentum, ut egestas nibh lobortis. Nulla fringilla magna et arcu porta, eget lobortis nunc "
            + "dignissim. In euismod imperdiet diam. Sed tempor elementum mi, id laoreet arcu sodales eget. Duis vel enim erat.\n"

            + "Nam fringilla, diam eu semper placerat, nibh metus vestibulum lorem, sed fringilla felis justo et velit. "
            + " Suspendisse sed tincidunt diam, ut condimentum lectus. Duis ac molestie urna, eu aliquet enim. Proin mattis "
            + "feugiat tortor. Nulla facilisi. Suspendisse potenti. Curabitur mauris purus, scelerisque in rhoncus eget, "
            + "vehicula eu diam. Donec mattis nec eros quis pellentesque. Integer imperdiet purus elit, pretium cursus libero "
            + "varius ut. Nulla auctor, velit eget viverra bibendum, lacus lorem laoreet orci, ut tempor leo nulla id nisi. "
            + "Aenean in nisl nec nisi malesuada porta. Curabitur consequat mollis est, sed gravida dui consectetur suscipit. "
            + "Aenean placerat nibh vitae nisl sagittis porta. Phasellus ornare lectus a erat interdum ornare ut vitae lorem. "
            + "Fusce sed laoreet lorem, sit amet congue leo. Cras tincidunt ullamcorper lorem, ac laoreet augue ornare sed.\n"

            + "Fusce blandit non justo a tempus. Fusce eu sem sem. Donec vehicula tempor nulla non tempus. Vivamus sit amet "
            + "adipiscing odio. Sed eleifend, nulla sed iaculis interdum, enim sapien dictum justo, sed placerat odio diam id "
            + "orci. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Duis nec sem sit amet metus sagittis blandit "
            + "at in mi. Donec consectetur rutrum adipiscing.\n"

            + "Etiam vel suscipit mi. Aliquam erat volutpat. Nulla nec justo sit amet lectus sollicitudin accumsan. Mauris rutrum "
            + "rhoncus viverra. Vestibulum tellus velit, tristique vel odio id, dapibus pulvinar enim. Integer eget mi auctor, "
            + "ultrices metus id, ullamcorper augue. Mauris consequat ullamcorper nisi ultrices fermentum. Ut non diam "
            + "sollicitudin, bibendum purus at, fringilla dolor. Phasellus posuere tincidunt gravida. Vestibulum sed volutpat "
            + "dui. Pellentesque id tellus quis purus ornare ultrices. Nulla sodales laoreet tempor. In fringilla, ipsum quis "
            + "eleifend accumsan, lacus mauris imperdiet sapien, sed feugiat tellus nunc et nunc. Quisque at nunc ut sem congue "
            + "vestibulum quis ornare est. Quisque tortor est, tempor nec viverra a, dignissim non turpis. Pellentesque at "
            + "iaculis elit.\n"

            + "Maecenas facilisis lacus nec erat egestas varius non vitae nibh. Integer lobortis vestibulum commodo. Fusce at "
            + "convallis ligula. Vivamus adipiscing dapibus turpis, sit amet rhoncus sem pretium eu. Phasellus viverra risus "
            + "quis eros consectetur faucibus. Phasellus gravida dolor tellus, sed tristique metus blandit vel. Fusce "
            + "euismod ligula felis, eu elementum nibh rhoncus congue.\n"

            + "Vivamus elementum sapien dolor, vitae viverra metus fringilla ut. Morbi nec dignissim purus, vitae scelerisque "
            + "velit. Aliquam dictum condimentum nisl. Vivamus malesuada sollicitudin purus nec viverra. Nam ut odio sit amet "
            + "nulla malesuada blandit. Etiam pharetra tellus ac dui tincidunt, non tristique augue semper. Quisque ac "
            + "luctus dui, vitae dignissim tortor. Integer sed dui ac lectus pulvinar congue eu vel augue. Phasellus luctus, "
            + "ante non rutrum lacinia, odio dui egestas velit, vitae auctor dolor mi ut tellus. Suspendisse in libero suscipit, "
            + "rutrum mi sit amet, consectetur dui.\n"

            + "Nulla pretium, sapien eu cursus fringilla, leo nibh mattis leo, ac hendrerit tellus eros id turpis. Nam aliquam "
            + "est ut massa consectetur ultrices. Fusce non lacus at sem consectetur pretium. Cum sociis natoque penatibus et "
            + "magnis dis parturient montes, nascetur ridiculus mus. Etiam commodo suscipit ultricies. Nunc aliquet pretium "
            + "diam, non rhoncus arcu placerat sed. Sed congue eu ligula eget porttitor. Cum sociis natoque penatibus et"
            + " magnis dis parturient montes, nascetur ridiculus mus. Phasellus rutrum dui libero. Nullam a fermentum risus, "
            + "a convallis lacus. Curabitur id dolor a diam lobortis consequat ac sed magna. Phasellus porta, mi quis sagittis "
            + "posuere, sapien velit mollis tellus, sed scelerisque magna massa nec ligula. Cras aliquet nisi lobortis aliquam "
            + "condimentum. Fusce non nisl id nulla commodo pellentesque in sit amet risus. Nullam faucibus, dolor bibendum "
            + "pulvinar gravida, turpis enim imperdiet urna, et dictum dolor nibh eu tortor.\n"

            + "Cras mollis lacus eget augue egestas auctor at eu eros. Integer vel risus sodales, fringilla justo vel, sodales "
            + "est. Integer nec eros ut mi cursus dictum a a sapien. Integer a velit sem. Integer fermentum facilisis eros, eget "
            + "pellentesque arcu. Duis vitae velit at leo sollicitudin facilisis sed vitae orci. Aliquam erat volutpat. "
            + "Vivamus nisl sapien, ullamcorper nec tortor in, fermentum mattis felis. Vestibulum feugiat ante vehicula "
            + "molestie laoreet. In a mattis mi. Mauris congue lacus in commodo mollis. Nunc quis condimentum eros. Duis "
            + "varius libero eros. Nulla pulvinar pellentesque elementum.\n"

            + "Donec convallis ornare malesuada. Curabitur dolor lacus, molestie nec tempus non, eleifend vitae velit. "
            + "Pellentesque vel consectetur nunc, nec dapibus urna. Etiam sollicitudin dui vitae leo blandit, vitae euismod felis"
            + " semper. Nunc ac arcu nisi. Aenean eget fringilla erat, nec gravida massa. Donec tempus enim at risus "
            + "fermentum, sed euismod quam blandit. Suspendisse faucibus, turpis sed aliquet interdum, lectus ipsum "
            + "venenatis lorem, nec vulputate nibh magna vitae tortor. Donec eget diam dapibus, consequat ante non, ultrices "
            + "neque. Sed non pulvinar arcu. Sed erat nisl, mollis rhoncus quam eu, tempor euismod risus. Integer in eros urna. "
            + "Nullam ultrices pretium cursus. Quisque consectetur neque id mollis sagittis. Donec euismod auctor lectus eu "
            + "bibendum. Phasellus massa dolor, egestas et erat a, fringilla tristique erat.\n"

            + "Proin vehicula bibendum pulvinar. Morbi in neque dui. Duis posuere felis et dui pellentesque bibendum. Sed "
            + "facilisis facilisis elit, quis sollicitudin orci lobortis a. Curabitur scelerisque sed elit vitae ullamcorper. "
            + "Phasellus feugiat sapien ut lacus pulvinar, in condimentum ante condimentum. Praesent malesuada, justo a "
            + "sollicitudin euismod, urna leo vulputate libero, quis ornare dolor quam non diam. Ut mi orci, sodales in "
            + "venenatis sit amet, malesuada ut mi. Vivamus felis libero, rutrum fermentum dignissim sit amet, vestibulum "
            + "quis sapien. Cras sed sodales lectus. Proin imperdiet purus arcu, venenatis varius ipsum imperdiet eget. In id "
            + "sem convallis, sagittis odio id, congue arcu. Fusce at enim non leo vulputate imperdiet.\n"

            + "Suspendisse et sagittis purus, in ultrices orci. Suspendisse potenti. Pellentesque rutrum lorem quis sapien "
            + "pulvinar, non ullamcorper dolor rhoncus. Quisque tristique risus vel odio commodo ultrices. Praesent vestibulum "
            + "est porta lectus hendrerit luctus. In nec sem quis urna mollis ultricies ut eu tortor. Cum sociis natoque "
            + "penatibus et magnis dis parturient montes, nascetur ridiculus mus. Suspendisse scelerisque congue est in "
            + "tincidunt. Morbi turpis dui, eleifend sit amet sapien ac, adipiscing tristique ligula. Aenean interdum urna "
            + "tincidunt elit posuere semper. Ut non pellentesque nibh. Cras tincidunt neque et mattis tincidunt.\n"

            + "Donec id felis sed odio vulputate dignissim eu id velit. In aliquam velit odio, et auctor orci vehicula euismod. "
            + "Fusce quis mi a ligula eleifend semper non vel ante. Praesent dictum id orci ut auctor. Ut sodales purus ut "
            + "consectetur gravida. Cras sit amet posuere ante. Nulla felis nisl, vehicula ac placerat ac, mollis eu arcu. "
            + "Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Pellentesque eu arcu "
            + "suscipit, lacinia quam nec, eleifend erat. Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n"

            + "Praesent vel porttitor diam. Aenean eu elementum nibh, rutrum sagittis sem. Donec tempus enim id arcu iaculis, "
            + "et rutrum lorem vehicula. Curabitur sodales sed dui et rutrum. Pellentesque interdum arcu at vulputate interdum. "
            + "Fusce vulputate, lorem at porta molestie, dolor arcu condimentum nisl, vel aliquet sapien nulla et diam. In "
            + "venenatis sagittis urna eu porttitor. Sed accumsan sollicitudin metus et tristique. Quisque ac tellus eu "
            + "lacus accumsan porttitor nec eu urna. Nullam in mi imperdiet, tempus velit eu, dapibus velit. Fusce a lobortis "
            + "diam, non fringilla sem. Suspendisse viverra suscipit nisi iaculis aliquam. Vivamus vitae nunc ligula.\n"

            + "Vivamus id tincidunt eros. Phasellus vitae cursus enim. Maecenas auctor purus mauris, sit amet faucibus nibh "
            + "ornare non. Proin vel felis nec nisi gravida venenatis id a metus. Ut dui nisi, consequat vitae neque non, "
            + "dignissim accumsan mauris. Suspendisse lacinia quam magna, eget tincidunt diam eleifend eget. Donec et magna "
            + "porta odio ultrices vehicula. Suspendisse a bibendum mi, vitae iaculis lectus. Nunc lacinia nec mi ac "
            + "gravida. Sed sit amet urna in ipsum laoreet malesuada.\n"

            + "Pellentesque facilisis velit metus, a aliquam nibh egestas quis. Sed aliquet, lectus vel imperdiet dapibus, "
            + "metus massa porttitor felis, vel fringilla leo lorem non mauris. Mauris mauris nulla, ullamcorper non porta et, "
            + "dapibus sit amet arcu. Maecenas placerat id dolor at feugiat. Donec laoreet ante lectus, molestie vehicula "
            + "lorem eleifend ultricies. Etiam molestie enim sit amet lacus malesuada, ac tempus sapien euismod. Lorem "
            + "ipsum dolor sit amet, consectetur adipiscing elit. Sed blandit elementum dolor et scelerisque. Cras quis luctus"
            + " ante, laoreet rhoncus tellus. Nam ut elit at magna vestibulum venenatis eu vitae nunc. Etiam sed lectus "
            + "condimentum, lobortis erat in, mollis lectus.\n"

            + "Aliquam erat volutpat. Nunc dignissim enim at turpis bibendum, aliquam vulputate augue condimentum. Cras diam "
            + "orci, venenatis vitae congue a, faucibus in justo. Aenean quis facilisis nunc, sit amet posuere ipsum. Interdum "
            + "et malesuada fames ac ante ipsum primis in faucibus. Integer fermentum, purus et rutrum viverra, ligula ipsum"
            + " tempor libero, eget venenatis lorem tellus eu augue. Sed lobortis, velit a pulvinar commodo, diam est "
            + "auctor dolor, sed molestie sem diam a augue. Aenean hendrerit, sem id dignissim ornare, libero ipsum dapibus "
            + "dui, a feugiat ligula nulla sed neque. Donec rutrum lacus eu fermentum porttitor. Phasellus ac elit fermentum, "
            + "vestibulum lacus vel, fringilla nisi. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per"
            + " inceptos himenaeos. Fusce laoreet massa tincidunt, lacinia lectus ut, gravida magna. Sed varius est sapien, id "
            + "venenatis nunc placerat et. Curabitur imperdiet adipiscing ultrices. Curabitur faucibus, diam in lacinia "
            + "vulputate, metus elit faucibus turpis, et porttitor odio sapien malesuada felis.\n"

            + "Sed dignissim porttitor lorem, id euismod augue vehicula a. Nam tincidunt vitae purus at lacinia. Nunc porta "
            + "bibendum mauris vitae tempor. Morbi eu luctus nulla. Suspendisse in erat nulla. Nam semper iaculis arcu, congue "
            + "lacinia tortor. Fusce sed fermentum eros. Proin at hendrerit enim, at ultricies purus. Sed porttitor mattis "
            + "libero quis fermentum. Quisque eu nisi quis nisi feugiat mollis sit amet eget nibh. Integer hendrerit "
            + "augue id tortor laoreet, quis pretium erat sollicitudin. Quisque hendrerit tellus at eros gravida, eu tempor "
            + "sapien placerat. Nunc et enim fermentum, ultricies massa et, scelerisque tellus.\n"

            + "Curabitur vehicula id nisl sodales gravida. Donec molestie, nunc sed mattis bibendum, mi mi posuere elit, a "
            + "vehicula turpis leo sed urna. Suspendisse eget varius turpis. Aliquam porttitor sed orci non pharetra. In "
            + "viverra nibh sit amet urna eleifend facilisis. Etiam vehicula fermentum elit non lobortis. Nulla ut metus "
            + "augue. Nullam convallis nisi pulvinar, dictum magna eu, pharetra augue.\n"

            + "Fusce est libero, laoreet a luctus a, scelerisque et nulla. Praesent sed laoreet odio. Cras ultrices sodales "
            + "felis a volutpat. Nam magna quam, tempus pretium condimentum sed, tincidunt vitae mi. Cras sit amet mauris "
            + "malesuada, euismod tellus ac, convallis magna. Aliquam luctus, orci sit amet dictum viverra, purus libero "
            + "congue sem, vitae iaculis orci massa nec erat. Sed ultricies pharetra lorem, vitae dignissim erat varius a."
            + " Donec viverra ipsum sed ante aliquet, in vestibulum odio interdum. Donec et est quis lorem elementum congue "
            + "in ac libero. Suspendisse potenti. Vivamus vel pretium odio, vel bibendum leo. Aenean iaculis pellentesque "
            + "ligula, ut molestie lorem placerat et. Cras viverra non quam in vulputate. Interdum et malesuada fames ac ante"
            + " ipsum primis in faucibus. Vestibulum eu mi ullamcorper, vehicula velit vitae, iaculis enim.\n"

            + "Curabitur a ornare risus. Aliquam nec arcu porta, tempus justo a, varius lacus. Mauris vulputate semper elementum. "
            + "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc non odio dictum, aliquam mauris vitae, ornare "
            + "magna. Morbi consequat dignissim purus ac rutrum. Duis fermentum diam et tortor interdum pulvinar. Etiam "
            + "aliquam blandit risus, et dignissim nulla facilisis non. Nulla sodales leo eu turpis dictum, nec varius "
            + "neque hendrerit. In hac habitasse platea dictumst. Donec lobortis dolor nisl, nec dapibus tellus dapibus ut. "
            + "Sed vestibulum neque vitae lacus euismod, id ultricies odio sollicitudin.\n"

            + "Pellentesque in orci sed ligula lobortis faucibus at ac nibh. Pellentesque habitant morbi tristique senectus et "
            + "netus et malesuada fames ac turpis egestas. Vivamus in ipsum tortor. Nunc id ullamcorper eros. Sed blandit "
            + "ultrices nisi ac fringilla. Duis lobortis consectetur pharetra. Curabitur euismod nisi eu libero aliquam "
            + "pharetra. Praesent ut enim lectus. Aenean vitae viverra ligula. Morbi lacinia semper aliquet. Pellentesque "
            + "sit amet lectus semper, congue nulla in, iaculis velit. Nunc ut purus gravida, placerat velit ut, ultrices"
            + " neque. Nam at tincidunt nisi. Vestibulum eget libero ut neque bibendum scelerisque. Aenean eget lacus "
            + "vestibulum, aliquet neque vitae, dictum felis.\n"

            + "In euismod id nibh aliquet interdum. Proin eget ullamcorper libero. Sed at massa vitae felis porta placerat sed "
            + "in felis. Etiam congue volutpat eros quis varius. Cum sociis natoque penatibus et magnis dis parturient montes, "
            + "nascetur ridiculus mus. Nam non ornare nunc. Proin adipiscing lacus quis sapien imperdiet, vitae posuere "
            + "magna luctus. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus.\n"

            + "Donec egestas felis et ipsum dapibus ultricies. Sed vitae vulputate dui. Ut quis mollis lectus. Vestibulum "
            + "ultrices imperdiet nibh dignissim facilisis. Proin nisl elit, bibendum at tellus quis, accumsan accumsan nisi. "
            + "Morbi convallis imperdiet mattis. In sit amet rutrum felis. Donec scelerisque ut eros at adipiscing. Quisque "
            + "hendrerit felis ac velit mattis pulvinar.\n"

            + "Donec vel dolor quis mauris sagittis malesuada in quis justo. Nulla a turpis auctor, gravida leo a, varius "
            + "est. Phasellus sagittis accumsan nibh, non luctus dui bibendum sit amet. Aliquam ut odio ut nisl blandit "
            + "gravida vitae in orci. Proin convallis lacus et elit faucibus, id ullamcorper augue rutrum. Proin faucibus "
            + "blandit ultrices. Vestibulum ut felis dapibus, fringilla nisl quis, tempus nulla. Donec a tincidunt erat."
            + " Nunc ligula risus, cursus eget hendrerit eu, facilisis non turpis. Cras sollicitudin condimentum felis, non "
            + "pellentesque odio fringilla in. Aliquam mi mi, lobortis scelerisque aliquam sed, feugiat ac felis.\n"

            + "Nam quis neque eget nulla rutrum elementum. Suspendisse nec nunc placerat nibh lacinia adipiscing sed tempor "
            + "quam. Curabitur mattis, est sed consequat vestibulum, dui felis auctor velit, eget faucibus urna nunc non "
            + "nulla. Morbi facilisis posuere nunc, ac tincidunt enim cursus ac. Nam nibh arcu, pulvinar vitae lacus sed, "
            + "suscipit feugiat eros. Duis eleifend metus sed erat sagittis, at sollicitudin nunc dictum. Praesent eget elit"
            + " rhoncus, placerat nisl in, ultricies justo. Sed sit amet mauris ligula. Mauris consequat, augue eleifend "
            + "lobortis consequat, mauris velit ornare velit, at accumsan lorem nunc sed nunc. Sed varius hendrerit urna nec "
            + "facilisis.\n"

            + "Aenean ultrices eget justo a interdum. Etiam dapibus vehicula dui, eu volutpat purus pulvinar non. Fusce nec "
            + "posuere ligula. Cras ultrices pulvinar erat eu pretium. Duis elementum ligula eu est commodo sodales. Donec "
            + "eleifend eleifend lectus, sed adipiscing lacus laoreet ut. Curabitur sit amet nunc sed dolor auctor dictum.\n"

            + "Mauris in massa turpis. Duis sit amet risus condimentum, dictum felis a, tempus massa. Aenean condimentum nisl "
            + "sapien, et tempus erat semper quis. In suscipit, est id accumsan pulvinar, leo metus laoreet arcu, sed "
            + "fermentum diam ligula a sem. Nullam auctor vestibulum velit a interdum. Quisque cursus tincidunt est eu "
            + "aliquam. Mauris pharetra tellus vel viverra pharetra. Vivamus congue enim sit amet mi dapibus, sit amet "
            + "vulputate risus tincidunt. Fusce varius sed turpis a congue. Curabitur dignissim purus orci, gravida aliquet "
            + "turpis euismod id. Vivamus sem orci, mattis eu felis ac, lobortis imperdiet ipsum. Aenean auctor malesuada nunc"
            + " molestie aliquet.\n"

            + "Mauris quis nisi et ligula dapibus bibendum sit amet in quam. Aenean rhoncus nec arcu ut fringilla. Proin eu "
            + "sapien nec nisi rhoncus dapibus ut eget dolor. Nulla ultrices, elit malesuada auctor condimentum, leo mi "
            + "auctor ipsum, vitae ultrices risus elit sed dolor. Suspendisse sed ante porttitor diam cursus sagittis. "
            + "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus in lacus ac massa condimentum egestas sed "
            + "quis velit. Nunc in augue pellentesque, vestibulum urna et, iaculis tortor. In hac habitasse platea dictumst.\n"

            + "Duis nec purus vitae justo facilisis fringilla id in nulla. Vivamus ac metus a quam ultricies tempus id eget "
            + "arcu. Fusce adipiscing tincidunt nunc. Suspendisse consectetur, tortor a pretium luctus, tellus neque dapibus "
            + "elit, a aliquam augue diam eu justo. Duis ac sapien leo. Vestibulum mollis gravida scelerisque. Nunc "
            + "pellentesque risus et sapien scelerisque ullamcorper. Proin viverra rutrum nisi id ornare. Fusce blandit"
            + " mattis risus sed adipiscing. Vivamus ut sapien a erat euismod suscipit eget vel nisi. Phasellus et augue "
            + "vitae diam porttitor consectetur lacinia et justo. Fusce pretium magna nec felis semper, pretium elementum augue "
            + "varius. Maecenas purus erat, laoreet nec malesuada ut, mattis eleifend odio.\n"

            + "Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Integer pharetra "
            + "nisi purus, vitae tempus neque sodales quis. Integer eu tempor tellus. Quisque non laoreet arcu. Integer eget"
            + " lorem ut lectus consectetur fringilla a elementum libero. Proin id viverra arcu. Aliquam sodales, urna "
            + "at aliquam tempus, diam leo volutpat mi, quis malesuada lorem augue vitae purus. Nam augue dui, iaculis id "
            + "nisl eget, vestibulum bibendum dui. Suspendisse ut tincidunt mauris. Quisque lobortis, lorem ut pellentesque "
            + "viverra, nulla urna interdum libero, a tristique nisi mi ut leo.\n"

            + "Fusce mattis, quam vel rutrum rhoncus, quam enim euismod quam, id adipiscing mi sem at felis. Curabitur ut "
            + "hendrerit nisi. Nulla vehicula, tellus ac tincidunt ultrices, diam felis pulvinar enim, vel viverra metus "
            + "tortor ac leo. Integer tortor lorem, volutpat a gravida quis, hendrerit vel nisl. Mauris consectetur nulla "
            + "lorem, non imperdiet lacus porttitor vel. Nulla sodales quam purus, in sodales libero blandit quis. "
            + "Vestibulum nec vulputate ligula. Morbi suscipit mattis magna, eu ultricies neque lobortis eget. Nunc pharetra, "
            + "neque sit amet fringilla cursus, nulla sapien blandit odio, eget mollis tellus ligula at risus. Cras sit amet "
            + "convallis dui. Fusce id felis risus. Nulla ac arcu ornare odio luctus rutrum pulvinar ac libero. Maecenas quis "
            + "nulla nec lorem consequat sagittis. Sed rhoncus aliquam urna quis iaculis. Morbi euismod erat in odio pretium, "
            + "in dignissim neque pretium.\n"

            + "In vel tellus vitae odio scelerisque volutpat ut sed justo. Morbi a turpis massa. Praesent ut velit egestas, "
            + "scelerisque erat eu, tempus sem. Etiam vehicula imperdiet porttitor. Quisque vehicula sodales elementum. "
            + "Pellentesque venenatis tempus dui suscipit convallis. Nulla ultricies congue dolor, et elementum elit mollis "
            + "at. Nullam iaculis rutrum risus, eu malesuada augue imperdiet eget. Maecenas a urna eu nibh feugiat imperdiet. "
            + "Aenean egestas aliquam diam sit amet tempor. Donec fringilla, neque eu bibendum aliquet, nibh libero semper "
            + "lectus, eget placerat enim tortor ut lorem.\n"

            + "Donec tempor purus sit amet augue rhoncus, ac aliquam arcu luctus. Maecenas accumsan tincidunt vulputate. Proin "
            + "quis velit eget arcu ullamcorper posuere eget vel orci. In dictum, purus nec posuere tincidunt, libero turpis"
            + " placerat turpis, eu fermentum libero nibh accumsan risus. Integer non enim pellentesque, congue nisi eget, "
            + "faucibus arcu. Maecenas eros nunc, luctus id vehicula ut, bibendum eget est. Pellentesque vel feugiat leo, "
            + "eget commodo turpis. Cras lobortis in orci sed imperdiet. Nulla dui felis, tempus sit amet orci et, elementum "
            + "commodo magna. Mauris eget vehicula metus. Etiam viverra, augue ac vestibulum condimentum, nisl sapien "
            + "ultricies est, et elementum eros odio et nisl. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices"
            + " posuere cubilia Curae; Integer ac fringilla odio. Nam id turpis condimentum, euismod lacus non, hendrerit diam.\n"

            + "Cras ullamcorper augue sit amet sem fermentum laoreet. Ut commodo sapien dapibus sem luctus, vel rutrum justo "
            + "pellentesque. Praesent non diam quis dui tincidunt lacinia. Phasellus id egestas turpis, nec placerat sapien. "
            + "Etiam ut nibh mollis, pretium magna vel, fermentum magna. Praesent convallis blandit nunc eget molestie."
            + " Quisque ornare mi vitae libero viverra sodales. Proin auctor molestie elit a accumsan. Sed ac rhoncus lacus. "
            + "Aenean consectetur, orci in faucibus porttitor, mauris velit malesuada libero, nec porta tortor libero a odio. "
            + "Pellentesque quis magna faucibus massa malesuada facilisis. Nam condimentum lacus eget nibh tristique, ut rutrum "
            + "augue blandit. Etiam adipiscing aliquam sapien, vel placerat nunc sodales in. Proin rutrum viverra urna, nec "
            + "dictum purus ornare nec. Duis feugiat ipsum eu libero tempus venenatis nec sed massa. Vivamus sodales nunc erat,"
            + " sit amet porttitor eros sagittis nec.\n"

            + "Praesent sodales sit amet eros at eleifend. Donec mattis augue mollis neque hendrerit tincidunt. Pellentesque "
            + "habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Suspendisse neque diam, dictum "
            + "rutrum volutpat quis, vulputate lacinia mauris. Aliquam interdum dolor at mauris ullamcorper condimentum. "
            + "Fusce iaculis rhoncus dui. Ut sit amet risus varius massa ultricies porttitor. Aliquam tellus orci, accumsan "
            + "a vulputate in, dapibus id arcu. Suspendisse sollicitudin massa nibh, sit amet posuere purus tempor sed. Donec "
            + "pharetra, risus ut facilisis porta, lorem magna rhoncus metus, id bibendum metus sapien consectetur metus. "
            + "Vivamus a placerat arcu. Suspendisse suscipit in eros sed aliquam. Donec laoreet dolor eu metus bibendum, sit "
            + "amet molestie dolor dictum.\n"

            + "Nullam lobortis neque ligula, a elementum velit ultrices in. Ut mollis porta est eu posuere. Pellentesque "
            + "tristique, orci at iaculis luctus, purus lorem venenatis massa, vitae vehicula arcu urna vitae lorem. Morbi et"
            + " risus dictum, dignissim tortor in, molestie lectus. Quisque id metus eu massa lacinia malesuada. Nunc "
            + "tristique purus orci, in blandit enim porta pharetra. Etiam mattis, lorem vulputate aliquet interdum, ligula "
            + "tellus luctus velit, eget commodo velit sapien in felis. Fusce et varius ante. Praesent scelerisque, ante non "
            + "sollicitudin aliquet, magna nisl vulputate dui, nec aliquet quam libero ac turpis.\n"

            + "Aenean nunc tellus, sodales a metus ac, imperdiet posuere augue. Phasellus id dui eu erat rhoncus rhoncus. "
            + "Sed velit quam, convallis vel arcu quis, fringilla pretium metus. Integer dapibus velit sed ultricies dapibus. "
            + "Praesent sollicitudin vehicula mauris, vel lacinia purus tempor quis. Nam quis rutrum sem, vitae varius "
            + "urna. Quisque lacinia est quis nulla ornare ultricies. Praesent fermentum, enim vel eleifend cursus, velit "
            + "magna condimentum orci, non dapibus est est eu quam. Ut aliquet lacus eu lectus interdum tincidunt. Cras risus"
            + " tortor, euismod ac tincidunt quis, fermentum venenatis sem. Aenean lacinia sodales porttitor. Pellentesque "
            + "habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas.\n"

            + "Fusce et dignissim nulla. Ut risus dui, pharetra dictum est vitae, sagittis venenatis nisi. Etiam ac tincidunt "
            + "turpis. Vivamus id elit nibh. Curabitur lorem leo, dignissim nec feugiat at, convallis et ipsum. Phasellus non "
            + "tincidunt leo, at egestas eros. Sed at molestie lorem. Sed pretium orci sit amet massa vulputate, eu "
            + "pellentesque libero sodales. Mauris commodo velit vulputate elit vestibulum vestibulum. Aliquam a est erat. "
            + "Fusce et odio sed est lobortis facilisis ut ut metus. Quisque pellentesque nec nisi et fringilla. Praesent"
            + " sapien leo, porta vel elementum convallis, euismod at arcu. Mauris euismod justo in nulla malesuada eleifend. "
            + "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n"

            + "Fusce eget feugiat nisl. Etiam vitae mollis ipsum. Vivamus imperdiet, magna ac pretium suscipit, augue nulla "
            + "varius purus, vitae vulputate massa odio id diam. Morbi pharetra velit eget consequat pulvinar. Vestibulum sit "
            + "amet semper dolor. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos "
            + "himenaeos. Nulla facilisis nisl eu ante sollicitudin varius. Aenean vel ultrices tellus. Nunc consectetur "
            + "nec odio ac accumsan. Integer vel justo massa. Donec pellentesque aliquam eros, feugiat cursus elit imperdiet at.\n"

            + "Maecenas viverra hendrerit fringilla. Ut et dolor placerat, bibendum diam nec, interdum mi. Maecenas tempor "
            + "dolor ac blandit auctor. Donec a ligula nec tortor hendrerit aliquam vitae ut nunc. Donec adipiscing magna in "
            + "cursus rutrum. Praesent faucibus sodales mi, vitae scelerisque velit fringilla laoreet. Praesent nec "
            + "rutrum eros. Fusce suscipit, libero eget lobortis convallis, velit lacus lobortis enim, eget imperdiet nunc "
            + "dolor adipiscing augue. Aliquam nunc quam, malesuada in tempus eget, consectetur a nunc. Maecenas vel enim "
            + "laoreet, euismod odio quis, tincidunt urna. Mauris molestie purus erat. Proin vehicula metus sit amet ante "
            + "sagittis, eget volutpat lectus convallis.\n"

            + "Duis enim diam, aliquam at varius ac, viverra in libero. Etiam ullamcorper fringilla volutpat. Morbi non rutrum "
            + "libero. Aliquam fermentum ac enim id tristique. Suspendisse vehicula consequat tempor. Vestibulum feugiat "
            + "ante nisl, nec porta tortor commodo nec. Phasellus tincidunt erat sed justo scelerisque sollicitudin. "
            + "Cras sit amet auctor nunc, eu mollis leo. Donec malesuada vel purus at tempus. Sed faucibus tincidunt lectus, "
            + "vitae aliquam odio venenatis nec. Aenean egestas ante quis varius varius. Phasellus ac velit in lacus congue "
            + "dapibus.\n"

            + "Duis et dapibus dui, a aliquam nunc. Ut in tortor sit amet dui ultrices sollicitudin. Vivamus nec hendrerit "
            + "tortor. Donec a orci ut justo laoreet lobortis. Praesent id nunc sodales sapien pulvinar eleifend. Sed eu "
            + "enim auctor, lacinia lorem non, ultrices tellus. Class aptent taciti sociosqu ad litora torquent per "
            + "conubia nostra, per inceptos himenaeos. Maecenas sed laoreet sem. Suspendisse tincidunt, dolor et suscipit "
            + "venenatis, justo nunc tincidunt metus, sed malesuada erat purus et quam. Etiam nec consequat est, eget aliquet "
            + "erat. Quisque ac arcu rutrum, vestibulum augue sed, vehicula ante. Donec posuere justo ac enim viverra "
            + "eleifend. Vivamus quam lorem, volutpat vitae quam et, convallis egestas lorem. Maecenas lobortis eros in "
            + "scelerisque porta. Sed quis velit lorem. Maecenas suscipit ante quis mattis tincidunt.\n"

            + "Nunc a magna ut nunc adipiscing placerat. Phasellus varius lorem massa, nec blandit diam malesuada sit amet. "
            + "Curabitur id tortor vel neque sodales ultrices vitae egestas ligula. Maecenas vitae sapien turpis. Aenean et "
            + "justo enim. Ut aliquam ullamcorper risus, eget aliquam nisl mattis et. Suspendisse cursus varius porta. "
            + "Fusce tempor est nec leo feugiat, porttitor gravida neque faucibus. Curabitur velit felis, imperdiet elementum "
            + "mi a, lobortis interdum augue. Vivamus feugiat accumsan urna id fringilla.\n"

            + "Donec euismod, dui ut iaculis tempor, ligula elit ultrices dolor, at auctor velit massa vel diam. Mauris ut "
            + "nisi quis magna sollicitudin dapibus. Pellentesque malesuada, diam quis ornare sollicitudin, neque lectus "
            + "imperdiet quam, vitae rutrum enim risus ullamcorper sapien. Nulla ut tellus risus. Cras pretium libero "
            + "quis justo porta imperdiet. Donec eu dignissim felis. Morbi congue enim odio, quis sollicitudin sem porttitor "
            + "in. Donec augue arcu, molestie ac dictum in, dapibus ut leo. Ut quis eros a nibh dignissim eleifend. Quisque "
            + "pellentesque suscipit est in vulputate.\n"

            + "Fusce tristique magna sit amet odio tempor bibendum. Mauris dignissim quam vel ultricies elementum. Nulla "
            + "sapien libero, malesuada dictum vestibulum et, elementum in tortor. Vestibulum suscipit lectus non libero "
            + "aliquet vestibulum. Aliquam malesuada libero mi, ac tempus ante pellentesque in. Sed sodales elit nec "
            + "libero eleifend tempor ac quis dolor. Sed consectetur sem sollicitudin, ullamcorper nisl eu, bibendum urna. "
            + "Suspendisse lacinia elementum nisi in elementum. Fusce dapibus tempus odio, ut semper orci aliquet eu. Curabitur "
            + "in sodales quam, in sodales sapien.\n"

            + "Fusce pellentesque, orci a hendrerit semper, magna massa sollicitudin felis, vel faucibus libero elit eget "
            + "libero. Sed viverra imperdiet enim vitae blandit. Morbi tristique adipiscing augue, eu varius nisi sagittis "
            + "quis. Nulla non nisl feugiat, tincidunt lacus ultrices, luctus odio. Nam faucibus nunc vitae felis interdum "
            + "lacinia. Praesent ipsum massa, suscipit sit amet volutpat id, condimentum vitae massa. Nullam sollicitudin "
            + "eros a faucibus blandit.\n"

            + "Proin vestibulum ipsum at elit fringilla, nec dignissim nisi aliquam. Praesent pretium elit vel nulla auctor, "
            + "ac consequat metus porta. Morbi suscipit pretium ipsum non viverra. Aliquam erat volutpat. Praesent accumsan "
            + "sollicitudin purus sit amet porttitor. In eget viverra augue. Nulla suscipit cursus dui ut egestas. Nullam ut"
            + " mi interdum, aliquam orci ut, sagittis felis. Curabitur in ligula id urna dictum molestie non in libero. "
            + "Vestibulum eu sem lorem. Curabitur nec enim justo. Etiam egestas mattis lacus nec facilisis. Duis vel leo ligula. "
            + "Maecenas metus magna, fermentum a mollis id, hendrerit in ante.\n"

            + "Praesent vulputate purus erat, id venenatis mauris posuere congue. Donec velit sem, volutpat eget velit ac, "
            + "varius tristique dui. Duis sit amet commodo augue. Interdum et malesuada fames ac ante ipsum primis in "
            + "faucibus. Vivamus egestas erat vitae urna pulvinar, imperdiet bibendum mi placerat. Ut pulvinar bibendum urna,"
            + " ut posuere sem pharetra ac. Donec sed dui velit. Suspendisse lacus mauris, commodo in fringilla vitae, feugiat "
            + "sit amet nisl. Praesent non lobortis turpis.\n"

            + "Duis sit amet rutrum tortor. Praesent imperdiet mauris ante, id interdum justo lacinia et. Etiam dignissim "
            + "augue id mi fermentum, vel commodo urna vulputate. Sed velit justo, viverra eget cursus vitae, viverra "
            + "congue ligula. Nunc molestie a metus vitae venenatis. Phasellus placerat nisl a arcu pharetra, a consequat "
            + "elit venenatis. Aliquam placerat sem gravida lacus vehicula pretium. Cras aliquam tortor tellus, sed semper "
            + "libero fermentum sit amet. Nullam lacinia tellus vitae venenatis feugiat. Sed nec urna sed felis elementum "
            + "ornare. Sed ultrices massa id nisl dapibus condimentum. Maecenas vehicula condimentum condimentum.\n"

            + "Nam bibendum sed lorem non accumsan. Vestibulum dictum quam et augue tempor, quis gravida neque placerat. "
            + "Pellentesque at sollicitudin augue. Nulla suscipit id nulla nec dapibus. Quisque varius, elit sit amet laoreet "
            + "bibendum, lacus magna tempus urna, vitae euismod augue nisi et nisl. Lorem ipsum dolor sit amet, consectetur "
            + "adipiscing elit. Suspendisse scelerisque quis ante non imperdiet. Aliquam eu semper enim. Proin sapien felis, "
            + "porta id urna vehicula, vulputate ultricies enim. Nulla eu aliquet sem. Nullam at arcu nec orci pretium "
            + "ultrices. Sed sodales felis ac sem euismod tristique at eget dolor. Integer in tempor eros. Nam massa enim, "
            + "viverra eu viverra id, volutpat nec sem. Nam posuere arcu sit amet nulla dictum dictum.";

}
