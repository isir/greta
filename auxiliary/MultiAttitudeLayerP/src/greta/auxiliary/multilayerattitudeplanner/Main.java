package greta.auxiliary.multilayerattitudeplanner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import greta.auxiliary.socialparameters.SocialDimension;
import greta.auxiliary.socialparameters.SocialParameterFrame;
import greta.auxiliary.tts.cereproc.CereProcTTS;
import greta.core.intentions.FMLFileReader;
import greta.core.signals.BMLFileWriter;
import greta.core.util.CharacterManager;
import greta.core.util.id.IDProvider;
import greta.core.util.speech.Speech;
import greta.core.util.speech.TTS;

/**
 *
 * @author Mathieu
 */


public class Main {


    public static void main(String[] args, CharacterManager cm)
    {
        try {

            TTS tts = new CereProcTTS(cm);
            Speech speech = new Speech(cm);
            speech.setTTS(tts);
            SequencePlanner sp = new SequencePlanner(cm);

            List<SocialParameterFrame> listspf = new ArrayList<SocialParameterFrame>();
            SocialParameterFrame socialFrame = new SocialParameterFrame();
            socialFrame.applyValue(SocialDimension.Liking, 0);
            socialFrame.applyValue(SocialDimension.Dominance, 0);
            socialFrame.applyValue(SocialDimension.Familiarity, 0);

            /*ValuedAttitudeVariation attitudeVarNULLFRD = new ValuedAttitudeVariation(AttitudeDimension.Friendliness, 0.0, AttitudeCluster.Null);
            ValuedAttitudeVariation attitudeVarNULLDOM = new ValuedAttitudeVariation(AttitudeDimension.Dominance, 0.0, AttitudeCluster.Null);

            ValuedAttitudeVariation attitudeVarDomBigDecr =  new ValuedAttitudeVariation(AttitudeDimension.Dominance, -0.5,AttitudeCluster.BigDecr);
            ValuedAttitudeVariation attitudeVarDomSmallDecr =  new ValuedAttitudeVariation(AttitudeDimension.Dominance, -0.1,AttitudeCluster.SmallDecr);
            ValuedAttitudeVariation attitudeVarDomSmallIncr =  new ValuedAttitudeVariation(AttitudeDimension.Dominance, 0.1,AttitudeCluster.SmallIncr);
            ValuedAttitudeVariation attitudeVarDomBigIncr =  new ValuedAttitudeVariation(AttitudeDimension.Dominance, 0.5,AttitudeCluster.BigIncr);
            ValuedAttitudeVariation attitudeVarFrdBigDecr =  new ValuedAttitudeVariation(AttitudeDimension.Friendliness, -0.5,AttitudeCluster.BigDecr);
            ValuedAttitudeVariation attitudeVarFrdSmallDecr =  new ValuedAttitudeVariation(AttitudeDimension.Friendliness, -0.1,AttitudeCluster.SmallDecr);
            ValuedAttitudeVariation attitudeVarFrdSmallIncr =  new ValuedAttitudeVariation(AttitudeDimension.Friendliness, 0.1,AttitudeCluster.SmallIncr);
            ValuedAttitudeVariation attitudeVarFrdBigIncr =  new ValuedAttitudeVariation(AttitudeDimension.Friendliness, 0.5,AttitudeCluster.BigIncr);*/

            File[] listFiles;
            FMLFileReader fmlreader = new FMLFileReader(cm);
            fmlreader.addIntentionPerformer(sp);
            File dir = new File("./Common/Data/MultiLayerAttitude/SelectedFML/");
            listFiles = dir.listFiles();

            for(File fmlfile : listFiles){
                //DomBigDecr
                File dombigdecr = new File("./Common/Data/MultiLayerAttitude/BML/"+fmlfile.getName().substring(0,fmlfile.getName().length()-4)
                        +"DomBigDecr/");
                if(!dombigdecr.exists())
                {
                    dombigdecr.mkdir();
                }
                for(File f : dombigdecr.listFiles())
                {
                    f.delete();
                }
                BMLFileWriter bmlwriter = new BMLFileWriter(dombigdecr.getAbsolutePath());
                sp.addSignalPerformer(bmlwriter);
                socialFrame.setDoubleValue(SocialDimension.Dominance, -0.5);
                listspf.add(socialFrame);
                sp.performSocialParameter(listspf, IDProvider.createID("mainMultilayer"));
                //sp.performAttitude(attitudeVarNULLFRD);
                //sp.performAttitude(attitudeVarDomBigDecr);
                fmlreader.load(fmlfile.getAbsolutePath());
                sp.removeSignalPerformer(bmlwriter);

                //DomSmallDecr
                File domsmalldecr = new File("./Common/Data/MultiLayerAttitude/BML/"+fmlfile.getName().substring(0,fmlfile.getName().length()-4)
                        +"DomSmallDecr/");
                if(!domsmalldecr.exists())
                {
                    domsmalldecr.mkdir();
                }
                for(File f : domsmalldecr.listFiles())
                {
                    f.delete();
                }
                bmlwriter = new BMLFileWriter(domsmalldecr.getAbsolutePath());
                sp.addSignalPerformer(bmlwriter);
                listspf.clear();
                socialFrame.setDoubleValue(SocialDimension.Dominance, -0.1);
                listspf.add(socialFrame);
                sp.performSocialParameter(listspf, IDProvider.createID("mainMultilayer"));
                //sp.performAttitude(attitudeVarNULLFRD);
                //sp.performAttitude(attitudeVarDomSmallDecr);
                fmlreader.load(fmlfile.getAbsolutePath());
                sp.removeSignalPerformer(bmlwriter);
                //DomSmallIncr
                File domsmallincr = new File("./Common/Data/MultiLayerAttitude/BML/"+fmlfile.getName().substring(0,fmlfile.getName().length()-4)
                        +"DomSmallIncr/");
                if(!domsmallincr.exists())
                {
                    domsmallincr.mkdir();
                }
                for(File f : domsmallincr.listFiles())
                {
                    f.delete();
                }
                bmlwriter = new BMLFileWriter(domsmallincr.getAbsolutePath());
                sp.addSignalPerformer(bmlwriter);
                listspf.clear();
                socialFrame.setDoubleValue(SocialDimension.Dominance, 0.1);
                listspf.add(socialFrame);
                sp.performSocialParameter(listspf, IDProvider.createID("mainMultilayer"));
                //sp.performAttitude(attitudeVarNULLFRD);
                //sp.performAttitude(attitudeVarDomSmallIncr);
                fmlreader.load(fmlfile.getAbsolutePath());
                sp.removeSignalPerformer(bmlwriter);
                //DomBigIncr
                File dombigincr = new File("./Common/Data/MultiLayerAttitude/BML/"+fmlfile.getName().substring(0,fmlfile.getName().length()-4)
                        +"DomBigIncr/");
                if(!dombigincr.exists())
                {
                    dombigincr.mkdir();
                }
                for(File f : dombigincr.listFiles())
                {
                    f.delete();
                }
                bmlwriter = new BMLFileWriter(dombigincr.getAbsolutePath());
                sp.addSignalPerformer(bmlwriter);
                listspf.clear();
                socialFrame.setDoubleValue(SocialDimension.Dominance, 0.5);
                listspf.add(socialFrame);
                sp.performSocialParameter(listspf, IDProvider.createID("mainMultilayer"));
                //sp.performAttitude(attitudeVarNULLFRD);
                //sp.performAttitude(attitudeVarDomBigIncr);
                fmlreader.load(fmlfile.getAbsolutePath());
                sp.removeSignalPerformer(bmlwriter);

                //FrdBigDecr
                File frdbigdecr = new File("./Common/Data/MultiLayerAttitude/BML/"+fmlfile.getName().substring(0,fmlfile.getName().length()-4)
                        +"FrdBigDecr/");
                if(!frdbigdecr.exists())
                {
                    frdbigdecr.mkdir();
                }
                for(File f : frdbigdecr.listFiles())
                {
                    f.delete();
                }
                bmlwriter = new BMLFileWriter(frdbigdecr.getAbsolutePath());
                sp.addSignalPerformer(bmlwriter);
                listspf.clear();
                socialFrame.setDoubleValue(SocialDimension.Liking, -0.5);
                listspf.add(socialFrame);
                sp.performSocialParameter(listspf, IDProvider.createID("mainMultilayer"));
                //sp.performAttitude(attitudeVarNULLDOM);
                //sp.performAttitude(attitudeVarFrdBigDecr);
                fmlreader.load(fmlfile.getAbsolutePath());
                sp.removeSignalPerformer(bmlwriter);

                //FrdSmallDecr
                File frdsmalldecr = new File("./Common/Data/MultiLayerAttitude/BML/"+fmlfile.getName().substring(0,fmlfile.getName().length()-4)
                        +"FrdSmallDecr/");
                if(!frdsmalldecr.exists())
                {
                    frdsmalldecr.mkdir();
                }
                for(File f : frdsmalldecr.listFiles())
                {
                    f.delete();
                }
                bmlwriter = new BMLFileWriter(frdsmalldecr.getAbsolutePath());
                sp.addSignalPerformer(bmlwriter);
                listspf.clear();
                socialFrame.setDoubleValue(SocialDimension.Liking, -0.1);
                listspf.add(socialFrame);
                sp.performSocialParameter(listspf, IDProvider.createID("mainMultilayer"));
                //sp.performAttitude(attitudeVarNULLDOM);
                //sp.performAttitude(attitudeVarFrdSmallDecr);
                fmlreader.load(fmlfile.getAbsolutePath());
                sp.removeSignalPerformer(bmlwriter);

                //FrdSmallIncr
                File frdsmallincr = new File("./Common/Data/MultiLayerAttitude/BML/"+fmlfile.getName().substring(0,fmlfile.getName().length()-4)
                        +"FrdSmallIncr/");
                if(!frdsmallincr.exists())
                {
                    frdsmallincr.mkdir();
                }
                for(File f : frdsmallincr.listFiles())
                {
                    f.delete();
                }
                bmlwriter = new BMLFileWriter(frdsmallincr.getAbsolutePath());
                sp.addSignalPerformer(bmlwriter);
                listspf.clear();
                socialFrame.setDoubleValue(SocialDimension.Liking, 0.1);
                listspf.add(socialFrame);
                sp.performSocialParameter(listspf, IDProvider.createID("mainMultilayer"));
                //sp.performAttitude(attitudeVarNULLDOM);
                //sp.performAttitude(attitudeVarFrdSmallIncr);
                fmlreader.load(fmlfile.getAbsolutePath());
                sp.removeSignalPerformer(bmlwriter);

                //FrdBigIncr
                File frdbigincr = new File("./Common/Data/MultiLayerAttitude/BML/"+fmlfile.getName().substring(0,fmlfile.getName().length()-4)
                        +"FrdBigIncr/");
                if(!frdbigincr.exists())
                {
                    frdbigincr.mkdir();
                }
                for(File f : frdbigincr.listFiles())
                {
                    f.delete();
                }
                bmlwriter = new BMLFileWriter(frdbigincr.getAbsolutePath());
                sp.addSignalPerformer(bmlwriter);
                listspf.clear();
                socialFrame.setDoubleValue(SocialDimension.Liking, 0.5);
                listspf.add(socialFrame);
                sp.performSocialParameter(listspf, IDProvider.createID("mainMultilayer"));
                //sp.performAttitude(attitudeVarNULLDOM);
                //sp.performAttitude(attitudeVarFrdBigIncr);
                fmlreader.load(fmlfile.getAbsolutePath());
                sp.removeSignalPerformer(bmlwriter);

            }

            //BayesNet bnfrd;
           // BayesNet bndom;
           // try {
            //    bnfrd = new BayesNet(frd,false);
            //System.out.println(bnfrd.getProbability("HeadDownL",1,"SmallIncr","HeadAt"));

                //System.out.println(bnfrd.getProbability("HeadDownL",2,"BigDecr","HeadAt"));
                //System.out.println(bnfrd.getProbability("HeadDownL",2,"BigIncr","HeadAt"));
                //System.out.println(bnfrd.getProbability("No",7,"SmallDecr","HeadDownL"));

                //for(String s : bnfrd.signalenum.get("Signal7"))
                //{
                //    for(String s2 : bnfrd.signalenum.get("Signal6"))
                //File attDir = new File(ComputedProbability.PROBA_DIR);
                //        System.out.println("prev "+s2+" this "+s+bnfrd.getProbability(s,7,"SmallDecr",s2));
                //        System.out.println("###");
                //    }
                //}
                //System.out.println(bnfrd.getProbability("HeadDownL",1,"SmallIncr","HeadAt"));

                //String[] s3 = {"HeadAt","BodyReclineL","HeadAt"};
                //System.out.println(bndom.getProbabilitySequence(3,"BigIncr",s3));

                //SequenceProbabilityTuple bs =bndom.getBestSequence(4,"SmallIncr");

                //String[] attitudes = {"BigIncr","SmallIncr","SmallDecr","BigDecr"};

                //File attDir = new File(ComputedProbability.PROBA_DIR);


                //List<NVBEventType> smaller = new ArrayList<NVBEventType>();
                //List<NVBEventType> bigger = new ArrayList<NVBEventType>();
                //smaller.add(NVBEventType.HeadDown);
                //smaller.add(NVBEventType.GestComm);
                //smaller.add(NVBEventType.BodyLean);
                //smaller.add(NVBEventType.Smile);
                //bigger.add(NVBEventType.Any);
                //bigger.add(NVBEventType.GestComm);
                //bigger.add(NVBEventType.Any);
                //bigger.add(NVBEventType.Any);
                //NVBEventType.isSubsequence(smaller, bigger);

                //createAttDir(attitudes);
                //bndom.computeChildrenAtDepth(1, 6, "BigIncr", new ArrayList<String>());
                //bndom.computeAllProbasForAttitudeArray(attitudes,5);
                //List<NVBEventType> lst = new ArrayList<NVBEventType>();
                //lst.add(NVBEventType.GestComm);
                //lst.add(NVBEventType.HeadNod);
                //List<SequenceProbabilityTuple> lstspt = new ArrayList<SequenceProbabilityTuple>();
                //lstspt=bnfrd.getOrderedTuplesContainingSignals(4,new AttitudeVariation(AttitudeDimension.Friendliness, AttitudeCluster.BigIncr),
                //        lst,null);
                //System.out.println("t");
                //bndom.computeAllSequenceProbabilities(5, attitudes);
                //bndom.seqProbabilitiesMap.put("BigIncr",bndom.order(bndom.seqProbabilitiesMap.get("BigIncr")));

                //SequencePlanner sp = new SequencePlanner();
                //sp.loadSequences(sp.SEQUENCES_FILE);

                //List<FrequentSequence> matchingsequences = new ArrayList<FrequentSequence>();
                /*for(SequenceProbabilityTuple spt : bndom.seqProbabilitiesMap.get("BigDecr"))
                {
                    FrequentSequence fs =sp.findMatchingFrequentSequence(spt.signals, AttitudeCluster.BigIncr);
                    if(fs!=null)
                        matchingsequences.add(fs);
                }*/

                /*for(NVBEventType s : bs.signals)
                    System.out.print(s.toString()+",");
                /*System.out.println(bndom.getBestSequence(1,"SmallIncr")[0]);
                System.out.println(bndom.getBestSequence(1,"SmallDecr")[0]);
                System.out.println(bndom.getBestSequence(1,"BigDecr")[0]);*/

        } catch (Exception ex) {
            System.err.println(ex);
        }
    }
}
