/*
 * This file is part of the auxiliaries of Greta.
 * 
 * Greta is free software: you can redistribute it and / or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Greta.If not, see <http://www.gnu.org/licenses/>.
 */

package vib.auxiliary.player.ogre.natives;

/**
 *
 * @author André-Marie
 */
public class LogManager {

    
//        vib.auxiliary.player.ogre.natives.LogManager.getSingleton().setLogDetail(vib.auxiliary.player.ogre.natives.LoggingLevel.LL_BOREME);
    public static native void set_LL_BOREME();

    public static native void set_LL_LOW();
    
}
