package com.lostandfound.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResponse {

    private Integer id;

    @JsonProperty("student_id")
    private Integer studentId;

    private String name;
    private String email;
    private String phone;
}