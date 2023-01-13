package com.cleevio.vexl.module.inbox.dto.request;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

public record BatchDeletionRequest(

        @NotEmpty
        List<@Valid DeletionRequest> dataForRemoval
) {
}
