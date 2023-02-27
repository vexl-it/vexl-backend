package com.cleevio.vexl.module.user.controller;

import com.cleevio.vexl.module.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Inactivity")
@RestController
@RequestMapping("/internal/inactivity")
@RequiredArgsConstructor
public class InternalController {
    private final UserService userService;
    @Value("${inactivity.period}")
    private final Integer notificationAfter;

    @RequestMapping("/process-user-inactivity")
    public void processUserInactivity() {
        this.userService.processNotificationsForInactivity(notificationAfter);
    }
}
