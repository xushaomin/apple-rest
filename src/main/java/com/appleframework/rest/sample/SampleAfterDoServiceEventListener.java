/**
 * 版权声明：中图一购网络科技有限公司 版权所有 违者必究 2012 
 * 日    期：12-6-2
 */
package com.appleframework.rest.sample;

import com.appleframework.rest.RestRequestContext;
import com.appleframework.rest.event.AfterDoServiceEvent;
import com.appleframework.rest.event.RestEventListener;
import com.appleframework.rest.marshaller.MessageMarshallerUtils;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <pre>
 * 功能说明：
 * </pre>
 *
 * @author 陈雄华
 * @version 1.0
 */
public class SampleAfterDoServiceEventListener implements RestEventListener<AfterDoServiceEvent> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void onRestEvent(AfterDoServiceEvent restEvent) {
        RestRequestContext restRequestContext = restEvent.getRestRequestContext();
        if(restRequestContext != null){
            Map<String,String> allParams = restRequestContext.getAllParams();
            String message = MessageMarshallerUtils.asUrlString(allParams);
            logger.info("message("+restEvent.getServiceEndTime()+")"+message);
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }
}

