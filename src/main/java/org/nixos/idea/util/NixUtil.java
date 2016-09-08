package org.nixos.idea.util;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.FileBasedIndex;

import java.util.Collections;
import java.util.List;

import org.nixos.idea.psi.NixAttr;

public class NixUtil {

    public static final List<NixAttr> findAttrs(Project proj, String key) {
       return Collections.<NixAttr>emptyList();
    }
}
