package com.sfq.ecosystem.model;

/**
 * 评估类型枚举
 * <p>
 * 用于标识和切换不同的评估对象，例如“协同”、“资源”和“服务”。
 */
public enum EvaluationType {
    XIETONG("协同"),
    ZIYUAN("资源"),
    FUWU("服务"); // 新增服务类型

    private final String description;

    EvaluationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}