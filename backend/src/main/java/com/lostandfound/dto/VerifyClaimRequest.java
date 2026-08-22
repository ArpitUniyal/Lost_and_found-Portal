package com.lostandfound.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyClaimRequest {

    private Integer foundItemId;
    private String location;
    private String date;
    private String time;
}