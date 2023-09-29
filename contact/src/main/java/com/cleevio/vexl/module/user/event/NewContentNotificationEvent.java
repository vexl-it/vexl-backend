package com.cleevio.vexl.module.user.event;

import com.cleevio.vexl.module.user.dto.InactivityNotificationDto;
import com.cleevio.vexl.module.user.dto.NewContentNotificationDto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

public record NewContentNotificationEvent(
      @NotEmpty
      List<@NotNull NewContentNotificationDto> dtos
){}
