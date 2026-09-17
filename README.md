# ATHAR: Building on Experience

أنشئ موقع "أثر — ATHAR" كتطبيق مستقل وظيفي بالكامل (وليس مجرد صفحة تعريفية ثابتة)، متصل بـ Supabase لكل البيانات والمصادقة الحقيقية.

## الهوية والرسالة

الشعار: "لا تبدأ من الصفر."

الرسالة: كل مبادرة تطوعية تترك وراءها معرفة تُستخدم في المبادرة القادمة.

## الأسلوب البصري

- عربي RTL بالكامل

- الألوان: أخضر غابي داكن (#1F5E4C) أساسي، بيج/طيني دافئ (#B0703B) ثانوي، خلفية بيضاء مائلة للكريمي

- خطوط: Cairo للعناوين، Tajawal للنصوص

- تصميم SaaS حديث، بطاقات واضحة

## الصفحة التعريفية (Landing Page) — تعمل كبوابة دخول

### 1. Hero Section

- العنوان: "لا تبدأ من الصفر."

- نص فرعي: "منصة تجمع فرص التطوع، إدارة الفرق، وذاكرة كل تجربة"

- زرّان: "جرّب المنصة" (يوجّه لتسجيل الدخول/إنشاء حساب) و"شاهد كيف تعمل" (سكرول للأقسام التالية)

- **رسم توضيحي متحرك حقيقي (وليس صورة ثابتة)**: شجرة معرفة/دائرة مترابطة تنمو وتتحرك بصريًا (باستخدام CSS animations أو SVG animation) — أغصان أو عقد تظهر تباعًا بحركة ناعمة مستمرة، تعبيرًا عن تراكم المعرفة

### 2. المشكلة

"كل مبادرة تبدأ من نقطة الصفر" — شرح أن الخبرة تضيع مع نهاية كل مبادرة

### 3. خطوات العمل (4 خطوات بصرية)

أنشئ مبادرتك ← أدر فريقك ← سجّل ما يحدث ← AI يستخرج الدروس تلقائيًا

### 4. الميزات (6 بطاقات)

فرص تطوع، إدارة فريق ومهام، ذاكرة المبادرة، مساعد AI، تقارير تلقائية، ذاكرة جماعية

### 5. لحظة "الواو"

"تعلمنا من مبادرتك السابقة" — بطاقات دروس ومخاطر تظهر تدريجيًا عند التمرير (scroll animation)

### 6. Call to Action ختامي

"جرّب أثر الآن مجانًا" → يوجّه لتسجيل الدخول

### 7. Footer بسيط

## الوظائف الحقيقية خلف الأزرار (هذا الجزء الأهم)

### تفعيل Supabase

فعّل تكامل Supabase الأصلي في Lovable (Connect to Supabase).

### نظام تسجيل الدخول الحقيقي

- صفحتا "إنشاء حساب" و"تسجيل الدخول" عبر Supabase Auth (بريد + كلمة مرور)

- زرّا "جرّب أثر الآن" و"جرّب المنصة" يوجّهان فعليًا لهاتين الصفحتين

### قاعدة البيانات (أنشئ الجداول التالية في Supabase)

- `profiles` (id, full_name, email, skills, location, created_at)

- `initiatives` (id, name, category, goal, description, location, date, status, creator_id, created_at)

- `tasks` (id, initiative_id, name, category, status)

- `volunteer_requests` (id, initiative_id, name, skill, task, status)

- `initiative_memory` (id, initiative_id, type: 'decision'|'problem'|'solution'|'lesson', text, tag, created_at)

### لوحة التحكم الفعلية (بعد تسجيل الدخول)

وجّه المستخدم إلى Dashboard حقيقي متصل بـ Supabase يعرض بياناته الفعلية:

- إنشاء مبادرة جديدة (نموذج يحفظ في جدول initiatives فعليًا)

- عرض المبادرات النشطة الخاصة به من قاعدة البيانات

- إضافة مهام، ملاحظات، مشاكل، دروس — تُحفظ فعليًا في initiative_memory

- عند إنشاء مبادرة من نفس فئة مبادرة سابقة، اعرض تلقائيًا الدروس والمخاطر المرتبطة بها من قاعدة البيانات الحقيقية (هذه هي لحظة "الواو" الفعلية، وليست شرحًا فقط)

### الأمان

فعّل Row Level Security (RLS): كل مستخدم يرى/يعدل بياناته الخاصة فقط، مع قراءة عامة للمبادرات والفرص النشطة للجميع.

## مهم جدًا

هذا ليس عرضًا تسويقيًا فقط — كل زر ونموذج يجب أن يعمل فعليًا ويحفظ/يقرأ من Supabase. لا تترك أي وظيفة كـ placeholder أو محاكاة بصرية فقط.

This project was built with [Lovable](https://lovable.dev).

## Build with Lovable

Continue developing this project in the [Lovable editor](https://lovable.dev/projects/242c6683-1c8a-4d6d-9274-78b65f80ccbb).

- **Ship faster**: describe what you want to build and Lovable handles the code.
- **Stay in sync**: every change made in Lovable is committed straight to this repository.
- **Full ownership**: this code is yours. Push to `main` on GitHub and your changes sync back into Lovable, ready for your next prompt.

## التشغيل على حاسوب جديد

المتطلبات: Node.js 20 أو أحدث و npm.

```powershell
git clone https://github.com/youcef2608/01.web.git
cd 01.web
npm install
Copy-Item .env.example .env
npm run dev
```

افتح الرابط الذي يظهر في الطرفية، غالبًا `http://localhost:5173`.

### إعداد Supabase وResend

ضع القيم الحقيقية في `.env` المحلي فقط. لا ترفع هذا الملف إلى GitHub.

```env
VITE_SUPABASE_URL=https://your-project.supabase.co
VITE_SUPABASE_PUBLISHABLE_KEY=your-publishable-key
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_SERVICE_ROLE_KEY=your-service-role-key
RESEND_API_KEY=your-resend-api-key
RESEND_FROM_EMAIL=أثر <verified@example.com>
```

طبّق ملفات SQL الموجودة في `drizzle/migrations` على مشروع Supabase بالترتيب. يجب أن يكون عنوان المرسل في Resend موثقًا. فعّل جلسات البريد في Supabase حتى يستطيع المستخدم إكمال تحقق OTP المخصص داخل أثر.

لا تحتاج إلى إنشاء مشروع Supabase جديد عند استخدام حاسوب آخر؛ استخدم نفس `SUPABASE_URL` والمفاتيح، وستبقى البيانات والمستخدمون كما هي.

لإنشاء نسخة إنتاجية:

```powershell
npm run build
npm run preview
```
