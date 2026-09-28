import { ref } from "vue";
import { notifications } from "../api";

export const unreadCount = ref(0);
export const recentNotifications = ref([]);
let requestSequence = 0;

export const refreshNotifications = async () => {
  const sequence = ++requestSequence;
  try {
    const [count, list] = await Promise.all([
      notifications.unreadCount(),
      notifications.list({ page: 1, size: 5, unreadOnly: true }),
    ]);
    if (sequence === requestSequence) {
      unreadCount.value = count;
      recentNotifications.value = list.records || [];
    }
  } catch {
    if (sequence === requestSequence) {
      unreadCount.value = 0;
      recentNotifications.value = [];
    }
  }
  return unreadCount.value;
};

export const refreshUnread = refreshNotifications;
