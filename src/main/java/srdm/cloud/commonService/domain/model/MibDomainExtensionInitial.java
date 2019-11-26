package srdm.cloud.commonService.domain.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonRootName;

import lombok.Data;
import srdm.common.constant.SrdmConstants;

@Data
@JsonRootName("domainExtension")
@JsonIgnoreProperties(ignoreUnknown = true)
public class MibDomainExtensionInitial implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 5363435984922737427L;

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	protected static class StatusCondition implements Serializable {
		/**
		 *
		 */
		private static final long serialVersionUID = 6942857850232244548L;

		@Data
		@JsonIgnoreProperties(ignoreUnknown = true)
		protected static class RemainingAmountSet implements Serializable {

			/**
			 *
			 */
			private static final long serialVersionUID = -7060081636523768076L;

			@Data
			@JsonIgnoreProperties(ignoreUnknown = true)
			protected static class RemainingAmount implements Serializable {
				/**
				 *
				 */
				private static final long serialVersionUID = -217915505847319506L;

				private static int level1 = 0;

				private static int level2 = 25;

				private static int level3 = 50;

				private static int level4 = 75;

				private static int level5 = 100;
			}

			private RemainingAmount paper = new RemainingAmount();

			private RemainingAmount toner = new RemainingAmount();
		}

		@Data
		@JsonIgnoreProperties(ignoreUnknown = true)
		protected static class ConditionSet implements Serializable {
			/**
			 *
			 */
			private static final long serialVersionUID = 5966593705862060019L;

			@Data
			@JsonIgnoreProperties(ignoreUnknown = true)
			protected static class Condition implements Serializable {
				/**
				 *
				 */
				private static final long serialVersionUID = 3905579846526601826L;

				private String dispErrorLevel;

				private String priority;

				protected Condition(String dispErrorLevel, String priority) {
					this.dispErrorLevel = dispErrorLevel;
					this.priority = priority;
				}
			}

			Condition unknown = new Condition("error", "27");

			Condition printerError = new Condition("error", "1");

			Condition accountLimit = new Condition("error", "2");

			Condition overduePreventMaintenance = new Condition("error", "3");

			Condition paperJam = new Condition("error", "4");

			Condition markerSupplyMissing = new Condition("error", "5");

			Condition tonerEmpty = new Condition("error", "6");

			Condition coverOpen = new Condition("error", "7");

			Condition paperEmpty = new Condition("error", "8");

			Condition specifiedInputTrayEmpty = new Condition("error", "9");

			Condition specifiedInputTrayMissing = new Condition("error", "10");

			Condition allOutputTrayFull = new Condition("error", "11");

			Condition specifiedOutputTrayMissing = new Condition("error", "12");

			Condition offline = new Condition("error", "13");

			Condition printerWarning = new Condition("warning", "14");

			Condition tonerLow = new Condition("warning", "15");

			Condition paperLow = new Condition("warning", "16");

			Condition inputTrayMissing = new Condition("warning", "17");

			Condition outputTrayFull = new Condition("warning", "18");

			Condition outputTrayNearFull = new Condition("warning", "19");

			Condition outputTrayMissing = new Condition("warning", "20");

			Condition stackerNotInstalled = new Condition("warning", "21");

			Condition nearOverduePreventMaintenance = new Condition("warning", "22");

			Condition standby = new Condition("normal", "23");

			Condition warmUp = new Condition("normal", "24");

			Condition printing = new Condition("normal", "25");

			Condition online = new Condition("normal", "26");
		}

		private ConditionSet conditionSet = new ConditionSet();

		private RemainingAmountSet remainingAmountSet = new RemainingAmountSet();

	}

	private String domainId;

	private String groupId;

	private String theme = SrdmConstants.DEFAULT_DOMAIN_MIB_THEME;

//	private String filterList = ""; // 実際のデータは、リストになるがノードを作るだけなのでStringで定義

	private StatusCondition statusCondition = new StatusCondition();
}
