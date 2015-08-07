#!/bin/bash

scriptDir="$(readlink -f $(dirname $0))"

sourceFolder=$1

jpylyzerSourceFolder=$sourceFolder/../jpylyzer/

targetFolder=$scriptDir/../resources/scratch/B400022028241-RT1/

jpylyzerTargetFolder=$targetFolder/../../jpylyzerFiles/

mkdir -p $targetFolder
pushd $targetFolder
git rm -fr $targetFolder
popd
mkdir -p $targetFolder

cp -r $sourceFolder/* $targetFolder
cp -r $jpylyzerSourceFolder/* $jpylyzerTargetFolder

pushd $targetFolder
find -name *.jp2 | xargs -I'{}' bash -c "rm  '{}'; touch '{}'; md5sum '{}' > '{}'.md5 "
find -name *.xml | xargs -I'{}' bash -c "md5sum '{}' > '{}'.md5 "

find | xargs git add
popd

pushd $jpylyzerTargetFolder
find | xargs git add
popd




