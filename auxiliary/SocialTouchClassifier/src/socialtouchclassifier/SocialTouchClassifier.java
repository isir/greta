/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package socialtouchclassifier;

/**
 *
 * @author Michele
 */
import com.illposed.osc.MessageSelector;
import com.illposed.osc.OSCBadDataEvent;
import com.illposed.osc.OSCMessageEvent;
import com.illposed.osc.OSCMessageListener;
import com.illposed.osc.OSCPacketEvent;
import com.illposed.osc.OSCPacketListener;
import com.illposed.osc.messageselector.OSCPatternAddressMessageSelector;
import com.illposed.osc.transport.udp.OSCPortIn;
import greta.core.util.CharacterManager;
import greta.core.util.enums.DistanceType;
import greta.core.util.environment.Environment;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import weka.classifiers.Classifier;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ArffLoader;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.stemmers.LovinsStemmer;
import weka.filters.unsupervised.attribute.StringToWordVector;
public class SocialTouchClassifier {
    
    public CharacterManager cm;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, Exception {
        // TODO code application logic here
        SocialTouchClassifier p= new SocialTouchClassifier(new CharacterManager(new Environment()));
        p.cleanARFF(true);
        p.demo();
    }
    
    
    public SocialTouchClassifier(CharacterManager cm){
        
        this.cm=cm;
        
        //Thread t1 = new Thread(new OscDistanceReceiver(cm));
        //t1.start();
    }
    
    
    public void demo() throws IOException, Exception{
        Classifier rdforest = new RandomForest();  
        DataSource source = new DataSource(System.getProperty("user.dir")+"//..//..//bin//train.arff");//Training corpus file    
        Instances instancesTrain = source.getDataSet(); // Read in training documents      
        //inputFile = new File("F:/java/weka/testData.arff");//Test corpus file  
        //atf.setFile(inputFile);            
        //Instances instancesTest = atf.getDataSet(); // Read in the test file  
        //instancesTest.setClassIndex(0); //Setting the line number of the categorized attribute (No. 0 of the first action), instancesTest.numAttributes() can get the total number of attributes.  
        //double sum = instancesTest.numInstances(),//Examples of test corpus  
        //right = 0.0f; 
        instancesTrain.setClassIndex(instancesTrain.numAttributes()-1);
        StringToWordVector filter = new StringToWordVector();
        filter.setInputFormat(instancesTrain);
        filter.setIDFTransform(true);
        filter.setUseStoplist(true);
        LovinsStemmer stemmer = new LovinsStemmer();
        filter.setStemmer(stemmer);
        filter.setLowerCaseTokens(true);
        //Create the FilteredClassifier object
        FilteredClassifier fc = new FilteredClassifier();
        //specify filter
        fc.setFilter(filter);
        ////specify base classifier
        fc.setClassifier(rdforest);
        fc.buildClassifier(instancesTrain); //train
       
        
        // Preservation model
        SerializationHelper.write("RandomForest.model", fc);//Parameter 1 saves the file for the model, and classifier 4 saves the model.
        
        //for(int  i = 0;i<sum;i++)//Test classification result 1
        //{  
        //    if(m_classifier.classifyInstance(instancesTest.instance(i))==instancesTest.instance(i).classValue())//If the predictive value is equal to the answer value (the correct answer must be provided by the categorized column in the test corpus, then the result will be meaningful)  
        //    {  
        //        right++;//Correct value plus 1  
        //    }  
        //} 
        
        // Get the model saved above
        Classifier classifier8 = (Classifier) weka.core.SerializationHelper.read("RandomForest.model"); 
        double right2 = 0.0f;  
        //for(int  i = 0;i<sum;i++)//Test Classification Result 2 (Pass)
        //{  
        //    if(classifier8.classifyInstance(instancesTest.instance(i))==instancesTest.instance(i).classValue())//If the predictive value is equal to the answer value (the correct answer must be provided by the categorized column in the test corpus, then the result will be meaningful)  
        //    {  
        //        right2++;//Correct value plus 1  
        //    }  
        //} 
        //System.out.println(right);
        //System.out.println(right2);
        //System.out.println(sum);
        //System.out.println("RandomForest classification precision:"+(right/sum));  
    }
    
    
    
    
    public void cleanARFF(boolean train) throws FileNotFoundException, IOException{
     
         boolean flag=false;
         try {
      File myObj = new File(System.getProperty("user.dir")+"//..//..//bin//TouchSequences.arff");
      Scanner myReader = new Scanner(myObj);
      String header="";
      String data="";
      System.out.println("socialtouchclassifier.SocialTouchClassifier.cleanARFF()");
      String filename=System.getProperty("user.dir")+"//..//..//bin//train.arff";
      if(!train){
        filename=System.getProperty("user.dir")+"//..//..//bin//test.arff";
      }
      FileWriter myWriter = new FileWriter(filename);
      while (myReader.hasNextLine()) {
        if(!flag){
         header=myReader.nextLine();
         myWriter.write(header+"\n");
            //System.out.println(header);
        if(header.contains("@DATA")){
            //System.out.println("FLAG");
            flag=true;
        }
        }
        else{
        //System.out.println("FLAG TRUE");
        data=myReader.nextLine();
        data=data.replace("NaN","0");
        String[] rows=data.split(";");
        rows[0]=rows[0].replace(",", ".");
        rows[1]=rows[1].replace(",", ".");
        rows[2]=rows[2].replace(",", ".");
        rows[3]=rows[3].replace(",", ".");
        
        rows[4]=rows[4].replace("'","");
        String [] tactical_cell_1=rows[4].split("#");
        Set<String> tactset = new HashSet<String> ();
        for(int i=0;i<tactical_cell_1.length;i++){
            tactset.add(tactical_cell_1[i]);
        }
        
        rows[5]=rows[5].replace("'","");
        String [] handparts=rows[5].split("#");
        Set<String> handpartsset = new HashSet<String> ();
        for(int i=0;i<handparts.length;i++){
             handpartsset.add(handparts[i]);
        }
        
        String tacticcell="'";
        int i = 0;
        for (String s: tactset) {
            tacticcell += s.trim()+",";
        }
        
        tacticcell=tacticcell.substring(0, tacticcell.length()-1)+"'";
        
       String handpart="'";
        for (String s: handpartsset) {
            handpart += s.trim()+",";
        }
        
        handpart=handpart.substring(0, handpart.length()-1)+"'";
        
            //System.out.println(tacticcell);
            //System.out.println(handpart);
        
        myWriter.write(rows[0]+","+rows[1]+","+rows[2]+","+rows[3]+","+tacticcell+","+handpart+","+rows[6]+"\n");
     
        }
      }
      myReader.close();
      myWriter.close();
    } catch (FileNotFoundException e) {
      System.out.println("An error occurred.");
      e.printStackTrace();
    }
        
         
         
    }
    

