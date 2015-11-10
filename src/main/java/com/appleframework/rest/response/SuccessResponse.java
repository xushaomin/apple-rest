/**
 *
 * 日    期：12-2-10
 */
package com.appleframework.rest.response;

import javax.xml.bind.annotation.*;

/**
 * <pre>
 * 功能说明：
 * </pre>
 * 
 * @author 陈雄华
 * @version 1.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "successResponse")
public class SuccessResponse {

    @XmlAttribute
    protected Integer flag = 1;

    @XmlElement
    protected String msg = "ok";

	public Integer getFlag() {
		return flag;
	}

	public void setFlag(Integer flag) {
		this.flag = flag;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
	
}
