#!/bin/bash

scriptDir="$(readlink -f $(dirname $0))"

sourceFolder="$1"
targetFolder="$scriptDir/../resources/scratch/B400022028241-RT2/"

pushd "$targetFolder"
git rm -fr "$targetFolder"
popd

cp -r "$sourceFolder"/* "$targetFolder"

pushd "$targetFolder"
find -name *.jp2 | xargs -I'{}' bash -c "rm  '{}'; touch '{}'; md5sum '{}' > '{}'.md5 "
find -name *.xml | xargs -I'{}' bash -c "md5sum '{}' > '{}'.md5 "

find | xargs git add

popd




