package com.cleevio.vexl.module.user.controller;

import com.cleevio.vexl.module.user.dto.request.TestRequest;
import com.cleevio.vexl.module.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Internal")
@RestController
@RequestMapping(value = "/internal")
@RequiredArgsConstructor
@Slf4j
public class InternalController {
    private final UserService userService;

    @PostMapping("/test-log")
    @ResponseStatus(HttpStatus.OK)
    void testLog(@RequestBody TestRequest request) {
        switch (request.logLevel()) {
            case "INFO" -> log.info(request.textToLog());
            case "WARN" -> log.warn(request.textToLog());
            case "ERROR" -> log.error(request.textToLog());
            default -> log.debug(request.textToLog());
        }
    }

    @PostMapping("/test-error")
    @ResponseStatus(HttpStatus.OK)
    void testError() {
        userService.willFail();
    }
}
