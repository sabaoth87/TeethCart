package com.tnk;

public class Contract_OperationInput {

	private static boolean isEnabled = false;
	private static boolean isDisabled = true;
	/**
	 * @return the isEnabled
	 */
	public static boolean isEnabled() {
		return isEnabled;
	}
	/**
	 * @param isEnabled the isEnabled to set
	 */
	public static void setEnabled(boolean isEnabled) {
		Contract_OperationInput.isEnabled = isEnabled;
	}
	/**
	 * @return the isDisabled
	 */
	public static boolean isDisabled() {
		return isDisabled;
	}
	/**
	 * @param isDisabled the isDisabled to set
	 */
	public static void setDisabled(boolean isDisabled) {
		Contract_OperationInput.isDisabled = isDisabled;
	}
	
	
}
