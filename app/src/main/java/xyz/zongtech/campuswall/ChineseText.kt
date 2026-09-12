package xyz.zongtech.campuswall

fun displayState(value: String): String =
    when (value) {
        "pending" -> "待审核"
        "approved" -> "已通过"
        "rejected",
        "returned" -> "已退回"
        "awaiting_publication" -> "待发布"
        "visible",
        "published" -> "已公开"
        "hidden" -> "已隐藏"
        "deleted" -> "已删除"
        "draft" -> "草稿"
        "archived" -> "已归档"
        "active" -> "正常"
        "disabled" -> "已停用"
        "in_progress" -> "处理中"
        "resolved" -> "已解决"
        "closed" -> "已关闭"
        "user" -> "同学"
        "reviewer" -> "审核员"
        "admin" -> "管理员"
        "super_admin" -> "超级管理员"
        "spam" -> "垃圾信息"
        "abuse" -> "辱骂攻击"
        "porn" -> "不当内容"
        "rumor" -> "不实信息"
        "other" -> "其他"
        "true" -> "是"
        "false" -> "否"
        else -> value
    }
