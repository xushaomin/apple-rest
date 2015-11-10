/**
 *
 * 日    期：12-2-14
 */
package com.appleframework.rest.security;

/**
 * <pre>
 * 功能说明：
 * </pre>
 *
 * @author 陈雄华
 * @version 1.0
 */
public class SimpleMainError implements MainError {

	private Integer flag;

	private String msg;

	public SimpleMainError(Integer flag, String msg) {
		this.flag = flag;
		this.msg = msg;
	}

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
