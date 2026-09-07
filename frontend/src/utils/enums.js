export const maps = {
  leadStatus: {
    NEW: "新建",
    CONTACTED: "已联系",
    QUALIFIED: "已确认",
    UNQUALIFIED: "不合格",
    CONVERTED: "已转化",
  },
  leadSource: {
    WEB: "网站",
    PHONE: "电话",
    REFERRAL: "转介绍",
    EVENT: "活动",
    PARTNER: "合作伙伴",
    OTHER: "其他",
  },
  stage: {
    QUALIFICATION: "资格审查",
    NEEDS_ANALYSIS: "需求分析",
    PROPOSAL: "方案报价",
    NEGOTIATION: "商务谈判",
    CLOSED_WON: "赢单",
    CLOSED_LOST: "丢单",
  },
  caseStatus: {
    NEW: "新建",
    IN_PROGRESS: "处理中",
    PENDING_CUSTOMER: "等待客户",
    ESCALATED: "已升级",
    RESOLVED: "已解决",
    CLOSED: "已关闭",
  },
  priority: {
    LOW: "低",
    MEDIUM: "中",
    HIGH: "高",
    URGENT: "紧急",
  },
  activityStatus: {
    PLANNED: "计划中",
    DONE: "已完成",
    CANCELLED: "已取消",
  },
  articleStatus: {
    DRAFT: "草稿",
    PUBLISHED: "已发布",
    ARCHIVED: "已归档",
  },
};

export const colors = {
  NEW: "info",
  CONTACTED: "warning",
  QUALIFIED: "success",
  CONVERTED: "success",
  CLOSED_WON: "success",
  CLOSED_LOST: "danger",
  RESOLVED: "success",
  CLOSED: "info",
  URGENT: "danger",
  HIGH: "warning",
  MEDIUM: "",
  LOW: "info",
  DONE: "success",
  PUBLISHED: "success",
  DRAFT: "warning",
};

export const text = (map, value) => map[value] || value || "-";
export const tagType = (value) => colors[value] || "";
