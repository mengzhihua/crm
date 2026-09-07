package com.mengzhihua.crm.common;

public final class Enums {
    private Enums() {}
    public enum LeadSource { WEB, PHONE, REFERRAL, EVENT, PARTNER, OTHER }
    public enum LeadStatus { NEW, CONTACTED, QUALIFIED, UNQUALIFIED, CONVERTED }
    public enum Rating { HOT, WARM, COLD }
    public enum AccountType { PROSPECT, CUSTOMER, PARTNER }
    public enum AccountLevel { A, B, C }
    public enum OpportunityStage {
        QUALIFICATION(10), NEEDS_ANALYSIS(20), PROPOSAL(50), NEGOTIATION(75), CLOSED_WON(100), CLOSED_LOST(0);
        private final int probability;
        OpportunityStage(int p) { probability = p; }
        public int getProbability() { return probability; }
    }
    public enum ActivityType { CALL, MEETING, EMAIL, TASK }
    public enum RelatedType { LEAD, ACCOUNT, CONTACT, OPPORTUNITY, CASE }
    public enum ActivityStatus { PLANNED, DONE, CANCELLED }
    public enum CaseType { QUESTION, PROBLEM, FEATURE_REQUEST, COMPLAINT }
    public enum CasePriority { LOW, MEDIUM, HIGH, URGENT }
    public enum CaseStatus { NEW, IN_PROGRESS, PENDING_CUSTOMER, ESCALATED, RESOLVED, CLOSED }
    public enum CaseOrigin { PHONE, EMAIL, WEB, WECHAT }
    public enum ArticleStatus { DRAFT, PUBLISHED, ARCHIVED }
}
