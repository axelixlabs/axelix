import FreeBSD from "assets/icons/freeBSD.svg";

import { ECopyableField } from "models";

import BuildIcon from "assets/icons/build.svg";
import GitIcon from "assets/icons/git.svg";
import JavaIcon from "assets/icons/java.svg";
import KotlinIcon from "assets/icons/kotlin.svg";
import LinuxIcon from "assets/icons/linux.svg";
import SpringIcon from "assets/icons/spring.svg";
import WindowsIcon from "assets/icons/windows.svg";

export const detailsCardsPreferredOrder = ["git", "build", "spring", "runtime", "os"];

export const detailsIcons = {
    git: GitIcon,
    spring: SpringIcon,
    kotlin: KotlinIcon,
    linux: LinuxIcon,
    windows: WindowsIcon,
    build: BuildIcon,
    freeBSD: FreeBSD,
    java: JavaIcon,
};

export const isCopyNeeded: ECopyableField[] = [
    ECopyableField.CommitShaShort,
    ECopyableField.Branch,
    ECopyableField.Artifact,
];
