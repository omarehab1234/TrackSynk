package com.yourapp.dto.company;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class JoinCompanyRequest {

    @NotBlank(message = "Company slug is required")
    @Size(max = 255, message = "Company slug is too long")
    private String slug;

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }
}