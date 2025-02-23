package com.zero.admin.base.encrypt.core.encryptor;

import com.zero.admin.base.encrypt.core.EncryptContext;
import com.zero.admin.base.encrypt.core.IEncryptor;

/**
 * 所有加密执行者的基类
 *
 * @author Akai
 */
public abstract class AbstractEncryptor implements IEncryptor {

    public AbstractEncryptor(EncryptContext context) {
        // 用户配置校验与配置注入
    }

}
