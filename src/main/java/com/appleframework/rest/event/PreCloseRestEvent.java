/**
 * 版权声明： 版权所有 违者必究 2012
 * 日    期：12-6-5
 */
package com.appleframework.rest.event;

import com.appleframework.rest.RestContext;

/**
 * <pre>
 * 功能说明：
 * </pre>
 * 
 * @author 陈雄华
 * @version 1.0
 */
@SuppressWarnings("serial")
public class PreCloseRestEvent extends RestEvent {
	
	public PreCloseRestEvent(Object source, RestContext restContext) {
		super(source, restContext);
	}
}
