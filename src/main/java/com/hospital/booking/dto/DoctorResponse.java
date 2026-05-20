package com.hospital.booking.dto;

public record DoctorResponse(Long id, String name, String department, String hospitalName, boolean available) {}
