package cc.cflags.nixitch.util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class NixPathVerifier {
    private String nixpath;
    private Map<String, File> searchPaths;

    public NixPathVerifier(String nixpath) {
        this.nixpath = nixpath;
        searchPaths = new HashMap<String, File>();
        this.verify();
    }

    public boolean verify() {
        boolean ret = true, fret;
        String[] sps = nixpath.split(":");
        for (int i = 0; i < sps.length; i++) {
            String[] ns = sps[i].split("=");
            String name, path;
            File file;
            if (ns.length == 2) {
                name = ns[0];
                path = ns[1];
            } else {
                name = "";
                path = ns[0];
            }
            if (path.endsWith(".nix"))
                file = new File(path);
            else
                file = new File(path + File.separator + "default.nix");
            fret = file.exists() && file.canRead();
            if (fret) searchPaths.put(name, file);
            ret &= fret;
        }
        return ret;
    }

    public Map<String, File> asMap() {
        return searchPaths;
    }
    public static Map<String, File> asMap(String nixpath) {
        return (new NixPathVerifier(nixpath)).asMap();
    }
}
