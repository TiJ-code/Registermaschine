#!/usr/bin/python3

import subprocess, sys

MODULES = ['core', 'ui', 'console']
KINDS = ['major', 'minor', 'patch']


def run_cmd(cmd):
    result = subprocess.run(
        cmd,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True
    )
    if result.returncode != 0:
        print("Error:", result.stderr.strip())
        sys.exit(1)
    return result.stdout.strip()


def run_git(args):
    return run_cmd(["git"] + args)


def get_latest_version(module):
    pattern = f"{module}-v*"
    tags = run_git(["tag", "--list", pattern]).splitlines()

    if not tags:
        return (0, 0, 0)

    sorted_tags = run_git(["tag", "--list", pattern, "--sort=-v:refname"]).splitlines()
    latest = sorted_tags[0]

    version_str = latest.replace(f"{module}-v", "")
    parts = version_str.split(".")

    return tuple(int(p) for p in parts)


def bump_version(version, kind):
    major, minor, patch = version

    if kind == "major":
        return (major + 1, 0, 0)
    elif kind == "minor":
        return (major, minor + 1, 0)
    elif kind == "patch":
        return (major, minor, patch + 1)
    else:
        return ValueError("Invalid bump type")


def select_from_list(prompt, options):
    print(prompt)
    for i, option in enumerate(options, start=1):
        print(f"{i}) {option}")

    while True:
        choice = input("> ").strip()
        if choice.isdigit():
            index = int(choice) - 1
            if 0 <= index < len(options):
                return options[index]
        print("Invalid selection. Try again.")


def main():
    module = select_from_list("Select module:", MODULES)
    kind = select_from_list("Select version bump:", KINDS)

    current_version = get_latest_version(module)
    next_version = bump_version(current_version, kind)

    tag_name = f"{module}-v{next_version[0]}.{next_version[1]}.{next_version[2]}"

    print(f"\nCurrent version: {'.'.join(map(str, current_version))}")
    print(f"Next version:    {'.'.join(map(str, next_version))}")
    print(f"Tag to create:   {tag_name}")
    
    confirm = input("\nCreate this tag? (y/n) ").strip().lower()
    if confirm != "y":
        print("Aborted")
        return

    auto_tags = []

    if module == "core":
        new_version = f"{next_version[0]}.{next_version[1]}.{next_version[2]}"
        print(f"Updating registermaschine-core POM to {new_version}")
        run_cmd(["bash", "-c", f" ./mvnw versions:set -DnewVersion={new_version} -pl registermaschine-core -DgenerateBackupPoms=false"])

        print("Installing core to local Maven repo")
        run_cmd(["bash", "-c", f" ./mvnw install -pl registermaschine-core -DskipTests"])

        print("Updating UI and Console POM dependency on core")
        for mod in ["ui", "console"]:
            run_cmd(["bash", "-c", f" ./mvnw versions:set-property -Dproperty=registermaschine-core.version -DnewVersion={new_version} -pl registermaschine-{mod} -DgenerateBackupPoms=false"])

        for mod in ["ui", "console"]:
            v = get_latest_version(mod)
            nv = bump_version(v, kind)
            new_ver = f"{nv[0]}.{nv[1]}.{nv[2]}"
            t = f"{mod}-v{new_ver}"
            auto_tags.append(t)
            # update POM version so it's included in this commit
            run_cmd(["bash", "-c", f"./mvnw versions:set -DnewVersion={new_ver} -pl registermaschine-{mod} -DgenerateBackupPoms=false"])
    else:
        new_version = f"{next_version[0]}.{next_version[1]}.{next_version[2]}"
        print(f"Updating registermaschine-{module} POM to {new_version}")
        run_cmd(["bash", "-c", f" ./mvnw versions:set -DnewVersion={new_version} -pl registermaschine-{module} -DgenerateBackupPoms=false"])

    run_git(["add", "."])
    run_git(["commit", "-m", "[ACTION] Version Bump for Core/UI/Console"])
    run_git(["tag", tag_name])
    print(f"Tag {tag_name} created.")

    for t in auto_tags:
        run_git(["tag", t])
        print(f"Auto-tagged {t}")

    run_git(["add", "."])
    run_git(["push", "origin"])

    push = input("Push tag to origin? (y/n) ").strip().lower()
    if push == "y":
        run_git(["push", "origin"] + auto_tags)

        run_git(["push", "origin", tag_name])
        print("All tags pushed.")


if __name__ == '__main__':
    main()