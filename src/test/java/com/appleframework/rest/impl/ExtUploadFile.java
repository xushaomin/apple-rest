/**
 *  
 * 日    期：12-8-1
 */
package com.appleframework.rest.impl;

import com.appleframework.rest.request.UploadFile;

import java.io.File;

/**
 * <pre>
 * 功能说明：
 * </pre>
 *
 * @author 陈雄华
 * @version 1.0
 */
public class ExtUploadFile extends UploadFile {
    public ExtUploadFile(String fileType, byte[] content) {
        super(fileType, content);
    }

    public ExtUploadFile(File file) {
        super(file);
    }
}

