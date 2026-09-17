import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import { useEffect, useState } from "react";
import { toast } from "sonner";
import { Network } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { supabase } from "@/integrations/supabase/client";

type Search = { mode?: "signin" | "signup" };

export const Route = createFileRoute("/auth")({
  ssr: false,
  validateSearch: (search: Record<string, unknown>): Search => ({
    mode: search['mode'] === "signup" ? "signup" : "signin",
  }),
  head: () => ({
    meta: [
      { title: "الدخول إلى أثر" },
      { name: "description", content: "سجّل الدخول أو أنشئ حسابًا في منصة أثر للعمل التطوعي." },
      { property: "og:title", content: "الدخول إلى أثر" },
      { property: "og:description", content: "سجّل الدخول أو أنشئ حسابًا في منصة أثر." },
    ],
  }),
  component: AuthPage,
});

function AuthPage() {
  const { mode } = Route.useSearch();
  const navigate = useNavigate();
  const [isSignup, setIsSignup] = useState(mode === "signup");
  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    supabase.auth.getSession().then(({ data }) => {
      if (data.session) navigate({ to: "/dashboard", replace: true });
    });
  }, [navigate]);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      if (isSignup) {
        const { data, error } = await supabase.auth.signUp({
          email,
          password,
          options: {
            emailRedirectTo: window.location.origin,
            data: { full_name: fullName },
          },
        });
        if (error) throw error;
        if (data.session) {
          toast.success("تم إنشاء حسابك");
          navigate({ to: "/dashboard", replace: true });
        } else {
          toast.success("تحقّق من بريدك الإلكتروني لتأكيد الحساب");
        }
      } else {
        const { error } = await supabase.auth.signInWithPassword({ email, password });
        if (error) throw error;
        toast.success("أهلاً بعودتك");
        navigate({ to: "/dashboard", replace: true });
      }
    } catch (err) {
      toast.error(err instanceof Error ? err.message : "تعذّر إتمام العملية");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-sand/60 px-5 py-12">
      <div className="w-full max-w-md">
        <Link to="/" className="mb-6 flex items-center justify-center gap-2">
          <span className="grid h-9 w-9 place-items-center rounded-xl bg-primary text-primary-foreground">
            <Network className="h-5 w-5" />
          </span>
          <span className="font-display text-xl font-bold">أثر</span>
        </Link>

        <div className="rounded-2xl border border-border bg-card p-7 shadow-sm">
          <h1 className="font-display text-2xl font-bold">
            {isSignup ? "إنشاء حساب جديد" : "تسجيل الدخول"}
          </h1>
          <p className="mt-1 text-sm text-muted-foreground">
            {isSignup ? "ابدأ أول مبادرة لك على أثر." : "أكمل من حيث توقفت."}
          </p>

          <form onSubmit={submit} className="mt-6 space-y-4">
            {isSignup && (
              <div className="space-y-2">
                <Label htmlFor="fullName">الاسم الكامل</Label>
                <Input
                  id="fullName"
                  value={fullName}
                  onChange={(e) => setFullName(e.target.value)}
                  placeholder="اسمك"
                  required
                />
              </div>
            )}
            <div className="space-y-2">
              <Label htmlFor="email">البريد الإلكتروني</Label>
              <Input
                id="email"
                type="email"
                dir="ltr"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="name@example.com"
                required
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="password">كلمة المرور</Label>
              <Input
                id="password"
                type="password"
                dir="ltr"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                minLength={6}
                required
              />
            </div>
            <Button type="submit" className="w-full" disabled={loading}>
              {loading ? "جارٍ المعالجة..." : isSignup ? "إنشاء الحساب" : "دخول"}
            </Button>
          </form>

          <button
            type="button"
            onClick={() => setIsSignup((v) => !v)}
            className="mt-5 w-full text-sm text-muted-foreground underline-offset-4 hover:text-primary hover:underline"
          >
            {isSignup ? "لديك حساب بالفعل؟ سجّل الدخول" : "ليس لديك حساب؟ أنشئ حسابًا"}
          </button>
        </div>
      </div>
    </div>
  );
}
