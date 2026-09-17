export const CATEGORIES = [
  "تنظيف البيئة",
  "توزيع مساعدات",
  "تعليم ودعم دراسي",
  "صحة وتوعية",
  "إغاثة وطوارئ",
  "ثقافة وفنون",
  "تشجير وزراعة",
  "أخرى",
] as const;

export type MemoryType = "decision" | "problem" | "solution" | "lesson";

export const MEMORY_TYPES: { value: MemoryType; label: string; hint: string }[] = [
  { value: "decision", label: "قرار", hint: "قرار اتُّخذ أثناء المبادرة" },
  { value: "problem", label: "مشكلة", hint: "عائق أو خطر واجهكم" },
  { value: "solution", label: "حل", hint: "كيف تم تجاوز المشكلة" },
  { value: "lesson", label: "درس", hint: "خلاصة تفيد المبادرة القادمة" },
];

export const MEMORY_LABEL: Record<MemoryType, string> = {
  decision: "قرار",
  problem: "مشكلة",
  solution: "حل",
  lesson: "درس",
};

export const MEMORY_STYLE: Record<MemoryType, string> = {
  decision: "bg-primary/10 text-primary border-primary/20",
  problem: "bg-destructive/10 text-destructive border-destructive/20",
  solution: "bg-clay/10 text-clay border-clay/25",
  lesson: "bg-accent text-accent-foreground border-clay/20",
};

export const TASK_STATUSES = [
  { value: "todo", label: "لم تبدأ" },
  { value: "doing", label: "قيد التنفيذ" },
  { value: "done", label: "مكتملة" },
];

export const INITIATIVE_STATUSES = [
  { value: "active", label: "نشطة" },
  { value: "completed", label: "مكتملة" },
  { value: "planning", label: "قيد التخطيط" },
];

export function statusLabel(list: { value: string; label: string }[], value: string) {
  return list.find((s) => s.value === value)?.label ?? value;
}
