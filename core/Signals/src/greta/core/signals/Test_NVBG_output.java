package greta.core.signals;
import greta.core.util.log.LogOutput;
import greta.core.util.log.LogPrinter;
import greta.core.util.log.Logs;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Test_NVBG_output {
    
    
        public Test_NVBG_output(){

        }

	public List<String> traitement(String input) throws FileNotFoundException {
		// TODO Auto-generated method stub
		Scanner reader;
		List<String> st = new ArrayList<String>();
		List<String> stNVBG = new ArrayList<String>();
		List<String> stGRETA = new ArrayList<String>();
		List<String> stTYPE = new ArrayList<String>();
                reader = new Scanner(input);
                String line=reader.nextLine();
                while (reader.hasNextLine()) {
                    //System.out.println(line);
                    // read next line
                    st.add(line);
                    line = reader.nextLine();
                }
                reader.close();
		File text = new File(System.getProperty("user.dir")+"\\mapping_file.txt");
                //System.out.println("MAPPING FILE DIRECTORY:"+text.getAbsolutePath());
                reader = new Scanner(text);
                //System.out.println("Line:"+line);
                while (reader.hasNextLine()) {
                    //System.out.println(line);
                    // read next line
                    line = reader.nextLine();
                    if (line==null){
                        break;
                    }
                   // System.out.println("NVBG " + line);
                   if (line.length()!=0 && line.charAt(0)!='/'){
                    String[] as = line.split("::");
                    //System.out.println("Linea:"+line+"  "+line.length());
                    for(String s: as){
                        //System.out.println("Mots:"+s);
                    }
                    stNVBG.add(as[0]);
                   // System.out.println("AS[0]:"+as[0]+"  "+as[1]);
                    String[] as1=as[1].split("=");
                    //System.out.println("AS1[0]:"+as1[0]);
                    stGRETA.add(as1[1]);
                    stTYPE.add(as1[0]);
                    
                       
                   }
                    
                }
                reader.close();
        //System.out.println("LUNGHEZZA VETTORI: "+stTYPE.size()+" "+stGRETA.size()+"  "+stNVBG.size());
	st.remove(st.size()-1);
	String[] st2 ;
	List<String>st3 = new ArrayList<String>();
        List<String>st3_types = new ArrayList<String>();
	for (String s : st) {
		st2=s.split(" ");
		for(int i=0; i<st2.length;i++) {
			//System.out.println(st2[i]);
			if (st2[i].equalsIgnoreCase("<animation")){
				st3.add(s);
			}
                        if(st2[i].equalsIgnoreCase("<!--")){
                            if(st.contains("Animation"))
                            st3_types.add(s);
                        }
                        
		}
	}
	Logs.debug("[NVBG INFO]:Found animations " +  st3);
        Logs.debug("[NVBG INFO]:Found types "+stTYPE);
        //Create another mapping file for types
	//Conversion to gesture of Greta
	List<String>st4 = new ArrayList<String>();
        String item="";
        String tm="";
        int id=1;
	for(String s: st3) {
                //System.out.println("PRINT INFO:"+ s);
		s=s.replace("priority", "importance");
		//s=s.replace("stroke=", "start=");
                String[] sm=s.split("stroke=");
                int m=0;
                int l=0;
                int h=0;
                int p=0;
                for(String kl : sm){
                    if(p==1){
                    //System.out.println("COSA:" +kl+"  "+h);
                    String[] kl2=kl.split(":");
                    for(String kl3: kl2){
                        if(l==1){
                            //System.out.println(kl3+"  "+h);
                            kl3=kl3.replace("\"","");
                            //System.out.println("INFO: greta");
                            String[] kl4=kl3.split(" ");
                            for(String kl5: kl4){
                                if(m==0){
                                item=kl5;
                                //System.out.println("INFO"+item);
                                tm=item;
                                item=item.substring(1);
                                }
                                m+=1;
                        }
                    }
                    l+=1;
                    }
                    h++;
                }
                    p++;
                }
                //System.out.println("item:"+item);
                int end =Integer.parseInt(item)+12;
                end=end/2;
                int item_int=Integer.parseInt(item)/2;
                Logs.debug("[NVBG DEBUG]: "+s+"            "+"\"stroke=\"sp1:"+tm+"\""+"           "+end);
                Logs.debug("[NVBG DEBUG]: "+s.replace("stroke=\"sp1:"+tm+"\"", "start=\""+item_int+"\" end=\""+end+"\""));
                s=s.replace("stroke=\"sp1:"+tm+"\"", "start=\"s1:tm"+item_int+"\" end=\"s1:tm"+end+"\"");
		String[] start = s.split("name=");
		Logs.debug("[NVBG DEBUG]:"+s);
                
		//for(String g: start) {
		//	System.out.println("Composants:"+g);
		//}
		
		//System.out.println("INDEX " + start[1].trim().replace("\"", "").replace("/>","").trim());
		//System.out.println(stNVBG+" "+s);
		//System.out.println("INDEX " + stNVBG.indexOf(start[1].trim().replace("\"", "").replace("/>","").trim()));
		int index = stNVBG.indexOf(start[1].trim().replace("\"", "").replace("/>","").trim());
                if(index<=stNVBG.size() && index >=0){
                    //System.out.println("INDEX OF "+index);
                    //System.out.println("greta");
                    //System.out.println("GRETA "+stGRETA.get(index)+ s.replace(start[1].trim().replace("/>","").trim(), "\""+stGRETA.get(index)+ "\""));
                    s=s.replace("animation", stTYPE.get(index)+" id=\""+id+"\" lexeme="+"\""+stGRETA.get(index)+"\"");
                    id++;
                    s=s.replaceFirst("    ","");
                    //System.out.println("Substitution name="+start[1].replace("/> ",""));
                    s=s.replace("name="+start[1].replace("/> ",""),"");
                    s=s+"/>";
                    String[] mol=s.split("#");
                    s=mol[0];
                    //System.out.println(mol[1]);
                    //s=mol[0]+" "+mol[1].substring(s.indexOf("i"));
                    s=mol[0]+" importance=\"1.0\">";
                    //System.out.println("WHAT? "+s);
                    if (!st4.contains(s)){
                    st4.add(s);
                    }
                }
		
	}
        if(st4.size()==0){
            return null;
        };
        Logs.info("[NVBG INFO]:Found animations " +  st4.get(0));
	
	return st4;
	}
}
	
	

	


