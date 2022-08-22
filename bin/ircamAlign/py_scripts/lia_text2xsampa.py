#! /usr/bin/env python

from __future__ import print_function, absolute_import, division

import sys
import os
import argparse
import ircamAlign_func

#Test Args and options

version = "unknown"
try :
    with open(os.path.join(os.path.dirname(__file__), "version.txt"), "r") as ff:
        vv=ff.readline()
        version = vv.strip()
except IOError :
    # try root dir
    with open(os.path.join(os.path.dirname(os.path.dirname(__file__)), "version.txt"), "r") as ff:
        vv=ff.readline()
        version = vv.strip()

description ="""
{0}::Version {1} 
""".format(os.path.splitext(os.path.basename(__file__))[0], version)

epilog = """===================================================================
Copyright 2017-2021 IRCAM 
-------------------------------------------------------------------
based on LIA_PHON
===================================================================
"""

parser = argparse.ArgumentParser(prog=os.path.splitext(os.path.basename(__file__))[0],
                                        epilog=epilog,
                                        description = description,
                                        formatter_class=argparse.RawTextHelpFormatter)
parser.add_argument("-t", "--txt_file", default=None, type=str, help="text file containing the text to be phonetized (Def: stdin)!")
parser.add_argument("-o", "--out_file", default=None, type=str, help="file name that will be used to store the generated phoneme sequence (Def: stdout)!")
parser.add_argument("-l","--lia_phonetisation_flag",  action="store_true", help=" use lia_phonetisation with variants (lia_text2phon_lattice instead of lia_text2phon)" )
parser.add_argument("-lp","--lia_permissive_phonetisation",
                    action="store_true",
                    help="Does not throw an error if any characters in the input text cannot be encoded in latin1 for communication with liaphon. "
                         "If set these characters will simply be ignoredill be ignored." )
parser.add_argument("-v","--verbose", action="store_true", help="verbose processing (def: %(default)s)" )
parser.add_argument("-nx","--no_xsampa", action="store_true", help="directly output the result of the lia_text2phon command  (def: %(default)s)" )
parser.add_argument("--version", action="store_true", help="display ircamAlign version and exit")
install_dir_def = os.path.dirname(os.path.dirname(os.path.realpath(__file__)))
parser.add_argument("-i","--install_dir", type=str, default=install_dir_def, help="output directory (def: {0})".format(install_dir_def))

# first version of args does not 
args = parser.parse_args()

if args.version :
    print("{0}::Version {1}".format(os.path.basename(__file__), version))
    sys.exit(0)

#INSTALLER NEED TO BE IN THE PARENT FOLDER
INSTALL_DIR 		= args.install_dir
#accessor to the type of separator
LIA_PHON_REP 		= os.path.join(INSTALL_DIR, "liaphon_build") + os.sep
HTK_BIN_DIR 		= os.path.join(INSTALL_DIR, "htk_build", "bin") + os.sep
sv56                = os.path.join(INSTALL_DIR, "sv56_build", "sv56")
os.environ["INSTALL_DIR"] 	= INSTALL_DIR
os.environ["HTK_BIN_DIR"] 	= HTK_BIN_DIR

#Test if there is a text to analyse
text = None
if args.txt_file:
    try:
        with open(args.txt_file, "r", encoding="utf-8-sig") as fi:
            text = fi.read()
        if args.verbose:
            print(f"read utf-8 encoded text <{text}>")
    except UnicodeDecodeError:
        pass
    if text is None:
        with open(args.txt_file, "r", encoding="latin1") as fi:
            text = fi.read()
        if args.verbose:
            print(f"read latin1 encoded text <{text}>")

else:
    text = sys.stdin.read()

#confidence_flag 		= False
lia_phonetisation_flag 	= args.lia_phonetisation_flag
lia_tag = 0
if lia_phonetisation_flag :
    print("  *Use lia phonetisation..", file=sys.stderr)
    lia_tag          = 1 #default 1

lia_phon = ircamAlign_func.text2phonOpen(ircamAlign_func.filtre(text, trace=args.verbose), None, 0,
                                         lia_strict_phonetisation=not args.lia_permissive_phonetisation,
                                         lia_phon_dir=LIA_PHON_REP,
                                         trace=args.verbose)
out = ""
elem = []
for line in lia_phon:
    if args.no_xsampa:
        elem.append(line.strip())
    else:
        elem += ircamAlign_func.lia2XSampa(line.split()[1]).split()

if args.out_file :
    with open(args.out_file, "w") as fo:
        if args.no_xsampa:
            print("\n".join(elem), file=fo)
        else:
            print(" ".join(elem), file=fo)
else:
    if args.no_xsampa:
        print("\n".join(elem))
    else:
        print(" ".join(elem))

