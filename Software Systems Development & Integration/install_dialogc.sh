#!/bin/bash

# Name: Jireh Agda
# Student ID: 0795472
# Date Last Modified: 2015 04 02
# Dialogc Installer (Bash)

# maximum number of arguments install_dialogc.sh accepts is 1 (only 0 or 1)
maxArgs=1
numOfArgs=${#}

# Dialogc Installer will only continue if there is/are only 0 or 1 flag(s)
if [ ${numOfArgs} \> ${maxArgs} ]; then
    echo 'install_dialogc.sh only accepts 0 or 1 flag(s)'
    echo 'Please use install_dialogc.sh with --build, --install or no flags'
    exit 1
fi

# the flag/argument 1 is checked to see if it is valid/supported, attaching an installType and message if accepted or exiting
if [ ${numOfArgs} = '0' ]; then
    message='Dialogc Installer will now build Dialogc in ./Dialogc and install Dialogc to <directed directory>/Dialogc'
    installType='--full'
elif [ ${1} = '--build' ]; then
    message='Dialogc Installer will now build Dialogc in ./Dialogc'
    installType='--build'
elif [ ${1} = '--install' ]; then
    message='Dialogc Installer will now install Dialogc to <directed directory>/Dialogc'
    installType='--install'
else
    echo 'install_dialogc.sh was used with an unsupported flag'
    echo 'Please use install_dialogc.sh with --build, --install or no flags'
    exit 1
fi

echo 'Dialogc Installer - Jireh Agda - CIS*2750'
echo ''
echo 'Welcome!'

# user is informed of upcoming process and script asks for confirmation to continue
confirm=false
while [ ${confirm} = 'false' ]; do
    echo ${message}
    echo -n 'Would you like to proceed? (y/n) '
    read proceed
    if [ ${proceed} = 'y' ] || [ ${proceed} = 'Y' ]; then
        confirm=true
    elif [ ${proceed} = 'n' ] || [ ${proceed} = 'N' ]; then
        echo 'Exiting Installer...'
        exit 0
    else
        echo "Please enter 'y' or 'n'"
        echo ''
    fi
done

echo ''

# installation is divided into two parts - build and install
# depending on the flag the user chooses, install_dialogc.sh will do build only, install only or both by falling through the first 'case'
case ${installType} in
    '--full' | '--build' )
        echo '-> Building Dialogc'                            # ADD >& /dev/null to commands! and some !

        echo 'Extracting Dialogc build...'
        if [ ! echo 'uudecode install_dialogc.sh' ]; then
            echo 'Extraction failed'
            echo 'Now exiting installer...'
            exit 1
        fi

        echo 'Unpacking Dialogc build...'
        if [ ! echo 'tar -zxf Dialogc.tar.gz' ]; then
            echo 'Unpacking failed'
            echo 'Now exiting installer...'
            exit 1
        fi

        echo 'Building Dialogc...'
        if ! [ cd './Dialogc' ] || ! [ echo 'make' ]; then
            echo 'Building failed'
            echo 'Now exiting installer...'
            exit 1
        fi

        echo 'Build complete'
        if [ ${installType} = '--build' ]; then
            echo 'Now exiting installer...'
            exit 0
        fi

        echo ' '
        ;&

    '--install' )
        # installation only starts if the yadc executable and Dialogc class have already been built
        dialogc='./Dialogc/Dialogc.class'
        yadc='./Dialogc/yadc'
        if ! [ -e ${dialogc} ] || ! [ -e ${yadc} ]; then
            echo 'No Dialogc build present'
            echo 'Please build Dialogc before continuing installation'
            echo 'Now exiting installer...'
            exit 1
        fi

        echo '-> Installing Dialogc'

        # user is prompted to choose a directory to install to
        echo -n 'Please choose a directory to install to: '
        read directory
        # if the directory does not exist yet, user is prompted to confirm creating it or not
        if ! [ -d ${directory} ]; then
            confirmCreate=false
            while [ ${confirmCreate} = 'false' ]; do
                echo -n "Directory ${directory} does not exist. Would you like to create it? (y/n) "
                read makeDir
                if [ ${makeDir} = 'y' ] || [ ${makeDir} = 'Y' ]; then
                    confirmCreate=true

                    # individual mkdir commands are made for each new directory that doesn't exist
                    makeDirPath=${directory%%/*}
                    tempPath=${directory}

                    directoryLoop='incomplete'
                    while [ ${directoryLoop} = 'incomplete' ]; do
                        # this exits the loop when we match and create the last directory
                        if [ ${tempPath} = ${directory##*/} ]; then
                             directoryLoop='complete'
                        fi

                        # if the directory does not exist already, then it is made
                        if ! [ -d ${makeDirPath} ]; then
                            if [ echo "mkdir ${makeDirPath}" ]; then
                                echo "Making ${makeDirPath} directory failed"
                                echo 'Now exiting installer...'
                                exit 1
                            fi
                        fi

                        tempPath=${tempPath#*/}
                        makeDirPath=${makeDirPath}'/'${tempPath%%/*}
                    done
                elif [ ${makeDir} = 'n' ] || [ ${makeDir} = 'N' ]; then
                    echo 'Unable to create directory for installation'
                    echo 'Exiting installer...'
                    exit 0
                else
                    echo "Please enter 'y' or 'n'"
                    echo ''
                fi
            done
        # if the directory exists but we do not have write permission, the installation exits with messages
        elif [ -d ${directory} ] && ! [ -w ${directory} ]; then
            echo 'Unable to write to directory for installation (Invalid write permission)'
            echo 'Exiting installer...'
            exit 1
        fi

        # make subdirectories
        if [ echo 'mkdir subdirectories' ]; then
            echo 'Making directories failed'
            echo 'Now exiting installer...'
            exit 1
        fi

        # user is notified of the modifying of .bashrc and is prompted to confirm
        confirmMod=false
        while [ ${confirmMod} = 'false' ]; do
            echo 'Modifying .bashrc to set LD_LIBRARY_PATH...'
            echo -n 'Would you like to proceed? (y/n) '
            read setConfirm
            if [ ${setConfirm} = 'y' ] || [ ${setConfirm} = 'Y' ]; then
                confirmMod=true
                if [ echo 'append to ~/.bashrc export LD_LIBRARY_PATH=${LD_LIBRARY_PATH}:../lib' ]; then
                    echo '~/.bashrc modification failed'
                    echo 'Now exiting installer...'
                    exit 1
                fi
            elif [ ${setConfirm} = 'n' ] || [ ${setConfirm} = 'N' ]; then
                echo 'Exiting installer...'
                exit 0
            else
                echo "Please enter 'y' or 'n'"
                echo ''
            fi
        done

        dialogcDef=${HOME}'/.dialogc'
        echo 'Setting up defaults...'; echo ${dialogcDef}
        if [ touch ${dialogcDef} >& /dev/null ]; then
            echo 'Creating defaults failed'
            echo 'Now exiting installer...'
            exit 1
        fi
        echo "JAVA_C=$(which javac)" >> ${dialogcDef} >2 /dev/null
#            echo 'Setting defaults failed'
#            echo 'Now exiting installer...'
#            exit 1
#        fi


        # add Dialogc?
        echo 'Copying files...'
        if ! [ echo "cp ./*.class ${directory}/bin/" ] || ! [ echo "cp ./yadc ${directory}/bin/" ]; then
            echo 'Copying executables failed'
            echo 'Now exiting installer...'
            exit 1
        elif ! [ echo "cp ./src/libpm.a ${directory}/lib/" ] || ! [ echo "cp ./libJNIpm.so ${directory}/lib/" ]; then
            echo 'Copying libraries failed'
            echo 'Now exiting installer...'
            exit 1
        elif ! [ echo "cp ./config/* ${directory}/config/" ]; then
            echo 'Copying config files failed'
            echo 'Now exiting installer...'
            exit 1
        elif ! [ echo "cp ./images/* ${directory}/images/" ]; then
            echo 'Copying images failed'
            echo 'Now exiting installer...'
            exit 1
        fi

        # the installer cleans up the build by removing the Dialogc directory and the tar archive
        echo 'Cleaning up installation...'
        if [ echo 'rm -r ./Dialogc' ] || [ echo 'rm Dialogc.tar.gz' ]; then
            echo 'Cleaning failed'
            echo 'Now exiting installer...'
            exit 1
        fi

        echo 'Installation Complete!'
        ;;
    * )
        echo 'Invalid flag entered'
        echo 'Now exiting installer...'
        exit 1
esac

exit 0

