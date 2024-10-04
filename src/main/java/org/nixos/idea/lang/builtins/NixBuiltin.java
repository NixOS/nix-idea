package org.nixos.idea.lang.builtins;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.util.NixVersion;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class NixBuiltin {
    //region Constants

    /**
     * List of builtins which are available in the global scope (i.e. without “builtins.” prefix).
     * To verify this list against your installation, start {@code nix repl} and press <kbd>Tab</kbd>.
     */
    private static final Set<String> GLOBAL_SCOPE = Set.of(
            "false",
            "null",
            "true",
            "abort",
            "baseNameOf",
            "break",
            "builtins",
            "derivation",
            "derivationStrict",
            "dirOf",
            "fetchGit",
            "fetchMercurial",
            "fetchTarball",
            "fetchTree",
            "fromTOML",
            "import",
            "isNull",
            "map",
            "placeholder",
            "removeAttrs",
            "scopedImport",
            "throw",
            "toString");

    /**
     * List of all builtins. To verify this list against your installation, start {@code nix repl},
     * type {@code builtins.}, and press <kbd>Tab</kbd>.
     */
    private static final Map<String, NixBuiltin> BUILTINS = Map.ofEntries(
            builtin("false", HighlightingType.LITERAL),
            builtin("null", HighlightingType.LITERAL),
            builtin("true", HighlightingType.LITERAL),
            builtin("import", HighlightingType.IMPORT),
            builtin("abort"),
            builtin("add"),
            builtin("addErrorContext"),
            builtin("all"),
            builtin("any"),
            builtin("appendContext"),
            builtin("attrNames"),
            builtin("attrValues"),
            builtin("baseNameOf"),
            builtin("bitAnd"),
            builtin("bitOr"),
            builtin("bitXor"),
            builtin("break", NixVersion.V2_09),
            builtin("builtins"),
            builtin("catAttrs"),
            builtin("ceil", NixVersion.V2_04),
            builtin("compareVersions"),
            builtin("concatLists"),
            builtin("concatMap"),
            builtin("concatStringsSep"),
            builtin("currentSystem"),
            builtin("currentTime"),
            builtin("deepSeq"),
            builtin("derivation"),
            builtin("derivationStrict"),
            builtin("dirOf"),
            builtin("div"),
            builtin("elem"),
            builtin("elemAt"),
            builtin("fetchClosure", NixVersion.V2_08, "fetch-closure"),
            builtin("fetchGit"),
            builtin("fetchMercurial"),
            builtin("fetchTarball"),
            builtin("fetchTree", NixVersion.V2_04),
            builtin("fetchurl"),
            builtin("filter"),
            builtin("filterSource"),
            builtin("findFile"),
            builtin("floor", NixVersion.V2_04),
            builtin("foldl'"),
            builtin("fromJSON"),
            builtin("fromTOML"),
            builtin("functionArgs"),
            builtin("genList"),
            builtin("genericClosure"),
            builtin("getAttr"),
            builtin("getContext"),
            builtin("getEnv"),
            builtin("getFlake", NixVersion.V2_04, "flakes"),
            builtin("groupBy", NixVersion.V2_05),
            builtin("hasAttr"),
            builtin("hasContext"),
            builtin("hashFile"),
            builtin("hashString"),
            builtin("head"),
            builtin("intersectAttrs"),
            builtin("isAttrs"),
            builtin("isBool"),
            builtin("isFloat"),
            builtin("isFunction"),
            builtin("isInt"),
            builtin("isList"),
            builtin("isNull"),
            builtin("isPath"),
            builtin("isString"),
            builtin("langVersion"),
            builtin("length"),
            builtin("lessThan"),
            builtin("listToAttrs"),
            builtin("map"),
            builtin("mapAttrs"),
            builtin("match"),
            builtin("mul"),
            builtin("nixPath"),
            builtin("nixVersion"),
            builtin("parseDrvName"),
            builtin("partition"),
            builtin("path"),
            builtin("pathExists"),
            builtin("placeholder"),
            builtin("readDir"),
            builtin("readFile"),
            builtin("removeAttrs"),
            builtin("replaceStrings"),
            builtin("scopedImport"),
            builtin("seq"),
            builtin("sort"),
            builtin("split"),
            builtin("splitVersion"),
            builtin("storeDir"),
            builtin("storePath"),
            builtin("stringLength"),
            builtin("sub"),
            builtin("substring"),
            builtin("tail"),
            builtin("throw"),
            builtin("toFile"),
            builtin("toJSON"),
            builtin("toPath"),
            builtin("toString"),
            builtin("toXML"),
            builtin("trace"),
            builtin("traceVerbose", NixVersion.V2_10),
            builtin("tryEval"),
            builtin("typeOf"),
            builtin("unsafeDiscardOutputDependency"),
            builtin("unsafeDiscardStringContext"),
            builtin("unsafeGetAttrPos"),
            builtin("zipAttrsWith", NixVersion.V2_06));

    //endregion
    //region Factories

    private static @NotNull Map.Entry<String, NixBuiltin> builtin(@NotNull String name) {
        return Map.entry(name, new NixBuiltin(name, null, null, null, HighlightingType.OTHER));
    }

    private static @NotNull Map.Entry<String, NixBuiltin> builtin(@NotNull String name, @NotNull HighlightingType highlightingType) {
        return Map.entry(name, new NixBuiltin(name, null, null, null, highlightingType));
    }

    private static @NotNull Map.Entry<String, NixBuiltin> builtin(@NotNull String name, @NotNull NixVersion since) {
        return Map.entry(name, new NixBuiltin(name, since, null, null, HighlightingType.OTHER));
    }

    private static @NotNull Map.Entry<String, NixBuiltin> builtin(@NotNull String name, @NotNull NixVersion since, @NotNull String featureFlag) {
        return Map.entry(name, new NixBuiltin(name, since, featureFlag, null, HighlightingType.OTHER));
    }

    //endregion
    //region Instance members

    private final @NotNull String name;
    private final @Nullable NixVersion since;
    private final @Nullable String featureFlag;
    private final @Nullable NixVersion featureFlagIntegration;
    private final @NotNull HighlightingType highlightingType;
    private final boolean global;

    private NixBuiltin(@NotNull String name,
                       @Nullable NixVersion since,
                       @Nullable String featureFlag,
                       @Nullable NixVersion featureFlagIntegration,
                       @NotNull HighlightingType highlightingType) {
        this.name = name;
        this.since = since;
        this.featureFlag = featureFlag;
        this.featureFlagIntegration = featureFlagIntegration;
        this.highlightingType = highlightingType;
        this.global = GLOBAL_SCOPE.contains(name);
    }

    public @NotNull String name() {
        return name;
    }

    public @NotNull HighlightingType highlightingType() {
        return highlightingType;
    }

    //endregion
    //region Static members

    public static final @NotNull NixBuiltin ROOT = Objects.requireNonNull(resolveBuiltin("builtins"));

    public static @Nullable NixBuiltin resolveBuiltin(@NotNull String name) {
        return BUILTINS.get(name);
    }

    public static @Nullable NixBuiltin resolveGlobal(@NotNull String name) {
        NixBuiltin builtin = BUILTINS.get(name);
        return builtin != null && builtin.global ? builtin : null;
    }

    //endregion
    //region Inner classes

    public enum HighlightingType {
        /**
         * Builtins which are casually described as literals.
         * Specifically, only “{@code true}”, “{@code null}”, and “{@code false} have this type”.
         */
        LITERAL,
        /**
         * Import function.
         */
        IMPORT,
        /**
         * Any other builtin which does not match any of the other types.
         */
        OTHER,
    }

    //endregion
}
