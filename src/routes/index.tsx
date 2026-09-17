import { createFileRoute, Link } from "@tanstack/react-router";
import { useQuery } from "@tanstack/react-query";
import {
  ArrowLeft,
  Users,
  ListChecks,
  BrainCircuit,
  Sparkles,
  FileText,
  Network,
  AlertTriangle,
  Lightbulb,
} from "lucide-react";

import { Button } from "@/components/ui/button";
import { KnowledgeTree } from "@/components/KnowledgeTree";
import { Reveal } from "@/components/Reveal";
import { supabase } from "@/integrations/supabase/client";
import { MEMORY_LABEL, type MemoryType } from "@/lib/athar";

export const Route = createFileRoute("/")({
  head: () => ({
    meta: [
      { title: "أثر — لا تبدأ من الصفر" },
      {
        name: "description",
        content:
          "أثر منصة تجمع فرص التطوع، إدارة الفرق، وذاكرة كل تجربة، حتى لا تبدأ أي مبادرة من الصفر.",
      },
      { property: "og:title", content: "أثر — لا تبدأ من الصفر" },
      {
        property: "og:description",
        content: "منصة تجمع فرص التطوع، إدارة الفرق، وذاكرة كل تجربة.",
      },
    ],
  }),
  component: Landing,
});

const STEPS = [
  { title: "أنشئ مبادرتك", desc: "عرّف الهدف، الفئة، المكان والتاريخ." },
  { title: "أدر فريقك", desc: "مهام واضحة وطلبات تطوع منظمة." },
  { title: "سجّل ما يحدث", desc: "قرارات، مشاكل، حلول أثناء التنفيذ." },
  { title: "استخرج الدروس", desc: "الذاكرة تُعرض تلقائيًا للمبادرة القادمة." },
];

const FEATURES = [
  { icon: Users, title: "فرص تطوع", desc: "اعرض مبادرتك واستقبل طلبات المتطوعين بمهاراتهم." },
  { icon: ListChecks, title: "إدارة فريق ومهام", desc: "وزّع المهام وتابع حالتها لحظة بلحظة." },
  { icon: BrainCircuit, title: "ذاكرة المبادرة", desc: "كل قرار ومشكلة وحل محفوظ في مكان واحد." },
  { icon: Sparkles, title: "مساعد ذكي", desc: "يقترح الدروس والمخاطر المرتبطة بفئة مبادرتك." },
  { icon: FileText, title: "تقارير تلقائية", desc: "ملخص جاهز لما أُنجز وما تعلّمتموه." },
  { icon: Network, title: "ذاكرة جماعية", desc: "خبرة كل المبادرات تتراكم لصالح الجميع." },
];

