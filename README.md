# AutoExporter
A tool made in Java/Python to automatically export and importing Fortnite Skins

## Usage

### Prerequisites
* 64-bit Java
* The Latest [Blender PSK/PSA Import Plguin](https://github.com/Befzz/blender3d_import_psk_psa)
* 14.30 Fortnite Pak Files
* The [September 30th Build](https://github.com/gildor2/UEViewer/blob/25f494af0f64293ced8ac4fab8506467aa0b5876/umodel.exe) of uModel along with [SDL2.dll](https://github.com/gildor2/UEViewer/blob/master/libs/SDL2/x64/SDL2.dll)
    * Any umodel version past September 30th crashes with Skeletal Meshes  


### Exporter
* Extract the zip file
* Edit the [config.json](#Config) to fit your needs
* Run the RunExporter.bat file

### Importer
* Open Blender and import the python script **autoexporter.py** in the scripting tab
* In the script, change the **workingDirectory** variable to the path you got from the exporter

## Config
* **PaksDirectory** : Path to Fortnite's pak folder
* **UEVersion** : Unreal Engine Version
* **EncryptionKey** : AES Key to load the paks with
* **dumpAssets** : Dump each step of the parsing process as a .json file into the Dumps folder