    private class OscDistanceReceiver implements Runnable {
 
        public CharacterManager cm;
        
        public OscDistanceReceiver(CharacterManager cm){
            this.cm=cm;
        }
        public void run()
        {
			try {
				SocketAddress socketAddress = new InetSocketAddress("127.0.0.1",9998) ;
                                OSCPortIn receiver = new OSCPortIn(socketAddress);
				//SocketAddress socketAddress1 = receiver.getRemoteAddress();
				MessageSelector messageSelector = new OSCPatternAddressMessageSelector("/unity/distance");
				OSCMessageListener messageListener = new OSCMessageListener() {
					
					@Override
					public void acceptMessage(OSCMessageEvent arg0) {
						// TODO Auto-generated method stub
						System.out.println("message recieved");
                                                System.out.println("[INFO]:"+arg0.getMessage().getArguments());
                                                
                                                System.out.println("RECEIVED MESSAGE");
                                                String obj =arg0.getMessage().getArguments().toString();
                                                String distance = obj.replace("[","").replace("]","");
                                                System.out.println("[INFO_2]:   "+distance.trim().toUpperCase());
                                                cm.setDistance(DistanceType.valueOf(distance.toUpperCase()));
                                                       
                                                    
                                        }             
                                        
				};
				OSCPacketListener listener = new OSCPacketListener() {
					
					@Override
					public void handlePacket(OSCPacketEvent arg0) {
						// TODO Auto-generated method stub
						//System.out.println("[INFO_4]:recieved");
						//System.out.println(arg0.getSource().toString());
						//System.out.println(arg0.getPacket().toString());
					}
					
					@Override
					public void handleBadData(OSCBadDataEvent arg0) {
						// TODO Auto-generated method stub
						
					}
				};

				receiver.getDispatcher().addListener(messageSelector, messageListener);
				receiver.addPacketListener(listener);
				receiver.startListening();
				if (receiver.isListening())
					System.out.println("Server is listening");
				receiver.run();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("error " + e);
			}
		}
	};
        
        
        
    }


