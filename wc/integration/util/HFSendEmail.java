package com.hf.wc.integration.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSLog;
import com.lcs.wc.util.LCSProperties;

import wt.util.WTProperties;

public final class HFSendEmail {
	/**
	 * logger object.
	 */
	static final Logger logger = Logger.getLogger(HFSendEmail.class);
	
	/**
	 * TO_EMAIL_ADDRESS for TO_EMAIL_ADDRESS.
	 */
	public static final String TO_EMAIL_ADDRESS = LCSProperties.get("com.hf.wc.integration.salsify.outbound.toEmailAddresses");
	/** Class Constructor */
	private HFSendEmail() {
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		sendEmail("Outbound trigger failed", null, null);
	}
	
	/**
	 * Send a simple, single part, text/plain e-mail.
	 * 
	 * @param message
	 *            the message.
	 * 
	 *  
	 */
	/**
	 * 
	 * @param message
	 */
	@SuppressWarnings("deprecation")
	public static void sendEmail(String message,String sub,String toAdd) {
		try {
			String subject 	= sub;
			String  toAddresses = toAdd;
			 subject =WTProperties.getLocalProperties().getProperty(
					"wt.rmi.server.hostname");
			// SUBSTITUTING ISP'S MAIL SERVER HERE!!!
			// SMTP Host
			String host = WTProperties.getLocalProperties().getProperty(
					"wt.mail.mailhost");
			//host = "10.6.12.229";
			// for local host 10.6.12.229 , port  25
			// SUBSTITUTING E-Mail addresses here!!!
			String fromAdddress =  WTProperties.getLocalProperties().getProperty(
					"wt.mail.from");
			toAddresses	=	null;
			// In case mailTo is null ,send it to the ultimate group
			if (!FormatHelper.hasContent(toAddresses)) {
				toAddresses = TO_EMAIL_ADDRESS;
			}
			
			if (FormatHelper.hasContent(toAddresses)){
			Collection<String> addressList = Arrays.asList(toAddresses
					.split(","));
			// Create properties, get Session
			Properties props = new Properties();
			// If using static Transport.send(),need to specify which host
			// to
			// send it to
			props.put("mail.smtp.host", host);
			// To see what is going on behind the scene
			props.put("mail.debug", "true");
			Session session = Session.getInstance(props);
			// Instantiate a message
			Message msg = new MimeMessage(session);
			// Set message attributes

			Address[] addresses = new InternetAddress[addressList.size()];

			// declaring an integer to be used for the array
			int i = 0;
			for (String toAddress : addressList) {
				addresses[i] = new InternetAddress(toAddress);
				i++;
			}
			msg.setFrom(new InternetAddress(fromAdddress));
			msg.setRecipients(Message.RecipientType.TO, addresses);
			msg.setSubject(subject);
			msg.setSentDate(new Date());
			// Set message content
			msg.setText(message);
			// Send the message
			logger.info("debugBefore sending attempt the email debug");
			Transport.send(msg);
			logger.info("debugAfter sending attempt the message");
			}
		} catch (MessagingException mex) {
			// Prints all nested (chained) exceptions as well
			LCSLog.stackTrace(mex);
		} catch (IOException ioexp) {
			LCSLog.stackTrace(ioexp);
		}
	}


}
