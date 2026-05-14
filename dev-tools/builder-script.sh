#!/usr/bin/env bash
set -e

CACHE_DIR=".build-cache"
mkdir -p "$CACHE_DIR"

ORIGINAL_JAVA=""

detect_os() {
  uname -s
}

is_arch() {
  [[ -f /etc/arch-release ]]
}

is_ubuntu() {
  [[ -f /etc/lsb-release ]] || grep -qi ubuntu /etc/os-release
}

set_java_arch() {
  echo "Detected Arch Linux"

  ORIGINAL_JAVA=$(archlinux-java get)

  echo "Switching to GraalVM via archlinux-java..."
  sudo archlinux-java set java-25-graalvm
}

restore_java_arch() {
  echo "Restoring Java: $ORIGINAL_JAVA"
  sudo archlinux-java set "$ORIGINAL_JAVA"
}

set_java_ubuntu() {
  echo "Detected Ubuntu / CI environment"

  # Try GraalVM via setup-java / environment variable
  if [[ -n "$GRAALVM_HOME" ]]; then
    echo "Using GRAALVM_HOME=$GRAALVM_HOME"
    export PATH="$GRAALVM_HOME/bin:$PATH"
  else
    echo "WARNING: GRAALVM_HOME not set. Assuming native-image is already available."
  fi
}

setup_java() {
  if is_arch; then
    set_java_arch
  else
    set_java_ubuntu
  fi
}

cleanup() {
  if is_arch && [[ -n "$ORIGINAL_JAVA" ]]; then
    echo "----------------------------------------"
    restore_java_arch
  fi
}

trap cleanup EXIT


echo "----------------------------------------"
setup_java

echo "Current Java:"
java -version

if ! command -v native-image >/dev/null 2>&1; then
  echo "ERROR: native-image not found"
  exit 1
fi

native-image --version


# ---------------- HASHING ----------------

hash_file() {
  sha256sum "$1" | awk '{print $1}'
}

read_old_hash() {
  local tool="$1"
  local file="$CACHE_DIR/$tool.hash"

  [[ -f "$file" ]] && cat "$file" || echo ""
}

write_hash() {
  local tool="$1"
  local hash="$2"

  echo "$hash" > "$CACHE_DIR/$tool.hash"
}


# ---------------- BUILD ----------------

build_tool() {
  local TOOL="$1"
  local SRC="${TOOL}.java"
  local BIN="../${TOOL}.tool"

  echo "----------------------------------------"
  echo "Checking $TOOL"

  if [[ ! -f "$SRC" ]]; then
    echo "Missing source: $SRC"
    exit 1
  fi

  local NEW_HASH OLD_HASH
  NEW_HASH=$(hash_file "$SRC")
  OLD_HASH=$(read_old_hash "$TOOL")

  if [[ "$NEW_HASH" == "$OLD_HASH" && -f "$BIN" ]]; then
    echo "Skipping $TOOL (no changes)"
    return
  fi

  echo "Building $TOOL"

  javac "$SRC" || exit 1

  native-image \
    --initialize-at-build-time \
    -H:+ReportExceptionStackTraces \
    -o "../${TOOL}.tool" "$TOOL" || exit 1

  rm -f "${TOOL}"*.class

  write_hash "$TOOL" "$NEW_HASH"

  echo "Built $TOOL successfully"
}


build_tool "ChangelogPatchParser"
build_tool "ChangelogFileBuilder"
build_tool "Versioning"