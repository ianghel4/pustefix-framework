/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package de.schlund.pfixcore.util.email;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Utility class for sending mails via java mail-api.
 *
 * @author <a href="mailto: haecker@schlund.de">Joerg Haecker</a>
 */

public class EmailSender {

    private static final String CHARSET = "ISO-8859-15";

    /**
     * Send an email (without headers). 
     * 
     * @param subject  A String specifying the mail subject. Not null.
     * @param text     The text the mail should contain. Not null.
     * @param to       A String arry specifying the recipients. Not null.
     * @param from     A String specifying the from address. Not null.
     * @param smtphost A String specifying the smtp-server to use. Not null.
     * @throws EmailSenderException on errors when trying to send the mail.
     * @throws IllegalArgumentExceptionwhen trying to pass NPs as paramters.
     */
    public static void sendMail(
            String subject,
            String text,
            String[] to,
            String from,
            String smtphost)
            throws EmailSenderException {
        sendMail(subject, text, null, to, from, smtphost, null, null, false);
    }
    
    public static void sendMail(
            String subject,
            String text,
            String[] to,
            String from,
            String smtphost,
            String encoding)
            throws EmailSenderException {
        sendMail(subject, text, null, to, from, smtphost, null, null, false, encoding);
    }
    
    /**
     * Send an email (without headers). 
     * 
     * @param subject  A String specifying the mail subject. Not null.
     * @param text     The text the mail should contain. Not null.
     * @param to       A String arry specifying the recipients. Not null.
     * @param from     A String specifying the from address. Not null.
     * @param smtphost A String specifying the smtp-server to use. Not null.
     * @param authuser username to use for authentication against the mail server.
     *                  If set to <code>null</code>, no authentication will be used.
     * @param authpassword password to use for authentication against the mail server.
     *                  If set to <code>null</code>, no authentication will be used.
     * @throws EmailSenderException on errors when trying to send the mail.
     * @throws IllegalArgumentExceptionwhen trying to pass NPs as paramters.
     */
    public static void sendMail(
            String subject,
            String text,
            String[] to,
            String from,
            String smtphost,
            String authuser,
            String authpassword)
            throws EmailSenderException {
        sendMail(subject, text, null, to, from, smtphost, authuser, authpassword, false);
    }
    
    public static void sendMail(
            String subject,
            String text,
            String[] to,
            String from,
            String smtphost,
            String authuser,
            String authpassword,
            String encoding)
            throws EmailSenderException {
        sendMail(subject, text, null, to, from, smtphost, authuser, authpassword, false, encoding);
    }
    
    /**
     * Send an email.
     * 
     * @param subject  A String specifying the mail subject. Not null.
     * @param text     The text the mail should contain. Not null.
     * @param headers  A Map contain strings as key and values, these headers will be
     *                  appended to the headers of the email. Maybe null.
     * @param to       A String arry specifying the recipients. Not null.
     * @param from     A String specifying the from address. Not null.
     * @param smtphost A String specifying the smtp-server to use. Not null.
     * @throws EmailSenderException on errors when trying to send the mail.
     * @throws IllegalArgumentExceptionwhen trying to pass NPs as paramters.
     */
    public static void sendMail(
            String subject,
            String text,
            Map<String, String> headers,
            String[] to,
            String from,
            String smtphost)
            throws EmailSenderException {
        sendMail(subject, text, headers, to, from, smtphost, null, null, false);
    }
    
    public static void sendMail(
            String subject,
            String text,
            Map<String, String> headers,
            String[] to,
            String from,
            String smtphost,
            String encoding)
            throws EmailSenderException {
        sendMail(subject, text, headers, to, from, smtphost, null, null, false, encoding);
    }
    
    /**
     * Send an email.
     * 
     * @param subject  A String specifying the mail subject. Not null.
     * @param text     The text the mail should contain. Not null.
     * @param headers  A Map contain strings as key and values, these headers will be
     *                  appended to the headers of the email. Maybe null.
     * @param to       A String arry specifying the recipients. Not null.
     * @param from     A String specifying the from address. Not null.
     * @param smtphost A String specifying the smtp-server to use. Not null.
     * @param authuser username to use for authentication against the mail server.
     *                  If set to <code>null</code>, no authentication will be used.
     * @param authpassword password to use for authentication against the mail server.
     *                  If set to <code>null</code>, no authentication will be used.
     * @param secure   flag indicating whether to use the STARTTLS command if the server
     *                  supports it.
     * @throws EmailSenderException on errors when trying to send the mail.
     * @throws IllegalArgumentExceptionwhen trying to pass NPs as paramters.
     */
    public static void sendMail(
            String subject,
            String text,
            Map<String, String> headers,
            String[] to,
            String from,
            String smtphost,
            String authuser,
            String authpassword,
            boolean secure)
            throws EmailSenderException {
        sendMail(subject, text, headers, to, from, smtphost, authuser, authpassword, secure, CHARSET);
    }
    
