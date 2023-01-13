package com.cleevio.vexl.common.service;

import com.cleevio.vexl.common.constant.ModuleLockNamespace;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdvisoryLockService {

    private final AdvisoryLockRepository repository;

    @Transactional(propagation = Propagation.MANDATORY)
    public void lock(ModuleLockNamespace module, @Nullable String lockName, Object... params) {
        repository.lockExclusively(constructKey(module, lockName, params));
    }

    private static String constructKey(ModuleLockNamespace module, @Nullable String lockName, Object... params) {
        List<String> key = new ArrayList<>();

        key.add(module.name());
        Optional.ofNullable(lockName).ifPresentOrElse(key::add, () -> key.add(""));
        key.addAll(Arrays.stream(params).map(Object::toString).toList());

        return String.join("/", key);
    }
}
