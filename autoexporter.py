'''
Fortnite Auto Exporter by Half
'''

import bpy
import json
import os
import io_import_scene_unreal_psa_psk_280

# SETTINGS

# CHANGE THE BELOW VALUE TO THE PATH GIVEN AT THE END OF THE EXPORTER
workingDirectory = "D:\\Blender Files\\Fortnite Auto Exporter"
# CHANGE THE ABOVE VALUE TO THE PATH GIVEN AT THE END OF THE EXPORTER

bReorientBones = True
textureCharacter = True

# DONT EDIT ANYTHING BELOW HERE

def find_collection(context, item):
    collections = item.users_collection
    if len(collections) > 0:
        return collections[0]
    return context.scene.collection

def make_collection(collection_name, parent_collection):
    if collection_name in bpy.data.collections:
        return bpy.data.collections[collection_name]
    else:
        new_collection = bpy.data.collections.new(collection_name)
        parent_collection.children.link(new_collection)
        return new_collection
def textureSkin(MaterialName, Index):
    mat = bpy.data.materials[MaterialName]
    mat.use_nodes = True

    nodes = mat.node_tree.nodes

    nodes.clear()

    shaderfile = workingDirectory + "\\shader.blend\\NodeTree\\"
    shadersection = "\\NodeTree\\"
    shaderobject = "Fortnite Auto Exporter Shader"

    bpy.ops.wm.append(directory = shaderfile, filename = shaderobject)

    links = mat.node_tree.links

    group = nodes.new('ShaderNodeGroup')
    group.node_tree = bpy.data.node_groups[shaderobject]
    group.inputs[3].default_value = 5

    node_output = nodes.new(type="ShaderNodeOutputMaterial")
    node_output.location = 200, 0

    if "Diffuse" in processedLOADS["Materials"][i]:
        node_diffuse = nodes.new(type="ShaderNodeTexImage")
        node_diffuse.image = bpy.data.images.load(processedLOADS["Materials"][i]["Diffuse"])
        node_diffuse.location = -400,-75
        node_diffuse.hide = True
        link = links.new(node_diffuse.outputs[0], group.inputs[0])

    if "SpecularMasks" in processedLOADS["Materials"][i]:
         node_specular = nodes.new(type="ShaderNodeTexImage")
         node_specular.image = bpy.data.images.load(processedLOADS["Materials"][i]["SpecularMasks"])
         node_specular.image.colorspace_settings.name = 'Linear'
         node_specular.location = -400,-200
         node_specular.hide = True
         link = links.new(node_specular.outputs[0], group.inputs[6])
    if "Normals" in processedLOADS["Materials"][i]:
        node_normal = nodes.new(type="ShaderNodeTexImage")
        node_normal.image = bpy.data.images.load(processedLOADS["Materials"][i]["Normals"])
        node_normal.image.colorspace_settings.name = 'Linear'
        node_normal.location = -400,-150
        node_normal.hide = True
        link = links.new(node_normal.outputs[0], group.inputs[4])

    if "M" in processedLOADS["Materials"][i]:
        node_M = nodes.new(type="ShaderNodeTexImage")
        node_M.image = bpy.data.images.load(processedLOADS["Materials"][i]["M"])
        node_M.location = -400,-110
        node_M.hide = True
        link = links.new(node_M.outputs[0], group.inputs[1])

    if "Emissive" in processedLOADS["Materials"][i]:
        node_emissive = nodes.new(type="ShaderNodeTexImage")
        node_emissive.image = bpy.data.images.load(processedLOADS["Materials"][i]["Emissive"])
        node_emissive.location = -400,-300
        node_emissive.hide = True
        link = links.new(node_emissive.outputs[0], group.inputs[2])



    link = links.new(group.outputs[0], node_output.inputs[0])

    ob = bpy.context.view_layer.objects.active
    ob.select_set(True)

    if ob.data.materials:
        ob.data.materials[Index] = mat
    else:
        ob.data.materials.append(mat)

    bpy.ops.object.select_all(action='DESELECT')

