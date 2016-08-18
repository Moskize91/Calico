#!/bin/bash

CALICO_JAR_FILE_NAME="Calico-debug.jar"

#check build builded?
if [[ ! -f "$PWD/release/$CALICO_JAR_FILE_NAME" ]]; then
    echo "calico has bean not build, please run `./build` first."
    exit 1
fi

#check installed java1.8
if type -p java; then
    _java=java
elif [[ -n "$JAVA_HOME" ]]&&[[ -x "$JAVA_HOME/bin/java" ]]; then
    _java="$JAVA_HOME/bin/java"
else
    echo "calico need Java (version >= 1.8). you have to install Java first."
    exit 1
fi

if [[ "$_java" ]]; then
    version=$("$_java" -version 2>&1 | awk -F '"' '/version/ {print $2}')
    if [[ "$version" < "1.8" ]]; then
        echo "the version of your Java is too low.(need 1.8 but $version)"
        exit 1
    fi
fi
#check ant

echo "Installing calico..."
rm -rf $PWD/release/bin
mkdir $PWD/release/bin
echo "#!/bin/sh" > $PWD/release/bin/calico
echo "java -jar $PWD/release/$CALICO_JAR_FILE_NAME \$1" >> $PWD/release/bin/calico
chmod a+x $PWD/release/bin/calico

echo "Try to link calico. (Need Permission)"
if [ -f "/usr/local/bin/calico" ]
then
    echo "Try to relink calico. (Need Permission)"
    sudo rm /usr/local/bin/calico
fi

sudo ln $PWD/release/bin/calico /usr/local/bin/calico

echo "Success."
