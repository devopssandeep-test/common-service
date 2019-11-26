/*!
      Copyright 2013-2014 (c), SHARP CORPORATION.
      All rights are reserved.  Reproduction or transmission in whole or in part,
      in any form or by any means, electronic, mechanical or otherwise, is
      prohibited without the prior written consent of the copyright owner.
      \file                  SmtpSetting.java
      \author                lravinder
      \date                  Apr 11, 2014
      \brief                 This class is used for serialization of SMTP Setting from Database
      \note
      Base Module Used :      FD Module
      Last Reviewed By:
      Last Reviewed Date:
      Revision History:
      All the above information in this section is added by SSDI
*/
package srdm.cloud.commonService.util.MailAlert.mail;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import lombok.Data;

@Data
@JsonRootName("smtpSetting")
@XmlType(propOrder = { "host", "port", "useAuth", "userName", "password", "fromAddress", "useSsl" })
@JsonIgnoreProperties(ignoreUnknown = true)
public class SmtpSetting implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4243930009311857697L;
	@JsonProperty
	private String host;
	@JsonProperty
	private String port;
	@JsonProperty
	private String useAuth;
	@JsonProperty
	private String userName;
	@JsonProperty
	private String password;
	@JsonProperty
	private String fromAddress;
	@JsonProperty
	private String useSsl;
}
