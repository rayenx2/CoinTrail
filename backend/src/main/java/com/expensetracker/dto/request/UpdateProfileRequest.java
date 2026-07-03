package com.expensetracker.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UpdateProfileRequest {
    @NotBlank
    private String name;
    @NotBlank
    @Pattern(regexp = "EUR|USD|GBP|CHF", message = "Unsupported currency")
    private String currency;

    public UpdateProfileRequest() {}
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