class Logger():
    def INFO(content):
        print("[{Program}] {Type}: {Content}".format(Program = "FortniteAutoExporter", Type = "INFO", Content = content))
    def ERROR(content):
        print("[{Program}] {Type}: {Content}".format(Program = "FortniteAutoExporter", Type = "ERROR", Content = content))
    def WARN(content):
        print("[{Program}] {Type}: {Content}".format(Program = "FortniteAutoExporter", Type = "WARN", Content = content))

os.chdir(workingDirectory)

f = open("processed.json", "r")
processedJSON = f.read()
processedLOADS = json.loads(processedJSON)
Logger.INFO("Reading processed.json")

characterID = processedLOADS.get("characterName")

i = 0
while i < len(processedLOADS["Meshes"]):

    if not "Earpiece" in processedLOADS["Meshes"][i]:
        io_import_scene_unreal_psa_psk_280.pskimport(processedLOADS["Meshes"][i], bpy.context, bReorientBones = bReorientBones)
    splitAssetPaths = processedLOADS["Meshes"][i].split("\\")
    rawAssetName = splitAssetPaths[len(splitAssetPaths) -1].replace(".psk", "")

    # Moves Imports to new Collection

    for o in bpy.context.scene.objects:
        if o.name == rawAssetName + ".ao":
            objectAO = bpy.data.objects[rawAssetName + ".ao"]
            object_collectionAO = find_collection(bpy.context, objectAO)
            create_collectionAO = make_collection(characterID, object_collectionAO)

            create_collectionAO.objects.link(objectAO)
            object_collectionAO.objects.unlink(objectAO)


    for o in bpy.context.scene.objects:
        if o.name == rawAssetName + ".mo":

            objectMO = bpy.data.objects[rawAssetName + ".mo"]
            object_collectionMO = find_collection(bpy.context, objectMO)
            create_collectionMO = make_collection(characterID, object_collectionMO)

            create_collectionMO.objects.link(objectMO)
            object_collectionMO.objects.unlink(objectMO)

            bpy.data.objects[rawAssetName + ".mo"].select_set(True)
            bpy.ops.object.shade_smooth()
            bpy.ops.object.select_all(action='DESELECT')
    i += 1

# Merging

bpy.ops.object.select_all(action='DESELECT')

for obj in bpy.data.collections[characterID].all_objects:
    obj.select_set(True)

bpy.ops.object.join()
bpy.ops.object.select_all(action='DESELECT')
bpy.ops.object.editmode_toggle()

bpy.ops.armature.select_all(action='DESELECT')
bpy.ops.object.select_pattern(pattern="*.001")
bpy.ops.object.select_pattern(pattern="*.002")
bpy.ops.object.select_pattern(pattern="*.003")
bpy.ops.object.select_pattern(pattern="*.004")
bpy.ops.object.select_pattern(pattern="*.005")
bpy.ops.armature.delete()
bpy.ops.object.editmode_toggle()


# Texturing

i = 0
while i < len(processedLOADS["Meshes"]):
    splitMeshPath = processedLOADS["Meshes"][i].split("\\")
    if not "Earpiece" in processedLOADS["Meshes"][i]:
        pskTOmo = splitMeshPath[len(splitMeshPath) -1].replace(".psk", ".mo")
    bpy.ops.object.select_pattern(pattern=pskTOmo)
    i += 1

bpy.context.view_layer.objects.active = bpy.data.objects[pskTOmo]
bpy.ops.object.join()

ob = bpy.context.view_layer.objects.active
mat_dict = {mat.name: i for i, mat in enumerate(ob.data.materials)}

if textureCharacter == True:
    i = 0
    while i < len(mat_dict):
        splitMeshPath = processedLOADS["Materials"][i]["materialPath"].split("/")
        matName = splitMeshPath[len(splitMeshPath) -1].replace(".psk", "")
        if matName in mat_dict:
            textureSkin(matName, mat_dict[matName])
        i += 1
