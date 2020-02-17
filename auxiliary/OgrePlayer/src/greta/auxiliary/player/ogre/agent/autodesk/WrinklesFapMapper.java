/*
 * This file is part of the auxiliaries of Greta.
 *
 * Greta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Greta.  If not, see <https://www.gnu.org/licenses/>.
 *
 */
package greta.auxiliary.player.ogre.agent.autodesk;

import greta.auxiliary.player.ogre.Ogre;
import greta.core.animation.mpeg4.fap.FAP;
import greta.core.animation.mpeg4.fap.FAPFrame;
import greta.core.util.log.Logs;
import java.util.List;
import vib.auxiliary.player.ogre.natives.Entity;

/**
 *
 * @author Andre-Marie Pez
 */
public class WrinklesFapMapper extends FapMapper {

    private static int instance_count = 0;
    int instance_num;
    vib.auxiliary.player.ogre.natives.GpuProgramParameters params;
    vib.auxiliary.player.ogre.natives.IntBuffer textureIndex;
    vib.auxiliary.player.ogre.natives.FloatBuffer textureValue;

    float tricky_factor = 0.001f;

    boolean wrinklesAviable = false;

    public WrinklesFapMapper(Entity body, String materialName, int target) {
        if (!vib.auxiliary.player.ogre.natives.MaterialManager.getSingleton().resourceExists(materialName)) {
            Logs.error("Wrinkles not aviable. Material " + materialName + " does not exist.");
            wrinklesAviable = false;
            return;
        }
        instance_num = instance_count++;

        //create a new instance of the material
        String newMaterialName = materialName + "-" + instance_num;

//            greta.auxiliary.player.ogre.natives.ResourcePtr resourceMaterialPTR = new greta.auxiliary.player.ogre.natives.ResourcePtr();
//            Ogre.dontDelete(resourceMaterialPTR);//added to prevent crash
        vib.auxiliary.player.ogre.natives.Material originalMaterialPtr = vib.auxiliary.player.ogre.natives.MaterialManager.getSingleton().getByName(materialName);
        if (!originalMaterialPtr.getTechnique(0).getPass(0).hasFragmentProgram()) {
            Logs.error("Wrinkles not aviable. Material " + materialName + " does not contain any fragment program.");
            wrinklesAviable = false;
            return;
        }

        vib.auxiliary.player.ogre.natives.Material matClonePtr = originalMaterialPtr.clone(newMaterialName, false, "");

        //get a pointer to the parameters of the shader
//            greta.auxiliary.player.ogre.natives.GpuProgramParameters paramsClonePtr = new greta.auxiliary.player.ogre.natives.GpuProgramParametersSharedPtr();
//            Ogre.dontDelete(paramsClonePtr);//added to prevent crash
        params = matClonePtr.getTechnique(0).getPass(0).getFragmentProgramParameters();
//            params = paramsClonePtr;

        //assign the material on the face
        Ogre.setMaterial(body, newMaterialName, target);

        //create variables to pass to the shader
        textureIndex = new vib.auxiliary.player.ogre.natives.IntBuffer(13);
        textureValue = new vib.auxiliary.player.ogre.natives.FloatBuffer(13);
        wrinklesAviable = true;
    }

