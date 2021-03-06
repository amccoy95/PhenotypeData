
package org.mousephenotype.impress;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="isValidPipelineResult" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "isValidPipelineResult"
})
@XmlRootElement(name = "isValidPipelineResponse")
public class IsValidPipelineResponse {

    protected boolean isValidPipelineResult;

    /**
     * Gets the value of the isValidPipelineResult property.
     * 
     */
    public boolean isIsValidPipelineResult() {
        return isValidPipelineResult;
    }

    /**
     * Sets the value of the isValidPipelineResult property.
     * 
     */
    public void setIsValidPipelineResult(boolean value) {
        this.isValidPipelineResult = value;
    }

}
