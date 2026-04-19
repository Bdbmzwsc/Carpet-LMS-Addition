import { createI18n } from "vue-i18n";

import { messages } from "@/i18n/messages";

export type AppLocale = "zh-CN" | "en-US";

export const STORAGE_WEBSITE_LOCALE_KEY = "storageWebsite.locale";

function normalizeBrowserLocale(language: string): AppLocale {
  return language.toLowerCase().startsWith("zh") ? "zh-CN" : "en-US";
}

function getInitialLocale(): AppLocale {
  if (typeof window === "undefined") {
    return "en-US";
  }

  const saved = window.localStorage.getItem(STORAGE_WEBSITE_LOCALE_KEY);
  if (saved === "zh-CN" || saved === "en-US") {
    return saved;
  }

  return normalizeBrowserLocale(window.navigator.language);
}

const i18n = createI18n({
  legacy: false,
  locale: getInitialLocale(),
  fallbackLocale: "en-US",
  messages,
});

export default i18n;
