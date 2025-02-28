package com.oviva.epa.client.konn;

import telematik.ws.conn.authsignatureservice.wsdl.v7_4.AuthSignatureServicePortType;
import telematik.ws.conn.cardservice.wsdl.v8_1.CardServicePortType;
import telematik.ws.conn.certificateservice.wsdl.v6_0.CertificateServicePortType;
import telematik.ws.conn.eventservice.wsdl.v6_1.EventServicePortType;
import telematik.ws.conn.phrs.phrmanagementservice.wsdl.v2_5.PHRManagementServicePortType;
import telematik.ws.conn.phrs.phrservice.wsdl.v2_0.PHRServicePortType;
import telematik.ws.conn.signatureservice.wsdl.v7_5.SignatureServicePortType;
import telematik.ws.conn.vsds.vsdservice.wsdl.v5_2.VSDServicePortType;

public interface KonnektorConnection {

  PHRServicePortType phrService();

  PHRManagementServicePortType phrManagementService();

  EventServicePortType eventService();

  CardServicePortType cardService();

  CertificateServicePortType certificateService();

  SignatureServicePortType signatureService();

  VSDServicePortType vsdService();

  AuthSignatureServicePortType authSignatureService();
}
