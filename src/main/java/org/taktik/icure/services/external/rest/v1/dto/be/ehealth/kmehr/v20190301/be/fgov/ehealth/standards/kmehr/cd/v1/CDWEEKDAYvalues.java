//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.06.14 at 03:49:43 PM CEST 
//


package org.taktik.icure.services.external.rest.v1.dto.be.ehealth.kmehr.v20190301.be.fgov.ehealth.standards.kmehr.cd.v1;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CD-WEEKDAYvalues.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="CD-WEEKDAYvalues">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="sunday"/>
 *     &lt;enumeration value="monday"/>
 *     &lt;enumeration value="tuesday"/>
 *     &lt;enumeration value="wednesday"/>
 *     &lt;enumeration value="thursday"/>
 *     &lt;enumeration value="friday"/>
 *     &lt;enumeration value="saturday"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "CD-WEEKDAYvalues")
@XmlEnum
public enum CDWEEKDAYvalues {

    @XmlEnumValue("sunday")
    SUNDAY("sunday"),
    @XmlEnumValue("monday")
    MONDAY("monday"),
    @XmlEnumValue("tuesday")
    TUESDAY("tuesday"),
    @XmlEnumValue("wednesday")
    WEDNESDAY("wednesday"),
    @XmlEnumValue("thursday")
    THURSDAY("thursday"),
    @XmlEnumValue("friday")
    FRIDAY("friday"),
    @XmlEnumValue("saturday")
    SATURDAY("saturday");
    private final String value;

    CDWEEKDAYvalues(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static CDWEEKDAYvalues fromValue(String v) {
        for (CDWEEKDAYvalues c: CDWEEKDAYvalues.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
