ALTER TABLE public.profiles
  ADD COLUMN IF NOT EXISTS phone text;

CREATE TABLE IF NOT EXISTS public.help_calls (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  title text NOT NULL,
  description text NOT NULL,
  category text NOT NULL DEFAULT 'other',
  urgency text NOT NULL DEFAULT 'medium',
  status text NOT NULL DEFAULT 'open',
  latitude double precision NOT NULL DEFAULT 0,
  longitude double precision NOT NULL DEFAULT 0,
  location_name text,
  requester_id uuid NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
  author_name text NOT NULL DEFAULT '',
  author_phone text,
  volunteer_id uuid REFERENCES auth.users(id) ON DELETE SET NULL,
  helper_name text,
  created_at timestamptz NOT NULL DEFAULT now(),
  completed_at timestamptz
);

GRANT SELECT ON public.help_calls TO anon, authenticated;
GRANT INSERT, UPDATE, DELETE ON public.help_calls TO authenticated;
ALTER TABLE public.help_calls ENABLE ROW LEVEL SECURITY;
CREATE POLICY "public read open help calls" ON public.help_calls FOR SELECT TO anon, authenticated USING (status = 'open' OR auth.uid() = requester_id OR auth.uid() = volunteer_id);
CREATE POLICY "verified users create help calls" ON public.help_calls FOR INSERT TO authenticated WITH CHECK (public.is_current_user_verified() AND auth.uid() = requester_id);
CREATE POLICY "participants update help calls" ON public.help_calls FOR UPDATE TO authenticated USING (public.is_current_user_verified() AND (auth.uid() = requester_id OR auth.uid() = volunteer_id)) WITH CHECK (public.is_current_user_verified() AND (auth.uid() = requester_id OR auth.uid() = volunteer_id));
CREATE POLICY "requesters delete help calls" ON public.help_calls FOR DELETE TO authenticated USING (public.is_current_user_verified() AND auth.uid() = requester_id);

CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS trigger LANGUAGE plpgsql SECURITY DEFINER SET search_path = public AS $$
BEGIN
  INSERT INTO public.profiles (id, full_name, email, phone, location, skills, is_verified)
  VALUES (NEW.id, COALESCE(NEW.raw_user_meta_data->>'full_name', ''), NEW.email, NEW.raw_user_meta_data->>'phone', NEW.raw_user_meta_data->>'location', NEW.raw_user_meta_data->>'skills', false)
  ON CONFLICT (id) DO UPDATE SET email = EXCLUDED.email, phone = EXCLUDED.phone;
  RETURN NEW;
END;
$$;