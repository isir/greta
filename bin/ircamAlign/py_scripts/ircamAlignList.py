#! /usr/bin/env python


import os
import sys
import subprocess

import argparse

parser = argparse.ArgumentParser("Process list of files with ircamAlign",prefix_chars = "+")

parser.add_argument("sndfilelist", help="list of sound files to process")
parser.add_argument("txtfilelist", help="list of txt files ordered following sndfile list")
parser.add_argument("++args", nargs="*", help="arguments pa&ssed on  to ircamAlign")

args = parser.parse_args()

## Open the file with read only permit
snds = open(args.sndfilelist, "r")

## Open the file with read only permit
txts = open(args.txtfilelist, "r")


for snd, txt in zip(snds.readlines(),txts.readlines()) :
    cmd = ["ircamalign", snd.strip(), "-t", txt.strip()] + args.args
    print " ".join(cmd)
    try :
        out=subprocess.check_output(cmd, shell=False )
        print out
    except CalledProcessError as ex:
        print "cmd :"," ".join(cmd), " returned with code: {0:d} output:: {1}".format(ex.returncode, err.output)
        sys.exit(1)

