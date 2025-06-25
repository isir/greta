/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package greta.auxiliary.DiffSHEG;

import greta.auxiliary.DiffSHEG.BVHFrameToBAPFrame;

import greta.core.animation.common.Skeleton;
import greta.core.intentions.Intention;
import greta.core.intentions.IntentionPerformer;
import greta.core.intentions.FMLTranslator;
import greta.core.signals.Signal;
import greta.core.signals.SignalPerformer;
import greta.core.signals.BMLTranslator;
import greta.core.util.CharacterManager;
import greta.core.util.Mode;
import greta.core.util.enums.CompositionType;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLParser;
import greta.core.util.xml.XMLTree;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.jms.JMSException;
/**
 *
 * @author Leroux Paul
 */
public class DiffSHEG {
    private final String base_bvh_path = "Common\\Data\\DiffSHEG\\data\\GRETA\\Base_greta_fingers_bis.bvh";
    private final String python_env_checker_path = "Common\\Data\\DiffSHEG\\check_env.py";
    private final String batch_env_installer_path = "Common\\Data\\DiffSHEG\\init_env.bat";
    private final String batch_main_path = "Common\\Data\\DiffSHEG\\run_DiffSHEG.bat";
    private final String batch_kill_path = "Common\\Data\\DiffSHEG\\kill_server.bat";
    private Process server_process;

    private Server DiffSHEG_server;
    private InputStream inputStream;
    private String result;

    private Server input_server;
    private String audio;

    private BVHFrameToBAPFrame frameConverter;

    private CharacterManager cm;
    
    /**
     *
     * @throws IOException
     */

    public DiffSHEG (CharacterManager cm) throws IOException {
        System.out.println("greta.auxiliary.DiffSHEG.DiffSHEG()");
        
        input_server = new Server();
        input_server.setAddress("localhost");
        input_server.setPort("6501");


        DiffSHEG_server = new Server();
        DiffSHEG_server.setAddress("localhost");
        DiffSHEG_server.setPort("6500");

        frameConverter = new BVHFrameToBAPFrame();
        try (BufferedReader br = new BufferedReader(new FileReader(base_bvh_path))) {
            Skeleton skeleton = frameConverter.BVHSkeleton(br);
            frameConverter.setSkeleton(skeleton);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ///////////////////////
        // Check environment
        ///////////////////////

        try{
            server_process = new ProcessBuilder("python", python_env_checker_path).redirectErrorStream(true).start();
            // server_process.waitFor();
        } catch (Exception e){
           e.printStackTrace();
        }
        inputStream = server_process.getInputStream();
        result = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n")
                );
        System.out.println(".init_DiffSHEG_server(): DiffSHEG, python env exist: " + result);        
        
        ///////////////////////
        // Create environment if not exit
        ///////////////////////

        if(result.equals("0")){
            System.out.println(".init_DiffSHEG_server(): DiffSHEG, installing python environment...");
            try{
                server_process = new ProcessBuilder(batch_env_installer_path).redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.INHERIT).start();
                server_process.waitFor();
            } catch (Exception e){
                e.printStackTrace();
            }            
        }


    
    }

}