    @Override
    public void applyFap(FAPFrame fapFrame) {
        if (!wrinklesAviable) {
            return;
        }
        List<FAP> fap = fapFrame.getAnimationParametersList();
        int index = 0;
        if ((fap.get(31).getValue() > 0)
                || (fap.get(32).getValue() > 0)) {
            int tepmax = Math.max(fap.get(31).getValue(), fap.get(32).getValue());
            float value = (tepmax > 450 ? 1 : tepmax / 450.0f);
            textureIndex.setIndex(index, 0);
            textureValue.setIndex(index, value * tricky_factor);
            index++;
            //index0 of AU1
        }

        if (fap.get(33).getValue() > 0) {
            float value = fap.get(33).getValue() > 500 ? 1 : fap.get(33).getValue() / 500.0f;
            textureIndex.setIndex(index, 1);
            textureValue.setIndex(index, value * tricky_factor);
            index++;
            //index1 of AU2 left
        }

        if (fap.get(34).getValue() > 0) {
            float value = fap.get(34).getValue() > 500 ? 1 : fap.get(34).getValue() / 500.0f;
            textureIndex.setIndex(index, 2);
            textureValue.setIndex(index, value * tricky_factor);
            index++;
            //index2 of AU2 right
        }

        if ((fap.get(37).getMask()
                && fap.get(37).getValue() > 0)
                || (fap.get(38).getMask() && fap.get(38).getValue() > 0)) {
            int tepmax = Math.max(fap.get(37).getValue(), fap.get(38).getValue());

            //Original rules
            //float value = tepmax>150 ? 1 : tepmax/150.0f;
            //Enhanced
            float value = tepmax > 50 ? 1 : tepmax / 50.0f;

            textureIndex.setIndex(index, 3);
            textureValue.setIndex(index, value * tricky_factor);
            index++;
            //index3 of AU4
        }

        if (fap.get(41).getValue() > 0) {
            float value = fap.get(41).getValue() > 400 ? 1 : fap.get(41).getValue() / 400.0f;
            textureIndex.setIndex(index, 4);
            textureValue.setIndex(index, value * tricky_factor);
            index++;
            //index4 of AU6 left
        }

        if (fap.get(42).getValue() > 0) {
            float value = fap.get(42).getValue() > 400 ? 1 : fap.get(42).getValue() / 400.0f;
            textureIndex.setIndex(index, 5);
            textureValue.setIndex(index, value * tricky_factor);
            index++;
            //index5 of AU6 right
        }

        if ((fap.get(55).getValue() < -90)
                || (fap.get(51).getValue() < -90)) {
            int fap_value = Math.min(fap.get(55).getValue(), fap.get(51).getValue());
            float value = (fap_value + 90.0) < -210.0 ? 1 : (fap_value + 90.0f) / (-210.0f);
            textureIndex.setIndex(index, 6);
            textureValue.setIndex(index, value * tricky_factor);
            index++;
            //index6 of AU10 left
        }

        if ((fap.get(56).getValue() < -90)
                || (fap.get(51).getValue() < -90)) {
            int fap_value = Math.min(fap.get(56).getValue(), fap.get(51).getValue());
            float value = (fap_value + 90.0) < -210.0 ? 1 : (fap_value + 90.0f) / (-210.0f);
            textureIndex.setIndex(index, 7);
            textureValue.setIndex(index, value * tricky_factor);
            index++;
            //index7 of AU10 right
        }

        if ((fap.get(53).getValue() > 0)
                && (fap.get(59).getValue() > 0)) {
            float val53 = fap.get(53).getValue() > 200 ? 1 : fap.get(53).getValue() / 200.0f;
            float val59 = fap.get(59).getValue() > 150 ? 1 : fap.get(59).getValue() / 150.0f;
            float value = Math.min(val59, val53);
            textureIndex.setIndex(index, 8);
            textureValue.setIndex(index, value * tricky_factor);
            index++;
            //index8 of AU12 left
        }

        if ((fap.get(54).getValue() > 0)
                && (fap.get(60).getValue() > 0)) {
            float val54 = fap.get(54).getValue() > 200 ? 1 : fap.get(54).getValue() / 200.0f;
            float val60 = fap.get(60).getValue() > 150 ? 1 : fap.get(60).getValue() / 150.0f;
            float value = Math.min(val60, val54);
            textureIndex.setIndex(index, 9);
            textureValue.setIndex(index, value * tricky_factor);
            index++;
            //index9 of AU12 right
        }

        if ((fap.get(53).getValue() > 0)
                && (fap.get(59).getValue() <= 0)) {
            float value = fap.get(53).getValue() > 100 ? 1 : fap.get(53).getValue() / 100.0f;
            textureIndex.setIndex(index, 10);
            textureValue.setIndex(index, value * tricky_factor);
            index++;
            //index10 of AU14left
        }

        if ((fap.get(54).getValue() > 0)
                && (fap.get(60).getValue() <= 0)) {
            float value = fap.get(54).getValue() > 100 ? 1 : fap.get(54).getValue() / 100.0f;
            textureIndex.setIndex(index, 11);
            textureValue.setIndex(index, value * tricky_factor);
            index++;
            //index11 of AU14right
        }

        if ((fap.get(51).getValue() > 50)
                && (fap.get(52).getValue() > 50)) {
            float value = fap.get(52).getValue() > 200 ? 1 : fap.get(52).getValue() / 200.0f;
            textureIndex.setIndex(index, 12);
            textureValue.setIndex(index, value * tricky_factor);
            index++;
            //index12 of AU24
        }
        if (index > 0) {
            params.setNamedConstant("textureIndex", textureIndex, index, 1);
            params.setNamedConstant("textureValue", textureValue, index, 1);
        }
        params.setNamedConstant("nbTextureApplied", index);
    }

}
