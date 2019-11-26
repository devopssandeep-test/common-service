package srdm.cloud.commonService.testNoDB;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({

	AccountTestNoDB.class,
	DomainTestNoDB.class,
	RoleTestNoDB.class,
	GroupTestNoDB.class,
	LogTestNoDB.class,
	SystemTestNoDB.class,

	AccountErrorTestNoDB.class,
	DomainErrorTestNoDB.class,
	RoleErrorTestNoDB.class,
	GroupErrorTestNoDB.class,
	LogErrorTestNoDB.class,
	ExportErrorTestNoDB.class,
	MaintenanceErrorTestNoDB.class,
	SystemErrorTestNoDB.class

})
public class AllTestsNoDB {
}