SUMMARY = "6LBR is a 6LoWPAN Border Router based on The Contiki Operating System."
DESCRIPTION = "A 6LoWPAN Border Router connects your 6LoWPAN devices \
               to the Internet and is responsible for handling traffic \
               to and from the IPv6 and 802.15.4 interfaces."
HOMEPAGE = "https://github.com/cetic/6lbr"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=b2276b027815460f098d51494e2ff4f1"
DEPENDS = "ncurses bridge-utils"
SRCREV = "56f8407964377406e1d0210b0928024a9b870f22"
SRC_URI = "gitsm://github.com/cetic/6lbr.git;protocol=git;branch=develop\
          file://001-6lbr_sysvinit.patch \
          file://001-cross_compile_makefile.patch \
          file://6lbr.conf \
          file://nvm.dat \
          file://6lbr.service \
"
S = "${WORKDIR}/git"
# COMPATIBLE_MACHINE = "raspberrypi"
# PARALLEL_MAKE=""
EXTRA_OEMAKE = "CC='${CC}' LD_OVERRIDE='${CC}'"
RDEPENDS_${PN} = "bridge-utils"
RRECOMMENDS_${PN} = "kernel-module-tun"

INITSCRIPT_NAME = "6lbr"
INITSCRIPT_PARAMS = "defaults"

SYSTEMD_AUTO_ENABLE = "enable"
SYSTEMD_SERVICE_${PN} = "6lbr.service"

inherit update-rc.d
inherit ${@bb.utils.contains('VIRTUAL-RUNTIME_init_manager','systemd','systemd','', d)}

do_compile() {
    oe_runmake -C examples/6lbr all
    oe_runmake -C examples/6lbr plugins
    oe_runmake -C examples/6lbr tools
}

do_install() {
    oe_runmake -C examples/6lbr install DESTDIR=${D}
    oe_runmake -C examples/6lbr plugins-install DESTDIR=${D}
    if ${@bb.utils.contains('DISTRO_FEATURES','sysvinit','false','true',d)}; then
       rm -rf ${D}/etc/init.d/
    fi
    if ${@bb.utils.contains('DISTRO_FEATURES','systemd','true','false',d)}; then
       install -d ${D}${systemd_system_unitdir}
       install -m 0644 ${WORKDIR}/6lbr.service ${D}${systemd_system_unitdir}
    fi
    install ${WORKDIR}/6lbr.conf ${D}/etc/6lbr/
    install ${WORKDIR}/nvm.dat ${D}/etc/6lbr/
}

FILES_${PN} = "/usr/lib/6lbr/* \
              /usr/lib/6lbr/*/* \
              /usr/lib/6lbr/*/*/* \
              /etc/6lbr/* \
              /usr/bin/6lbr \
              ${@bb.utils.contains('DISTRO_FEATURES','systemd','${systemd_system_unitdir}/*','',d)} \
              ${@bb.utils.contains('DISTRO_FEATURES','sysvinit','/etc/init.d /etc/init.d/6lbr','',d)} \
"