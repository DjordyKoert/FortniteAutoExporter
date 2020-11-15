# AutoExporter
A tool made in Java/Python to automatically export and importing Fortnite Skins

## Usage

### Prerequisites
* The Latest [Java Runtime Environment](https://www.oracle.com/java/technologies/javase-server-jre8-downloads.html) and [JDK 14](https://www.oracle.com/java/technologies/javase/jdk14-archive-downloads.html)
* The Latest [Blender PSK/PSA Import Plguin](https://github.com/Befzz/blender3d_import_psk_psa)
* 14.30 Fortnite Pak Files


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
