package com.lostandfound.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotifyOwnerRequest {

    private Integer lostItemId;
    private Integer foundItemId;
}