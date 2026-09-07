export const menuPermissions = {
  "/dashboard": ["ADMIN", "SALES_MANAGER", "SALES_REP", "SERVICE_AGENT"],
  "/leads": ["ADMIN", "SALES_MANAGER", "SALES_REP"],
  "/accounts": ["ADMIN", "SALES_MANAGER", "SALES_REP", "SERVICE_AGENT"],
  "/contacts": ["ADMIN", "SALES_MANAGER", "SALES_REP", "SERVICE_AGENT"],
  "/opportunities": ["ADMIN", "SALES_MANAGER", "SALES_REP"],
  "/activities": ["ADMIN", "SALES_MANAGER", "SALES_REP", "SERVICE_AGENT"],
  "/cases": ["ADMIN", "SALES_MANAGER", "SALES_REP", "SERVICE_AGENT"],
  "/knowledge": ["ADMIN", "SALES_MANAGER", "SALES_REP", "SERVICE_AGENT"],
  "/products": ["ADMIN", "SALES_MANAGER", "SALES_REP"],
  "/pricebooks": ["ADMIN", "SALES_MANAGER", "SALES_REP"],
  "/quotes": ["ADMIN", "SALES_MANAGER", "SALES_REP"],
  "/approvals": ["ADMIN", "SALES_MANAGER"],
  "/users": ["ADMIN"],
};

export const canSee = (path, role) =>
  Boolean(role && menuPermissions[path]?.includes(role));

export const currentUser = () => {
  try {
    return JSON.parse(localStorage.getItem("crm_user") || "null");
  } catch {
    return null;
  }
};
