package com.contact.index.sidebar.model;

import java.io.Serializable;

/**
 * author:feng.G
 * time:  2019-05-28 15:25
 * desc:
 */
public class City extends BaseIndexPinyinBean implements Serializable {
    private String userId;
    private String name;
    private boolean isTop;//是否是最上面的 不需要被转化成拼音的

    public boolean isTop() {
        return isTop;
    }

    public void setTop(boolean top) {
        isTop = top;
    }

    @Override
    public String getTarget() {
        return name;
    }

    @Override
    public boolean isNeedToPinyin() {
        return !isTop;
    }


    @Override
    public boolean isShowSuspension() {
        return !isTop;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
