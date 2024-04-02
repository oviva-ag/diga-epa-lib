package com.oviva.epa.client.model;

import java.time.LocalDate;

/**
 * @param name the name of the application, e.g. 'ePA'
 * @param validTo the date until when this authorization is valid
 */
public record AuthorizedApplication(String name, LocalDate validTo) {}
