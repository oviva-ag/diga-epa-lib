package com.oviva.poc;

public class Export {
  public static final String EXPORT_XML =
      """
<Bundle xmlns="http://hl7.org/fhir">
   <id value="7b176945-c341-4117-b8b5-e7a387dbc88c"/>
   <meta>
      <versionId value="1"/>
      <lastUpdated value="2023-03-01T15:44:24.313+00:00"/>
      <profile value="https://fhir.kbv.de/StructureDefinition/KBV_PR_MIO_DIGA_Bundle|1.1.0"/>
   </meta>
   <identifier>
      <type>
         <coding>
            <system value="http://terminology.hl7.org/CodeSystem/v2-0203"/>
            <version value="2.9"/>
            <code value="RI"/>
            <display value="Resource identifier"/>
         </coding>
      </type>
      <system value="urn:ietf:rfc:3986"/>
      <value value="urn:uuid:7b176945-c341-4117-b8b5-e7a387dbc88c"/>
   </identifier>
   <type value="document"/>
   <timestamp value="2023-03-01T15:44:24.313+00:00"/>
   <entry>
      <fullUrl value="urn:uuid:539dbf21-27dc-4913-9b07-f260922a0e1d"/>
      <resource>
         <Composition xmlns="http://hl7.org/fhir">
            <id value="539dbf21-27dc-4913-9b07-f260922a0e1d"/>
            <meta>
               <versionId value="1"/>
               <lastUpdated value="2023-03-01T15:44:24.313+00:00"/>
               <profile value="https://fhir.kbv.de/StructureDefinition/KBV_PR_MIO_DIGA_Composition|1.1.0"/>
            </meta>
            <extension url="https://fhir.kbv.de/StructureDefinition/KBV_EX_MIO_DIGA_Betrachtungszeitraum">
               <valuePeriod>
                  <start value="2023-03-01T15:32:58+00:00"/>
                  <end value="2023-03-01T15:44:24+00:00"/>
               </valuePeriod>
            </extension>
            <status value="final"/>
            <type>
               <coding>
                  <system value="http://loinc.org"/>
                  <version value="2.72"/>
                  <code value="53576-5"/>
                  <display value="Personal health monitoring report Document"/>
               </coding>
            </type>
            <subject>
               <reference value="urn:uuid:3071accc-8240-4b87-8118-9128c9c783cb"/>
            </subject>
            <date value="2023-03-01T15:44:24+00:00"/>
            <author>
               <reference value="urn:uuid:c61470b0-e65b-410b-a858-52ee1f6fd69b"/>
            </author>
            <title value="DiGA-Export"/>
            <section>
               <title value="Befunde und Ergebnisse"/>
               <code>
                  <coding>
                     <system value="http://snomed.info/sct"/>
                     <version value="http://snomed.info/sct/900000000000207008/version/20220331"/>
                     <code value="423100009"/>
                     <display value="Results section (record artifact)"/>
                  </coding>
               </code>
               <entry>
                  <reference value="urn:uuid:6627a226-3fa2-44d0-9f01-2c89810ff33d"/>
               </entry>
               <entry>
                  <reference value="urn:uuid:c19bc648-26c3-41ef-8e96-d3a92feaf0b1"/>
               </entry>
               <section>
                  <title value="Vitalzeichen und Körpermaße"/>
                  <code>
                     <coding>
                        <system value="http://snomed.info/sct"/>
                        <version value="http://snomed.info/sct/900000000000207008/version/20220331"/>
                        <code value="1184593002"/>
                        <display value="Vital sign document section (record artifact)"/>
                     </coding>
                  </code>
                  <entry>
                     <reference value="urn:uuid:75f306e9-2529-4a5f-aa35-452917b35fac"/>
                  </entry>
                  <entry>
                     <reference value="urn:uuid:d7d5da28-ccd3-423d-b961-f032c17309ba"/>
                  </entry>
                  <entry>
                     <reference value="urn:uuid:9fbb10e8-941f-40a2-8678-0f6377aa610c"/>
                  </entry>
               </section>
            </section>
            <section>
               <title value="Aktivitäten"/>
               <code>
                  <coding>
                     <system value="https://fhir.kbv.de/CodeSystem/KBV_CS_MIO_DIGA_Section_Codes"/>
                     <version value="1.1.0"/>
                     <code value="SectionAktivitaeten"/>
                     <display value="Bereich Aktivitäten"/>
                  </coding>
               </code>
               <entry>
                  <reference value="urn:uuid:90183677-068f-4d71-a6ca-f53ff01224b0"/>
               </entry>
            </section>
            <section>
               <title value="Nahrung"/>
               <code>
                  <coding>
                     <system value="https://fhir.kbv.de/CodeSystem/KBV_CS_MIO_DIGA_Section_Codes"/>
                     <version value="1.1.0"/>
                     <code value="SectionNahrung"/>
                     <display value="Bereich Nahrung"/>
                  </coding>
               </code>
               <entry>
                  <reference value="urn:uuid:82f88103-45a0-45ec-b02d-0fe6eed262cc"/>
               </entry>
            </section>
            <section>
               <title value="Beurteilungen"/>
               <code>
                  <coding>
                     <system value="http://snomed.info/sct"/>
                     <version value="http://snomed.info/sct/900000000000207008/version/20220331"/>
                     <code value="424836000"/>
                     <display value="Assessment section (record artifact)"/>
                  </coding>
               </code>
               <entry>
                  <reference value="urn:uuid:0bb75740-165e-4501-8264-81fae14244e0"/>
               </entry>
            </section>
         </Composition>
      </resource>
   </entry>
   <entry>
      <fullUrl value="urn:uuid:3b1ebbe9-6f62-443a-8f77-a6cb931f2068"/>
      <resource>
         <DeviceDefinition xmlns="http://hl7.org/fhir">
            <id value="3b1ebbe9-6f62-443a-8f77-a6cb931f2068"/>
            <meta>
               <versionId value="1"/>
               <lastUpdated value="2023-03-01T15:44:24.313+00:00"/>
               <profile value="https://fhir.kbv.de/StructureDefinition/KBV_PR_MIO_DIGA_DeviceDefinition_DIGA|1.1.0"/>
            </meta>
            <identifier>
               <type>
                  <coding>
                     <system value="http://terminology.hl7.org/CodeSystem/v2-0203"/>
                     <version value="2.9"/>
                     <code value="RI"/>
                     <display value="Resource identifier"/>
                  </coding>
               </type>
               <system value="urn:ietf:rfc:3986"/>
               <value value="urn:uuid:3b1ebbe9-6f62-443a-8f77-a6cb931f2068"/>
            </identifier>
            <type>
               <coding>
                  <system value="http://snomed.info/sct"/>
                  <version value="http://snomed.info/sct/900000000000207008/version/20220331"/>
                  <code value="706689003"/>
                  <display value="Application program software (physical object)"/>
               </coding>
            </type>
         </DeviceDefinition>
      </resource>
   </entry>
   <entry>
      <fullUrl value="urn:uuid:c61470b0-e65b-410b-a858-52ee1f6fd69b"/>
      <resource>
         <Device xmlns="http://hl7.org/fhir">
            <id value="c61470b0-e65b-410b-a858-52ee1f6fd69b"/>
            <meta>
               <versionId value="1"/>
               <lastUpdated value="2023-03-01T15:44:24.313+00:00"/>
               <profile value="https://fhir.kbv.de/StructureDefinition/KBV_PR_MIO_DIGA_Device_DIGA|1.1.0"/>
            </meta>
            <identifier>
               <type>
                  <text value="DiGA-VE-ID"/>
               </type>
               <system value="urn:ietf:rfc:3986"/>
               <value value="urn:uuid:c61470b0-e65b-410b-a858-52ee1f6fd69b"/>
            </identifier>
            <definition>
               <reference value="urn:uuid:3b1ebbe9-6f62-443a-8f77-a6cb931f2068"/>
            </definition>
            <deviceName>
               <name value="Oviva Direkt IT"/>
               <type value="user-friendly-name"/>
            </deviceName>
            <type>
               <coding>
                  <system value="http://fhir.de/CodeSystem/ifa/pzn"/>
                  <code value="123456789"/>
               </coding>
            </type>
         </Device>
      </resource>
   </entry>
   <entry>
      <fullUrl value="urn:uuid:3071accc-8240-4b87-8118-9128c9c783cb"/>
      <resource>
         <Patient xmlns="http://hl7.org/fhir">
            <id value="3071accc-8240-4b87-8118-9128c9c783cb"/>
            <meta>
               <versionId value="1"/>
               <lastUpdated value="2023-03-01T15:44:24.314+00:00"/>
               <profile value="https://fhir.kbv.de/StructureDefinition/KBV_PR_MIO_DIGA_Patient|1.1.0"/>
            </meta>
            <identifier>
               <type>
                  <coding>
                     <system value="http://terminology.hl7.org/CodeSystem/v2-0203"/>
                     <version value="2.9"/>
                     <code value="RI"/>
                     <display value="Resource identifier"/>
                  </coding>
               </type>
               <system value="urn:ietf:rfc:3986"/>
               <value value="urn:uuid:3071accc-8240-4b87-8118-9128c9c783cb"/>
            </identifier>
            <name>
               <use value="official"/>
               <family value="Testen">
                  <extension url="http://hl7.org/fhir/StructureDefinition/humanname-own-name">
                     <valueString value="Testen"/>
                  </extension>
               </family>
               <given value="Andreasen"/>
            </name>
            <telecom>
               <system value="email"/>
               <value value="andreas.huegli+p1@oviva.com"/>
            </telecom>
            <gender value="male"/>
            <birthDate value="1999-11-11"/>
            <address>
               <type value="both"/>
               <line value="in non et dolore"/>
               <city value="adipisicing Ut"/>
               <postalCode value="sit dolor"/>
               <country value="GB"/>
            </address>
            <communication>
               <language>
                  <coding>
                     <system value="urn:ietf:bcp:47"/>
                     <code value="de"/>
                     <display value="German"/>
                  </coding>
               </language>
            </communication>
         </Patient>
      </resource>
   </entry>
   <entry>
      <fullUrl value="urn:uuid:90183677-068f-4d71-a6ca-f53ff01224b0"/>
      <resource>
         <Procedure xmlns="http://hl7.org/fhir">
            <id value="90183677-068f-4d71-a6ca-f53ff01224b0"/>
            <meta>
               <versionId value="1"/>
               <lastUpdated value="2023-03-01T15:42:49.975+00:00"/>
               <profile value="https://fhir.kbv.de/StructureDefinition/KBV_PR_MIO_DIGA_Procedure_Activity|1.1.0"/>
            </meta>
            <identifier>
               <type>
                  <coding>
                     <system value="http://terminology.hl7.org/CodeSystem/v2-0203"/>
                     <version value="2.9"/>
                     <code value="RI"/>
                     <display value="Resource identifier"/>
                  </coding>
               </type>
               <system value="urn:ietf:rfc:3986"/>
               <value value="urn:uuid:90183677-068f-4d71-a6ca-f53ff01224b0"/>
            </identifier>
            <status value="completed"/>
            <code>
               <text value="Running"/>
            </code>
            <subject>
               <reference value="urn:uuid:3071accc-8240-4b87-8118-9128c9c783cb"/>
            </subject>
            <performedPeriod>
               <start value="2023-03-01T15:42:46+00:00"/>
               <end value="2023-03-01T16:52:46+00:00"/>
            </performedPeriod>
         </Procedure>
      </resource>
   </entry>
   <entry>
      <fullUrl value="urn:uuid:75f306e9-2529-4a5f-aa35-452917b35fac"/>
      <resource>
         <Observation xmlns="http://hl7.org/fhir">
            <id value="75f306e9-2529-4a5f-aa35-452917b35fac"/>
            <meta>
               <versionId value="1"/>
               <lastUpdated value="2023-03-01T15:43:20.863+00:00"/>
               <profile value="https://fhir.kbv.de/StructureDefinition/KBV_PR_MIO_DIGA_Observation_Glucose_Concentration|1.1.0"/>
            </meta>
            <identifier>
               <type>
                  <coding>
                     <system value="http://terminology.hl7.org/CodeSystem/v2-0203"/>
                     <version value="2.9"/>
                     <code value="RI"/>
                     <display value="Resource identifier"/>
                  </coding>
               </type>
               <system value="urn:ietf:rfc:3986"/>
               <value value="urn:uuid:75f306e9-2529-4a5f-aa35-452917b35fac"/>
            </identifier>
            <status value="final"/>
            <category>
               <coding>
                  <system value="http://terminology.hl7.org/CodeSystem/observation-category"/>
                  <code value="vital-signs"/>
               </coding>
            </category>
            <code>
               <coding>
                  <system value="http://loinc.org"/>
                  <version value="2.72"/>
                  <code value="15074-8"/>
                  <display value="Glucose [Moles/volume] in Blood"/>
               </coding>
               <coding>
                  <system value="http://snomed.info/sct"/>
                  <version value="http://snomed.info/sct/900000000000207008/version/20220331"/>
                  <code value="434912009"/>
                  <display value="Blood glucose concentration (observable entity)"/>
               </coding>
            </code>
            <subject>
               <reference value="urn:uuid:3071accc-8240-4b87-8118-9128c9c783cb"/>
            </subject>
            <effectiveDateTime value="2023-03-01T15:43:14+00:00"/>
            <valueQuantity>
               <value value="5.000000000"/>
               <unit value="mmol/L"/>
               <system value="http://unitsofmeasure.org"/>
               <code value="mmol/L"/>
            </valueQuantity>
         </Observation>
      </resource>
   </entry>
   <entry>
      <fullUrl value="urn:uuid:82f88103-45a0-45ec-b02d-0fe6eed262cc"/>
      <resource>
         <Observation xmlns="http://hl7.org/fhir">
            <id value="82f88103-45a0-45ec-b02d-0fe6eed262cc"/>
            <meta>
               <versionId value="1"/>
               <lastUpdated value="2023-03-01T15:42:31.662+00:00"/>
               <profile value="https://fhir.kbv.de/StructureDefinition/KBV_PR_MIO_DIGA_Observation_Nutrition_Intake|1.1.0"/>
            </meta>
            <identifier>
               <type>
                  <coding>
                     <system value="http://terminology.hl7.org/CodeSystem/v2-0203"/>
                     <version value="2.9"/>
                     <code value="RI"/>
                     <display value="Resource identifier"/>
                  </coding>
               </type>
               <system value="urn:ietf:rfc:3986"/>
               <value value="urn:uuid:82f88103-45a0-45ec-b02d-0fe6eed262cc"/>
            </identifier>
            <status value="final"/>
            <code>
               <coding>
                  <system value="http://loinc.org"/>
                  <version value="2.72"/>
                  <code value="8999-5"/>
                  <display value="Fluid intake oral Estimated"/>
               </coding>
               <coding>
                  <system value="http://snomed.info/sct"/>
                  <version value="http://snomed.info/sct/900000000000207008/version/20220331"/>
                  <code value="251992000"/>
                  <display value="Fluid intake (observable entity)"/>
               </coding>
            </code>
            <subject>
               <reference value="urn:uuid:3071accc-8240-4b87-8118-9128c9c783cb"/>
            </subject>
            <effectiveDateTime value="2023-03-01T15:42:23+00:00"/>
            <component>
               <extension url="https://fhir.kbv.de/StructureDefinition/KBV_EX_MIO_DIGA_Nutrition_Intake_Not_Consumed">
                  <valueBoolean value="true"/>
               </extension>
               <extension url="https://fhir.kbv.de/StructureDefinition/KBV_EX_MIO_DIGA_Nutrition_Intake_Not_Consumed_Reason">
                  <valueCodeableConcept>
                     <coding>
                        <system value="http://snomed.info/sct"/>
                        <version value="http://snomed.info/sct/900000000000207008/version/20220331"/>
                        <code value="229918000"/>
                        <display value="Food and drink intake (observable entity)"/>
                     </coding>
                  </valueCodeableConcept>
               </extension>
               <code>
                  <coding>
                     <system value="http://loinc.org"/>
                     <version value="2.72"/>
                     <code value="8999-5"/>
                     <display value="Fluid intake oral Estimated"/>
                  </coding>
                  <coding>
                     <system value="http://snomed.info/sct"/>
                     <version value="http://snomed.info/sct/900000000000207008/version/20220331"/>
                     <code value="251992000"/>
                     <display value="Fluid intake (observable entity)"/>
                  </coding>
               </code>
               <valueQuantity>
                  <value value="200.0000000"/>
                  <unit value="mL"/>
                  <system value="http://unitsofmeasure.org"/>
                  <code value="mL"/>
               </valueQuantity>
            </component>
         </Observation>
      </resource>
   </entry>
   <entry>
      <fullUrl value="urn:uuid:6627a226-3fa2-44d0-9f01-2c89810ff33d"/>
      <resource>
         <Observation xmlns="http://hl7.org/fhir">
            <id value="6627a226-3fa2-44d0-9f01-2c89810ff33d"/>
            <meta>
               <versionId value="1"/>
               <lastUpdated value="2023-03-01T15:42:41.509+00:00"/>
               <profile value="https://fhir.kbv.de/StructureDefinition/KBV_PR_MIO_DIGA_Observation_Free|1.1.0"/>
            </meta>
            <identifier>
               <type>
                  <coding>
                     <system value="http://terminology.hl7.org/CodeSystem/v2-0203"/>
                     <version value="2.9"/>
                     <code value="RI"/>
                     <display value="Resource identifier"/>
                  </coding>
               </type>
               <system value="urn:ietf:rfc:3986"/>
               <value value="urn:uuid:6627a226-3fa2-44d0-9f01-2c89810ff33d"/>
            </identifier>
            <status value="final"/>
            <category>
               <coding>
                  <system value="http://terminology.hl7.org/CodeSystem/observation-category"/>
                  <code value="social-history"/>
               </coding>
            </category>
            <code>
               <coding>
                  <system value="http://loinc.org"/>
                  <version value="2.72"/>
                  <code value="80296-7"/>
                  <display value="Patient Mood"/>
               </coding>
               <coding>
                  <system value="http://snomed.info/sct"/>
                  <version value="http://snomed.info/sct/900000000000207008/version/20220331"/>
                  <code value="1155968006"/>
                  <display value="Mood (observable entity)"/>
               </coding>
            </code>
            <subject>
               <reference value="urn:uuid:3071accc-8240-4b87-8118-9128c9c783cb"/>
            </subject>
            <effectiveDateTime value="2023-03-01T15:42:36+00:00"/>
            <valueString value="Bad"/>
         </Observation>
      </resource>
   </entry>
   <entry>
      <fullUrl value="urn:uuid:d7d5da28-ccd3-423d-b961-f032c17309ba"/>
      <resource>
         <Observation xmlns="http://hl7.org/fhir">
            <id value="d7d5da28-ccd3-423d-b961-f032c17309ba"/>
            <meta>
               <versionId value="1"/>
               <lastUpdated value="2023-03-01T15:42:14.049+00:00"/>
               <profile value="https://fhir.kbv.de/StructureDefinition/KBV_PR_MIO_DIGA_Observation_Body_Weight|1.1.0"/>
            </meta>
            <identifier>
               <type>
                  <coding>
                     <system value="http://terminology.hl7.org/CodeSystem/v2-0203"/>
                     <version value="2.9"/>
                     <code value="RI"/>
                     <display value="Resource identifier"/>
                  </coding>
               </type>
               <system value="urn:ietf:rfc:3986"/>
               <value value="urn:uuid:d7d5da28-ccd3-423d-b961-f032c17309ba"/>
            </identifier>
            <status value="final"/>
            <category>
               <coding>
                  <system value="http://terminology.hl7.org/CodeSystem/observation-category"/>
                  <code value="vital-signs"/>
               </coding>
            </category>
            <code>
               <coding>
                  <system value="http://loinc.org"/>
                  <version value="2.72"/>
                  <code value="29463-7"/>
                  <display value="Body weight"/>
               </coding>
               <coding>
                  <system value="http://snomed.info/sct"/>
                  <version value="http://snomed.info/sct/900000000000207008/version/20220331"/>
                  <code value="27113001"/>
                  <display value="Body weight (observable entity)"/>
               </coding>
            </code>
            <subject>
               <reference value="urn:uuid:3071accc-8240-4b87-8118-9128c9c783cb"/>
            </subject>
            <effectiveDateTime value="2023-03-01T15:42:13+00:00"/>
            <valueQuantity>
               <value value="80.00000000"/>
               <unit value="kg"/>
               <system value="http://unitsofmeasure.org"/>
               <code value="kg"/>
            </valueQuantity>
         </Observation>
      </resource>
   </entry>
   <entry>
      <fullUrl value="urn:uuid:c19bc648-26c3-41ef-8e96-d3a92feaf0b1"/>
      <resource>
         <Observation xmlns="http://hl7.org/fhir">
            <id value="c19bc648-26c3-41ef-8e96-d3a92feaf0b1"/>
            <meta>
               <versionId value="1"/>
               <lastUpdated value="2023-03-01T15:44:12.114+00:00"/>
               <profile value="https://fhir.kbv.de/StructureDefinition/KBV_PR_MIO_DIGA_Observation_Free|1.1.0"/>
            </meta>
            <identifier>
               <type>
                  <coding>
                     <system value="http://terminology.hl7.org/CodeSystem/v2-0203"/>
                     <version value="2.9"/>
                     <code value="RI"/>
                     <display value="Resource identifier"/>
                  </coding>
               </type>
               <system value="urn:ietf:rfc:3986"/>
               <value value="urn:uuid:c19bc648-26c3-41ef-8e96-d3a92feaf0b1"/>
            </identifier>
            <status value="final"/>
            <category>
               <coding>
                  <system value="http://terminology.hl7.org/CodeSystem/observation-category"/>
                  <code value="vital-signs"/>
               </coding>
            </category>
            <code>
               <coding>
                  <system value="http://loinc.org"/>
                  <version value="2.72"/>
                  <code value="56117-5"/>
                  <display value="Waist Circumference by WHI"/>
               </coding>
               <coding>
                  <system value="http://snomed.info/sct"/>
                  <version value="http://snomed.info/sct/900000000000207008/version/20220331"/>
                  <code value="276361009"/>
                  <display value="Waist circumference (observable entity)"/>
               </coding>
            </code>
            <subject>
               <reference value="urn:uuid:3071accc-8240-4b87-8118-9128c9c783cb"/>
            </subject>
            <effectiveDateTime value="2023-03-01T15:44:12+00:00"/>
            <valueQuantity>
               <value value="99.00000000"/>
               <unit value="cm"/>
               <system value="http://unitsofmeasure.org"/>
               <code value="cm"/>
            </valueQuantity>
         </Observation>
      </resource>
   </entry>
   <entry>
      <fullUrl value="urn:uuid:0bb75740-165e-4501-8264-81fae14244e0"/>
      <resource>
         <Observation xmlns="http://hl7.org/fhir">
            <id value="0bb75740-165e-4501-8264-81fae14244e0"/>
            <meta>
               <versionId value="1"/>
               <lastUpdated value="2023-03-01T15:44:17.966+00:00"/>
               <profile value="https://fhir.kbv.de/StructureDefinition/KBV_PR_MIO_DIGA_Observation_Score_or_Assessment_by_Scale|1.1.0"/>
            </meta>
            <identifier>
               <type>
                  <coding>
                     <system value="http://terminology.hl7.org/CodeSystem/v2-0203"/>
                     <version value="2.9"/>
                     <code value="RI"/>
                     <display value="Resource identifier"/>
                  </coding>
               </type>
               <system value="urn:ietf:rfc:3986"/>
               <value value="urn:uuid:0bb75740-165e-4501-8264-81fae14244e0"/>
            </identifier>
            <status value="final"/>
            <code>
               <coding>
                  <system value="http://snomed.info/sct"/>
                  <version value="http://snomed.info/sct/900000000000207008/version/20220331"/>
                  <code value="443172007"/>
                  <display value="Bristol stool form score (observable entity)"/>
               </coding>
            </code>
            <subject>
               <reference value="urn:uuid:3071accc-8240-4b87-8118-9128c9c783cb"/>
            </subject>
            <effectiveDateTime value="2023-03-01T15:44:17+00:00"/>
            <valueQuantity>
               <value value="2.000000000"/>
               <unit value="{score}"/>
               <system value="http://unitsofmeasure.org"/>
               <code value="{score}"/>
            </valueQuantity>
         </Observation>
      </resource>
   </entry>
   <entry>
      <fullUrl value="urn:uuid:9fbb10e8-941f-40a2-8678-0f6377aa610c"/>
      <resource>
         <Observation xmlns="http://hl7.org/fhir">
            <id value="9fbb10e8-941f-40a2-8678-0f6377aa610c"/>
            <meta>
               <versionId value="1"/>
               <lastUpdated value="2023-03-01T15:43:28.515+00:00"/>
               <profile value="https://fhir.kbv.de/StructureDefinition/KBV_PR_MIO_DIGA_Observation_Blood_Pressure|1.1.0"/>
            </meta>
            <identifier>
               <type>
                  <coding>
                     <system value="http://terminology.hl7.org/CodeSystem/v2-0203"/>
                     <version value="2.9"/>
                     <code value="RI"/>
                     <display value="Resource identifier"/>
                  </coding>
               </type>
               <system value="urn:ietf:rfc:3986"/>
               <value value="urn:uuid:9fbb10e8-941f-40a2-8678-0f6377aa610c"/>
            </identifier>
            <status value="final"/>
            <category>
               <coding>
                  <system value="http://terminology.hl7.org/CodeSystem/observation-category"/>
                  <code value="vital-signs"/>
               </coding>
            </category>
            <code>
               <coding>
                  <system value="http://loinc.org"/>
                  <version value="2.72"/>
                  <code value="85354-9"/>
                  <display value="Blood pressure panel with all children optional"/>
               </coding>
               <coding>
                  <system value="http://snomed.info/sct"/>
                  <version value="http://snomed.info/sct/900000000000207008/version/20220331"/>
                  <code value="75367002"/>
                  <display value="Blood pressure (observable entity)"/>
               </coding>
            </code>
            <subject>
               <reference value="urn:uuid:3071accc-8240-4b87-8118-9128c9c783cb"/>
            </subject>
            <effectiveDateTime value="2023-03-01T15:43:26+00:00"/>
            <component>
               <code>
                  <coding>
                     <system value="http://snomed.info/sct"/>
                     <version value="http://snomed.info/sct/900000000000207008/version/20220331"/>
                     <code value="271649006"/>
                     <display value="Systolic blood pressure (observable entity)"/>
                  </coding>
                  <coding>
                     <system value="http://loinc.org"/>
                     <version value="2.72"/>
                     <code value="8480-6"/>
                     <display value="Systolic blood pressure"/>
                  </coding>
                  <text value="systolisch"/>
               </code>
               <valueQuantity>
                  <value value="40"/>
                  <unit value="mm Hg"/>
                  <system value="http://unitsofmeasure.org"/>
                  <code value="mm[Hg]"/>
               </valueQuantity>
            </component>
            <component>
               <code>
                  <coding>
                     <system value="http://snomed.info/sct"/>
                     <version value="http://snomed.info/sct/900000000000207008/version/20220331"/>
                     <code value="271650006"/>
                     <display value="Diastolic blood pressure (observable entity)"/>
                  </coding>
                  <coding>
                     <system value="http://loinc.org"/>
                     <version value="2.72"/>
                     <code value="8462-4"/>
                     <display value="Diastolic blood pressure"/>
                  </coding>
                  <text value="diastolisch"/>
               </code>
               <valueQuantity>
                  <value value="40"/>
                  <unit value="mm Hg"/>
                  <system value="http://unitsofmeasure.org"/>
                  <code value="mm[Hg]"/>
               </valueQuantity>
            </component>
            <component>
               <code>
                  <coding>
                     <system value="http://snomed.info/sct"/>
                     <version value="http://snomed.info/sct/900000000000207008/version/20220331"/>
                     <code value="6797001"/>
                     <display value="Mean blood pressure (observable entity)"/>
                  </coding>
                  <coding>
                     <system value="http://loinc.org"/>
                     <version value="2.72"/>
                     <code value="8478-0"/>
                     <display value="Mean blood pressure"/>
                  </coding>
                  <text value="mittlerer"/>
               </code>
               <valueQuantity>
                  <value value="40"/>
                  <unit value="mm Hg"/>
                  <system value="http://unitsofmeasure.org"/>
                  <code value="mm[Hg]"/>
               </valueQuantity>
            </component>
         </Observation>
      </resource>
   </entry>
</Bundle>
""";
}
