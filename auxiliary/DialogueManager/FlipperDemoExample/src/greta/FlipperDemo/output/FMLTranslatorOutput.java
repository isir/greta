/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package greta.FlipperDemo.output;

/**
 *
 * @author admin
 */



public class FMLTranslatorOutput {
    public OutputSender sender = null;
   
    public FMLTranslatorOutput()
    {
        sender = new OutputSender("localhost", "61616" ,  "greta.input.FML12");

    }
    
    public boolean init(){
        System.out.println("Initializing FML translator");
        return true;
        
    }
    
    public void speak(String message){
        
        System.out.println("sending fml msg to greta : "+ message);
        
         String text = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\n";
            text += "<fml-apml>\n";
            text += "<bml>\n";
            text += "<speech id =\"s1\" start=\"0.0\" language=\"english\" voice=\"marytts\" type=\"SAPI4\" text=\"\">\n";
            text += "<description level =\"1\" type=\"gretabml\">\n";
            text += "<reference>tmp/from-fml-apml.pho</reference>\n";
            text += "</description>\n";
           // text += "Om bhoorbhuwah swah tatsawiturwarenyam bhargo devasya dheemahi dhiyo yo nah prachodayaat. My name is Mukesh and I live in Paris.";
            text += "Hello, My name is Camille. I am one of the virtual character of the Greta platform.";	
        //  text += "<pitchaccent id=\"pa2\" type=\"HStar\" level=\"medium\" start=\"s1:tm1\" end=\"s1:tm1+2\" importance=\"1\"/>\n";
          //  text += "<boundary type=\"LL\" id=\"b1\" start=\"s1:tm5\" end=\"s1:tm5+0.5\"/>\n";
            text += " </speech>\n";
            text += " </bml>\n";
            text += "<fml>\n";
            //text += "<emotion id =\"e1\" type=\"joyStrong\" start=\"s1:tm1\" end=\"s1:tm3\" importance=\"1.0\"/>\n";
          //   text += "<performative id =\"p2\" type=\"propose\" start=\"s1:tm3\" end=\"s1:tm5\" importance=\"1.0\"/>\n";
         //  text += "<deictic id = \"d1\" type = \"selftouch\" start = \"s1:tm1\" end = \"s1:tm3\" importance = \"1.0\"/>\n";
        //     text += "<performative id = \"p1\" type = \"greet\" start = \"s1:tm1\" end = \"s1:tm2\" importance = \"1.0\" />\n";
       //       text += "<emotion id = \"e1\" type = \"joyStrong\" start = \"s1:tm2\" end = \"s1:tm3\" importance = \"1.0\" />\n";
  //           text += "<performative id = \"p2\" type = \"propose\" start = \"s1:t3\" end = \"s1:tm5\" importance = \"1.0\" />\n";
            text += "</fml>\n";
       text += "</fml-apml>";
        
        
        
        sender.send(text);
    }
    
}
