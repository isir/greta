#!/bin/bash

# Lance la chaine de traitement complete d'un texte

# $1,$2,$3 = '-reacc' or '-syllabe' or '-liaison'

for tt in "$1" "$2" "$3"; do
    if [ "$tt" = "-reacc" ]; then
        REACC="-reacc"
    fi
    if [ "$tt" = "-syllabe" ]; then
        SYLLA="-s"
    fi

    if [ "$tt" == "-liaison" ]; then
        LIAISON="-liaison"
    fi
done

# dos to unix conversion (when necessary)
tr -d "\r" | \
$LIA_PHON_REP/script/lia_nett | \
$LIA_PHON_REP/script/lia_taggreac $REACC | \
$LIA_PHON_REP/script/lia_phon $SYLLA $LIAISON 

