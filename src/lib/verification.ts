import { createServerFn } from "@tanstack/react-start";
import { getRequest } from "@tanstack/react-start/server";
import { createClient } from "@supabase/supabase-js";

type VerificationInput = { email: string; code?: string };

function serverSupabase(token: string) {
  const url = process.env["SUPABASE_URL"];
  const key = process.env["SUPABASE_SERVICE_ROLE_KEY"];
  if (!url || !key) throw new Error("SUPABASE_SERVICE_ROLE_KEY غير مضبوط على الخادم");
  return createClient(url, key, { global: { headers: { Authorization: `Bearer ${token}` } }, auth: { persistSession: false, autoRefreshToken: false } });
}

function adminSupabase() {
  const url = process.env["SUPABASE_URL"];
  const key = process.env["SUPABASE_SERVICE_ROLE_KEY"];
  if (!url || !key) throw new Error("SUPABASE_SERVICE_ROLE_KEY غير مضبوط على الخادم");
  return createClient(url, key, { auth: { persistSession: false, autoRefreshToken: false } });
}

function hashCode(code: string) {
  let hash = 2166136261;
  for (const character of code) hash = Math.imul(hash ^ character.charCodeAt(0), 16777619);
  return (hash >>> 0).toString(16);
}

async function currentUser() {
  const authorization = getRequest()?.headers.get("authorization");
  if (!authorization?.startsWith("Bearer ")) throw new Error("يلزم تسجيل الدخول");
  const authClient = serverSupabase(authorization.slice(7));
  const { data, error } = await authClient.auth.getUser();
  if (error || !data.user) throw new Error("جلسة الدخول غير صالحة");
  return { supabase: adminSupabase(), user: data.user };
}

async function sendCode(email: string, code: string) {
  const apiKey = process.env["RESEND_API_KEY"];
  const from = process.env["RESEND_FROM_EMAIL"] ?? "أثر <onboarding@resend.dev>";
  if (!apiKey) throw new Error("RESEND_API_KEY غير مضبوط على الخادم");
  const response = await fetch("https://api.resend.com/emails", { method: "POST", headers: { Authorization: `Bearer ${apiKey}`, "Content-Type": "application/json" }, body: JSON.stringify({ from, to: [email], subject: "رمز التحقق من أثر — ATHAR", html: `<div dir="rtl" style="font-family:Arial,sans-serif;color:#1F5E4C"><h1>أثر — ATHAR</h1><p>رمز التحقق الخاص بك:</p><strong style="font-size:32px;letter-spacing:8px">${code}</strong><p>ينتهي هذا الرمز خلال 10 دقائق.</p></div>` }) });
  if (!response.ok) throw new Error("تعذر إرسال رسالة التحقق");
}

export const requestVerificationCode = createServerFn({ method: "POST" })
  .inputValidator((input: VerificationInput) => input)
  .handler(async ({ data }) => {
    const { supabase, user } = await currentUser();
    if (user.email?.toLowerCase() !== data.email.toLowerCase()) throw new Error("البريد لا يطابق الجلسة");
    const { data: previous } = await supabase.from("email_verification_codes").select("last_sent_at").eq("user_id", user.id).maybeSingle();
    if (previous && Date.now() - new Date(previous.last_sent_at).getTime() < 60_000) throw new Error("انتظر 60 ثانية قبل إعادة الإرسال");
    const code = Math.floor(100000 + Math.random() * 900000).toString();
    const { error } = await supabase.from("email_verification_codes").upsert({ user_id: user.id, code_hash: hashCode(code), expires_at: new Date(Date.now() + 10 * 60 * 1000).toISOString(), last_sent_at: new Date().toISOString() });
    if (error) throw error;
    await sendCode(user.email, code);
    return { sent: true };
  });

export const verifyCode = createServerFn({ method: "POST" })
  .inputValidator((input: VerificationInput) => input)
  .handler(async ({ data }) => {
    if (!data.code || !/^\d{6}$/.test(data.code)) throw new Error("أدخل رمزًا من 6 أرقام");
    const { supabase, user } = await currentUser();
    const { data: stored, error } = await supabase.from("email_verification_codes").select("code_hash, expires_at").eq("user_id", user.id).single();
    if (error || !stored || new Date(stored.expires_at) < new Date() || stored.code_hash !== hashCode(data.code)) throw new Error("رمز التحقق غير صحيح أو منتهي الصلاحية");
    const { error: profileError } = await supabase.from("profiles").update({ is_verified: true }).eq("id", user.id);
    if (profileError) throw profileError;
    await supabase.from("email_verification_codes").delete().eq("user_id", user.id);
    return { verified: true };
  });