function Landing() {
  const { data: memories } = useQuery({
    queryKey: ["public-memory-preview"],
    queryFn: async () => {
      const { data, error } = await supabase
        .from("initiative_memory")
        .select("id, type, text, tag, initiatives(name, category)")
        .in("type", ["lesson", "problem"])
        .order("created_at", { ascending: false })
        .limit(4);
      if (error) throw error;
      return data;
    },
  });

  const scrollToHow = () => {
    document.getElementById("how")?.scrollIntoView({ behavior: "smooth" });
  };

  return (
    <div className="min-h-screen bg-background">
      {/* Nav */}
      <header className="sticky top-0 z-40 border-b border-border/60 bg-background/80 backdrop-blur">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-5 py-4">
          <div className="flex items-center gap-2">
            <span className="grid h-9 w-9 place-items-center rounded-xl bg-primary text-primary-foreground">
              <Network className="h-5 w-5" />
            </span>
            <span className="font-display text-xl font-bold">أثر</span>
          </div>
          <nav className="flex items-center gap-2">
            <Button asChild variant="ghost">
              <Link to="/auth">تسجيل الدخول</Link>
            </Button>
            <Button asChild>
              <Link to="/auth" search={{ mode: "signup" }}>
                جرّب المنصة
              </Link>
            </Button>
          </nav>
        </div>
      </header>

      {/* Hero */}
      <section className="mx-auto grid max-w-6xl items-center gap-10 px-5 py-16 md:grid-cols-2 md:py-24">
        <div>
          <span className="inline-flex items-center gap-2 rounded-full border border-clay/30 bg-sand px-3 py-1 text-sm text-secondary-foreground">
            <Sparkles className="h-4 w-4" /> ذاكرة العمل التطوعي
          </span>
          <h1 className="mt-5 font-display text-4xl leading-tight font-black text-primary md:text-6xl">
            لا تبدأ من الصفر.
          </h1>
          <p className="mt-4 max-w-md text-lg text-muted-foreground">
            منصة تجمع فرص التطوع، إدارة الفرق، وذاكرة كل تجربة.
          </p>
          <div className="mt-8 flex flex-wrap gap-3">
            <Button asChild size="lg">
              <Link to="/auth" search={{ mode: "signup" }}>
                جرّب المنصة
                <ArrowLeft className="h-4 w-4" />
              </Link>
            </Button>
            <Button size="lg" variant="outline" onClick={scrollToHow}>
              شاهد كيف تعمل
            </Button>
          </div>
        </div>
        <KnowledgeTree />
      </section>

      {/* Problem */}
      <section className="border-y border-border/60 bg-sand/60">
        <div className="mx-auto max-w-3xl px-5 py-16 text-center">
          <Reveal>
            <h2 className="font-display text-3xl font-bold">كل مبادرة تبدأ من نقطة الصفر</h2>
            <p className="mt-4 leading-relaxed text-muted-foreground">
              ينتهي العمل التطوعي وتنتهي معه الخبرة: الملاحظات في مجموعات دردشة، القرارات في رؤوس
              المنظّمين، والأخطاء نفسها تتكرر في المبادرة التالية. ما يضيع ليس وقتًا فقط، بل معرفة
              كاملة دفع الفريق ثمنها.
            </p>
          </Reveal>
        </div>
      </section>

      {/* Steps */}
      <section id="how" className="mx-auto max-w-6xl scroll-mt-20 px-5 py-20">
        <Reveal>
          <h2 className="text-center font-display text-3xl font-bold">كيف تعمل أثر</h2>
        </Reveal>
        <div className="mt-10 grid gap-5 md:grid-cols-4">
          {STEPS.map((s, i) => (
            <Reveal key={s.title} delay={i * 90}>
              <div className="h-full rounded-2xl border border-border bg-card p-6 shadow-sm">
                <span className="grid h-10 w-10 place-items-center rounded-xl bg-primary font-display text-lg font-bold text-primary-foreground">
                  {i + 1}
                </span>
                <h3 className="mt-4 font-display text-lg font-bold">{s.title}</h3>
                <p className="mt-2 text-sm text-muted-foreground">{s.desc}</p>
              </div>
            </Reveal>
          ))}
        </div>
      </section>

      {/* Features */}
      <section className="bg-sand/60 py-20">
        <div className="mx-auto max-w-6xl px-5">
          <Reveal>
            <h2 className="text-center font-display text-3xl font-bold">ما تقدّمه المنصة</h2>
          </Reveal>
          <div className="mt-10 grid gap-5 md:grid-cols-3">
            {FEATURES.map((f, i) => (
              <Reveal key={f.title} delay={i * 80}>
                <div className="h-full rounded-2xl border border-border bg-card p-6 shadow-sm transition-shadow hover:shadow-md">
                  <span className="grid h-11 w-11 place-items-center rounded-xl bg-accent text-clay">
                    <f.icon className="h-5 w-5" />
                  </span>
                  <h3 className="mt-4 font-display text-lg font-bold">{f.title}</h3>
                  <p className="mt-2 text-sm leading-relaxed text-muted-foreground">{f.desc}</p>
                </div>
              </Reveal>
            ))}
          </div>
        </div>
      </section>

      {/* Wow */}
      <section className="mx-auto max-w-5xl px-5 py-20">
        <Reveal>
          <p className="text-center text-sm font-medium text-clay">لحظة أثر</p>
          <h2 className="mt-2 text-center font-display text-3xl font-bold">
            «تعلّمنا من مبادرتك السابقة»
          </h2>
          <p className="mx-auto mt-3 max-w-xl text-center text-muted-foreground">
            عند إنشاء مبادرة جديدة، تعرض أثر الدروس والمخاطر المسجّلة في مبادرات من نفس الفئة — من
            قاعدة بيانات حقيقية، لا من أمثلة.
          </p>
        </Reveal>

        <div className="mt-10 grid gap-4 md:grid-cols-2">
          {(memories && memories.length > 0
            ? memories.map((m) => ({
                type: m.type as MemoryType,
                text: m.text,
                tag:
                  (m.initiatives as { name?: string; category?: string } | null)?.category ??
                  m.tag ??
                  "",
              }))
            : [
                {
                  type: "lesson" as MemoryType,
                  text: "لا توجد بعد دروس مسجّلة — أول مبادرة تُنشئها تبدأ ببناء هذه الذاكرة.",
                  tag: "ابدأ الآن",
                },
                {
                  type: "problem" as MemoryType,
                  text: "الذاكرة الجماعية تنمو مع كل ملاحظة تسجّلها الفرق داخل المنصة.",
                  tag: "ذاكرة جماعية",
                },
              ]
          ).map((m, i) => (
            <Reveal key={i} delay={i * 120}>
              <div className="flex h-full gap-4 rounded-2xl border border-border bg-card p-5 shadow-sm">
                <span
                  className={`grid h-10 w-10 shrink-0 place-items-center rounded-xl ${
                    m.type === "problem" ? "bg-destructive/10 text-destructive" : "bg-accent text-clay"
                  }`}
                >
                  {m.type === "problem" ? (
                    <AlertTriangle className="h-5 w-5" />
                  ) : (
                    <Lightbulb className="h-5 w-5" />
                  )}
                </span>
                <div>
                  <p className="text-xs font-medium text-muted-foreground">
                    {MEMORY_LABEL[m.type]} {m.tag ? `· ${m.tag}` : ""}
                  </p>
                  <p className="mt-1 leading-relaxed">{m.text}</p>
                </div>
              </div>
            </Reveal>
          ))}
        </div>
      </section>

      {/* CTA */}
      <section className="bg-primary py-20 text-primary-foreground">
        <div className="mx-auto max-w-3xl px-5 text-center">
          <h2 className="font-display text-3xl font-bold md:text-4xl">جرّب أثر الآن مجانًا</h2>
          <p className="mt-3 text-primary-foreground/80">
            أنشئ حسابك وابدأ أول مبادرة — واترك أثرًا يستفيد منه من بعدك.
          </p>
          <Button asChild size="lg" variant="secondary" className="mt-8">
            <Link to="/auth" search={{ mode: "signup" }}>
              إنشاء حساب مجاني
              <ArrowLeft className="h-4 w-4" />
            </Link>
          </Button>
        </div>
      </section>

      <footer className="border-t border-border/60 bg-background py-8">
        <div className="mx-auto flex max-w-6xl flex-col items-center justify-between gap-3 px-5 text-sm text-muted-foreground md:flex-row">
          <span className="font-display font-bold text-foreground">أثر — ATHAR</span>
          <span>لا تبدأ من الصفر.</span>
          <span>© {new Date().getFullYear()} جميع الحقوق محفوظة</span>
        </div>
      </footer>
    </div>
  );
}
