package com.cleevio.vexl.module.user.controller;

import com.cleevio.vexl.module.user.service.UserRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User")
@RestController
@RequestMapping(value = "/internal")
@RequiredArgsConstructor
@Slf4j
public class InternalController {
    private final UserRepository userRepository;

    @PostMapping("/fail")
    void refreshUser() {
        this.userRepository.shouldFail();
    }

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
}