    public static void sendMail(
        String subject,
        String text,
        Map<String, String> headers,
        String[] to,
        String from,
        String smtphost,
        String authuser,
        String authpassword,
        boolean secure,
        String encoding)
        throws EmailSenderException {

        if(subject == null)
            throw new IllegalArgumentException("EmailSender: A NP as subject is not allowed!");
      
        if(text == null)
            throw new IllegalArgumentException("EmailSender: A NP as text is not allowed!");
            
        if(to == null)
            throw new IllegalArgumentException("EmailSender: A NP as recipient-list is not allowed!");      
      
        for(int i=0; i<to.length; i++) {
        	if(to[i] == null  || to[i].equals("")) {
        		throw new IllegalArgumentException("EmailSender: To address["+i+"] not valid: '"+to[i]+"'");
        	}
        }
        
        if(from == null)
            throw new IllegalArgumentException("EmailSender: A NP as from address is not allowed!");

        if(smtphost == null)
            throw new IllegalArgumentException("EmailSender: A NP as SMTP host is not allowed!");


        Properties properties = new Properties();
        properties.put("mail.smtp.host", smtphost);
        if (secure) {
            properties.setProperty("mail.smtp.starttls.enable", "true");
        }
        if (authuser != null && authpassword != null) {
            properties.setProperty("mail.smtp.auth", "true");
        }
        Session session = Session.getInstance(properties);
        MimeMessage msg =
            new MimeMessage(session);

        StringBuffer strError = new StringBuffer();

        //remove all new lines from subject
        subject = subject.replace('\n', ' ');

        // handle to addresses
        InternetAddress[] toaddresses = new InternetAddress[to.length];
        for (int i = 0; i < to.length; i++) {
            try {
                toaddresses[i] = new InternetAddress(to[i]);
            } catch (AddressException e) {
                strError.append("Caught " + e.getClass().getName() + "\n");
                strError.append(
                    "Text: " + to[i] + " is not a valid address!" + "\n");
                strError.append("Message: " + e.getMessage() + "\n");
                throw new EmailSenderException(strError.toString());
            }
        }

        // handle from address
        InternetAddress fromaddress = null;
        try {
            fromaddress = new InternetAddress(from);
        } catch (AddressException e) {
            strError.append("Caught " + e.getClass().getName() + "\n");
            strError.append(
                "Text: " + from + " is not a valid address!" + "\n");
            strError.append("Message: " + e.getMessage() + "\n");
            throw new EmailSenderException(strError.toString());
        }

        // got everything, now send mail
        try {
            msg.setText(text, encoding);
            msg.setHeader("Content-Type", "text/plain; charset=" + encoding);
            msg.setHeader("Content-Transfer-Encoding", "8bit");
            
            if (headers != null) {
                Set<String> keys = headers.keySet();
                
                for (Iterator<String> iter = keys.iterator(); iter.hasNext();) {
                    String key = iter.next();
                    String value = headers.get(key);
                    msg.setHeader(key, value);
                }
                
            }
            
            msg.setRecipients(Message.RecipientType.TO, toaddresses);
            msg.setSubject(subject, encoding);
            msg.setFrom(fromaddress);
            msg.setSentDate(new Date());
            
            Transport transport;
            try {
                transport = session.getTransport("smtp");
            } catch (NoSuchProviderException e) {
                throw new EmailSenderException("Could not get a transport for smtp protocol", e);
            }
            
            if (authuser != null && authpassword != null) {
                transport.connect(smtphost, authuser, authpassword);
            } else {
                transport.connect(smtphost, null, null);
            }
            
            transport.sendMessage(msg, msg.getAllRecipients());
        } catch (MessagingException mex) {
            Exception ex = mex;
            strError.append("Caught " + mex.getClass().getName() + "\n");
            strError.append("Message: " + mex.getMessage() + "\n\n");
            do {
                if (ex instanceof SendFailedException) {
                    SendFailedException sfex = (SendFailedException) ex;
                    Address[] invalid = sfex.getInvalidAddresses();
                    if (invalid != null) {
                        strError.append("** Invalid Addresses\n");
                        if (invalid != null) {
                            for (int i = 0; i < invalid.length; i++)
                                strError.append(" " + invalid[i] + "\n");
                        }
                    }
                    Address[] validUnsent = sfex.getValidUnsentAddresses();
                    if (validUnsent != null) {
                        strError.append("    ** ValidUnsent Addresses\n");
                        if (validUnsent != null) {
                            for (int i = 0; i < validUnsent.length; i++)
                                strError.append(" " + validUnsent[i] + "\n");
                        }
                    }
                    Address[] validSent = sfex.getValidSentAddresses();
                    if (validSent != null) {
                        strError.append("    ** ValidSent Addresses\n");
                        if (validSent != null) {
                            for (int i = 0; i < validSent.length; i++)
                                strError.append(" " + validSent[i] + "\n");
                        }
                    }
                }
                strError.append("\n");
                if (ex instanceof MessagingException)
                    ex = ((MessagingException) ex).getNextException();
                else
                    ex = null;
            } while (ex != null);
            throw new EmailSenderException(strError.toString());
        }
    }
    
}
