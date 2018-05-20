package com.tnk;

public class Item_OperationInput {

	private boolean isEnabled = false;
	private boolean isDisabled = true;
	
	private String opName;
	private String opInputPath;
	private String opOutputPath;
	
	// constructors
	public Item_OperationInput() {
		
	}
	
	public Item_OperationInput(boolean isEnabled,
								boolean isDisabled,
								String opName,
								String opInputPath,
								String opOutputPath) {
		this.isEnabled = isEnabled;
		this.isDisabled =  isDisabled;
		this.opName = opName;
		this.opInputPath = opInputPath;
		this.opOutputPath = opOutputPath;
	}
	
	/**
	 * @return the isEnabled
	 */
	public void setEnabled(boolean isEnabled) {this.isEnabled = isEnabled;}
	public boolean getEnabled() {return this.isEnabled;}
	/**
	 * @return the isDisabled
	 */
	public void setDisabled(boolean isDisabled) {this.isDisabled = isDisabled;}
	public boolean getDisabled() {return this.isDisabled;}
	/**
	 * @return the opName
	 */
	public void setName(String opName) {this.opName = opName;}
	public String getName() {return this.opName;}
	/**
	 * @return the opInputPath
	 */
	public void setInputPath(String opInputPath) {this.opInputPath = opInputPath;}
	public String getInputName() {return this.opInputPath;}
	/**
	 * @return the opOutputPath
	 */
	public void setOutputPath(String opOutputPath) {this.opOutputPath = opOutputPath;}
	public String getOutputName() {return this.opOutputPath;}
}
