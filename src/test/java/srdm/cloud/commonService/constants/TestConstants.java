package srdm.cloud.commonService.constants;

public class TestConstants {

	/*
	 * IDに全角文字を指定したい場合のテスト用文字列。
	 * テストコードではdriverIdの存在チェック等はOK扱いになるため、
	 * getSupportModelList等、driverIdをパス名にして動作するAPIでは
	 * スペース等の文字があると正しく動作しない(テストコードのみの問題)。
	 * このため、MBStringには特殊な文字を指定しないようにする。
	 */
	private static final String MBString = "ABC123###かなカナ漢字";

	public static class TestDomain {

		public static final String developerDomainId = "D-51a29222-8f56-499e-849c-4e66968c316c";
		public static final String serviceDomainId = "D-13574f53-a938-437e-a252-7cf5ea982c0d";
		public static final String topDomainId = "D-4dddb97a-3272-490f-b0fb-8762eaa69d43";

		public static final String dummyDomainId = MBString;
	}

	public static class TestHomeGroup {

		public static final String homeGroup100100 = "100100";
		public static final String homeGroup0 = "0";
	}

	public static class TestAccount {

		/* Developer domain account */
		public static final String developerAccountId = "A-d30a67bc-317a-4c60-b9b8-82446e839183";

		/* Service domain account */
		public static final String cloudserviceAccountId = "A-8cc64ec5-0848-4444-b435-4c77277798b4";
		public static final String fieldsupportAccountId = "A-0f7ce270-d55c-4999-851b-1531c4b01257";

		/* Top domainaccount */
		public static final String adminAccountId = "A-4bd42a8f-c390-4986-b142-94f71fb5a822";
		public static final String userAccountId = "A-cc885bd9-57c8-4b33-a221-e05f443da56e";
		public static final String srdmserviceAccountId = "A-95eb9606-c35a-4fc0-9f9e-f1184f24cb8b";
		public static final String serviceAccountId = "A-b4263256-4087-488a-96f4-1bd07430da07";

		public static final String adminAccountName = "admin";

	}

	public static class TestRole {

		public static final String developerRoleId = "R-bb5bdf35-0d87-44ea-86e3-4e56336ce398";
		public static final String[] developerRolePermissionList = {
				"cloudService", "group", "system",
				"fileDistribution", "advancedFeatureBasic", "deviceCloning",
				"powerManagement", "service", "agentInstall",
				"fieldSupportSystem", "firmwareUpdate", "domain",
				"account", "accountOfOwnDomain", "browse"};

		public static final String cloudserviceRoleId = "R-c0fd302e-07e8-47cc-a0e7-063921679bf4";
		public static final String[] cloudserviceRolePermissionList = {
				"cloudService", "domain", "account", "browse"};

		public static final String fieldsupportRoleId = "R-c273a7f2-971e-4578-b54e-1aa9b54be489";
		public static final String[] fieldsupportRolePermissionList = {
				"group", "system",
				"fileDistribution", "advancedFeatureBasic", "deviceCloning",
				"powerManagement", "service", "agentInstall",
				"fieldSupportSystem",
				"account", "browse"};

		public static final String adminRoleId = "R-c0c9c026-ba24-4698-8ccb-ff42ed8a26f2";
		public static final String[] adminRolePermissionList = {
				"group", "system",
				"fileDistribution", "advancedFeatureBasic", "deviceCloning",
				"powerManagement",
				"account", "accountOfOwnDomain", "browse"};

		public static final String userRoleId = "R-100c4c12-8f88-4fc5-9d7e-b2335e770c8a";
		public static final String[] userRolePermissionList = {
				"accountOfOwnDomain", "browse"};

		public static final String srdmserviceRoleId = "R-4f2f75cc-2e5b-4c80-a3b6-da7b57c65ee8";
		public static final String[] srdmserviceRolePermissionList = {
				"deviceCloning",
				"service", "agentInstall",
				"fieldSupportSystem", "firmwareUpdate",
				"browse"};

		public static final String serviceRoleId = "R-8f5f999d-3d3c-4f6f-b8d4-c3ace6caba8f";
		public static final String[] serviceRolePermissionList = {
				"deviceCloning",
				"service", "agentInstall",
				"browse"};

	}

	public static class TestIpAddress {

		public static final String testIpAddress = "192.168.1.10";

		public static final String dummyIpAddress = MBString;

	}

	public static class TestMailAddress {
		public static final String dummyMailAddress1 = MBString + "@test.com";
		public static final String dummyMailAddress2 = "test,\\\\ <>[]@test.com";
	}

	public static class TestOpeLog {

		public static final String dummyLogId = MBString;

	}
}
