JFileSync, Version 2.2.1
Copyright (C) 2022-2023, Oliver Burgmaier
Copyright (C) 2002-2007, Jens Heidrich

Release Notes

1) Introduction
2) License and Usage Terms
3) Requirements
4) Installation and Application Start
5) Known Issues
6) Development Notes
7) Included Directories and Files
8) Contact and Further Information


1) Introduction

JFileSync is used to synchronize directories of (usually) two different file
systems. For instance, you have a laptop and a workstation and you want to
synchronize different directories on those machines in a platform-independent
way.

What you have to do, to use JFileSync for that purpose is (1) making the
directories of the laptop available for the workstation or vice versa (e.g.,
via NFS, Samba, or Windows File System Sharing) or using a JFS server to
access an external file system structure directly and (2) specifying an
appropriate configuration profile for JFileSync.

All functions of JFileSync can be controlled via the Graphical User Interface
(GUI). However, JFileSync provides full access to all features (apart from
plug-ins) via the command line interface. Call 'java -jar jfs.jar -help' to
get an overview of all possible command line options.


2) License and Usage Terms

This program is free software; you can redistribute it and/or modify it under
the terms of the GNU General Public License as published by the Free Software
Foundation; either version 2 of the License, or (at your option) any later
version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
this program; if not, write to the Free Software Foundation, Inc., 51 Franklin
St, Fifth Floor, Boston, MA, 02110-1301, USA

JFileSync uses libraries or parts of the following Open Source projects:
- Eclipse icons, which are provided under the Common Public License - v 1.0.

You can find a copy all licenses of JFileSync and the used libraries in the
'docs' sub directory of this distribution.


3) Requirements

- Java 5 Runtime Environment >= 1.5.0 (see 'http://java.sun.com')


4) Installation and Application Start

- Unzip the distribution file to a directory of your choice.
- For simplicity reasons a Windows batch file ('JFileSync.bat') and a Unix
  shell script ('JFileSync.sh') are available via the main distribution
  directory in order to start the application directly.
  ATTENTION: Because JFileSync is distributed as a Zip archive, Unix users
  will have to give executable rights manually before launching the
  application, e.g.: 'chmod a+rx JFileSync.sh'.
  If you have problems with the file encoding or line delimiters (when using
  an older version of the shell script) you will have to convert the script
  from DOS format to Unix format, e.g.: 'dos2unix JFileSync.sh'.
- If you do not want to use the start scripts you may also do the following:
  Enter the 'lib' sub directory of the distribution and call
  'java -jar jfs.jar' in order to start the Graphical User Interface or
  call 'java -jar jfs.jar -help' in order to get information about command
  line options. Depending in your Operating System a double click on
  'jfs.jar' will also launch the application.


5) Known Issues

- When using a JFS server, the default timeout is set to 5 seconds. When using
  a slow modem connection this may cause a lot of timeouts during
  synchronization. Therefore, you should increase the timeout (via
  the JFS server properties) to at least a minute (3.600.000 ms) before
  starting a synchronization via modem.
- Currently, the used protocol for file transfers via a JFS server is not
  interlocked several times so that the success of some operations on server
  side are unknown for the client. This will be changed in future releases in
  order to improve the robustness.
- The JSF server transmits Java objects as part of its protocol. This leads
  to an increased communication overhead (compared to, e.g., a plain text
  protocol).
- Before synchronization you may select a sub-set of the computed copy and
  delete statements. You may, for instance, de-select copying a directory, but
  select copying the files included in the directory. In this case, copying the
  files only will fail, because their directory is never created.


6) Development Notes

Required packages for JFileSync development (not included in the distribution):
- Java 5 SDK >= 1.5.0 (see 'http://java.sun.com')
- Apache Ant >= 1.6.2 (see 'http://ant.apache.org')

Used and therewith recommended development tools:
- Java 5 SDK 1.5.0
- Apache Ant 1.6.2
- Eclipse 3.1.0

Ant build targets:
- 'ant' creates a jar file named 'jfs.jar' to the 'lib' sub directory.
- 'ant clean-all' cleans the development directory.
- 'ant docs' creates the API documentation to the 'docs' sub directory.
- 'ant release' creates a JFS end-user release.
- 'ant snapshot' creates a JFS snapshot release.
- 'ant dev' creates a JFS developer release.
- 'ant reset' resets the testing directories after a synchronization test.
- 'ant test' starts the JFS application.
- 'ant test-native' starts the native language JFS application.
- 'ant test-server' tests the JFS server implementation.
- 'ant src-license' replaces the license text of all source code files.
- 'ant web-preview' creates a preview of the JFS web pages.
- 'ant clean-web' cleans the preview of the JFS web pages.


7) Included Directories and Files

The following structure describes all directories and files included in the
JFileSync end user releases containing only resources necessary to run the
application:
- docs: The program licenses, QA plan, and change history.
- lib: Necessary libraries in order to run the system.
- profiles: Sample user profiles.
- jfilesync-src.zip: The packed source code of the overall application.
- JFileSync.bat: Batch file to launch JFileSync from Windows.
- JFileSync.sh: Shell script to launch JFileSync from Unix/Linux.
- JFileSync.ico: Icon resource file containing the JFileSync logo.
- ReadMe.txt: This file.

The following structure describes all directories and files included in the
JFileSync developer releases (apart from hidden files) containing all
resources necessary to develop the application:
- classes: Temporary class file folder, generated by the Ant build file.
- docs: The program licenses, QA plan, and change history.
- lib: Necessary libraries in order to run the system.
- profiles: Sample user profiles.
- src: The source code of the overall application.
- test: A testing directory structure including test profiles.
- JFileSync.bat: Batch file to launch JFileSync from Windows.
- JFileSync.sh: Shell script to launch JFileSync from Unix/Linux.
- JFileSync.ico: Icon resource file containing the JFileSync logo.
- ReadMe.txt: This file.
- Version.txt: The version of the current release.


8) Contact and Further Information

You may submit improvement suggestions, patches, and other enhancements to
Jens Heidrich <jensheidrich@web.de>.

JFileSync is a SourceForge project and available via the following URL:
http://jfilesync.sourceforge.net/
