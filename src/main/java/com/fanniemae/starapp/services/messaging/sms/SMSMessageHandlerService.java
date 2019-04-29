package com.fanniemae.starapp.services.messaging.sms;

import com.fanniemae.starapp.commons.MessageConstants;
import com.fanniemae.starapp.providers.externals.twilio.models.MessageResponse;
import com.fanniemae.starapp.providers.externals.twilio.models.SMSMessage;
import com.fanniemae.starapp.providers.externals.twilio.models.SMSMessageRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for handling incoming SMS
 */
@Service
public class SMSMessageHandlerService extends BaseSMService {

    private static final Logger LOGGER = LogManager.getLogger(SMSMessageHandlerService.class);

    @Autowired
    private TwilioSMSService twilioSMSService;


    /**
     * Handle SMS message received
     * @param request
     * @param traceId
     */
    public MessageResponse handleSmsMessage(final SMSMessageRequest request, Long requestId, String traceId){
        LOGGER.info("Creating an SMS Message log. traceId of {}", traceId);


        //TODO: NEED TO STORE THE REQUEST IN DB. INVOKE DAO HERE
        final boolean result =  true; //smsMessageLogDao.createMessageLog(request, traceId);
        if(result){

            //TODO: Need to invoke message body processing

            final SMSMessage successReply = new SMSMessageRequest();
            successReply.setBody("We received your message and created a request on behalf of you. Please use #"+requestId+" for furthur communication, Thank you!");
            return twilioSMSService.generateSMSReply(successReply, traceId);

        }else{
            // Will not throw an error but generate an error message as response
            final SMSMessage errorReply = new SMSMessageRequest();
            errorReply.setBody(errorHandler.getErrorMessage(MessageConstants.CODE_SMS_ERROR_REPLY));
            return twilioSMSService.generateSMSReply(errorReply, traceId);
        }

    }




}
