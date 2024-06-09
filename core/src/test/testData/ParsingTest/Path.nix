[
/bin/sh
./builder.sh
~/foo
<nixpkgs>
# antiquotations
/${fileName}
/${fileName}/
./${foo}-${bar}.nix
./${foo}-${bar}/
"${./foo.txt}"

# whitespace must not be part of paths
prefix/dir/file.txt next/path/element

# At least one slash (/) must appear before any interpolated expression for the result to be recognized as a path.
a/b

# https://nixos.org/manual/nix/stable/language/values.html#type-path
#   a.${foo}/b.${bar} is a syntactically valid division operation.
# but the Nix parser seems to handle this differently:
#   https://github.com/NixOS/nix-idea/issues/59#issuecomment-1494786812
a.${foo}/b.${bar}

# ./a.${foo}/b.${bar} is a path.
./a.${foo}/b.${bar}

# trailing slashes
/dir/subdir/
./dir/subdir/
]
