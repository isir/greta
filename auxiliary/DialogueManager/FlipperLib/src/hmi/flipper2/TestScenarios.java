package hmi.flipper2;

import hmi.flipper2.postgres.Database;

public class TestScenarios {

	public static void main(String[] args) {
		openCheckXCloseDestroy();
	}

	public static void openCheckXCloseDestroy() {
		try {
			String scenario = "TestMultiDB";
			Database db = null;
			//Database db = Database.openDatabaseFromConfig();
			//db.clearAll();; // do a complete new start
			TemplateController tc = TemplateController.create(scenario, "An iterative open/close setup", db);	
			tc.addTemplateFile( tc.resourcePath("example/Flipper2Count.xml") );
			tc.close();
			//
			for(int i=0; i<5; i++) {
				tc = new TemplateController(scenario, db);
				// System.out.println("\nBEFORE IS:\n---\n"+tc.getIs("is")+"\n");
				tc.checkTemplates();
				System.out.println("\nIS:\n---\n"+tc.getIs("is")+"\n");
				tc.close();
			}
			// TemplateController.destroy(scenario, db);
		} catch (FlipperException e) {
			FlipperException.handle(e);
		}
	}
	
}
