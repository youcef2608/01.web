import { createFileRoute, Link } from "@tanstack/react-router";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { toast } from "sonner";
import { AlertTriangle, ArrowLeft, Lightbulb, Plus, Sparkles } from "lucide-react";

import { AppHeader } from "@/components/AppHeader";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { supabase } from "@/integrations/supabase/client";
import { CATEGORIES, INITIATIVE_STATUSES, MEMORY_LABEL, statusLabel } from "@/lib/athar";

export const Route = createFileRoute("/_authenticated/dashboard")({
  head: () => ({
    meta: [
      { title: "لوحة التحكم — أثر" },
      { name: "description", content: "أدر مبادراتك التطوعية وذاكرتها في منصة أثر." },
      { property: "og:title", content: "لوحة التحكم — أثر" },
      { property: "og:description", content: "أدر مبادراتك التطوعية وذاكرتها في منصة أثر." },
    ],
  }),
  component: Dashboard,
});

const selectClass =
  "h-10 w-full rounded-md border border-input bg-background px-3 text-sm outline-none focus-visible:ring-2 focus-visible:ring-ring";

function Dashboard() {
  const { user } = Route.useRouteContext();
  const queryClient = useQueryClient();
  const [open, setOpen] = useState(false);
  const [form, setForm] = useState({
    name: "",
    category: CATEGORIES[0],
    goal: "",
    description: "",
    location: "",
    date: "",
  });

  const initiatives = useQuery({
    queryKey: ["my-initiatives", user.id],
    queryFn: async () => {
      const { data, error } = await supabase
        .from("initiatives")
        .select("*")
        .eq("creator_id", user.id)
        .order("created_at", { ascending: false });
      if (error) throw error;
      return data;
    },
  });

  // Real "wow": lessons and risks recorded on earlier initiatives of the same category.
  const priorKnowledge = useQuery({
    queryKey: ["prior-knowledge", form.category],
    enabled: open && !!form.category,
    queryFn: async () => {
      const { data: prior, error: e1 } = await supabase
        .from("initiatives")
        .select("id, name")
        .eq("category", form.category);
      if (e1) throw e1;
      if (!prior || prior.length === 0) return [];
      const names = new Map(prior.map((p) => [p.id, p.name]));
      const { data, error } = await supabase
        .from("initiative_memory")
        .select("id, type, text, initiative_id")
        .in(
          "initiative_id",
          prior.map((p) => p.id),
        )
        .in("type", ["lesson", "problem"])
        .order("created_at", { ascending: false })
        .limit(8);
      if (error) throw error;
      return (data ?? []).map((m) => ({ ...m, initiativeName: names.get(m.initiative_id) ?? "" }));
    },
  });

  const createInitiative = useMutation({
    mutationFn: async () => {
      const { data, error } = await supabase
        .from("initiatives")
        .insert({
          name: form.name,
          category: form.category,
          goal: form.goal || null,
          description: form.description || null,
          location: form.location || null,
          date: form.date || null,
          creator_id: user.id,
        })
        .select()
        .single();
      if (error) throw error;
      return data;
    },
    onSuccess: () => {
      toast.success("تم إنشاء المبادرة");
      setOpen(false);
      setForm({
        name: "",
        category: CATEGORIES[0],
        goal: "",
        description: "",
        location: "",
        date: "",
      });
      queryClient.invalidateQueries({ queryKey: ["my-initiatives"] });
    },
    onError: (e: Error) => toast.error(e.message),
  });

  return (
    <div className="min-h-screen bg-background">
      <AppHeader email={user.email} />

      <main className="mx-auto max-w-6xl px-5 py-10">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div>
            <h1 className="font-display text-3xl font-bold">مبادراتي</h1>
            <p className="mt-1 text-muted-foreground">كل مبادرة تترك معرفة للمبادرة القادمة.</p>
          </div>
          <Button onClick={() => setOpen((v) => !v)}>
            <Plus className="h-4 w-4" />
            مبادرة جديدة
          </Button>
        </div>

        {open && (
          <div className="mt-6 grid gap-5 lg:grid-cols-[1.4fr_1fr]">
            <form
              className="rounded-2xl border border-border bg-card p-6 shadow-sm"
              onSubmit={(e) => {
                e.preventDefault();
                createInitiative.mutate();
              }}
            >
              <h2 className="font-display text-xl font-bold">إنشاء مبادرة</h2>
              <div className="mt-5 grid gap-4 sm:grid-cols-2">
                <div className="space-y-2 sm:col-span-2">
                  <Label htmlFor="name">اسم المبادرة</Label>
                  <Input
                    id="name"
                    required
                    value={form.name}
                    onChange={(e) => setForm({ ...form, name: e.target.value })}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="category">الفئة</Label>
                  <select
                    id="category"
                    className={selectClass}
                    value={form.category}
                    onChange={(e) =>
                      setForm({ ...form, category: e.target.value as (typeof CATEGORIES)[number] })
                    }
                  >
                    {CATEGORIES.map((c) => (
                      <option key={c} value={c}>
                        {c}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="space-y-2">
                  <Label htmlFor="date">التاريخ</Label>
                  <Input
                    id="date"
                    type="date"
                    value={form.date}
                    onChange={(e) => setForm({ ...form, date: e.target.value })}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="location">المكان</Label>
                  <Input
                    id="location"
                    value={form.location}
                    onChange={(e) => setForm({ ...form, location: e.target.value })}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="goal">الهدف</Label>
                  <Input
                    id="goal"
                    value={form.goal}
                    onChange={(e) => setForm({ ...form, goal: e.target.value })}
                  />
                </div>
                <div className="space-y-2 sm:col-span-2">
                  <Label htmlFor="description">الوصف</Label>
                  <Textarea
                    id="description"
                    rows={3}
                    value={form.description}
                    onChange={(e) => setForm({ ...form, description: e.target.value })}
                  />
                </div>
              </div>
              <div className="mt-5 flex gap-2">
                <Button type="submit" disabled={createInitiative.isPending}>
                  {createInitiative.isPending ? "جارٍ الحفظ..." : "حفظ المبادرة"}
                </Button>
                <Button type="button" variant="ghost" onClick={() => setOpen(false)}>
                  إلغاء
                </Button>
              </div>
            </form>

            <aside className="rounded-2xl border border-clay/30 bg-sand/70 p-6">
              <p className="flex items-center gap-2 text-sm font-medium text-clay">
                <Sparkles className="h-4 w-4" /> تعلّمنا من مبادرة سابقة
              </p>
              <h3 className="mt-2 font-display text-lg font-bold">
                دروس ومخاطر في فئة «{form.category}»
              </h3>
              <div className="mt-4 space-y-3">
                {priorKnowledge.isLoading && (
                  <p className="text-sm text-muted-foreground">جارٍ البحث في الذاكرة...</p>
                )}
                {priorKnowledge.data?.length === 0 && (
                  <p className="text-sm text-muted-foreground">
                    لا توجد بعد ذاكرة مسجّلة في هذه الفئة. مبادرتك ستكون البداية.
                  </p>
                )}
                {priorKnowledge.data?.map((m) => (
                  <div key={m.id} className="flex gap-3 rounded-xl border border-border bg-card p-3">
                    <span
                      className={`grid h-8 w-8 shrink-0 place-items-center rounded-lg ${
                        m.type === "problem"
                          ? "bg-destructive/10 text-destructive"
                          : "bg-accent text-clay"
                      }`}
                    >
                      {m.type === "problem" ? (
                        <AlertTriangle className="h-4 w-4" />
                      ) : (
                        <Lightbulb className="h-4 w-4" />
                      )}
                    </span>
                    <div>
                      <p className="text-xs text-muted-foreground">
                        {MEMORY_LABEL[m.type as "lesson" | "problem"]} · {m.initiativeName}
                      </p>
                      <p className="mt-0.5 text-sm leading-relaxed">{m.text}</p>
                    </div>
                  </div>
                ))}
              </div>
            </aside>
          </div>
        )}

        <div className="mt-8 grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {initiatives.isLoading && <p className="text-muted-foreground">جارٍ التحميل...</p>}
          {initiatives.data?.length === 0 && !open && (
            <div className="rounded-2xl border border-dashed border-border p-8 text-center md:col-span-2 lg:col-span-3">
              <p className="text-muted-foreground">لا توجد مبادرات بعد. ابدأ أول مبادرة لك.</p>
            </div>
          )}
          {initiatives.data?.map((i) => (
            <Link
              key={i.id}
              to="/initiatives/$id"
              params={{ id: i.id }}
              className="group rounded-2xl border border-border bg-card p-5 shadow-sm transition-shadow hover:shadow-md"
            >
              <div className="flex items-start justify-between gap-2">
                <h3 className="font-display text-lg font-bold">{i.name}</h3>
                <span className="rounded-full bg-accent px-2 py-0.5 text-xs text-accent-foreground">
                  {statusLabel(INITIATIVE_STATUSES, i.status)}
                </span>
              </div>
              <p className="mt-1 text-sm text-clay">{i.category}</p>
              {i.goal && <p className="mt-2 text-sm text-muted-foreground">{i.goal}</p>}
              <p className="mt-4 flex items-center gap-1 text-sm font-medium text-primary">
                فتح المبادرة
                <ArrowLeft className="h-4 w-4 transition-transform group-hover:-translate-x-1" />
              </p>
            </Link>
          ))}
        </div>
      </main>
    </div>
  );
}
