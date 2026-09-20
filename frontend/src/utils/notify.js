import { ref } from "vue";
import { notifications } from "../api";

export const unreadCount = ref(0);

export const refreshUnread = async () => {
  try {
    unreadCount.value = await notifications.unreadCount();
  } catch {
    unreadCount.value = 0;
  }
  return unreadCount.value;
};
