package hmi.flipper2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
import hmi.flipper2.debugger.FlipperDebugger;
import hmi.flipper2.postgres.Database;

public class Main {
    public Main(){
        init();
    }

	private static Logger logger = LoggerFactory.getLogger(Main.class);
	
	//public static void main(String[] args) {
       public void init(){
		try {
			Database db = null;
			// Database db = Database.openDatabaseFromConfig();
			if ( db != null )
				db.clearAll(); // do a complete new start
			TemplateController tc = TemplateController.create("Test", "A test setup", db);
			//TemplateController tc = TemplateController.create("Test", "A test setup", db, new String[] {
			//	"jslibs/underscore-min.js"
			//});
				
			if (Config.debugging ) {
				tc.setDebugger(new FlipperDebugger(tc));
			}
			tc.addTemplateFile( tc.resourcePath("example/Flipper2Count.xml") );
			// tc.addTemplateFile( tc.resourcePath("example/Try.xml") );
			// tc.addTemplateFile( tc.resourcePath("example/ConditionalTemplates.xml") );
			// tc.addTemplateFile( tc.resourcePath("example/Underscore.xml") );
			// tc.addTemplateFile( tc.resourcePath("example/ChoiceExample.xml") );
			// tc.addTemplateFile( tc.resourcePath("example/PersonDbExample.xml") );
			// tc.dataflow.analyze();
			if ( db != null )
				db.commit(); // addTemplatefile does not automatically commit()
			
			int maxcount = 5;
			int count = 0;
			boolean changed = true;
			while( changed && (count < maxcount) ) {
				System.out.println("\nIS:\n---\n"+tc.getIs("is")+"\n");
				changed = tc.checkTemplates();
				count++;
			}
			TemplateController.destroy("Test", db);
		} catch (FlipperException e) {
			FlipperException.handle(e);
		}
		if ( false ) {
		    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		    StatusPrinter.print(lc);			
		}
	}
	
}
