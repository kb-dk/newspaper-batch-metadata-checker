1.4
* Updated to newspaper-parent 1.1, supporting new testing strategy
* Only read database information once on startup
* Update identifier scheme in agreement with Ninestars
* Only require brik-files to be mentioned by at least one MODS file per image, not all of them
* Support missing page mods files change request

1.3.2
* Updated framework dependency to 1.4.5 to enable maxResults
* Config parameter autonomous.component.maxResults is now respected

1.3.1
* Update to newspaper-batch-event-framekwork 1.4.2, to make the component quiet on stderr
* Remove System.out.println() from non-test code

1.3
* Update MIX checks to be different on WORKSHIFT-ISO-TARGET matching 9*
* Disable check for special characters, it is not well defined
* No roundtrip in ALTO fileName
* Allow empty date in dateMicrofilmCreated
* Fix of fuzzy date pattern

1.2
* Update to batch-event-framework 1.4
* Fix NullPointerException in ModsXPathEventHandler when missing section title element
* Add support for fuzzy dates

1.1
* Read logback configuration from classpath
* Check for Option B7 (reading of newspaper section titles)
* Check for Option B1/B2/B9 (alto-files)
* Update of film metadata schema to match discussions with Ninestars and to allow partial dates
* Various fixes, and getting ready for stage

1.0
* Checks are done on ALTO, page-MODS, edition-MODS, MIX, FILM and JP2 jpylyzer analysis as defined below
* All xml metadata blobs are checked for XML schema validity
* All xml metadata blobs are checked for conformance to specification
* Values from xml metadata blobs and file structure are cross correlated and checked to be consistent
* Values from xml metadata blobs are checked agains MFPAK database
* Jpylyzer output from analysis of JP2 files are checked to conform to specification
* All checks refer to the specification in the message

0.2
* Standalone page-mods, alto and mix xml file checks. This excludes checks against other files and the MFPak db.

0.1
* Initial release
