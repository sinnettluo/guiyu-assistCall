package com.guiji.botsentence.util.enums;

public enum TtsModelEnum {
    MH("mh", "毛慧"),
    SZJ("szj", "桑竹娟"),
    ZHAO_HANG("zhaohang", "招行模型")
    ;

    private String key;

    private String desc;

    TtsModelEnum(String key, String desc) {
        this.key = key;
        this.desc = desc;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
