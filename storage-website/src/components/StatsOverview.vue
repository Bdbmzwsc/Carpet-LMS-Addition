<script setup lang="ts">
import { useI18n } from "vue-i18n";

interface Props {
  storageCount: number;
  totalItemKinds: number;
  totalErrors: number;
}

defineProps<Props>();

const { t } = useI18n();
</script>

<template>
  <section class="grid gap-4 sm:grid-cols-3">
    <article class="rounded-xl border border-slate-800 bg-slate-900/70 p-4">
      <p class="text-xs uppercase tracking-wide text-slate-400">
        {{ t("stats.storageCount") }}
      </p>
      <p class="mt-2 text-2xl font-semibold text-cyan-300">
        {{ storageCount }}
      </p>
    </article>
    <article class="rounded-xl border border-slate-800 bg-slate-900/70 p-4">
      <p class="text-xs uppercase tracking-wide text-slate-400">
        {{ t("stats.itemKindsTotal") }}
      </p>
      <p class="mt-2 text-2xl font-semibold text-emerald-300">
        {{ totalItemKinds }}
      </p>
    </article>
    <article class="rounded-xl border border-slate-800 bg-slate-900/70 p-4">
      <p class="text-xs uppercase tracking-wide text-slate-400">
        {{ t("stats.errorPointsTotal") }}
      </p>
      <p class="mt-2 text-2xl font-semibold text-amber-300">
        {{ totalErrors }}
      </p>
    </article>
  </section>
</template>
