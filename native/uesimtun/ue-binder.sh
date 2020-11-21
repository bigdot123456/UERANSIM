#
# Copyright (c) 2020 ALİ GÜNGÖR (aligng1620@gmail.com)
# This software and all associated files are licensed under GPL-3.0.
#

if [ -z "$1" ]
  then
    echo "No IP address supplied"
    exit
fi

addr=$1
shift

LD_PRELOAD=./libue-binder.so UE_BIND_ADDR=$addr $@