package com.fanniemae.starapp.controllers.messaging;

import com.fanniemae.starapp.controllers.request.ContactUsBean;
import com.fanniemae.starapp.domains.MultiChannelAutoMessage;
import com.fanniemae.starapp.repositories.MultiChannelAutoMessageRepository;
import com.fanniemae.starapp.services.email.EmailSender;
import com.julienvey.trello.Trello;
import com.julienvey.trello.domain.Card;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Optional;

/**
 * Basic API that user can send a message and the APP will respond in the email
 */
@RestController
@RequestMapping("/communication")
public class ContactUsController {

    private static final Logger LOGGER = LogManager.getLogger(ContactUsController.class);

    @Value("${starapp.trello.idlist}")
    String idlist;

    @Autowired
    Trello trelloApi;

    @Autowired
    MultiChannelAutoMessageRepository multiChannelAutoMessageRepository;

    @Autowired
    MessageSource messageSource;

    @Autowired
    private EmailSender emailSender;

    /**
     * This API will capture user information and create a response back to the user's provided email
     *
     * @param message
     * @return
     */
    @PostMapping("/message")
    public ResponseEntity sendMessage(@RequestBody ContactUsBean message) {

        //TODO: Delegate to the service layer that will perform the following:
        // 1. Create a Trello Card
        // 2. Respond back to the requester's email using SendGrid implementation.
        // 3. Store the user information as email channel for future communication such as when a trello card gets updated.

        LOGGER.info("{}", message);
        MultiChannelAutoMessage multiCnlMsg;
        String msgBody = message.getMessage();
        if (msgBody.contains("#")) {
            int hashIndex = msgBody.indexOf("#");
            Long msgId = Long.parseLong(msgBody.substring(hashIndex + 1, msgBody.indexOf(" ", hashIndex)));
            Optional<MultiChannelAutoMessage> multiChannelAutoMessages = multiChannelAutoMessageRepository.findById(msgId);
            multiCnlMsg = multiChannelAutoMessages.get();
            trelloApi.addCommentToCard(multiCnlMsg.getCardId(), message.getMessage().replace("#" + msgId, ""));
            MessageFormat mf = new MessageFormat(messageSource.getMessage("starapp.twillio.acknoledgeupdate", null, Locale.US));
            message.setMessage(mf.format(new Object[]{multiCnlMsg.getId()}));
        } else {
            Card card = new Card();
            card.setName(message.getSubject());
            card.setDesc(message.getMessage());
            card = trelloApi.createCard(idlist, card);

            multiCnlMsg = new MultiChannelAutoMessage();
            multiCnlMsg.setCardId(card.getId());
            multiCnlMsg.setChannelType("EMAIL");
            multiCnlMsg.setContact(message.getEmail());
            multiCnlMsg.setLastName(message.getLastName());
            multiCnlMsg.setFirstName(message.getFirstName());
            multiCnlMsg = multiChannelAutoMessageRepository.save(multiCnlMsg);
            MessageFormat mf = new MessageFormat(messageSource.getMessage("starapp.twillio.acknoledgement", null, Locale.US));
            message.setMessage(mf.format(new Object[]{multiCnlMsg.getId()}));
        }
        emailSender.send(message);

        return new ResponseEntity(HttpStatus.OK);
    }

}
