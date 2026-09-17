ALTER TABLE public.profiles
  ADD COLUMN IF NOT EXISTS is_verified boolean NOT NULL DEFAULT false;

CREATE TABLE IF NOT EXISTS public.email_verification_codes (
  user_id uuid PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
  code_hash text NOT NULL,
  expires_at timestamptz NOT NULL,
  last_sent_at timestamptz NOT NULL DEFAULT now(),
  created_at timestamptz NOT NULL DEFAULT now()
);

REVOKE ALL ON public.email_verification_codes FROM anon, authenticated;
GRANT ALL ON public.email_verification_codes TO service_role;
ALTER TABLE public.email_verification_codes ENABLE ROW LEVEL SECURITY;

CREATE OR REPLACE FUNCTION public.is_current_user_verified()
RETURNS boolean LANGUAGE sql STABLE SECURITY DEFINER SET search_path = public AS $$
  SELECT EXISTS (
    SELECT 1 FROM public.profiles
    WHERE id = auth.uid() AND is_verified = true
  );
$$;

CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS trigger LANGUAGE plpgsql SECURITY DEFINER SET search_path = public AS $$
BEGIN
  INSERT INTO public.profiles (id, full_name, email, location, skills, is_verified)
  VALUES (NEW.id, COALESCE(NEW.raw_user_meta_data->>'full_name', ''), NEW.email, NEW.raw_user_meta_data->>'location', NEW.raw_user_meta_data->>'skills', false)
  ON CONFLICT (id) DO UPDATE SET email = EXCLUDED.email;
  RETURN NEW;
END;
$$;

DROP POLICY IF EXISTS "owner insert initiative" ON public.initiatives;
DROP POLICY IF EXISTS "owner update initiative" ON public.initiatives;
DROP POLICY IF EXISTS "owner delete initiative" ON public.initiatives;
CREATE POLICY "verified owner insert initiative" ON public.initiatives FOR INSERT TO authenticated
  WITH CHECK (public.is_current_user_verified() AND auth.uid() = creator_id);
CREATE POLICY "verified owner update initiative" ON public.initiatives FOR UPDATE TO authenticated
  USING (public.is_current_user_verified() AND auth.uid() = creator_id)
  WITH CHECK (public.is_current_user_verified() AND auth.uid() = creator_id);
CREATE POLICY "verified owner delete initiative" ON public.initiatives FOR DELETE TO authenticated
  USING (public.is_current_user_verified() AND auth.uid() = creator_id);