package com.cleevio.vexl.module.user.controller;

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
    private final Integer notificationAfter;
    private final TestService test;

    @Autowired
    private ThreadPoolTaskExecutor sendNotificationToContactsExecutor;

    @RequestMapping("/process-user-inactivity")
    public void processUserInactivity() {
        this.userService.processNotificationsForInactivity(notificationAfter);
    }


    @PostMapping(value="/async-test")
    @ResponseBody
    public String spawnAsyncThread() {
        test.runDummyAsyncThread();
        return "spawned";
    }

    @GetMapping(value="/async-test")
    @ResponseBody
    public String getAsyncThreadsCount() {
        return  " From executor: " + sendNotificationToContactsExecutor.getActiveCount()
                + " Max size: " + sendNotificationToContactsExecutor.getMaxPoolSize()
                + " Active: " + sendNotificationToContactsExecutor.getActiveCount();
    }
}
