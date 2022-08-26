
print "loading data/ogre/init-ogre-basiclevel-brad.py"


scene.setScale(1)
scene.command("path audio ../../data/ogre")
scene.addAssetPath("ME", "../../data/ogre/common-sk")
scene.addAssetPath("ME", "../../data/ogre/ChrBrad")
scene.loadAssets()


scene.run("zebra2-map.py")
scene.command("skeletonmap ChrBrad.sk zebra2")
scene.command("motionmapdir ../../data/ogre/ChrBrad zebra2")


chrBradFace = scene.createFaceDefinition("ChrBradFace")
chrBradFace.setFaceNeutral("ChrBrad@face_neutral")

chrBradFace.setViseme("open",    "ChrBrad@open")
chrBradFace.setViseme("W",       "ChrBrad@W")
chrBradFace.setViseme("ShCh",    "ChrBrad@ShCh")
chrBradFace.setViseme("PBM",     "ChrBrad@PBM")
chrBradFace.setViseme("FV",      "ChrBrad@FV")
chrBradFace.setViseme("wide",    "ChrBrad@wide")
chrBradFace.setViseme("tBack",   "ChrBrad@tBack")
chrBradFace.setViseme("tRoof",   "ChrBrad@tRoof")
chrBradFace.setViseme("tTeeth",  "ChrBrad@tTeeth")

#chrBradFace.setAU(1,  "both",    "ChrBrad@001_inner_brow_raiser")
chrBradFace.setAU(1,  "left",    "ChrBrad@001_inner_brow_raiser_lf")
chrBradFace.setAU(1,  "right",   "ChrBrad@001_inner_brow_raiser_rt")
#chrBradFace.setAU(2,  "both",    "ChrBrad@002_outer_brow_raiser")
chrBradFace.setAU(2,  "left",    "ChrBrad@002_outer_brow_raiser_lf")
chrBradFace.setAU(2,  "right",   "ChrBrad@002_outer_brow_raiser_rt")
#chrBradFace.setAU(4,  "both",    "ChrBrad@004_brow_lowerer")
chrBradFace.setAU(4,  "left",    "ChrBrad@004_brow_lowerer_lf")
chrBradFace.setAU(4,  "right",   "ChrBrad@004_brow_lowerer_rt")
chrBradFace.setAU(5,  "both",    "ChrBrad@005_upper_lid_raiser")
chrBradFace.setAU(6,  "both",    "ChrBrad@006_cheek_raiser")
chrBradFace.setAU(7,  "both",    "ChrBrad@007_lid_tightener")
#chrBradFace.setAU(9,  "both",    "ChrBrad@009_nose_wrinkle")
chrBradFace.setAU(10, "both",    "ChrBrad@010_upper_lip_raiser")
#chrBradFace.setAU(12, "both",    "ChrBrad@012_lip_corner_puller")
chrBradFace.setAU(12, "left",    "ChrBrad@012_lip_corner_puller_lf")
chrBradFace.setAU(12, "right",   "ChrBrad@012_lip_corner_puller_rt")
#chrBradFace.setAU(15, "both",    "ChrBrad@015_lip_corner_depressor")
#chrBradFace.setAU(23, "both",    "ChrBrad@023_lip_tightener")
chrBradFace.setAU(25, "both",    "ChrBrad@025_lips_part")
chrBradFace.setAU(26, "both",    "ChrBrad@026_jaw_drop")
#chrBradFace.setAU(27, "both",    "ChrBrad@027_mouth_stretch")
#chrBradFace.setAU(38, "both",    "ChrBrad@038_nostril_dilator")
#chrBradFace.setAU(45, "both",    "ChrBrad@blink")
chrBradFace.setAU(45, "left",    "ChrBrad@045_blink_lf")
chrBradFace.setAU(45, "right",   "ChrBrad@045_blink_rt")



scene.setBoolAttribute("internalAudio", True)


brad = scene.createCharacter("Brad", "")
bradSkeleton = scene.createSkeleton("ChrBrad.sk")
bradSkeleton.rescale(100)

motions = scene.getMotionNames()

for motion in motions:
    scene.getMotion(motion).scale(100)

brad.setSkeleton(bradSkeleton)
brad.setFaceDefinition(chrBradFace)
bradPos = SrVec(0, 0, 0)
brad.setPosition(bradPos)
bradHPR = SrVec(0, 0, 0)
brad.setHPR(bradHPR)
brad.createStandardControllers()
brad.setStringAttribute("deformableMesh", "Brad")

### Use pre-recorded speech, using the \sounds folder.
### Specify TTS as a backup if the prerecorded sound file is not found
brad.setVoice("audiofile")
brad.setVoiceCode("Sounds")
brad.setVoiceBackup("remote")
brad.setVoiceBackupCode("Festival_voice_rab_diphone")
brad.setUseVisemeCurves(True)

### set an idle posture, gazing at the camera
bml.execBML('Brad', '<body posture="ChrBrad@Idle01"/>')
bml.execBML('Brad', '<gaze target="Camera" sbm:joint-range="NECK EYES"/>')
