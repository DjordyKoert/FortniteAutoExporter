"""
Fortnite Auto Exporter v0.1.0
"""
import bpy
import json
import os
import json
import subprocess
from io_import_scene_unreal_psa_psk_280 import pskimport

workingDirectory = "D:\\Blender Files\\Fortnite Auto Exporter"
bReorientBones = True

# DONT EDIT ANYTHING BELOW HERE
os.chdir(workingDirectory)

f = open("processed.json", "r")
processedJSON = f.read()


processedLOADS = json.loads(processedJSON)

assetPath1 = processedLOADS.get("assetPath1")
assetPath2 = processedLOADS.get("assetPath2")
assetPath3 = processedLOADS.get("assetPath3")
assetPath4 = processedLOADS.get("assetPath4")
assetPath5 = processedLOADS.get("assetPath5")

if not assetPath1 == None:
    pskimport(assetPath1, bpy.context, bReorientBones = bReorientBones)
if not assetPath2 == None:
    pskimport(assetPath2, bpy.context, bReorientBones = bReorientBones)
if not assetPath3 == None:
    pskimport(assetPath3, bpy.context, bReorientBones = bReorientBones)
if not assetPath4 == None:
    pskimport(assetPath4, bpy.context, bReorientBones = bReorientBones)
if not assetPath5 == None:
    pskimport(assetPath5, bpy.context, bReorientBones = bReorientBones)

bpy.ops.object.select_all(action='SELECT')
bpy.ops.object.shade_smooth()
bpy.ops.object.select_all(action='DESELECT')