package com.oviva.epa.client.konn;

import telematik.ws.conn.authsignatureservice.wsdl.v7_4.AuthSignatureServicePortType;
import telematik.ws.conn.cardservice.wsdl.v8_1.CardServicePortType;
import telematik.ws.conn.certificateservice.wsdl.v6_0.CertificateServicePortType;
import telematik.ws.conn.eventservice.wsdl.v6_1.EventServicePortType;

public interface KonnektorConnection {

  EventServicePortType eventService();

  CardServicePortType cardService();

  CertificateServicePortType certificateService();

  AuthSignatureServicePortType authSignatureService();
}
