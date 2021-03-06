package com.cylan.jiafeigou.cache.db.view;

/**
 * Created by yanzhendong on 2017/3/1.
 */

public interface IDPEntity extends IEntity<IDPEntity> {

    IDPEntity setMsgId(Integer msgId);

    Integer getMsgId();

    IDPEntity setVersion(long version);

    long getVersion();

    IDPEntity setUuid(String uuid);

    String getUuid();

    byte[] getBytes();

    IDPEntity setBytes(byte[] bytes);

    IDPEntity setAccount(String account);

    String getAccount();

}
