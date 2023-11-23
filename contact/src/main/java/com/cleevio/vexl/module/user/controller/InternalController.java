package com.cleevio.vexl.module.user.controller;

import com.cleevio.vexl.common.integration.firebase.service.FirebaseService;
import com.cleevio.vexl.module.user.service.TestService;
import com.cleevio.vexl.module.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "Inactivity")
@RestController
@RequestMapping("/internal/inactivity")
@RequiredArgsConstructor
public class InternalController {

    private final UserService userService;
    @Value("${inactivity.period}")
    private final Integer inactivityNotificationAfter;
    @Value("${newContent.period}")
    private final Integer newContentNotificationAfter;

    private final FirebaseService firebaseService;

    @RequestMapping(value = "/process-user-inactivity", method = RequestMethod.POST)
    public void processUserInactivity() {
        this.userService.processNotificationsForInactivity(inactivityNotificationAfter);
    }

    @RequestMapping(value = "/process-new-content-notifications", method = RequestMethod.POST)
    public void processNewContentNotification() {
        this.userService.notifyInactiveUsersAboutNewContent(newContentNotificationAfter);
    }

    @RequestMapping(value = "/send-create-offer-prompt-to-general-topic", method = RequestMethod.POST)
    public void sendCreateOfferPromptToGeneralTopic() {
        this.firebaseService.sendCreateOfferPromptToGeneralTopic();
    }
}
