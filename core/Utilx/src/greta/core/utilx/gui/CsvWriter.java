 //Error reading included file Templates/Classes/Templates/Licenses/license-greta.txt
package greta.core.utilx.gui;

/**
 *
 * @author Michele
 */
import java.io.*;

public class CsvWriter {

	final protected String SEPARATEUR = "\n" ;
	final protected String DELIMITEUR = "," ;
	final protected String ENTETE = "Boutton, Temps(sec)";

	protected String csvPath;
	protected File csvFile ;
	protected boolean enterFile ; 

        public CsvWriter() {
                csvPath=null;
                csvFile=null;
                enterFile= true;
        }
	public CsvWriter(String SavePath, String nomParticipant, String nomAgent) {
		// constructeur : ouverture du fichier au format de chemin Windows
		csvPath = SavePath + "\\"+ nomParticipant+nomAgent + ".csv";
		csvFile = new File(csvPath);
		enterFile = true;
	}

	public void sendToCSV(String nomBoutton, String temps) throws IOException {
		// Méthode : enregistrement des valeurs sous forme de CSV
		
		try (BufferedWriter out =  new BufferedWriter(new FileWriter(csvFile, true))) {
			if (enterFile) {
				out.write(ENTETE+SEPARATEUR);
				enterFile = false ; 
			}
			String toWrite = nomBoutton+DELIMITEUR+temps+SEPARATEUR; 
			out.write(toWrite) ;
			System.out.println(toWrite) ;
			out.close();
		} catch (IOException e) {
			System.err.println("Valeur non enregistrée dans le CSV : "+ nomBoutton+" à " + temps);
                        System.err.println(e);
                }	
	}
}
