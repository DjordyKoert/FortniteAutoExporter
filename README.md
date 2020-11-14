# AutoExporter
A tool made in Java/Python to automatically export and importing Fortnite Skins

##Usage
###Exporter
* Make sure you have **64-bit Java** installed.
* Extract the zip file
* Edit the [config.json](#Config)
* Run the RunExporter.bat file

###Importer
* Make sure you have the latest [Blender PSK/PSA Import Plguin](https://github.com/Befzz/blender3d_import_psk_psa)
* Open Blender and import the python script **autoexporter.py** in the scripting tab
* In the script, change the **workingDirectory** variable to the path you got from the exporter

##Config
* **PaksDirectory** : Path to Fortnite's pak folder
* **UEVersion** : Unreal Engine Version
* **EncryptionKey** : AES Key to load the paks with
* **dumpAssets** : Dump each step of the parsing process as a .json file into the Dumps folder
