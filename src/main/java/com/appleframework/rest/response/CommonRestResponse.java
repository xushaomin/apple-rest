/**
 *
 * 日    期：12-2-22
 */
package com.appleframework.rest.response;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * <pre>
 *    通用的响应对象
 * </pre>
 *
 * @author 陈雄华
 * @version 1.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "response")
public class CommonRestResponse{

    @XmlAttribute
    private boolean successful = false;

    public static final CommonRestResponse SUCCESSFUL_RESPONSE = new CommonRestResponse(true);
    public static final CommonRestResponse FAILURE_RESPONSE = new CommonRestResponse(false);

    public CommonRestResponse() {
    }

    private CommonRestResponse(boolean successful) {
        this.successful = successful;
    }

    public boolean isSuccessful() {
        return successful;
    }
}

