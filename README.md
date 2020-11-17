# AutoExporter
A tool made in Java/Python to automatically import Fortnite Skins into blender

## Usage

### Exporter
* Download the [latest release](https://github.com/halfuwu/FortniteAutoExporter/releases)
* Extract the zip file
* Edit the [config.json](#Config) to fit your needs
* Run the RunExporter.bat file

### Importer
* Open Blender and import the python script **autoexporter.py** in the scripting tab
* In the script, change the **workingDirectory** variable to the path you got from the exporter (At the top of the importer script, make sure the path ahs double slashes)
* Feel free to change the other options as well
  * **bReorientBones** toggles reoriented bones
  * **textureCharacter** toggles automatic character texturing


## Config
* **PaksDirectory** : Path to Fortnite's pak folder
* **UEVersion** : Unreal Engine Version
* **EncryptionKey** : AES Key to load the paks with
* **dumpAssets** : Dump each step of the parsing process as a .json file into the Dumps folder


## Info

### Prerequisites
* The Latest [Java Runtime Environment](https://www.oracle.com/java/technologies/javase-server-jre8-downloads.html) and [JDK 11](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html) or higher
* The Latest [Blender PSK/PSA Import Plguin](https://github.com/Befzz/blender3d_import_psk_psa)
* 14.30 Fortnite Pak Files

## Required Files
* **umodel.exe** : Exports Meshes & Textures (September 30th Build works best with the v14.30 pak files)
* **SDL2.dll** : Required by umodel
* **RunExporter.bat** : Runs the exporter
* **FortniteAutoExporter.jar** : The exporter that is started by the **RunExporter.bat**
* **config.json** : Config file for the exporter
* **autoexporter.py** : The import script for blender
* **shader.blend** : The shader that is imported during the texturing process

## Limitations
* Reskins don't texture properly
* Skin styles aren't supported
* Bone parenting needs to be done manually
* Few skins with weird materials don't texture properly
