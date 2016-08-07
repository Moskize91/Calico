#!/bin/sh

STRUCT_FILE="$PWD/system/STRUCT"
if [ -f "$STRUCT_FILE" ]
then
    rm $STRUCT_FILE
fi
list_alldir(){  
    for file in `ls -a $1`  
    do  
        if [ x"$file" != x"." -a x"$file" != x".." ];then  
            if [ -d "$1/$file" ];then  
                echo "$2/$file" >> $STRUCT_FILE
                list_alldir "$1/$file" "$2/$file"
            fi  
        fi  
    done  
} 
list_alldir system