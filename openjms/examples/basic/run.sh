#!/bin/sh
# -----------------------------------------------------------------------------
# Run script for the OpenJMS examples
#
# Required Environment Variables
#
#   JAVA_HOME       Points to the Java Development Kit installation.
#
# Optional Environment Variables
# 
#   OPENJMS_HOME    Points to the OpenJMS installation directory.
#
# $Id: run.sh,v 1.1 2005/06/13 14:42:26 tanderson Exp $
# -----------------------------------------------------------------------------

# OS specific support.  $var _must_ be set to either true or false.
cygwin=false
case "`uname`" in
CYGWIN*) cygwin=true;;
esac

# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin; then
  [ -n "$JAVA_HOME" ] && JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
  [ -n "$OPENJMS_HOME" ] && OPENJMS_HOME=`cygpath --unix "$OPENJMS_HOME"`
fi

if [ -z "$JAVA_HOME" ]; then
  echo "The JAVA_HOME environment variable is not set."
  echo "This is required to run the examples."
  exit 1
fi
if [ ! -r "$JAVA_HOME"/bin/java ]; then
  echo "The JAVA_HOME environment variable is not set correctly."
  echo "This is required to run the examples."
  exit 1
fi
_RUNJAVA="$JAVA_HOME"/bin/java


# Guess OPENJMS_HOME if it is not set
if [ -z "$OPENJMS_HOME" ]; then
# resolve links - $0 may be a softlink
  PRG="$0"
  while [ -h "$PRG" ]; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '.*/.*' > /dev/null; then
      PRG="$link"
    else
      PRG=`dirname "$PRG"`/"$link"
    fi
  done

  PRGDIR=`dirname "$PRG"`
  OPENJMS_HOME=`cd "$PRGDIR/../.." ; pwd`
elif [ ! -r "$OPENJMS_HOME"/lib/openjms-0.7.7-beta-1.jar ]; then
  echo "The OPENJMS_HOME environment variable is not set correctly."
  echo "This is required to run the examples."
  exit 1
fi

CLASSPATH=./:"$OPENJMS_HOME"/lib/openjms-0.7.7-beta-1.jar


# For Cygwin, switch paths to Windows format before running java
if $cygwin; then
  JAVA_HOME=`cygpath --path --windows "$JAVA_HOME"`
  OPENJMS_HOME=`cygpath --path --windows "$OPENJMS_HOME"`
  CLASSPATH=`cygpath --path --windows "$CLASSPATH"`
fi

POLICY_FILE="$OPENJMS_HOME"/config/openjms.policy

# Execute the requested command

echo "Using OPENJMS_HOME: $OPENJMS_HOME"
echo "Using JAVA_HOME:    $JAVA_HOME"
echo "Using CLASSPATH:    $CLASSPATH"

if [ "$#" != "" ]; then
  MAINCLASS=$1
  shift
  exec "$_RUNJAVA" -classpath "$CLASSPATH" \
      -Djava.security.manager -Djava.security.policy="$POLICY_FILE" \
      $MAINCLASS "$@"

else

  echo "usage: run.sh (classname)"
  exit 1

fi
