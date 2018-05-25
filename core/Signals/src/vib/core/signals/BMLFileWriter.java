/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.signals;

import vib.core.util.IniManager;
import vib.core.util.Mode;
import vib.core.util.id.ID;
import java.util.List;

/**
 * This class is an implementation of {@code SignalPerformer} interface.<br/>
 * When {@code performSignals} function is called, the {@code Signals} received
 * are saved in a file in BML format.
 *
 * @author Andre-Marie Pez
 */
public class BMLFileWriter implements SignalPerformer {

    protected String bmlfolder;

    /**
     * Constructs a {@code BMLFileWriter} where the BML files will be saved in
     * the directory defined in the global {@code IniManager} as
     * "BML_FOLDER".<br/> If this parameter is not set (or empty) the files will
     * be saved in the execution path of the program.
     */
    public BMLFileWriter() {
        this(IniManager.getGlobals().getValueString("BML_FOLDER"));
    }

    /**
     * Constructs a {@code BMLFileWriter} where the BML files will be saved in
     * the specified directory.
     *
     * @param directory the directory where the BML files will be saved
     */
    public BMLFileWriter(String directory) {
        //choose the current folder if directory is null or an empty string
        bmlfolder = directory == null || directory.isEmpty() ? "./" : directory;
        //check slashes (linux comtibility)
        bmlfolder = bmlfolder.replaceAll("\\\\", "/");
        //add one at the end if not present
        bmlfolder += bmlfolder.endsWith("/") ? "" : "/";
    }

    /**
     * Saves a list of {@code Signals} in a BML file.<br/> It will be save in
     * the directory defined by the construtor of this {@code BMLFileWriter},
     * and the name of the file will be the {@code requestId} parameter prefixed
     * by "BML-" and suffixed by the XML extention.
     *
     * @param signals the list of {@code Signal}
     * @param requestId the identifier of the request
     * @param mode how the list is added to previous list : blend, replace,
     * append
     */
    @Override
    public void performSignals(List<Signal> signals, ID requestId, Mode mode) {
        BMLTranslator.SignalsToBML(signals, mode).save(bmlfolder + "BML-" + requestId + ".xml");
    }
    
}
