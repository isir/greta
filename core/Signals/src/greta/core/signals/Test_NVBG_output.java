package greta.core.signals;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Test_NVBG_output {
    
        String out;
    
        public Test_NVBG_output(){
            out="";
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
                System.out.println("MAPPING FILE DIRECTORY:"+text.getAbsolutePath());
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
                    System.out.println("Linea:"+line+"  "+line.length());
                    for(String s: as){
                        System.out.println("Mots:"+s);
                    }
                    stNVBG.add(as[0]);
                    System.out.println("AS[0]:"+as[0]+"  "+as[1]);
                    String[] as1=as[1].split("=");
                    System.out.println("AS1[0]:"+as1[0]);
                    stGRETA.add(as1[1]);
                    stTYPE.add(as1[0]);
                    
                       
                   }
                    
                }
                reader.close();
        System.out.println("LUNGHEZZA VETTORI: "+stTYPE.size()+" "+stGRETA.size()+"  "+stNVBG.size());
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
	System.out.println("Found animations" +  st3);
        System.out.println("Found types"+stTYPE);
        //Create another mapping file for types
	//Conversion to gesture of Greta
	List<String>st4 = new ArrayList<String>();
        String item="";
        String tm="";
	for(String s: st3) {
		s=s.replace("priority", "importance");
		//s=s.replace("stroke=", "start=");
                String[] sm=s.split("stroke=");
                int m=0;
                int l=0;
                int h=0;
                int p=0;
                for(String kl : sm){
                    if(p==1){
                    System.out.println("COSA:" +kl+"  "+h);
                    String[] kl2=kl.split(":");
                    for(String kl3: kl2){
                        if(l==1){
                            System.out.println(kl3+"  "+h);
                            kl3=kl3.replace("\"","");
                            System.out.println("greta");
                            String[] kl4=kl3.split(" ");
                            for(String kl5: kl4){
                                if(m==0){
                                item=kl5;
                                System.out.println("OOOOOOOOOOOOOOOOOO "+item);
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
                System.out.println("item:"+item);
                item=item.substring(0,1);
                int end =Integer.parseInt(item)+12;
                System.out.println("\"stroke=\"sp1:"+tm+"\"");
                System.out.println(s.replace("stroke=\"sp1:"+tm+"\"", "start=\""+item+"\" end=\""+end+"\""));
                s=s.replace("stroke=\"sp1:"+tm+"\"", "start=\"s1:tm"+item+"\" end=\"s1:tm"+end+"\"");
		String[] start = s.split("name=");
		System.out.println("DONE1:: "+s);
                
		//for(String g: start) {
		//	System.out.println("Composants:"+g);
		//}
		
		//System.out.println("INDEX " + start[1].trim().replace("\"", "").replace("/>","").trim());
		//System.out.println(stNVBG+" "+s);
		//System.out.println("INDEX " + stNVBG.indexOf(start[1].trim().replace("\"", "").replace("/>","").trim()));
		int index = stNVBG.indexOf(start[1].trim().replace("\"", "").replace("/>","").trim());
                if(index<=stNVBG.size() && index >=0){
                    System.out.println("INDEX OF "+index);
                    //System.out.println("greta");
                    //System.out.println("GRETA "+stGRETA.get(index)+ s.replace(start[1].trim().replace("/>","").trim(), "\""+stGRETA.get(index)+ "\""));
                    s=s.replace("animation", stTYPE.get(index)+" id=\"s01\" lexeme="+"\""+stGRETA.get(index)+"\"");
                    s=s.replaceFirst("    ","");
                    System.out.println("Substitution name="+start[1].replace("/> ",""));
                    s=s.replace("name="+start[1].replace("/> ",""),"");
                    s=s+"/>";
                    String[] mol=s.split("#");
                    s=mol[0];
                    //System.out.println("AAAAAAAAAAAAAAAAAAAA"+mol[0]);
                    //System.out.println(mol[1]);
                    //s=mol[0]+" "+mol[1].substring(s.indexOf("i"));
                    s=mol[0]+" importance=\"1.0\">";
                    System.out.println("WHAT? "+s);
                    if (!st4.contains(s)){
                    st4.add(s);
                    }
                }
		
	}
        if(st4.size()==0){
            return null;
        }
	System.out.println("Found animations " +  st4.get(0));
	
	return st4;
	}
}
	
	

	

