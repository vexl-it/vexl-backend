package com.cleevio.vexl.module.user.controller;

import com.cleevio.vexl.module.user.service.UserRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User")
@RestController
@RequestMapping(value = "/internal")
@RequiredArgsConstructor
public class InternalController {
        private final UserRepository userRepository;

        @PostMapping("/fail")
        void refreshUser() {
            this.userRepository.shouldFail();
        }
}
