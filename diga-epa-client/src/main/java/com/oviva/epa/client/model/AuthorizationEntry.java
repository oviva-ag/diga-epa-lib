package com.oviva.epa.client.model;

import java.time.LocalDate;

public record AuthorizationEntry(RecordIdentifier recordIdentifier, LocalDate validTo) {}
